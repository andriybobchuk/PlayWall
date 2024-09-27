package com.studios1299.playwall.core.data

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.studios1299.playwall.R
import com.studios1299.playwall.core.data.s3.S3Handler
import java.net.URL

class ChangeWallpaperWorker(context: Context, params: WorkerParameters) : CoroutineWorker(
    context, params) {
    override suspend fun doWork(): Result {
        val fileName = inputData.getString("file_name") ?: return Result.failure()
        val setLockScreen = true
        val setHomeScreen = true
        Log.e("ChangeWallpaperWorker", "start")
        return try {

            val wallpaperUrl = S3Handler.loadFromS3(fileName)

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