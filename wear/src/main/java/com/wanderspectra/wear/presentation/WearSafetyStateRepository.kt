package com.wanderspectra.wear.presentation

import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WearSafeZone(
    val id: String = "",
    val childId: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val radiusFeet: Int = 500,
    val monitoringEnabled: Boolean = true
) {
    val radiusMeters: Float
        get() = radiusFeet * 0.3048f
}

data class SafeZoneExitEvent(
    val childId: String,
    val safeZoneId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)

object WearSafetyStateRepository {

    private val _isElopementDetected = MutableStateFlow(false)
    val isElopementDetected: StateFlow<Boolean> = _isElopementDetected.asStateFlow()

    private val _isPlayingSound = MutableStateFlow(false)
    val isPlayingSound: StateFlow<Boolean> = _isPlayingSound.asStateFlow()

    private val _activeSafeZone = MutableStateFlow<WearSafeZone?>(null)
    val activeSafeZone: StateFlow<WearSafeZone?> = _activeSafeZone.asStateFlow()

    private var wasInsideSafeZone: Boolean? = null

    private val _safeZoneExitEvent = MutableStateFlow<SafeZoneExitEvent?>(null)
    val safeZoneExitEvent: StateFlow<SafeZoneExitEvent?> =
        _safeZoneExitEvent.asStateFlow()

    fun setElopementDetected(detected: Boolean) {
        _isElopementDetected.value = detected
    }

    fun setPlayingSound(playing: Boolean) {
        _isPlayingSound.value = playing
    }

    fun updateActiveSafeZone(safeZone: WearSafeZone) {
        val previousZone = _activeSafeZone.value

        if (previousZone != safeZone) {
            wasInsideSafeZone = null
            _safeZoneExitEvent.value = null
        }

        _activeSafeZone.value = safeZone

        if (!safeZone.monitoringEnabled) {
            wasInsideSafeZone = null
            _isElopementDetected.value = false
        }
    }

    fun clearActiveSafeZone() {
        _activeSafeZone.value = null
        _isElopementDetected.value = false
        wasInsideSafeZone = null
        _safeZoneExitEvent.value = null
    }

    fun checkLocationAgainstSafeZone(latitude: Double, longitude: Double) {
        val safeZone = _activeSafeZone.value

        if (safeZone == null || !safeZone.monitoringEnabled) {
            _isElopementDetected.value = false
            wasInsideSafeZone = null
            return
        }

        val distanceMeters = calculateDistanceMeters(
            lat1 = latitude,
            lng1 = longitude,
            lat2 = safeZone.lat,
            lng2 = safeZone.lng
        )

        val isInside = distanceMeters <= safeZone.radiusMeters

        // Generate an event only for an actual inside-to-outside transition.
        if (wasInsideSafeZone == true && !isInside) {
            _safeZoneExitEvent.value = SafeZoneExitEvent(
                childId = safeZone.childId,
                safeZoneId = safeZone.id,
                latitude = latitude,
                longitude = longitude,
                timestamp = System.currentTimeMillis()
            )
        }

        wasInsideSafeZone = isInside

        // Preserve the existing Stop and Wait behavior.
        _isElopementDetected.value = !isInside
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
