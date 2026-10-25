package com.wanderspectra.app

data class ChildSafeZoneMonitoring(
    val childId: String = "",
    val caregiverId: String = "",
    val activeSafeZoneId: String = "",
    val monitoringEnabled: Boolean = false
)