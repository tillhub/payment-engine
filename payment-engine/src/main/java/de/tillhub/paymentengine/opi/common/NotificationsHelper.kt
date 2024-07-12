package de.tillhub.paymentengine.opi.common

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.tillhub.paymentengine.R

internal object NotificationsHelper {

    private const val NOTIFICATION_CHANNEL_ID = "general_notification_channel"

    fun createNotificationChannel(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)

        // create the notification channel
        val channel = NotificationChannelCompat
            .Builder(
                NOTIFICATION_CHANNEL_ID,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            .setName("OPI Foreground service")
            .build()

        notificationManager.createNotificationChannel(channel)
    }

    fun buildNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("OPI Foreground service")
            .setContentText("OPI transaction in progress")
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_card)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }
}