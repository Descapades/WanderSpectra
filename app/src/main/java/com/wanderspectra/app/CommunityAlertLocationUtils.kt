package com.wanderspectra.app

import android.location.Location

object CommunityAlertLocationUtils {

    fun isWithinRadius(
        alertLatitude: Double,
        alertLongitude: Double,
        participantLatitude: Double,
        participantLongitude: Double,
        radiusKm: Double
    ): Boolean {

        if (radiusKm <= 0.0) return false

        if (alertLatitude !in -90.0..90.0 ||
            participantLatitude !in -90.0..90.0 ||
            alertLongitude !in -180.0..180.0 ||
            participantLongitude !in -180.0..180.0
        ) {
            return false
        }

        val results = FloatArray(1)

        Location.distanceBetween(
            alertLatitude,
            alertLongitude,
            participantLatitude,
            participantLongitude,
            results
        )

        val distanceKm = results[0] / 1000.0

        return distanceKm <= radiusKm
    }

    fun isEligibleParticipant(
        participant: CommunityAlertParticipant,
        alertLatitude: Double,
        alertLongitude: Double,
        radiusKm: Double
    ): Boolean {

        // Participant must opt in to Community Alerts.
        if (!participant.optedIn) return false

        // Participant must have a recorded location.
        val latitude = participant.latitude ?: return false
        val longitude = participant.longitude ?: return false

        // Location must have been updated within the last 30 minutes.
        val lastUpdated = participant.locationUpdatedAt ?: return false
        val thirtyMinutesMillis = 30 * 60 * 1000L
        val locationAge = System.currentTimeMillis() - lastUpdated.toDate().time

        if (locationAge < 0 || locationAge > thirtyMinutesMillis) {
            return false
        }

        // Participant must be inside the configured alert radius.
        return isWithinRadius(
            alertLatitude = alertLatitude,
            alertLongitude = alertLongitude,
            participantLatitude = latitude,
            participantLongitude = longitude,
            radiusKm = radiusKm
        )
    }
}