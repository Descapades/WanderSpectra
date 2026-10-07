package com.wanderspectra.app

data class Wearable(
    val deviceId: String = "",
    val deviceName: String = "",
    val deviceType: String = "Wear OS Device",
    val batteryLevel: Int = -1,
    val connected: Boolean = false,
    val lastSync: Long = 0L
)
