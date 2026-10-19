package com.wanderspectra.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class CommunityAlertEligibilityTest {

    private val alertLatitude = 27.9506
    private val alertLongitude = -82.4572
    private val radiusKm = 5.0

    private fun participant(
        optedIn: Boolean = true,
        latitude: Double = alertLatitude,
        longitude: Double = alertLongitude,
        minutesAgo: Long = 5
    ): CommunityAlertParticipant {
        val timestamp = Timestamp(
            Date(System.currentTimeMillis() - minutesAgo * 60_000L)
        )

        return CommunityAlertParticipant(
            userId = "test-user",
            optedIn = optedIn,
            latitude = latitude,
            longitude = longitude,
            locationUpdatedAt = timestamp
        )
    }

    @Test
    fun nearbyParticipantIsEligible() {
        assertTrue(
            CommunityAlertLocationUtils.isEligibleParticipant(
                participant(),
                alertLatitude,
                alertLongitude,
                radiusKm
            )
        )
    }

    @Test
    fun distantParticipantIsNotEligible() {
        assertFalse(
            CommunityAlertLocationUtils.isEligibleParticipant(
                participant(latitude = 28.5383, longitude = -81.3792),
                alertLatitude,
                alertLongitude,
                radiusKm
            )
        )
    }

    @Test
    fun optedOutParticipantIsNotEligible() {
        assertFalse(
            CommunityAlertLocationUtils.isEligibleParticipant(
                participant(optedIn = false),
                alertLatitude,
                alertLongitude,
                radiusKm
            )
        )
    }

    @Test
    fun staleLocationIsNotEligible() {
        assertFalse(
            CommunityAlertLocationUtils.isEligibleParticipant(
                participant(minutesAgo = 45),
                alertLatitude,
                alertLongitude,
                radiusKm
            )
        )
    }
}