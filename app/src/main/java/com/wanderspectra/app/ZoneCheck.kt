package com.wanderspectra.app

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object ZoneCheck {
    fun isOutside(zone: SafeZone, lat: Double, lng: Double): Boolean {
        val feet = distanceFeet(zone.lat, zone.lng, lat, lng)
        return feet > zone.radius
    }

    private fun distanceFeet(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthMeters = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        val meters = earthMeters * 2 * atan2(sqrt(a), sqrt(1 - a))
        return meters * 3.28084
    }
}