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
import com.studios1299.playwall.app.MyApp
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.core.data.networking.response.wallpapers.WallpaperHistoryResponse
import com.studios1299.playwall.feature.play.data.model.MessageStatus
import com.studios1299.playwall.feature.play.data.model.Reaction
import com.studios1299.playwall.feature.play.presentation.chat.viewmodel.WallpaperNotificationForChat
import com.studios1299.playwall.feature.play.presentation.play.WallpaperNotificationForPlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

data class FriendEvent(
    val type: String,
    val requesterId: Int,
    val recipientId: Int,
    val friendshipId: Int
)

object WallpaperEventManager {
    val wallpaperUpdates = MutableSharedFlow<WallpaperHistoryResponse>()
    val friendUpdates = MutableSharedFlow<FriendEvent>()
}

class FcmService : FirebaseMessagingService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e("FcmService", "onMessageReceived(): start: ${remoteMessage.data}")

        val notificationType = remoteMessage.data["type"]
        val wallpaperId = remoteMessage.data["wallpaperId"]
        val requesterId = remoteMessage.data["requesterId"]?.toIntOrNull() ?: -1
        val recipientId = remoteMessage.data["recipientId"]?.toIntOrNull() ?: -1
        val fileName = remoteMessage.data["fileName"]
        val comment = remoteMessage.data["comment"]
        val reaction = remoteMessage.data["reaction"]
        val timeSent = remoteMessage.data["timeSent"]
        val friendshipId = remoteMessage.data["friendshipId"]?.toIntOrNull() ?: -1

        if (wallpaperId != null) {
            val wallpaperHistoryResponse = WallpaperHistoryResponse(
                id = wallpaperId.toInt(),
                fileName = fileName ?: "",
                type = notificationType ?: "unknown",
                requesterId = requesterId,
                recipientId = recipientId,
                comment = comment,
                reaction = reaction?.let { Reaction.valueOf(it) },
                timeSent = timeSent ?: "",
                status = null
            )

            when (notificationType) {
                "wallpaper" -> {
                    Log.e("FcmService", "New wallpaper received")
                    emitWallpaperUpdate(wallpaperHistoryResponse)
                    handleWallpaperDownload(fileName)
                    WallpaperNotificationForChat.setNewWallpaperReceived(true)
                    WallpaperNotificationForPlay.setNewWallpaperReceived(true)
                    sendNotification(NotificationType.wallpaper)
                }
                "reaction" -> {
                    Log.e("FcmService", "New reaction received: $reaction for message $wallpaperId")
                    emitWallpaperUpdate(wallpaperHistoryResponse)
                }
                "reaction_removed" -> {
                    Log.e("FcmService", "Reaction removed for wallpaper")
                    emitWallpaperUpdate(wallpaperHistoryResponse.copy(reaction = Reaction.none))
                }
                "comment" -> {
                    Log.e("FcmService", "New comment received: $comment")
                    emitWallpaperUpdate(wallpaperHistoryResponse)
                }
                "message_read" -> {
                    Log.e("FcmService", "Message read receipt received")
                    emitWallpaperUpdate(wallpaperHistoryResponse.copy(status = MessageStatus.read))
                }
                else -> {
                    Log.e("FcmService", "Unknown notification type: $notificationType")
                }
            }
        } else {
            when (notificationType) {
                "friend_invite" -> {
                    val friendEvent = FriendEvent(
                        type = notificationType,
                        requesterId = requesterId,
                        recipientId = recipientId,
                        friendshipId = friendshipId
                    )
                    Log.e("FcmService", "Friend invite received: " + friendEvent)
                    emitFriendUpdate(friendEvent)
                    sendNotification(NotificationType.friendInvite)
                }
                "friend_accept" -> {
                    Log.e("FcmService", "Friend request accepted")
                    val friendEvent = FriendEvent(
                        type = notificationType,
                        requesterId = requesterId,
                        recipientId = recipientId,
                        friendshipId = friendshipId
                    )
                    emitFriendUpdate(friendEvent)
                    sendNotification(NotificationType.friendAccept)
                }
                "friend_remove" -> {
                    Log.e("FcmService", "Friend removed")
                    val friendEvent = FriendEvent(
                        type = notificationType,
                        requesterId = requesterId,
                        recipientId = recipientId,
                        friendshipId = friendshipId
                    )
                    emitFriendUpdate(friendEvent)
                }
                "friend_block" -> {
                    Log.e("FcmService", "Friend blocked")
                    val friendEvent = FriendEvent(
                        type = notificationType,
                        requesterId = requesterId,
                        recipientId = recipientId,
                        friendshipId = friendshipId
                    )
                    emitFriendUpdate(friendEvent)
                }
                "friend_unblock" -> {
                    Log.e("FcmService", "Friend unblocked")
                    val friendEvent = FriendEvent(
                        type = notificationType,
                        requesterId = requesterId,
                        recipientId = recipientId,
                        friendshipId = friendshipId
                    )
                    emitFriendUpdate(friendEvent)
                }
                else -> {
                    Log.e("FcmService", "Unknown notification type: $notificationType")
                }
            }
        }

    }

    private fun emitWallpaperUpdate(wallpaperHistoryResponse: WallpaperHistoryResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            WallpaperEventManager.wallpaperUpdates.emit(wallpaperHistoryResponse)
        }
    }

    private fun emitFriendUpdate(friendEvent: FriendEvent) {
        CoroutineScope(Dispatchers.Main).launch {
            WallpaperEventManager.friendUpdates.emit(friendEvent)
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
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("FcmService", "FCM Token was changed")
        Log.e("FcmService", "New token: $token")
        Preferences.setFcmToken(token)

        scope.launch {
            MyApp.appModule.authRepository.updatePushToken()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun sendNotification(notificationType: NotificationType) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = notificationType.id
        createNotificationChannel(notificationManager)
        val notification = NotificationCompat.Builder(this, "playwall_notifications")
            .setSmallIcon(R.drawable.pw)
            .setContentTitle(notificationType.title)
            .setContentText(notificationType.content)
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

enum class NotificationType(val id: Int, val title: String, val content: String) {
    wallpaper(1, "Wallpaper received", "Someone sent you a wallpaper! \uD83D\uDE08"),
    friendInvite(2, "Friend request", "You received a friend request, find out who! \uD83D\uDE08"),
    friendAccept(3, "Request accepted", "Your friend request was accepted, send them a wallpaper! \uD83D\uDE08")
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
