package com.studios1299.playwall.core.data

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.domain.model.WallpaperOption
import java.net.URL

class ChangeWallpaperWorker(context: Context, params: WorkerParameters) : CoroutineWorker(
    context, params) {
    override suspend fun doWork(): Result {
        val s3PathToFile = inputData.getString("file_name") ?: return Result.failure()
        val fromDevice = inputData.getBoolean("from_device", false) // it may also be from saved posts and then we do not need to presign the url again

        val s3DownloadableLink = S3Handler.pathToDownloadableLink(s3PathToFile)


        Log.e("WORKER", "Filename: " + s3DownloadableLink)

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
            var flags = 0
            if (setHomeScreen) {
                flags = flags or WallpaperManager.FLAG_SYSTEM
            }
            if (setLockScreen) {
                flags = flags or WallpaperManager.FLAG_LOCK
            }
            if (flags != 0) {
                wallpaperManager.setBitmap(bitmap, null, true, flags)
                Result.success()
            } else {
                Log.e("ChangeWallpaperWorker", "No␣valid␣flags␣set␣for␣wallpaper␣change")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e("ChangeWallpaperWorker", "Failed␣to␣set␣wallpaper", e)
            Result.failure()
        }
    }
}