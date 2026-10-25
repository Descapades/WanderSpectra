package com.wanderspectra.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ZoneCheckTest {
    private val zone = SafeZone(
        name = "Home",
        childId = "test-child",
        radius = 500,
        monitoringEnabled = true,
        lat = 30.43,
        lng = -86.57
    )

    @Test
    fun pointInsideRadius_returnsFalse() {
        val outside = ZoneCheck.isOutside(zone, 30.43, -86.57)
        assertFalse(outside)
    }

    @Test
    fun pointFarAway_returnsTrue() {
        val outside = ZoneCheck.isOutside(zone, 31.00, -86.57)
        assertTrue(outside)
    }
}