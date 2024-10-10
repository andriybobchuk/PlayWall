//package com.studios1299.playwall.core.data
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Context
//import android.os.Build
//import android.util.Log
//import androidx.core.app.NotificationCompat
//import androidx.work.OneTimeWorkRequestBuilder
//import androidx.work.WorkManager
//import androidx.work.workDataOf
//import com.google.firebase.messaging.FirebaseMessagingService
//import com.google.firebase.messaging.RemoteMessage
//import com.studios1299.playwall.R
//import com.studios1299.playwall.core.data.local.Preferences
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.MutableSharedFlow
//import kotlinx.coroutines.launch
//



package com.studios1299.playwall.core.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.studios1299.playwall.R
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.core.data.networking.response.WallpaperHistoryResponse
import com.studios1299.playwall.feature.play.data.model.MessageStatus
import com.studios1299.playwall.feature.play.data.model.Reaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

object WallpaperEventManager {
    val wallpaperUpdates = MutableSharedFlow<WallpaperHistoryResponse>()
}

class FcmService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e("FcmService", "onMessageReceived(): start: $remoteMessage")

        val notificationType = remoteMessage.data["type"] // e.g., "wallpaper", "friend_request", "reaction"
        val wallpaperId = remoteMessage.data["wallpaperId"]?.toIntOrNull() ?: -1
        val requesterId = remoteMessage.data["requesterId"]?.toIntOrNull() ?: -1
        val recipientId = remoteMessage.data["recipientId"]?.toIntOrNull() ?: -1
        val fileName = remoteMessage.data["fileName"]
        val comment = remoteMessage.data["comment"]
        val reaction = remoteMessage.data["reaction"]
        val timeSent = remoteMessage.data["timeSent"]

        // Build the WallpaperHistoryResponse dynamically
        val wallpaperHistoryResponse = WallpaperHistoryResponse(
            id = wallpaperId,
            fileName = fileName ?: "",
            type = notificationType ?: "unknown",
            requesterId = requesterId,
            recipientId = recipientId,
            comment = comment,
            reaction = reaction?.let { Reaction.valueOf(it) },
            timeSent = timeSent ?: "",
            status = MessageStatus.unread
        )

        when (notificationType) {
            "wallpaper" -> {
                Log.e("FcmService", "New wallpaper received")
                emitWallpaperUpdate(wallpaperHistoryResponse)
                handleWallpaperDownload(fileName)
            }
            "reaction" -> {
                Log.e("FcmService", "New reaction received: $reaction")
                emitWallpaperUpdate(wallpaperHistoryResponse)
            }
            "reaction_removed" -> {
                Log.e("FcmService", "Reaction removed for wallpaper")
                emitWallpaperUpdate(wallpaperHistoryResponse.copy(reaction = null))
            }
            "comment" -> {
                Log.e("FcmService", "New comment received: $comment")
                emitWallpaperUpdate(wallpaperHistoryResponse)
            }
            else -> {
                Log.e("FcmService", "Unknown notification type: $notificationType")
            }
        }
    }

    private fun emitWallpaperUpdate(wallpaperHistoryResponse: WallpaperHistoryResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            WallpaperEventManager.wallpaperUpdates.emit(wallpaperHistoryResponse)
        }
    }

    private fun handleWallpaperDownload(fileName: String?) {
        if (!fileName.isNullOrEmpty()) {
            val workData = workDataOf("file_name" to fileName)
            val changeWallpaperWork = OneTimeWorkRequestBuilder<ChangeWallpaperWorker>()
                .setInputData(workData)
                .build()

            Log.e("FcmService", "Launching wallpaper worker for file: $fileName")
            WorkManager.getInstance(applicationContext).enqueue(changeWallpaperWork)

            sendWallpaperNotification(fileName)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("FcmService", "New token: $token")
        Preferences.setFcmToken(token)
    }

    private fun sendWallpaperNotification(fileName: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 1

        createNotificationChannel(notificationManager)

        val notification = NotificationCompat.Builder(this, "playwall_notifications")
            .setSmallIcon(R.drawable.pw)
            .setContentTitle("New Wallpaper Received")
            .setContentText("You have received a new wallpaper: $fileName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "playwall_notifications",
                "PlayWall Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for PlayWall notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}

//object WallpaperEventManager {
//    val wallpaperUpdates = MutableSharedFlow<String>()
//}
//
//class FcmService : FirebaseMessagingService() {
//
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        Log.e("FcmService", "onMessageReceived(): start: " + remoteMessage)
//
//        val notificationType = remoteMessage.data["type"] // e.g., "wallpaper", "friend_request", "reaction"
//        val s3DownloadableLink = remoteMessage.data["fileName"]
//        val requesterName = remoteMessage.data["requesterName"] // For friend requests
//        val comment = remoteMessage.data["comment"] // For comment-related notifications
//        val reaction = remoteMessage.data["reaction"] // For reaction-related notifications
//        val wallpaperId = remoteMessage.data["reaction_removed"] // For reaction-related notifications
//
//        when (notificationType) {
//            "wallpaper" -> {
//
//                CoroutineScope(Dispatchers.Main).launch {
//                    Log.d("LOG_TAG", "shit sent ..")
//                    WallpaperEventManager.wallpaperUpdates.emit("anything")
//                }
//
//                if (!s3DownloadableLink.isNullOrEmpty()) {
//                    val workData = workDataOf(
//                        "file_name" to s3DownloadableLink,
//                    )
//
//                    val changeWallpaperWork = OneTimeWorkRequestBuilder<ChangeWallpaperWorker>()
//                        .setInputData(workData)
//                        .build()
//
//                    Log.e("FcmService", "onMessageReceived(): launching wallpaper worker")
//                    WorkManager.getInstance(applicationContext).enqueue(changeWallpaperWork)
//
//                    sendWallpaperNotification(s3DownloadableLink)
//                }
//            }
//            "friend_request" -> {
//                if (!requesterName.isNullOrEmpty()) {
//                    Log.e("FcmService", "onMessageReceived(): new friend request from $requesterName")
//
//                    // Trigger a UI notification or update
//                    sendFriendRequestNotification(requesterName)
//                }
//            }
//            "reaction" -> {
//                if (!reaction.isNullOrEmpty()) {
//                    Log.e("FcmService", "onMessageReceived(): new reaction: $reaction")
//                    CoroutineScope(Dispatchers.Main).launch {
//                        WallpaperEventManager.wallpaperUpdates.emit("anything")
//                    }
//                    sendReactionNotification(reaction)
//                }
//            }
//            "reaction_removed" -> {
//                CoroutineScope(Dispatchers.Main).launch {
//                    WallpaperEventManager.wallpaperUpdates.emit("anything")
//                }
//                Log.e("FcmService", "Reaction removed for wallpaper")
//            }
//            "comment" -> {
//                CoroutineScope(Dispatchers.Main).launch {
//                    WallpaperEventManager.wallpaperUpdates.emit("anything")
//                }
//                if (!comment.isNullOrEmpty()) {
//                    Log.e("FcmService", "onMessageReceived(): new comment: $comment")
//                    sendCommentNotification(comment)
//                }
//            }
//            else -> {
//                Log.e("FcmService", "Unknown notification type")
//            }
//        }
//    }
//
//    override fun onNewToken(token: String) {
//        super.onNewToken(token)
//        Log.e("FcmService", "New token: $token")
//        Preferences.setFcmToken(token)
//
//        Log.e("FcmService", "fcmTokem from Preferences:" + Preferences.getFcmToken())
//    }
//
//    private fun sendFriendRequestNotification(requesterName: String) {
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val notificationId = 1
//
//        createNotificationChannel(notificationManager)
//
//        val notification = NotificationCompat.Builder(this, "playwall_notifications")
//            .setSmallIcon(R.drawable.person)
//            .setContentTitle("New Friend Request")
//            .setContentText("You have a new friend request from $requesterName.")
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .build()
//
//        notificationManager.notify(notificationId, notification)
//    }
//
//    private fun sendReactionNotification(reaction: String) {
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val notificationId = 2
//
//        createNotificationChannel(notificationManager)
//
//        val notification = NotificationCompat.Builder(this, "playwall_notifications")
//            .setSmallIcon(R.drawable.danger) // Replace with your reaction icon
//            .setContentTitle("New Reaction")
//            .setContentText("You received a new reaction: $reaction")
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .build()
//
//        notificationManager.notify(notificationId, notification)
//    }
//
//    private fun sendCommentNotification(comment: String) {
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val notificationId = 3
//
//        createNotificationChannel(notificationManager)
//
//        val notification = NotificationCompat.Builder(this, "playwall_notifications")
//            .setSmallIcon(R.drawable.calendar) // Replace with your comment icon
//            .setContentTitle("New Comment")
//            .setContentText("You received a new comment: $comment")
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .build()
//
//        notificationManager.notify(notificationId, notification)
//    }
//
//    private fun sendWallpaperNotification(fileName: String) {
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val notificationId = 4
//        Log.e("LLL", "sendWallpaperNotification")
//        createNotificationChannel(notificationManager)
//
//        val notification = NotificationCompat.Builder(this, "playwall_notifications")
//            .setSmallIcon(R.drawable.pw) // Replace with your wallpaper icon
//            .setContentTitle("New Wallpaper Received")
//            .setContentText("You have received a new wallpaper: $fileName")
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .build()
//
//        notificationManager.notify(notificationId, notification)
//    }
//
//    private fun createNotificationChannel(notificationManager: NotificationManager) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                "playwall_notifications",
//                "PlayWall Notifications",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "Channel for PlayWall notifications"
//            }
//            notificationManager.createNotificationChannel(channel)
//            Log.e("LLL", "createNotificationChannel DONE")
//        }
//    }
//}
