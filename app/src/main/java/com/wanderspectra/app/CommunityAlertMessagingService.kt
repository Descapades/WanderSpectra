package com.wanderspectra.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CommunityAlertMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "community_alerts"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val alertId = message.data["alertId"] ?: return
        val alertType = message.data["type"] ?: return

        if (alertType != "COMMUNITY_ALERT") return
        if (alertId.isBlank()) return

        showCommunityAlertNotification(alertId)
    }

    private fun showCommunityAlertNotification(alertId: String) {
        val manager = getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Community Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Nearby missing child Community Alerts"
            }

            manager.createNotificationChannel(channel)
        }

        // Android 13+ requires notification permission.
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val launchIntent = packageManager
            .getLaunchIntentForPackage(packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                this,
                alertId.hashCode(),
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("WanderSpectra Community Alert")
            .setContentText(
                "A missing child alert has been issued in your area."
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .apply {
                pendingIntent?.let { setContentIntent(it) }
            }
            .build()

        manager.notify(alertId.hashCode(), notification)
    }
}