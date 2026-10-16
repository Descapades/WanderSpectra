package com.wanderspectra.wear.presentation

import android.os.BatteryManager
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import android.content.Context
import com.wanderspectra.wear.location.WearLocationManager
import org.json.JSONObject

class WearConnectionListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "WearConnection"
        private const val CONNECT_PATH = "/wanderspectra/connect"
        private const val CONNECTED_PATH = "/wanderspectra/connected"
        private const val LOCATION_PATH = "/wanderspectra/location"
        private const val LOCATION_RESPONSE_PATH = "/wanderspectra/location_response"
        private const val ELOPEMENT_PATH = "/wanderspectra/elopement"
        private const val SAFEZONE_PATH = "/wanderspectra/safezone_update"
    }

    private fun getBatteryLevel(): Int {
        val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
        val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return if (capacity in 0..100) capacity else -1
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        when (messageEvent.path) {
            CONNECT_PATH, LOCATION_PATH -> {
                Log.d(TAG, "Request received (${messageEvent.path}) from node ${messageEvent.sourceNodeId}")

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
                    val responsePath = if (messageEvent.path == LOCATION_PATH) LOCATION_RESPONSE_PATH else CONNECTED_PATH

                    Wearable.getMessageClient(this)
                        .sendMessage(
                            messageEvent.sourceNodeId,
                            responsePath,
                            responseData
                        )
                        .addOnSuccessListener {
                            Log.d(TAG, "Location & battery response sent successfully to caregiver app via $responsePath: $responseJson")
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Failed to send response to caregiver app", exception)
                        }
                }
            }
            ELOPEMENT_PATH -> {
                val payload = messageEvent.data.toString(Charsets.UTF_8).lowercase()
                val isElopement = payload.contains("true") || payload.contains("start") || payload.contains("exited") || payload.contains("elopement")
                WearSafetyStateRepository.setElopementDetected(isElopement)
                Log.d(TAG, "Elopement message received: $payload, setElopementDetected($isElopement)")
            }
            SAFEZONE_PATH -> {
                val payload = messageEvent.data.toString(Charsets.UTF_8)
                try {
                    val json = JSONObject(payload)
                    val lat = json.optDouble("lat", 0.0)
                    val lng = json.optDouble("lng", 0.0)
                    val radius = json.optInt("radius", 500)
                    val enabled = json.optBoolean("monitoringEnabled", true)
                    WearSafetyStateRepository.updateActiveSafeZone(
                        WearSafeZone(
                            lat = lat,
                            lng = lng,
                            radiusFeet = radius,
                            monitoringEnabled = enabled
                        )
                    )
                    Log.d(TAG, "Updated active safe zone from message: lat=$lat, lng=$lng, radius=$radius ft")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse safezone payload", e)
                }
            }
        }
    }
}
