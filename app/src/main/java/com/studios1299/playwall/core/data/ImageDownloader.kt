package com.studios1299.playwall.core.data

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.URL


fun downloadImageToDevice(context: Context, s3DownloadableLink: String, onComplete: (Boolean) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(s3DownloadableLink)
                val inputStream = url.openStream() // Download the content using standard URL connection
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "downloaded_image_${System.currentTimeMillis()}.jpg")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        inputStream.copyTo(outputStream)
                        Log.e("DownloadDebug", "Image download and save successful")
                        onComplete(true)
                    } ?: Log.e("DownloadDebug", "Failed to open output stream")
                } else {
                    Log.e("DownloadDebug", "Failed to create MediaStore entry for download")
                    onComplete(false)
                }
                inputStream?.close()
            } catch (e: Exception) {
                Log.e("DownloadDebug", "Error during image download or storage: ${e.message}", e)
                onComplete(false)
            }
        }
    } else {
        // Handle older versions using DownloadManager
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(s3DownloadableLink))
            .setTitle("Download Image")
            .setDescription("Downloading an image to the gallery.")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "downloaded_image_${System.currentTimeMillis()}.jpg")
        request.setDestinationUri(Uri.fromFile(file))

        try {
            val downloadId = downloadManager.enqueue(request)
            Log.e("DownloadDebug", "Download enqueued with ID: $downloadId")
        } catch (e: IllegalArgumentException) {
            Log.e("DownloadDebug", "Failed to enqueue download: ${e.message}")
            onComplete(false)
        } catch (e: Exception) {
            Log.e("DownloadDebug", "Error enqueuing the download: ${e.message}")
            onComplete(false)
        }
    }
}