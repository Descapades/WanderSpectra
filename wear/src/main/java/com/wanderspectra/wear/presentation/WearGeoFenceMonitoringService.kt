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
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject
import android.content.Context
import android.location.Location

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
    private var lastHandledReentryTimestamp: Long? = null
    private var isRouteTracking = false
    private var routeStartTimestamp: Long? = null
    private var lastRouteLocation: Location? = null
    private var routeDistanceMeters = 0.0
    private val routePoints = mutableListOf<Location>()

    private val monitoringRunnable = object : Runnable {
        override fun run() {
            if (!isMonitoring) return

            val safeZone = WearSafetyStateRepository.activeSafeZone.value

            if (safeZone == null || !safeZone.monitoringEnabled) {
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
                        if (isRouteTracking &&
                            WearSafetyStateRepository.isElopementDetected.value
                        ) {
                            val previousLocation = lastRouteLocation

                            if (previousLocation != null) {
                                val results = FloatArray(1)

                                Location.distanceBetween(
                                    previousLocation.latitude,
                                    previousLocation.longitude,
                                    location.latitude,
                                    location.longitude,
                                    results
                                )

                                val distance = results[0]

                                if (distance > 0f) {
                                    routeDistanceMeters += distance.toDouble()
                                }

                                lastRouteLocation = Location("route_point").apply {
                                    latitude = location.latitude
                                    longitude = location.longitude
                                    time = System.currentTimeMillis()
                                }

                                lastRouteLocation?.let { routePoint ->
                                    routePoints.add(Location(routePoint))
                                }
                            }
                        }
                        val exitEvent = WearSafetyStateRepository.safeZoneExitEvent.value

                        if (exitEvent != null &&
                            exitEvent.timestamp != lastHandledExitTimestamp
                        ) {
                            lastHandledExitTimestamp = exitEvent.timestamp
                            isRouteTracking = true
                            routeStartTimestamp = exitEvent.timestamp
                            routeDistanceMeters = 0.0

                            lastRouteLocation = Location("safe_zone_exit").apply {
                                latitude = exitEvent.latitude
                                longitude = exitEvent.longitude
                                time = exitEvent.timestamp
                            }

                            routePoints.clear()

                            lastRouteLocation?.let { startingPoint ->
                                routePoints.add(Location(startingPoint))
                            }

                            Log.i(
                                TAG,
                                "SAFE_ZONE_EXIT: childId=${exitEvent.childId}, " +
                                        "safeZoneId=${exitEvent.safeZoneId}, " +
                                        "timestamp=${exitEvent.timestamp}"
                            )
                            sendSafeZoneExitEvent(exitEvent)
                        }
                        val reentryEvent =
                            WearSafetyStateRepository.safeZoneReentryEvent.value

                        if (reentryEvent != null &&
                            reentryEvent.timestamp != lastHandledReentryTimestamp
                        ) {
                            lastHandledReentryTimestamp = reentryEvent.timestamp

                            Log.i(
                                TAG,
                                "GPS route before re-entry: " +
                                        "points=${routePoints.size}, " +
                                        "distanceMeters=$routeDistanceMeters, " +
                                        "tracking=$isRouteTracking"
                            )
                            isRouteTracking = false

                            val finalRoutePoint = Location("safe_zone_reentry").apply {
                                latitude = reentryEvent.latitude
                                longitude = reentryEvent.longitude
                                time = reentryEvent.timestamp
                            }

                            val previousRoutePoint = lastRouteLocation

                            if (previousRoutePoint != null) {
                                val finalDistance = previousRoutePoint.distanceTo(finalRoutePoint)

                                if (finalDistance > 0f) {
                                    routeDistanceMeters += finalDistance.toDouble()
                                }
                            }

                            lastRouteLocation = finalRoutePoint
                            routePoints.add(Location(finalRoutePoint))

                            Log.i(
                                TAG,
                                "SAFE_ZONE_REENTRY: childId=${reentryEvent.childId}, " +
                                        "safeZoneId=${reentryEvent.safeZoneId}, " +
                                        "timestamp=${reentryEvent.timestamp}"
                            )
                            sendSafeZoneReentryEvent(reentryEvent)
                        }
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

    private fun sendSafeZoneExitEvent(exitEvent: SafeZoneExitEvent) {
        val caregiverNodeId = getSharedPreferences(
            "wanderspectra_connection",
            Context.MODE_PRIVATE
        ).getString("caregiverNodeId", null)

        if (caregiverNodeId.isNullOrBlank()) {
            Log.w(TAG, "No caregiver phone saved. Cannot send Safe Zone exit.")
            return
        }

        val payload = JSONObject().apply {
            put("childId", exitEvent.childId)
            put("safeZoneId", exitEvent.safeZoneId)
            put("latitude", exitEvent.latitude)
            put("longitude", exitEvent.longitude)
            put("timestamp", exitEvent.timestamp)
        }

        Wearable.getMessageClient(this)
            .sendMessage(
                caregiverNodeId,
                "/wanderspectra/safezone/exit",
                payload.toString().toByteArray(Charsets.UTF_8)
            )
            .addOnSuccessListener {
                Log.i(TAG, "Safe Zone exit message sent to caregiver phone")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to send Safe Zone exit message", exception)
            }
    }

    private fun buildRoutePayload(): JSONObject {
        return JSONObject().apply {
            put("startTimestamp", routeStartTimestamp)
            put("distanceTraveledMeters", routeDistanceMeters)

            val pointsArray = org.json.JSONArray()

            routePoints.forEach { point ->
                pointsArray.put(
                    JSONObject().apply {
                        put("latitude", point.latitude)
                        put("longitude", point.longitude)
                        put("timestamp", point.time)
                    }
                )
            }

            put("routePoints", pointsArray)
        }
    }

    private fun sendSafeZoneReentryEvent(reentryEvent: SafeZoneReentryEvent) {
        val caregiverNodeId = getSharedPreferences(
            "wanderspectra_connection",
            Context.MODE_PRIVATE
        ).getString("caregiverNodeId", null)

        if (caregiverNodeId.isNullOrBlank()) {
            Log.w(TAG, "No caregiver phone saved. Cannot send Safe Zone re-entry.")
            return
        }

        val payload = JSONObject().apply {
            put("childId", reentryEvent.childId)
            put("safeZoneId", reentryEvent.safeZoneId)
            put("latitude", reentryEvent.latitude)
            put("longitude", reentryEvent.longitude)
            put("timestamp", reentryEvent.timestamp)
        }
        payload.put("routeData", buildRoutePayload())

        Wearable.getMessageClient(this)
            .sendMessage(
                caregiverNodeId,
                "/wanderspectra/safezone/reentry",
                payload.toString().toByteArray(Charsets.UTF_8)
            )
            .addOnSuccessListener {
                Log.i(TAG, "Safe Zone re-entry message sent to caregiver phone")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to send Safe Zone re-entry message", exception)
            }
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