package com.studios1299.playwall.feature.play.presentation.chat.util

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.compose.ui.unit.Dp
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.studios1299.playwall.R
import com.studios1299.playwall.feature.play.data.model.MessageStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Properties
import java.util.TimeZone

//fun timestampAsDate(timestamp: Long, context: Context): String {
//    val now = Calendar.getInstance()
//    val calendar = Calendar.getInstance().apply {
//        timeInMillis = timestamp
//    }
//    val today = now.clone() as Calendar
//    val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
//    val dateFormatCurrentYear = SimpleDateFormat("MMMM d", Locale.getDefault())
//    val dateFormatOtherYear = SimpleDateFormat("MMM d yyyy", Locale.getDefault())
//
//    return when {
//        calendar[Calendar.YEAR] == now[Calendar.YEAR] -> {
//            when {
//                calendar[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR] -> context.getString(R.string.today)
//                calendar[Calendar.DAY_OF_YEAR] == yesterday[Calendar.DAY_OF_YEAR] -> context.getString(
//                    R.string.yesterday
//                )
//                calendar[Calendar.MONTH] == today[Calendar.MONTH] -> dateFormatCurrentYear.format(calendar.time)
//                else -> dateFormatCurrentYear.format(calendar.time)
//            }
//        }
//        else -> dateFormatOtherYear.format(calendar.time)
//    }
//}

fun timestampAsDate(timestamp: String, context: Context): String {
    // Define the input date format
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")

    // Parse the timestamp into a Date object
    val date: Date = dateFormat.parse(timestamp) ?: return ""

    // Create Calendar instances
    val now = Calendar.getInstance()
    val calendar = Calendar.getInstance().apply { time = date }
    val today = now.clone() as Calendar
    val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
    val dateFormatCurrentYear = SimpleDateFormat("MMMM d", Locale.getDefault())
    val dateFormatOtherYear = SimpleDateFormat("MMM d yyyy", Locale.getDefault())

    return when {
        calendar[Calendar.YEAR] == now[Calendar.YEAR] -> {
            when {
                calendar[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR] -> context.getString(R.string.today)
                calendar[Calendar.DAY_OF_YEAR] == yesterday[Calendar.DAY_OF_YEAR] -> context.getString(R.string.yesterday)
                else -> dateFormatCurrentYear.format(calendar.time)
            }
        }
        else -> dateFormatOtherYear.format(calendar.time)
    }
}

//fun timestampAsTime(timestamp: Long): String {
//    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
//    val date = Date(timestamp)
//    return timeFormat.format(date)
//}
fun timestampAsTime(timestamp: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")
    val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val date: Date = inputFormat.parse(timestamp) ?: return ""
    return outputFormat.format(date)
}

fun timestampAsDateTime(timestamp: String, context: Context): String {
    // Define the input date format
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")

    // Parse the timestamp into a Date object
    val date: Date = dateFormat.parse(timestamp) ?: return ""

    // Create Calendar instances
    val now = Calendar.getInstance()
    val calendar = Calendar.getInstance().apply { time = date }
    val today = now.clone() as Calendar
    val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
    val dateFormatCurrentYear = SimpleDateFormat("MMMM d, HH:mm", Locale.getDefault()) // Date and Time for the current year
    val dateFormatOtherYear = SimpleDateFormat("MMM d yyyy, HH:mm", Locale.getDefault()) // Date and Time for other years

    return when {
        calendar[Calendar.YEAR] == now[Calendar.YEAR] -> {
            when {
                calendar[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR] -> {
                    context.getString(R.string.today) + ", " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
                }
                calendar[Calendar.DAY_OF_YEAR] == yesterday[Calendar.DAY_OF_YEAR] -> {
                    context.getString(R.string.yesterday) + ", " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
                }
                else -> dateFormatCurrentYear.format(calendar.time)
            }
        }
        else -> dateFormatOtherYear.format(calendar.time)
    }
}


fun formatStatus(status: MessageStatus, context: Context): String {
    return when (status) {
        MessageStatus.SENT -> context.getString(R.string.sent)
        MessageStatus.DELIVERED -> context.getString(R.string.delivered)
        MessageStatus.READ -> context.getString(R.string.read)
    }
}

fun calculateImageDimensions(aspectRatio: Float, maxWidth: Dp, maxHeight: Dp): Pair<Dp, Dp> {
    return if (aspectRatio > 1) {
        val imageWidth = maxWidth
        val imageHeight = (maxWidth / aspectRatio).coerceAtMost(maxHeight)
        imageWidth to imageHeight
    } else {
        val imageHeight = maxHeight
        val imageWidth = (maxHeight * aspectRatio).coerceAtMost(maxWidth)
        imageWidth to imageHeight
    }
}

/**
 * Triggers a haptic feedback vibration when a long click is detected.
 * Uses different APIs depending on the Android version.
 */
fun triggerHapticFeedback(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator
        vibrator.vibrate(
            VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    } else {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(50)
    }
}

fun getBuildCounter(context: Context): Int {
    val properties = Properties()
    return try {
        context.assets.open("build_counter.properties").use { properties.load(it) }
        properties.getProperty("buildCounter", "0").toInt()
    } catch (e: Exception) {
        Log.e("BuildCounter", "Failed to read build counter", e)
        0 // Default value in case of error
    }
}

//fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
//    val calendar1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
//    val calendar2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
//    return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
//            calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
//}
fun isSameDay(timestamp1: String, timestamp2: String): Boolean {
    // Define the input date format
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")

    // Parse both timestamps
    val date1: Date = dateFormat.parse(timestamp1) ?: return false
    val date2: Date = dateFormat.parse(timestamp2) ?: return false

    // Set both dates in calendar objects
    val calendar1 = Calendar.getInstance().apply { time = date1 }
    val calendar2 = Calendar.getInstance().apply { time = date2 }

    // Compare the year and the day of the year
    return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
}

fun requestNotificationPermissionWithDexter(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Dexter.withContext(context)
            .withPermission(Manifest.permission.POST_NOTIFICATIONS)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    Log.d("MainActivity", "Notification permission granted.")
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    Log.d("MainActivity", "Notification permission denied.")
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: com.karumi.dexter.listener.PermissionRequest?,
                    token: PermissionToken?
                ) {
                    Log.d("MainActivity", "Permission rationale should be shown.")
                    // Call continuePermissionRequest() to continue asking for the permission
                    token?.continuePermissionRequest()
                }
            }).check()
    }
}

