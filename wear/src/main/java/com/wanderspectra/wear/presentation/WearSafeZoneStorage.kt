package com.wanderspectra.wear.presentation

import android.content.Context
import android.util.Log
import org.json.JSONObject

object WearSafeZoneStorage {

    private const val TAG = "WearSafeZoneStorage"
    private const val PREFS_NAME = "wanderspectra_safezone"
    private const val SAFEZONE_KEY = "active_safezone"

    fun save(context: Context, safeZone: WearSafeZone) {
        val json = JSONObject().apply {
            put("id", safeZone.id)
            put("childId", safeZone.childId)
            put("lat", safeZone.lat)
            put("lng", safeZone.lng)
            put("radius", safeZone.radiusFeet)
            put("monitoringEnabled", safeZone.monitoringEnabled)
        }

        val saved = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).edit()
            .putString(SAFEZONE_KEY, json.toString())
            .commit()

        if (saved) {
        } else {
            Log.e(TAG, "Failed to save Safe Zone locally")
        }
    }

    fun restore(context: Context) {
        val savedJson = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).getString(SAFEZONE_KEY, null) ?: return

        try {
            val json = JSONObject(savedJson)

            val safeZone = WearSafeZone(
                id = json.optString("id", ""),
                childId = json.optString("childId", ""),
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
                Log.w(TAG, "Saved Safe Zone contains invalid values")
                return
            }

            WearSafetyStateRepository.updateActiveSafeZone(safeZone)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore Safe Zone", e)
        }
    }

    fun clear(context: Context): Boolean {
        val cleared = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).edit()
            .remove(SAFEZONE_KEY)
            .commit()

        if (cleared) {
        } else {
            Log.e(TAG, "Failed to remove saved Safe Zone")
        }

        return cleared
    }
}

