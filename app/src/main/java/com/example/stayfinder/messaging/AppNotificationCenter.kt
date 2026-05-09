package com.example.stayfinder.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.stayfinder.MainActivity
import com.example.stayfinder.R

object AppNotificationCenter {

    const val CHANNEL_EVENTS = "stayfinder_events"
    const val CHANNEL_FCM = "stayfinder_fcm"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_EVENTS,
                "StayFinder updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Listing and favorite updates" }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_FCM,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Firebase Cloud Messaging" }
        )
    }

    fun showNewListing(context: Context, title: String) {
        showSimple(context, CHANNEL_EVENTS, 1001, "New listing", title)
    }

    fun showFavoriteUpdated(context: Context, title: String) {
        showSimple(context, CHANNEL_EVENTS, 1002, "Favorite updated", title)
    }

    fun showFromFcm(context: Context, title: String?, body: String?) {
        showSimple(
            context,
            CHANNEL_FCM,
            (title + body).hashCode(),
            title ?: "StayFinder",
            body ?: ""
        )
    }

    private fun showSimple(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        text: String
    ) {
        ensureChannels(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_home)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }
}
