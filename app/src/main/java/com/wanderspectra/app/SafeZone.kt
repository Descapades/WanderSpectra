package com.wanderspectra.app

data class SafeZone(
    val id: String = "",
    val name: String = "",
    val childId: String = "",
    val radius: Int = 500,
    val monitoringEnabled: Boolean = true,
    val lat: Double = 0.0,
    val lng: Double = 0.0
)