package com.wanderspectra.wear

import android.content.Context
import android.os.BatteryManager
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject

class WearBatteryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    companion object {
        private const val TAG = "WearBatteryWorker"
    }

    override fun doWork(): Result {
        val context = applicationContext

        val childId = context.getSharedPreferences(
            "wanderspectra_child",
            Context.MODE_PRIVATE
        ).getString("childId", null)

        val caregiverNodeId = context.getSharedPreferences(
            "wanderspectra_connection",
            Context.MODE_PRIVATE
        ).getString("caregiverNodeId", null)

        if (childId.isNullOrBlank() || caregiverNodeId.isNullOrBlank()) {
            Log.w(TAG, "Battery reporting skipped: pairing information missing")
            return Result.success()
        }

        val batteryManager =
            context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        val batteryLevel = batteryManager.getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CAPACITY
        )

        if (batteryLevel !in 0..100) {
            Log.w(TAG, "Invalid battery reading")
            return Result.success()
        }

        val payload = JSONObject().apply {
            put("childId", childId)
            put("batteryLevel", batteryLevel)
            put("timestamp", System.currentTimeMillis())
        }

        return try {
            com.google.android.gms.tasks.Tasks.await(
                Wearable.getMessageClient(context).sendMessage(
                    caregiverNodeId,
                    "/wanderspectra/battery/status",
                    payload.toString().toByteArray(Charsets.UTF_8)
                )
            )

            Log.i(TAG, "Periodic battery status sent")
            Result.success()

        } catch (exception: Exception) {
            Log.e(TAG, "Failed to send periodic battery status", exception)
            Result.retry()
        }
    }
}