package com.testdm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "Test DM"
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: ""
        val url = remoteMessage.data["url"] ?: MainActivity.TARGET_URL
        val imageUrl = remoteMessage.data["image"]

        sendNotification(title, body, url, imageUrl)
    }

    private fun sendNotification(title: String, body: String, url: String, imageUrl: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("url", url)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "testdm_channel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаём канал уведомлений (нужно для Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Test DM уведомления",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Если передана картинка — загружаем и добавляем в уведомление
        if (!imageUrl.isNullOrEmpty()) {
            try {
                val bitmap = BitmapFactory.decodeStream(URL(imageUrl).openStream())
                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(null as android.graphics.Bitmap?)
                )
                builder.setLargeIcon(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(body))
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Здесь можно отправить токен на свой сервер
    }
}
