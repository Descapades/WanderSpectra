package com.wanderspectra.wear.presentation

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.wanderspectra.wear.location.WearLocationManager
import org.json.JSONObject
import android.content.Intent
import androidx.core.content.ContextCompat
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem

class WearConnectionListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "WearConnection"
        private const val SAFEZONE_PREFS = "wanderspectra_safezone"
        private const val SAFEZONE_DATA_KEY = "active_safezone"
        private const val CONNECT_PATH = "/wanderspectra/connect"
        private const val CONNECTED_PATH = "/wanderspectra/connected"
        private const val LOCATION_PATH = "/wanderspectra/location"
        private const val LOCATION_RESPONSE_PATH = "/wanderspectra/location_response"
        private const val PLAY_SOUND_PATH = "/wanderspectra/play_sound"
        private const val ELOPEMENT_PATH = "/wanderspectra/elopement"
        private const val SAFEZONE_PATH = "/wanderspectra/safezone"
        private const val SAFEZONE_DELETE_PATH = "/wanderspectra/safezone/delete"

    }

    private fun saveSafeZoneLocally(safeZone: WearSafeZone) {
        val json = JSONObject().apply {
            put("lat", safeZone.lat)
            put("lng", safeZone.lng)
            put("radius", safeZone.radiusFeet)
            put("monitoringEnabled", safeZone.monitoringEnabled)
        }

        getSharedPreferences(SAFEZONE_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(SAFEZONE_DATA_KEY, json.toString())
            .apply()

        Log.d(TAG, "Safe Zone saved locally on watch")
    }

    private fun restoreSafeZoneLocally() {
        val savedJson = getSharedPreferences(
            SAFEZONE_PREFS,
            Context.MODE_PRIVATE
        ).getString(SAFEZONE_DATA_KEY, null) ?: return

        try {
            val json = JSONObject(savedJson)

            val safeZone = WearSafeZone(
                lat = json.getDouble("lat"),
                lng = json.getDouble("lng"),
                radiusFeet = json.getInt("radius"),
                monitoringEnabled = json.getBoolean("monitoringEnabled")
            )

            if (
                !safeZone.lat.isFinite() ||
                !safeZone.lng.isFinite() ||
                safeZone.lat !in -90.0..90.0 ||
                safeZone.lng !in -180.0..180.0 ||
                safeZone.radiusFeet <= 0
            ) {
                Log.w(TAG, "Stored Safe Zone is invalid")
                return
            }

            WearSafetyStateRepository.updateActiveSafeZone(safeZone)
            Log.d(TAG, "Safe Zone restored from watch storage")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore Safe Zone", e)
        }
    }

    private fun getBatteryLevel(): Int {
        val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
        val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return if (capacity in 0..100) capacity else -1
    }

    override fun onCreate() {
        super.onCreate()
        WearSafeZoneStorage.restore(this)

        Wearable.getDataClient(this)
            .getDataItems()
            .addOnSuccessListener { dataItems ->
                try {
                    for (item in dataItems) {
                        if (item.uri.path != "/wanderspectra/safezone/state") {
                            continue
                        }

                        val dataMap = DataMapItem.fromDataItem(item).dataMap
                        val deleted = dataMap.getBoolean("deleted", false)
                        val zoneId = dataMap.getString("zoneId") ?: ""

                        val activeZone =
                            WearSafetyStateRepository.activeSafeZone.value

                        if (
                            deleted &&
                            zoneId.isNotBlank() &&
                            activeZone?.id == zoneId
                        ) {
                            val cleared = WearSafeZoneStorage.clear(this)

                            if (cleared) {
                                WearSafetyStateRepository.clearActiveSafeZone()

                                stopService(
                                    Intent(
                                        this,
                                        WearGeofenceMonitoringService::class.java
                                    )
                                )

                                Log.d(
                                    TAG,
                                    "Restored deletion state for Safe Zone $zoneId"
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to check saved Safe Zone state", e)
                } finally {
                    dataItems.release()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to retrieve Safe Zone Data Layer state", exception)
            }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type != DataEvent.TYPE_CHANGED) continue

            val item = event.dataItem

            if (item.uri.path == "/wanderspectra/safezone/state") {
                val dataMap = DataMapItem.fromDataItem(item).dataMap
                val deleted = dataMap.getBoolean("deleted", false)
                val zoneId = dataMap.getString("zoneId") ?: ""

                if (deleted) {
                    val activeZone = WearSafetyStateRepository.activeSafeZone.value

                    if (zoneId.isBlank() || activeZone?.id != zoneId) {
                        Log.w(TAG, "Ignoring deletion for a different Safe Zone")
                    } else {
                        val cleared = WearSafeZoneStorage.clear(this)

                        if (cleared) {
                            WearSafetyStateRepository.clearActiveSafeZone()

                            stopService(
                                Intent(this, WearGeofenceMonitoringService::class.java)
                            )

                            Log.d(TAG, "Safe Zone $zoneId deletion synchronized")
                        } else {
                            Log.e(TAG, "Failed to clear synchronized Safe Zone")
                        }
                    }
                } else {
                    Log.d(TAG, "Safe Zone active state received")
                }
            }
        }
    }
        override fun onMessageReceived(messageEvent: MessageEvent) {
            super.onMessageReceived(messageEvent)

            when (messageEvent.path) {
                CONNECT_PATH, LOCATION_PATH -> {
                    if (messageEvent.path == CONNECT_PATH) {
                        try {
                            val json = JSONObject(
                                messageEvent.data.toString(Charsets.UTF_8)
                            )

                            val childName = json.optString("childName", "").trim()
                            Log.d(TAG, "PAIRING DEBUG: Received childName='$childName'")

                            if (childName.isNotBlank()) {
                                getSharedPreferences(
                                    "wanderspectra_child",
                                    Context.MODE_PRIVATE
                                ).edit()
                                    .putString("childName", childName)
                                    .apply()

                                Log.d(TAG, "Child greeting name saved")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to read child name", e)
                        }
                    }
                    Log.d(
                        TAG,
                        "Request received (${messageEvent.path}) from node ${messageEvent.sourceNodeId}"
                    )

                    val batteryLevel = getBatteryLevel()

                    WearLocationManager.fetchLocation(this) { locationData ->
                        if (locationData != null) {
                            WearSafetyStateRepository.checkLocationAgainstSafeZone(
                                locationData.latitude,
                                locationData.longitude
                            )
                        }

                        val responseJson = JSONObject().apply {
                            put("batteryLevel", batteryLevel)
                            if (locationData != null) {
                                put("latitude", locationData.latitude)
                                put("longitude", locationData.longitude)
                                put("accuracy", locationData.accuracy.toDouble())
                                put("timestamp", locationData.timestamp)
                            } else {
                                put("latitude", JSONObject.NULL)
                                put("longitude", JSONObject.NULL)
                                put("accuracy", 0.0)
                                put("timestamp", System.currentTimeMillis())
                            }
                        }

                        val responseData = responseJson.toString().toByteArray(Charsets.UTF_8)
                        val responsePath =
                            if (messageEvent.path == LOCATION_PATH) LOCATION_RESPONSE_PATH else CONNECTED_PATH

                        Wearable.getMessageClient(this)
                            .sendMessage(
                                messageEvent.sourceNodeId,
                                responsePath,
                                responseData
                            )
                            .addOnSuccessListener {
                                Log.d(
                                    TAG,
                                    "Location & battery response sent successfully to caregiver app via $responsePath: $responseJson"
                                )
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "Failed to send response to caregiver app", exception)
                            }
                    }
                }

                PLAY_SOUND_PATH -> {
                    Log.d(TAG, "Play sound request received from node ${messageEvent.sourceNodeId}")
                    WearSafetyStateRepository.setPlayingSound(true)
                    playWearableSound(this) {
                        WearSafetyStateRepository.setPlayingSound(false)
                    }
                }

                ELOPEMENT_PATH -> {
                    val payload = messageEvent.data.toString(Charsets.UTF_8).lowercase()
                    val isElopement =
                        payload.contains("true") || payload.contains("start") || payload.contains("exited") || payload.contains(
                            "elopement"
                        )
                    WearSafetyStateRepository.setElopementDetected(isElopement)
                    Log.d(
                        TAG,
                        "Elopement message received: $payload, setElopementDetected($isElopement)"
                    )
                }

                SAFEZONE_DELETE_PATH -> {
                    try {
                        val payload = messageEvent.data.toString(Charsets.UTF_8)
                        val zoneId = JSONObject(payload).optString("id", "")

                        val activeZone = WearSafetyStateRepository.activeSafeZone.value

                        if (zoneId.isBlank() || activeZone?.id != zoneId) {
                            Log.w(TAG, "Ignoring deletion for a different Safe Zone")
                        } else {
                            val cleared = WearSafeZoneStorage.clear(this)

                            if (cleared) {
                                WearSafetyStateRepository.clearActiveSafeZone()

                                stopService(
                                    Intent(this, WearGeofenceMonitoringService::class.java)
                                )

                                Log.d(TAG, "Safe Zone $zoneId deleted from watch")
                            } else {
                                Log.e(TAG, "Failed to clear Safe Zone from storage")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to process Safe Zone deletion", e)
                    }
                }

                SAFEZONE_PATH -> {
                    val payload = messageEvent.data.toString(Charsets.UTF_8)
                    try {
                        val json = JSONObject(payload)
                        val zoneId = json.optString("id", "")
                        val childId = json.optString("childId", "")
                        val lat = json.optDouble("lat", 0.0)
                        val lng = json.optDouble("lng", 0.0)
                        val radius = json.optInt("radius", 500)
                        val enabled = json.optBoolean("monitoringEnabled", true)
                        val safeZone = WearSafeZone(
                            id = zoneId,
                            childId = childId,
                            lat = lat,
                            lng = lng,
                            radiusFeet = radius,
                            monitoringEnabled = enabled
                        )

                        if (
                            !lat.isFinite() ||
                            !lng.isFinite() ||
                            lat !in -90.0..90.0 ||
                            lng !in -180.0..180.0 ||
                            radius <= 0
                        ) {
                            Log.w(TAG, "Ignoring invalid Safe Zone configuration")
                            return
                        }

                        WearSafeZoneStorage.save(this, safeZone)
                        WearSafetyStateRepository.updateActiveSafeZone(safeZone)
                        if (safeZone.monitoringEnabled) {
                            Log.d(TAG, "Safe Zone monitoring enabled")

                            // If monitoring is already running, the service
                            // will use the updated Safe Zone automatically.
                            // Starting a new location foreground service from
                            // a background message may be restricted by Android.
                            Log.d(TAG, "Monitoring will start when the watch app is opened")
                        } else {
                            Log.d(TAG, "Safe Zone monitoring disabled. Stopping service.")

                            stopService(
                                Intent(this, WearGeofenceMonitoringService::class.java)
                            )
                        }
                        Log.d(
                            TAG,
                            "Updated Safe Zone for childId=$childId: " +
                                    "lat=$lat, lng=$lng, radius=$radius ft"
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse safezone payload", e)
                    }
                }
            }
        }

        private fun playWearableSound(context: Context, onCompletion: () -> Unit) {
            try {
                val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

                val mediaPlayer = MediaPlayer.create(context, alertUri)
                    ?: MediaPlayer.create(
                        context,
                        android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
                    )

                if (mediaPlayer != null) {
                    mediaPlayer.setOnCompletionListener {
                        try {
                            it.release()
                        } catch (_: Exception) {
                        }
                        onCompletion()
                    }
                    mediaPlayer.start()

                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            if (mediaPlayer.isPlaying) {
                                mediaPlayer.stop()
                                mediaPlayer.release()
                                onCompletion()
                            }
                        } catch (_: Exception) {
                            onCompletion()
                        }
                    }, 4000L)
                } else {
                    onCompletion()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to play wearable sound", e)
                onCompletion()
            }
        }
    }
