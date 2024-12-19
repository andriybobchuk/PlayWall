package com.studios1299.playwall.core.data

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.studios1299.playwall.app.MyApp
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.domain.model.WallpaperOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class ChangeWallpaperWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val s3PathToFile = inputData.getString("file_name") ?: return Result.failure()
        //val s3DownloadableLink = S3Handler.pathToDownloadableLink(s3PathToFile)
        val s3DownloadableLink = MyApp.appModule.coreRepository.getPresignedUrl(s3PathToFile)


        if (Preferences.isSavingIncomingWallpapersEnabled()) {
            val isSaved = saveImageToGallery(applicationContext, s3DownloadableLink ?: "", s3PathToFile.substringAfterLast("/"))
            if (isSaved) {
                Log.e("ChangeWallpaperWorker", "Image saved to gallery successfully")
            } else {
                Log.e("ChangeWallpaperWorker", "Failed to save image to gallery")
            }
        }

        Preferences.setPreviousWallpaperId(Preferences.getCurrentWallpaperId() ?: "")
        Preferences.setCurrentWallpaperId(s3PathToFile)

        var setLockScreen = false
        var setHomeScreen = false

        val wallpaperDestination = Preferences.getWallpaperDestination()
        when (wallpaperDestination) {
            WallpaperOption.HomeScreen -> setHomeScreen = true
            WallpaperOption.LockScreen -> setLockScreen = true
            WallpaperOption.Both -> {
                setLockScreen = true
                setHomeScreen = true
            }
        }

        Log.e("ChangeWallpaperWorker", "start")
        return try {
            val wallpaperUrl = s3DownloadableLink
            val url = URL(wallpaperUrl)
            val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())

            val wallpaperManager = WallpaperManager.getInstance(applicationContext)
            var result = Result.failure()

            if (setHomeScreen) {
                try {
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    Log.e("ChangeWallpaperWorker", "Home screen wallpaper set successfully")
                    result = Result.success()  // Only set to success if home screen is set
                } catch (e: Exception) {
                    Log.e("ChangeWallpaperWorker", "Failed to set home screen wallpaper", e)
                }
            }

            if (setLockScreen) {
                try {
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                    Log.e("ChangeWallpaperWorker", "Lock screen wallpaper set successfully")
                    result = Result.success()  // Only set to success if lock screen is set
                } catch (e: Exception) {
                    Log.e("ChangeWallpaperWorker", "Failed to set lock screen wallpaper", e)
                }
            }

            result
        } catch (e: Exception) {
            Log.e("ChangeWallpaperWorker", "Failed to set wallpaper", e)
            Result.failure()
        }
    }
}


suspend fun saveImageToGallery(context: Context, s3DownloadableLink: String, fileName: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            // Prepare ContentValues for the MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg") // Assuming it's a JPEG image
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PlayWall") // Folder inside Pictures
                put(MediaStore.Images.Media.IS_PENDING, 1) // Indicate the file is still being written
            }

            // Insert the image in the MediaStore and get the content URI
            val contentResolver = context.contentResolver
            val imageUri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (imageUri != null) {
                // Open output stream to write the file
                contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                    val url = URL(s3DownloadableLink)
                    val connection = url.openConnection()
                    connection.connect()

                    // Open input stream from the URL
                    val inputStream = connection.getInputStream()

                    // Buffer to hold chunks of data
                    val buffer = ByteArray(1024)
                    var bytesRead: Int

                    // Read from input stream and write to output stream
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }

                    // Close input stream
                    inputStream.close()

                    // Mark the file as completed (remove pending status)
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(imageUri, contentValues, null, null)

                    Log.d("SaveImageToGallery", "Wallpaper saved to gallery: $imageUri")
                    return@withContext true // Success
                } ?: run {
                    Log.e("SaveImageToGallery", "Failed to open output stream")
                    return@withContext false // Failed to open output stream
                }
            } else {
                Log.e("SaveImageToGallery", "Failed to insert into MediaStore")
                return@withContext false // Failed to insert into MediaStore
            }

        } catch (e: Exception) {
            Log.e("SaveImageToGallery", "Failed to save wallpaper to gallery", e)
            return@withContext false // An error occurred
        }
    }
}
