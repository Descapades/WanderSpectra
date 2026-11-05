package com.wanderspectra.wear.presentation

import android.content.Context
import android.media.MediaPlayer
import android.os.BatteryManager
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.wanderspectra.wear.location.WearLocationManager
import org.json.JSONObject
import android.content.Intent
import android.os.VibrationEffect
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import android.os.Vibrator
import android.media.AudioAttributes
import com.wanderspectra.wear.R
import android.os.Build
import android.os.VibratorManager

class WearConnectionListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "WearConnection"
        private const val CONNECT_PATH = "/wanderspectra/connect"
        private const val CONNECTED_PATH = "/wanderspectra/connected"
        private const val LOCATION_PATH = "/wanderspectra/location"
        private const val LOCATION_RESPONSE_PATH = "/wanderspectra/location_response"
        private const val PLAY_SOUND_PATH = "/wanderspectra/play_sound"
        private const val STOP_SOUND_PATH = "/wanderspectra/stop_sound"
        private const val ELOPEMENT_PATH = "/wanderspectra/elopement"
        private const val SAFEZONE_PATH = "/wanderspectra/safezone"
        private const val SAFEZONE_DELETE_PATH = "/wanderspectra/safezone/delete"
        private var alarmPlayer: MediaPlayer? = null
        private var alarmVibrator: Vibrator? = null
        private val alarmLock = Any()
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
                        val activeZone = WearSafetyStateRepository.activeSafeZone.value

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
                        } else {
                            Log.e(TAG, "Failed to clear synchronized Safe Zone")
                        }
                    }
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

                        if (childName.isNotBlank()) {
                            getSharedPreferences(
                                "wanderspectra_child",
                                Context.MODE_PRIVATE
                            ).edit()
                                .putString("childName", childName)
                                .apply()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to read child name", e)
                    }
                }

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
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Failed to send response to caregiver app", exception)
                        }
                }
            }

            PLAY_SOUND_PATH -> {
                playWearableSound(this)
            }

            STOP_SOUND_PATH -> {
                stopWearableAlarm()
            }

            ELOPEMENT_PATH -> {
                val payload = messageEvent.data.toString(Charsets.UTF_8).lowercase()
                val isElopement =
                    payload.contains("true") || payload.contains("start") || payload.contains("exited") || payload.contains(
                        "elopement"
                    )
                WearSafetyStateRepository.setElopementDetected(isElopement)
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

                        val serviceIntent = Intent(
                            this,
                            WearGeofenceMonitoringService::class.java
                        )

                        try {
                            androidx.core.content.ContextCompat.startForegroundService(
                                this,
                                serviceIntent
                            )

                        } catch (e: Exception) {
                            Log.e(
                                TAG,
                                "Unable to start monitoring service from background",
                                e
                            )
                        }
                    } else {

                        stopService(
                            Intent(this, WearGeofenceMonitoringService::class.java)
                        )
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse safezone payload", e)
                }
            }
        }
    }

    private fun stopWearableAlarm() {
        synchronized(alarmLock) {
            try {
                alarmPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.stop()
                    }
                    player.release()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop wearable alarm", e)
            } finally {
                alarmPlayer = null
            }

            try {
                alarmVibrator?.cancel()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop wearable vibration", e)
            } finally {
                alarmVibrator = null
            }

            WearSafetyStateRepository.setPlayingSound(false)
        }
    }

    private fun playWearableSound(context: Context) {
        synchronized(alarmLock) {
            if (alarmPlayer?.isPlaying == true) {

                return
            }

            stopWearableAlarm()

            try {
                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    val audioFile = context.resources.openRawResourceFd(R.raw.alert_sound)
                    setDataSource(
                        audioFile.fileDescriptor,
                        audioFile.startOffset,
                        audioFile.length
                    )
                    audioFile.close()
                    isLooping = true
                    prepare()
                }

                alarmPlayer = player
                player.start()

                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(VibratorManager::class.java)
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
                alarmVibrator = vibrator

                val vibrationPattern = longArrayOf(0, 500, 250, 500, 750)

                vibrator.vibrate(
                    VibrationEffect.createWaveform(vibrationPattern, 0)
                )

                WearSafetyStateRepository.setPlayingSound(true)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to start wearable alarm", e)
                stopWearableAlarm()
            }
        }
    }
}
