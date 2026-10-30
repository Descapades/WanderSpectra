package com.wanderspectra.wear.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.wanderspectra.wear.location.WearLocationManager
import androidx.core.app.NotificationCompat

class WearGeofenceMonitoringService : Service() {

    companion object {
        private const val TAG = "WearGeofenceMonitor"
        private const val CHANNEL_ID = "geofence_monitoring"
        private const val NOTIFICATION_ID = 1001

        private const val CHECK_INTERVAL_MS = 30_000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var isMonitoring = false
    private var locationRequestInProgress = false
    private var lastHandledExitTimestamp: Long? = null

    private val monitoringRunnable = object : Runnable {
        override fun run() {
            if (!isMonitoring) return

            val safeZone = WearSafetyStateRepository.activeSafeZone.value

            if (safeZone == null || !safeZone.monitoringEnabled) {
                Log.d(TAG, "No enabled Safe Zone. Stopping monitoring.")
                stopMonitoring()
                return
            }

            if (!locationRequestInProgress) {
                locationRequestInProgress = true

                WearLocationManager.fetchLocation(
                    this@WearGeofenceMonitoringService
                ) { location ->

                    locationRequestInProgress = false

                    if (!isMonitoring) return@fetchLocation

                    if (location != null) {
                        WearSafetyStateRepository.checkLocationAgainstSafeZone(
                            location.latitude,
                            location.longitude
                        )
                        Log.d(
                            TAG,
                            "Elopement state after GPS check: " +
                                    WearSafetyStateRepository.isElopementDetected.value
                        )
                        val exitEvent = WearSafetyStateRepository.safeZoneExitEvent.value

                        if (exitEvent != null &&
                            exitEvent.timestamp != lastHandledExitTimestamp
                        ) {
                            lastHandledExitTimestamp = exitEvent.timestamp

                            Log.i(
                                TAG,
                                "SAFE_ZONE_EXIT: childId=${exitEvent.childId}, " +
                                        "safeZoneId=${exitEvent.safeZoneId}, " +
                                        "timestamp=${exitEvent.timestamp}"
                            )
                        }

                        Log.d(
                            TAG,
                            "Geofence checked: ${location.latitude}, ${location.longitude}"
                        )
                    } else {
                        Log.w(TAG, "Location unavailable. Will retry.")
                    }
                }
            }

            handler.postDelayed(this, CHECK_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()

        WearSafeZoneStorage.restore(this)
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val safeZone = WearSafetyStateRepository.activeSafeZone.value

        if (safeZone == null || !safeZone.monitoringEnabled) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )

        if (!isMonitoring) {
            isMonitoring = true
            handler.post(monitoringRunnable)
        }

        return START_STICKY
    }

    private fun stopMonitoring() {
        isMonitoring = false
        handler.removeCallbacks(monitoringRunnable)
        stopSelf()
    }

    override fun onDestroy() {
        isMonitoring = false
        handler.removeCallbacks(monitoringRunnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Safe Zone Monitoring",
                NotificationManager.IMPORTANCE_LOW
            )

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WanderSpectra")
            .setContentText("Safe Zone monitoring is active")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
    }
}