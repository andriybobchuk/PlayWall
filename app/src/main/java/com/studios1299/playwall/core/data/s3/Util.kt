package com.studios1299.playwall.core.data.s3

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.InputStream

fun uriToFile(context: Context, uri: Uri): File? {
    val fileName = "temp_avatar.jpg"
    val tempFile = File(context.cacheDir, fileName)
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        tempFile.outputStream().use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        Log.d("uriToFile", "File created at: ${tempFile.absolutePath}")
        tempFile
    } catch (e: Exception) {
        Log.e("uriToFile", "Error converting Uri to File: ${e.message}", e)
        null
    }
}