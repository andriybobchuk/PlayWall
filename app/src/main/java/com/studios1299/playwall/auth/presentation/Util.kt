package com.studios1299.playwall.auth.presentation

import android.content.Context
import android.util.Log

fun getScreenRatio(context: Context?): Float {
    return try {
        context?.resources?.displayMetrics?.let { displayMetrics ->
            val width = displayMetrics.widthPixels
            val height = displayMetrics.heightPixels

            Log.e("ScreenRatio", "Width: $width, Height: $height")

            if (width == 0 || height == 0) {
                Log.e("ScreenRatio", "Invalid width or height: width=$width, height=$height")
                return@let 2f
            }

            val ratio = height.toFloat() / width.toFloat()
            Log.e("ScreenRatio", "Screen ratio calculated: $ratio")
            ratio
        } ?: run {
            Log.e("ScreenRatio", "Context or DisplayMetrics is null")
            2f
        }
    } catch (e: Exception) {
        Log.e("ScreenRatio", "Failed to get screen ratio: ${e.message}", e)
        2f
    }
}
