package com.wanderspectra.wear.presentation

import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WearSafeZone(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val radiusFeet: Int = 500,
    val monitoringEnabled: Boolean = true
) {
    val radiusMeters: Float
        get() = radiusFeet * 0.3048f
}

object WearSafetyStateRepository {

    private val _isElopementDetected = MutableStateFlow(false)
    val isElopementDetected: StateFlow<Boolean> = _isElopementDetected.asStateFlow()

    private val _activeSafeZone = MutableStateFlow<WearSafeZone?>(
        WearSafeZone(lat = 30.43, lng = -86.57, radiusFeet = 500, monitoringEnabled = true)
    )
    val activeSafeZone: StateFlow<WearSafeZone?> = _activeSafeZone.asStateFlow()

    fun setElopementDetected(detected: Boolean) {
        _isElopementDetected.value = detected
    }

    fun updateActiveSafeZone(safeZone: WearSafeZone) {
        _activeSafeZone.value = safeZone
    }

    fun checkLocationAgainstSafeZone(latitude: Double, longitude: Double) {
        val safeZone = _activeSafeZone.value ?: return
        if (!safeZone.monitoringEnabled) return

        val distanceMeters = calculateDistanceMeters(
            lat1 = latitude,
            lng1 = longitude,
            lat2 = safeZone.lat,
            lng2 = safeZone.lng
        )

        val exited = distanceMeters > safeZone.radiusMeters
        if (exited) {
            _isElopementDetected.value = true
        } else if (_isElopementDetected.value && distanceMeters <= safeZone.radiusMeters) {
            _isElopementDetected.value = false
        }
    }

    fun calculateDistanceMeters(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Float {
        return try {
            val results = FloatArray(1)
            Location.distanceBetween(lat1, lng1, lat2, lng2, results)
            results[0]
        } catch (_: Throwable) {
            haversineDistanceMeters(lat1, lng1, lat2, lng2)
        }
    }

    fun haversineDistanceMeters(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Float {
        val earthRadiusMeters = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return (earthRadiusMeters * c).toFloat()
    }
}
