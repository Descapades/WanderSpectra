package com.wanderspectra.wear.presentation

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import android.content.Context
import android.os.BatteryManager

class WearConnectionListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "WearConnection"
        private const val CONNECT_PATH = "/wanderspectra/connect"
        private const val CONNECTED_PATH = "/wanderspectra/connected"
    }

    private fun getBatteryLevel(): Int {
        val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
        val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return if (capacity in 0..100) capacity else -1
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        if (messageEvent.path == CONNECT_PATH) {

            Log.d(TAG, "Connection request received from caregiver app")

            val batteryLevel = getBatteryLevel()

            Wearable.getMessageClient(this)
                .sendMessage(
                    messageEvent.sourceNodeId,
                    CONNECTED_PATH,
                    batteryLevel.toString().toByteArray()
                )
                .addOnSuccessListener {
                    Log.d(TAG, "Connection response sent to caregiver app")
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to send connection response", exception)
                }
        }
    }
}

