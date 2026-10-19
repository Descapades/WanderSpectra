package com.wanderspectra.app

import android.content.Context

object CommunityAlertPreferences {

    private const val PREFS_NAME = "community_alert_preferences"
    private const val KEY_OPTED_IN = "community_alert_opted_in"

    fun isOptedIn(context: Context): Boolean {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        return preferences.getBoolean(KEY_OPTED_IN, false)
    }

    fun setOptedIn(context: Context, optedIn: Boolean) {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        preferences.edit()
            .putBoolean(KEY_OPTED_IN, optedIn)
            .apply()
    }
}
