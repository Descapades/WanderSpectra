package com.wanderspectra.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object CaregiverNotificationManager {

    const val CHANNEL_ID = "caregiver_alerts_channel"
    const val CHANNEL_NAME = "Caregiver Safety Alerts"
    const val CHANNEL_DESCRIPTION = "Urgent alerts for child location, safe zones, and emergency updates"
    const val NOTIFICATION_ID = 1001


    fun hasNotificationPermission(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        }

        return true
    }


    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }


    @SuppressLint("MissingPermission")
    fun sendCaregiverNotification(
        context: Context,
        title: String = "WanderSpectra Caregiver Alert",
        message: String = "Caregiver notifications are active! You will receive urgent child safety updates here.",
    ): Boolean {
        if (!hasNotificationPermission(context)) {
            return false
        }

        return try {
            createNotificationChannel(context)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.wanderspectra_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
            true
        } catch (_: Exception) {
            false
        }
    }


    fun openNotificationSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
        }
    }
}



object CaregiverNotificationPreferences {

    private const val PREFS_NAME = "caregiver_notification_preferences"

    private const val KEY_SAFE_ZONE_EXIT = "safe_zone_exit_enabled"
    private const val KEY_WEARABLE_DISCONNECTED = "wearable_disconnected_enabled"
    private const val KEY_LOW_BATTERY = "low_battery_alert_enabled"
    private const val KEY_SAFETY_CIRCLE_UPDATES = "safety_circle_updates_enabled"
    private const val KEY_COMMUNITY_SIGHTINGS = "community_sightings_enabled"
    private const val KEY_SOUND = "sound_enabled"
    private const val KEY_VIBRATION = "vibration_enabled"

    private fun getPreference(context: Context, key: String): Boolean {
        return context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).getBoolean(key, true)
    }

    private fun setPreference(context: Context, key: String, enabled: Boolean) {
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).edit()
            .putBoolean(key, enabled)
            .apply()
    }

    fun isSafeZoneExitEnabled(context: Context) =
        getPreference(context, KEY_SAFE_ZONE_EXIT)

    fun setSafeZoneExitEnabled(context: Context, enabled: Boolean) =
        setPreference(context, KEY_SAFE_ZONE_EXIT, enabled)

    fun isWearableDisconnectedEnabled(context: Context) =
        getPreference(context, KEY_WEARABLE_DISCONNECTED)

    fun setWearableDisconnectedEnabled(context: Context, enabled: Boolean) =
        setPreference(context, KEY_WEARABLE_DISCONNECTED, enabled)

    fun isLowBatteryAlertEnabled(context: Context) =
        getPreference(context, KEY_LOW_BATTERY)

    fun setLowBatteryAlertEnabled(context: Context, enabled: Boolean) =
        setPreference(context, KEY_LOW_BATTERY, enabled)

    fun isSafetyCircleUpdatesEnabled(context: Context) =
        getPreference(context, KEY_SAFETY_CIRCLE_UPDATES)

    fun setSafetyCircleUpdatesEnabled(context: Context, enabled: Boolean) =
        setPreference(context, KEY_SAFETY_CIRCLE_UPDATES, enabled)

    fun isCommunitySightingsEnabled(context: Context) =
        getPreference(context, KEY_COMMUNITY_SIGHTINGS)

    fun setCommunitySightingsEnabled(context: Context, enabled: Boolean) =
        setPreference(context, KEY_COMMUNITY_SIGHTINGS, enabled)

    fun isSoundEnabled(context: Context) =
        getPreference(context, KEY_SOUND)

    fun setSoundEnabled(context: Context, enabled: Boolean) =
        setPreference(context, KEY_SOUND, enabled)

    fun isVibrationEnabled(context: Context) =
        getPreference(context, KEY_VIBRATION)

    fun setVibrationEnabled(context: Context, enabled: Boolean) =
        setPreference(context, KEY_VIBRATION, enabled)
}


