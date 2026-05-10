package com.stayfinder.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.stayfinder.app.R
import com.stayfinder.app.activities.MainActivity

/**
 * StayFinderMessagingService
 *
 * Handles incoming Firebase Cloud Messages (FCM) for push notifications.
 * Supports:
 *  • Booking confirmations
 *  • Price drop alerts
 *  • General promotional messages
 */
class StayFinderMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        const val CHANNEL_ID_BOOKINGS = "bookings_channel"
        const val CHANNEL_ID_PROMOS = "promos_channel"
        const val CHANNEL_ID_GENERAL = "general_channel"

        /**
         * Create all notification channels (call from Application.onCreate or MainActivity.onCreate).
         */
        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager

                listOf(
                    NotificationChannel(
                        CHANNEL_ID_BOOKINGS,
                        "Booking Updates",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply { description = "Booking confirmations and status updates" },

                    NotificationChannel(
                        CHANNEL_ID_PROMOS,
                        "Price Alerts & Deals",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply { description = "Price drops and exclusive deals" },

                    NotificationChannel(
                        CHANNEL_ID_GENERAL,
                        "General Notifications",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply { description = "App news and updates" },
                ).forEach { manager.createNotificationChannel(it) }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        // Save the token to Firestore under the user's document so the server
        // can send targeted notifications.
        saveFcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM message received from: ${message.from}")

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "StayFinder"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""
        val type = message.data["type"] ?: "general"

        showNotification(title, body, type)
    }

    private fun showNotification(title: String, body: String, type: String) {
        val channelId = when (type) {
            "booking" -> CHANNEL_ID_BOOKINGS
            "promo", "price_drop" -> CHANNEL_ID_PROMOS
            else -> CHANNEL_ID_GENERAL
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationId = System.currentTimeMillis().toInt()

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_bell)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(
                if (channelId == CHANNEL_ID_BOOKINGS)
                    NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }

    private fun saveFcmToken(token: String) {
        // Retrieve last-saved email from SharedPreferences
        val prefs = getSharedPreferences("stayfinder_prefs", Context.MODE_PRIVATE)
        val email = prefs.getString("last_email", null) ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(email)
            .update("fcmToken", token)
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to save FCM token", e)
            }
    }
}
