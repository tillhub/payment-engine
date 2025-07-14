package de.tillhub.paymentengine.opi.common

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.tillhub.paymentengine.opi.R

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
            .setName(context.getString(R.string.opi_foreground_service_title))
            .build()

        notificationManager.createNotificationChannel(channel)
    }

    fun buildNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.opi_foreground_service_title))
            .setContentText(context.getString(R.string.opi_foreground_service_message))
            .setOngoing(true)
            .setSmallIcon(de.tillhub.paymentengine.R.drawable.ic_card)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }
}