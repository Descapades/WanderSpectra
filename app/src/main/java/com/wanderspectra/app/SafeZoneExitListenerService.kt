package com.wanderspectra.app

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject

class SafeZoneExitListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "SafeZoneExitListener"
        private const val SAFE_ZONE_EXIT_PATH = "/wanderspectra/safezone/exit"
        private const val SAFE_ZONE_REENTRY_PATH = "/wanderspectra/safezone/reentry"
        private const val LOW_BATTERY_PATH = "/wanderspectra/battery/low"
        private const val WEARABLE_STATUS_PATH = "/wanderspectra/status"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.i(TAG, "Wear OS message received: ${messageEvent.path}")

        if (messageEvent.path == WEARABLE_STATUS_PATH) {
            try {
                val payload = JSONObject(
                    String(messageEvent.data, Charsets.UTF_8)
                )

                val childId = payload.getString("childId")
                val batteryLevel = payload.getInt("batteryLevel")
                val latitude = payload.getDouble("latitude")
                val longitude = payload.getDouble("longitude")
                val timestamp = payload.getLong("timestamp")
                val now = System.currentTimeMillis()

                if (
                    childId.isBlank() ||
                    batteryLevel !in 0..100 ||
                    !latitude.isFinite() ||
                    !longitude.isFinite() ||
                    latitude !in -90.0..90.0 ||
                    longitude !in -180.0..180.0 ||
                    timestamp < now - 5 * 60_000L ||
                    timestamp > now + 60_000L
                ) {
                    Log.w(TAG, "Wearable status rejected: invalid data")
                    return
                }

                val caregiverId = FirebaseAuth.getInstance().currentUser?.uid

                if (caregiverId == null) {
                    Log.w(TAG, "Wearable status rejected: no authenticated caregiver")
                    return
                }

                WearableRepository().getWearable(
                    childId = childId,
                    onSuccess = { wearable ->
                        if (
                            wearable == null ||
                            wearable.deviceId.isBlank() ||
                            wearable.deviceId != messageEvent.sourceNodeId
                        ) {
                            Log.w(
                                TAG,
                                "Wearable status rejected: unrecognized device"
                            )
                        } else {
                            val repository = WearableRepository()

                            repository.updateWearableLocation(
                                childId = childId,
                                latitude = latitude,
                                longitude = longitude,
                                timestamp = timestamp,
                                onSuccess = {
                                    Log.i(TAG, "Background wearable location saved")
                                },
                                onFailure = { exception ->
                                    Log.e(TAG, "Failed to save background wearable location", exception)
                                }
                            )

                            repository.updateWearableBattery(
                                childId = childId,
                                batteryLevel = batteryLevel,
                                onSuccess = {
                                    Log.i(TAG, "Background wearable battery saved")
                                },
                                onFailure = { exception ->
                                    Log.e(TAG, "Failed to save background wearable battery", exception)
                                }
                            )
                        }
                    },
                    onFailure = { exception ->
                        Log.e(
                            TAG,
                            "Failed to validate wearable status",
                            exception
                        )
                    }
                )

            } catch (exception: Exception) {
                Log.e(TAG, "Invalid wearable status message", exception)
            }

            return
        }
        if (messageEvent.path == "/wanderspectra/battery/status") {
            try {
                val payload = JSONObject(
                    String(messageEvent.data, Charsets.UTF_8)
                )

                val childId = payload.getString("childId")
                val batteryLevel = payload.getInt("batteryLevel")
                val timestamp = payload.getLong("timestamp")
                val now = System.currentTimeMillis()

                if (
                    childId.isBlank() ||
                    batteryLevel !in 0..100 ||
                    timestamp < now - 5 * 60_000L ||
                    timestamp > now + 60_000L
                ) {
                    Log.w(TAG, "Periodic battery status rejected: invalid data")
                    return
                }

                if (FirebaseAuth.getInstance().currentUser == null) {
                    Log.w(TAG, "Periodic battery status rejected: no authenticated caregiver")
                    return
                }

                WearableRepository().getWearable(
                    childId = childId,
                    onSuccess = { wearable ->
                        if (
                            wearable == null ||
                            wearable.deviceId.isBlank() ||
                            wearable.deviceId != messageEvent.sourceNodeId
                        ) {
                            Log.w(TAG, "Periodic battery status rejected: unrecognized device")
                        } else {
                            WearableRepository().updateWearableBattery(
                                childId = childId,
                                batteryLevel = batteryLevel,
                                onSuccess = {
                                    Log.i(TAG, "Periodic battery status saved")
                                    if (batteryLevel > 20) {
                                        CaregiverNotificationPreferences.setLowBatteryNotificationSent(
                                            applicationContext,
                                            childId,
                                            false
                                        )

                                        Log.i(TAG, "Low battery notification flag reset")
                                    }
                                    if (
                                        batteryLevel <= 20 &&
                                        CaregiverNotificationPreferences.isLowBatteryAlertEnabled(applicationContext) &&
                                        !CaregiverNotificationPreferences.hasLowBatteryNotificationBeenSent(
                                            applicationContext,
                                            childId
                                        )
                                    ) {
                                        val notificationSent =
                                            CaregiverNotificationManager.sendCaregiverNotification(
                                                context = applicationContext,
                                                title = "Wearable Low Battery",
                                                message = "Your child's wearable battery is at $batteryLevel%. Please charge the device."
                                            )

                                        if (notificationSent) {
                                            CaregiverNotificationPreferences.setLowBatteryNotificationSent(
                                                applicationContext,
                                                childId,
                                                true
                                            )

                                            Log.i(TAG, "Periodic low battery notification posted")
                                        }
                                    }
                                },
                                onFailure = { exception ->
                                    Log.e(TAG, "Failed to save periodic battery status", exception)
                                }
                            )
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to validate periodic battery status", exception)
                    }
                )

            } catch (exception: Exception) {
                Log.e(TAG, "Invalid periodic battery status message", exception)
            }

            return
        }
        if (messageEvent.path == LOW_BATTERY_PATH) {
            try {
                val payload = JSONObject(
                    String(messageEvent.data, Charsets.UTF_8)
                )

                val childId = payload.getString("childId")
                val batteryLevel = payload.getInt("batteryLevel")
                val timestamp = payload.getLong("timestamp")
                val now = System.currentTimeMillis()

                if (
                    childId.isBlank() ||
                    batteryLevel !in 0..20 ||
                    timestamp < now - 5 * 60_000L ||
                    timestamp > now + 60_000L
                ) {
                    Log.w(TAG, "Low battery message rejected: invalid data")
                    return
                }

                Log.i(TAG, "Low battery message data validated: $batteryLevel%")
                val caregiverId = FirebaseAuth.getInstance().currentUser?.uid

                if (caregiverId == null) {
                    Log.w(TAG, "Low battery message rejected: no authenticated caregiver")
                    return
                }

                WearableRepository().getWearable(
                    childId = childId,
                    onSuccess = { wearable ->
                        if (
                            wearable == null ||
                            wearable.deviceId.isBlank() ||
                            wearable.deviceId != messageEvent.sourceNodeId
                        ) {
                            Log.w(
                                TAG,
                                "Low battery message rejected: wearable does not match registered device"
                            )
                        } else {
                            Log.i(
                                TAG,
                                "Low battery message validated against registered wearable"
                            )

                            if (
                                CaregiverNotificationPreferences.isLowBatteryAlertEnabled(applicationContext) &&
                                !CaregiverNotificationPreferences.hasLowBatteryNotificationBeenSent(
                                    applicationContext,
                                    childId
                                )
                            ) {
                                val notificationSent =
                                    CaregiverNotificationManager.sendCaregiverNotification(
                                        context = applicationContext,
                                        title = "Wearable Low Battery",
                                        message = "Your child's wearable battery is at $batteryLevel%. Please charge the device."
                                    )

                                if (notificationSent) {
                                    CaregiverNotificationPreferences.setLowBatteryNotificationSent(
                                        applicationContext,
                                        childId,
                                        true
                                    )

                                    Log.i(TAG, "Low battery notification posted")
                                } else {
                                    Log.w(TAG, "Low battery notification could not be posted")
                                }
                            }
                        }
                    },
                    onFailure = { exception ->
                        Log.e(
                            TAG,
                            "Failed to validate low battery wearable",
                            exception
                        )
                    }
                )
            } catch (exception: Exception) {
                Log.e(TAG, "Invalid low battery message", exception)
            }

            return
        }
        if (messageEvent.path != SAFE_ZONE_EXIT_PATH &&
            messageEvent.path != SAFE_ZONE_REENTRY_PATH
        ) return
        if (messageEvent.path == SAFE_ZONE_REENTRY_PATH) {
            handleSafeZoneReentry(messageEvent)
            return
        }

        try {
            val payload = JSONObject(
                String(messageEvent.data, Charsets.UTF_8)
            )

            val childId = payload.getString("childId")
            val safeZoneId = payload.getString("safeZoneId")
            val latitude = payload.getDouble("latitude")
            val longitude = payload.getDouble("longitude")
            val timestamp = payload.getLong("timestamp")

            if (
                childId.isBlank() ||
                safeZoneId.isBlank() ||
                !latitude.isFinite() ||
                !longitude.isFinite() ||
                latitude !in -90.0..90.0 ||
                longitude !in -180.0..180.0 ||
                timestamp < System.currentTimeMillis() - 5 * 60_000L ||
                timestamp > System.currentTimeMillis() + 60_000L
            ) {
                Log.w(TAG, "Safe Zone exit rejected: invalid event data")
                return
            }

            val caregiverId = FirebaseAuth.getInstance().currentUser?.uid

            if (caregiverId == null) {
                Log.w(TAG, "Safe Zone exit rejected: no authenticated caregiver")
                return
            }

            WearableRepository().getWearable(
                childId = childId,
                onSuccess = wearableCallback@{ wearable ->

                    if (
                        wearable == null ||
                        wearable.deviceId.isBlank() ||
                        wearable.deviceId != messageEvent.sourceNodeId
                    ) {
                        Log.w(
                            TAG,
                            "Safe Zone exit rejected: wearable does not match registered device"
                        )
                        return@wearableCallback
                    }

                    Log.i(TAG, "Safe Zone exit validated against registered wearable")

                    SafeZoneRepository().getById(
                        safeZoneId
                    ) safeZoneCallback@{ safeZone ->

                        if (
                            safeZone == null ||
                            safeZone.childId != childId ||
                            !safeZone.monitoringEnabled
                        ) {
                            Log.w(
                                TAG,
                                "Safe Zone exit rejected: invalid Safe Zone"
                            )
                            return@safeZoneCallback
                        }

                        Log.i(TAG, "Safe Zone exit validated against registered Safe Zone")

                        WearConnectionManager.openWatchApp(
                            context = applicationContext,
                            nodeId = messageEvent.sourceNodeId
                        ) { success ->
                            if (success) {
                                Log.i(TAG, "Open on Watch request sent after Safe Zone exit")
                            } else {
                                Log.w(TAG, "Failed to send Open on Watch request after Safe Zone exit")
                            }
                        }

                        val incident = Incident(
                            childId = childId,
                            caregiverId = caregiverId,
                            status = "closed",
                            startTime = timestamp,
                            endTime = timestamp,
                            lastKnownLat = latitude,
                            lastKnownLng = longitude,
                            type = "SAFE_ZONE_EXIT",
                            safeZoneId = safeZoneId,
                            safeZoneName = safeZone.name
                        )


                        IncidentRepository().saveSafeZoneExitIncident(incident) { success, message ->
                            if (success) {
                                Log.i(TAG, "Safe Zone exit incident saved to Firestore")

                                if (CaregiverNotificationPreferences.isSafeZoneExitEnabled(applicationContext)) {
                                    val notificationSent = CaregiverNotificationManager.sendCaregiverNotification(
                                        context = applicationContext,
                                        title = "Safe Zone Exit Alert",
                                        message = "Your child's wearable has left ${safeZone.name}."
                                    )

                                    if (notificationSent) {
                                        Log.i(TAG, "Safe Zone exit notification posted")
                                    } else {
                                        Log.w(TAG, "Safe Zone exit notification could not be posted")
                                    }
                                } else {
                                    Log.i(TAG, "Safe Zone exit notification disabled by caregiver")
                                }
                            } else {
                                Log.e(TAG, "Safe Zone exit incident save failed: $message")
                            }
                        }

                    }
                },
                onFailure = { exception ->
                    Log.e(
                        TAG,
                        "Failed to validate Safe Zone exit wearable",
                        exception
                    )
                }
            )

        } catch (exception: Exception) {
            Log.e(TAG, "Invalid Safe Zone exit message", exception)
        }
    }

    private fun handleSafeZoneReentry(messageEvent: MessageEvent) {
        try {
            val payload = JSONObject(
                String(messageEvent.data, Charsets.UTF_8)
            )

            val childId = payload.getString("childId")
            val safeZoneId = payload.getString("safeZoneId")
            val latitude = payload.getDouble("latitude")
            val longitude = payload.getDouble("longitude")
            val timestamp = payload.getLong("timestamp")
            val routeData = payload.optJSONObject("routeData")
            val routePoints = routeData?.optJSONArray("routePoints")

            val validRoutePoints = mutableListOf<Map<String, Any>>()

            if (routePoints != null && routePoints.length() <= 500) {
                for (i in 0 until routePoints.length()) {
                    val point = routePoints.optJSONObject(i) ?: continue

                    val pointLatitude = point.optDouble("latitude", Double.NaN)
                    val pointLongitude = point.optDouble("longitude", Double.NaN)
                    val pointTimestamp = point.optLong("timestamp", -1L)

                    if (
                        pointLatitude.isFinite() &&
                        pointLongitude.isFinite() &&
                        pointLatitude in -90.0..90.0 &&
                        pointLongitude in -180.0..180.0 &&
                        pointTimestamp > 0L &&
                        pointTimestamp <= timestamp
                    ) {
                        validRoutePoints.add(
                            mapOf(
                                "latitude" to pointLatitude,
                                "longitude" to pointLongitude,
                                "timestamp" to pointTimestamp
                            )
                        )
                    }
                }
            }

            val now = System.currentTimeMillis()

            if (
                childId.isBlank() ||
                safeZoneId.isBlank() ||
                !latitude.isFinite() ||
                !longitude.isFinite() ||
                latitude !in -90.0..90.0 ||
                longitude !in -180.0..180.0 ||
                timestamp < now - 5 * 60_000L ||
                timestamp > now + 60_000L
            ) {
                Log.w(TAG, "Safe Zone re-entry rejected: invalid event data")
                return
            }

            Log.i(TAG, "Safe Zone re-entry payload validated")

            val caregiverId = FirebaseAuth.getInstance().currentUser?.uid

            if (caregiverId == null) {
                Log.w(
                    TAG,
                    "Safe Zone re-entry rejected: no authenticated caregiver"
                )
                return
            }

            WearableRepository().getWearable(
                childId = childId,
                onSuccess = wearableCallback@{ wearable ->

                    if (
                        wearable == null ||
                        wearable.deviceId.isBlank() ||
                        wearable.deviceId != messageEvent.sourceNodeId
                    ) {
                        Log.w(
                            TAG,
                            "Safe Zone re-entry rejected: wearable does not match registered device"
                        )
                        return@wearableCallback
                    }

                    Log.i(
                        TAG,
                        "Safe Zone re-entry validated against registered wearable"
                    )

                    SafeZoneRepository().getById(
                        safeZoneId
                    ) safeZoneCallback@{ safeZone ->

                        if (
                            safeZone == null ||
                            safeZone.childId != childId ||
                            !safeZone.monitoringEnabled
                        ) {
                            Log.w(
                                TAG,
                                "Safe Zone re-entry rejected: invalid Safe Zone"
                            )
                            return@safeZoneCallback
                        }

                        Log.i(
                            TAG,
                            "Safe Zone re-entry validated against registered Safe Zone"
                        )

                        IncidentRepository().closeSafeZoneExitOnReentry(
                            caregiverId = caregiverId,
                            childId = childId,
                            safeZoneId = safeZoneId,
                            reentryTimestamp = timestamp,
                            reentryLatitude = latitude,
                            reentryLongitude = longitude,
                            routePoints = validRoutePoints,
                        ) { success, message ->

                            if (success) {
                                Log.i(TAG, "Safe Zone re-entry processed: $message")
                            } else {
                                Log.w(TAG, "Safe Zone re-entry could not close incident: $message")
                            }
                        }
                    }
                },
                onFailure = { exception ->
                    Log.e(
                        TAG,
                        "Failed to validate Safe Zone re-entry wearable",
                        exception
                    )
                }
            )

        } catch (exception: Exception) {
            Log.e(TAG, "Invalid Safe Zone re-entry message", exception)
        }
    }
}

