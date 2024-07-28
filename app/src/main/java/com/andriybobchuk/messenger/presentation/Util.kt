package com.andriybobchuk.messenger.presentation

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.andriybobchuk.messenger.model.Message
import com.andriybobchuk.messenger.model.MessageStatus
import kotlinx.coroutines.CoroutineScope
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun timestampAsDate(timestamp: Long): String {
    val now = Calendar.getInstance()
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }

    val today = now.clone() as Calendar
    val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }

    val dateFormatCurrentYear = SimpleDateFormat("MMMM d'th'", Locale.getDefault())
    val dateFormatOtherYear = SimpleDateFormat("MMM d'th' yyyy", Locale.getDefault())

    return when {
        calendar[Calendar.YEAR] == now[Calendar.YEAR] -> {
            when {
                calendar[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR] -> "Today"
                calendar[Calendar.DAY_OF_YEAR] == yesterday[Calendar.DAY_OF_YEAR] -> "Yesterday"
                calendar[Calendar.MONTH] == today[Calendar.MONTH] -> dateFormatCurrentYear.format(calendar.time)
                else -> dateFormatCurrentYear.format(calendar.time)
            }
        }
        else -> dateFormatOtherYear.format(calendar.time)
    }
}

fun timestampAsTime(timestamp: Long): String {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val date = Date(timestamp)
    return timeFormat.format(date)
}

fun groupMessagesByDate(messages: List<Message>): Map<String, List<Message>> {
    return messages.groupBy { message ->
        timestampAsDate(message.timestamp)
    }
}

fun formatStatus(status: MessageStatus): String {
    return when (status) {
        MessageStatus.SENT -> "Sent"
        MessageStatus.DELIVERED -> "Delivered"
        MessageStatus.READ -> "Read"
    }
}

/**
 * Provides haptic feedback on devices running both old and new Android versions.
 */
fun provideHapticFeedback(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                50,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    } else { // Old phones:
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(50)
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