package com.wanderspectra.app

data class SafeZone(
    val id: String = "",
    val caregiverId: String = "",
    val name: String = "",
    val childId: String = "",
    val radius: Int = 500,
    val monitoringEnabled: Boolean = true,
    val childMonitoring: Map<String, Boolean> = emptyMap(),
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val scheduleAlwaysOn: Boolean = true,
    val scheduleDays: List<String> = emptyList(),
    val scheduleStart: String = "08:00",
    val scheduleEnd: String = "20:00"
)