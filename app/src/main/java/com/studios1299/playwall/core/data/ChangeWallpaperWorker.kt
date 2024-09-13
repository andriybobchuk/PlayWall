package com.studios1299.playwall.core.data

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.studios1299.playwall.R

class ChangeWallpaperWorker(context: Context, params: WorkerParameters) : CoroutineWorker(
    context, params) {
    override suspend fun doWork(): Result {
        val fileName = inputData.getString("file_name") ?: return Result.failure()
        val setLockScreen = inputData.getBoolean("set_lock_screen", true)
        val setHomeScreen = inputData.getBoolean("set_home_screen", true)
       // val storageRef = FirebaseStorage.getInstance().reference.child("images/$fileName")

        return try {
            //val byteArray = storageRef.getBytes(Long.MAX_VALUE).await()
            //val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

            val bitmap = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.wall_a)
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