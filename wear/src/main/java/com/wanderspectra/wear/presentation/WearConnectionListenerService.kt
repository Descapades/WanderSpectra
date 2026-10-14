package com.wanderspectra.wear.presentation

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.wanderspectra.wear.location.WearLocationManager
import org.json.JSONObject
import android.util.Log

class WearConnectionListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "WearConnection"
        private const val CONNECT_PATH = "/wanderspectra/connect"
        private const val CONNECTED_PATH = "/wanderspectra/connected"
        private const val LOCATION_PATH = "/wanderspectra/location"
        private const val LOCATION_RESPONSE_PATH = "/wanderspectra/location_response"
    }

    private fun getBatteryLevel(): Int {
        val batteryStatus = registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val level = batteryStatus?.getIntExtra(
            BatteryManager.EXTRA_LEVEL,
            -1
        ) ?: -1

        val scale = batteryStatus?.getIntExtra(
            BatteryManager.EXTRA_SCALE,
            -1
        ) ?: -1

        return if (level >= 0 && scale > 0) {
            (level * 100) / scale
        } else {
            -1
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        when (messageEvent.path) {
            CONNECT_PATH, LOCATION_PATH -> {
                Log.d(TAG, "Request received (${messageEvent.path}) from node ${messageEvent.sourceNodeId}")

                val batteryLevel = getBatteryLevel()

                WearLocationManager.fetchLocation(this) { locationData ->
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
        }
    }
}
