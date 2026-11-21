package com.wanderspectra.wear


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject

class WearBatteryReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "WearBattery"
        private const val LOW_BATTERY_THRESHOLD = 20
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "WearBatteryReceiver triggered: ${intent.action}")
        if (
            intent.action != Intent.ACTION_BATTERY_LOW &&
            intent.action != Intent.ACTION_BATTERY_OKAY &&
            intent.action != "com.wanderspectra.wear.TEST_BATTERY_LOW"
        ) {
            return
        }

        val batteryManager =
            context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        val batteryLevel = batteryManager.getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CAPACITY
        )

        if (batteryLevel !in 0..100) {
            Log.w(TAG, "Invalid battery reading")
            return
        }

        Log.i(TAG, "Battery event received: $batteryLevel%")

        val childPreferences = context.getSharedPreferences(
            "wanderspectra_child",
            Context.MODE_PRIVATE
        )

        val childId = childPreferences.getString("childId", null)
            ?: com.wanderspectra.wear.presentation.WearSafetyStateRepository
                .activeSafeZone.value?.childId
        Log.d(
            TAG,
            "Battery receiver child ID available: ${!childId.isNullOrBlank()}"
        )

        val connectionPreferences = context.getSharedPreferences(
            "wanderspectra_connection",
            Context.MODE_PRIVATE
        )

        val caregiverNodeId = connectionPreferences.getString(
            "caregiverNodeId",
            null
        )

        if (childId.isNullOrBlank() || caregiverNodeId.isNullOrBlank()) {
            Log.w(TAG, "Battery alert skipped: wearable connection information missing")
            return
        }

        Log.i(TAG, "Battery alert connection information available")

        if (batteryLevel <= LOW_BATTERY_THRESHOLD) {
            Log.i(TAG, "Low battery detected: $batteryLevel%")

            val payload = JSONObject().apply {
                put("childId", childId)
                put("batteryLevel", batteryLevel)
                put("timestamp", System.currentTimeMillis())
            }

            Wearable.getMessageClient(context)
                .sendMessage(
                    caregiverNodeId,
                    "/wanderspectra/battery/low",
                    payload.toString().toByteArray(Charsets.UTF_8)
                )
                .addOnSuccessListener {
                    Log.i(TAG, "Low battery message sent to caregiver phone")
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to send low battery message", exception)
                }

        } else {
            Log.i(TAG, "Battery above threshold: $batteryLevel%")
        }
    }
}
