package com.wanderspectra.app

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.location.Location

class IncidentRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun startIncident(incident: Incident, onDone: (Boolean, String, String?) -> Unit) {
        val data = hashMapOf(
            "childId" to incident.childId,
            "caregiverId" to incident.caregiverId,
            "status" to "open",
            "startTime" to incident.startTime,
            "endTime" to null,
            "lastKnownLat" to incident.lastKnownLat,
            "lastKnownLng" to incident.lastKnownLng,
            "type" to incident.type,
            "safeZoneId" to incident.safeZoneId
        )

        db.collection("incidents")
            .add(data)
            .addOnSuccessListener { doc ->
                onDone(true, "Incident saved", doc.id)
            }
            .addOnFailureListener { error ->
                onDone(false, error.message ?: "Incident save failed", null)
            }
    }

    fun saveSafeZoneExitIncident(
        incident: Incident,
        onDone: (Boolean, String) -> Unit
    ) {
        val eventId = "${incident.childId}_${incident.safeZoneId}_${incident.startTime}"

        val data = hashMapOf(
            "childId" to incident.childId,
            "caregiverId" to incident.caregiverId,
            "status" to "open",
            "startTime" to incident.startTime,
            "endTime" to null,
            "lastKnownLat" to incident.lastKnownLat,
            "lastKnownLng" to incident.lastKnownLng,
            "type" to "SAFE_ZONE_EXIT",
            "safeZoneId" to incident.safeZoneId,
            "safeZoneName" to incident.safeZoneName
        )

        val incidentRef = db.collection("incidents").document(eventId)

        db.runTransaction { transaction ->
            val existingIncident = transaction.get(incidentRef)

            if (!existingIncident.exists()) {
                transaction.set(incidentRef, data)
            }

            null
        }.addOnSuccessListener {
            onDone(true, "Safe Zone exit incident processed")
        }.addOnFailureListener { exception ->
            onDone(
                false,
                exception.message ?: "Failed to save Safe Zone exit"
            )
        }
    }

    fun closeSafeZoneExitOnReentry(
        caregiverId: String,
        childId: String,
        safeZoneId: String,
        reentryTimestamp: Long,
        reentryLatitude: Double,
        reentryLongitude: Double,
        routePoints: List<Map<String, Any>> = emptyList(),
        onDone: (Boolean, String) -> Unit
    ) {
        if (
            caregiverId.isBlank() ||
            childId.isBlank() ||
            safeZoneId.isBlank() ||
            reentryTimestamp <= 0L ||
            !reentryLatitude.isFinite() ||
            !reentryLongitude.isFinite() ||
            reentryLatitude !in -90.0..90.0 ||
            reentryLongitude !in -180.0..180.0 ||
            (reentryLatitude == 0.0 && reentryLongitude == 0.0) ||
            auth.currentUser?.uid != caregiverId
        ) {
            onDone(false, "Invalid Safe Zone re-entry request")
            return
        }

        db.collection("incidents")
            .whereEqualTo("caregiverId", caregiverId)
            .whereEqualTo("childId", childId)
            .whereEqualTo("safeZoneId", safeZoneId)
            .whereEqualTo("type", "SAFE_ZONE_EXIT")
            .whereEqualTo("status", "open")
            .get()
            .addOnSuccessListener { snapshot ->

                val matchingIncident = snapshot.documents
                    .filter { document ->
                        val startTime = (document.get("startTime") as? Number)
                            ?.toLong()

                        startTime != null && startTime <= reentryTimestamp
                    }
                    .maxByOrNull { document ->
                        (document.get("startTime") as Number).toLong()
                    }

                if (matchingIncident == null) {
                    onDone(false, "No matching open Safe Zone Exit incident")
                    return@addOnSuccessListener
                }

                db.runTransaction { transaction ->
                    val incidentRef = matchingIncident.reference
                    val current = transaction.get(incidentRef)

                    if (
                        current.exists() &&
                        current.getString("caregiverId") == caregiverId &&
                        current.getString("childId") == childId &&
                        current.getString("safeZoneId") == safeZoneId &&
                        current.getString("type") == "SAFE_ZONE_EXIT" &&
                        current.getString("status") == "open" &&
                        ((current.get("startTime") as? Number)?.toLong()
                            ?: Long.MAX_VALUE) <= reentryTimestamp
                    ) {
                        val incidentStartTime =
                            (current.get("startTime") as? Number)?.toLong()
                                ?: reentryTimestamp

                        val incidentRoutePoints = routePoints.filter { point ->
                            val pointTimestamp = point["timestamp"] as? Long

                            pointTimestamp != null &&
                                    pointTimestamp >= incidentStartTime &&
                                    pointTimestamp <= reentryTimestamp
                        }.sortedBy { point ->
                            point["timestamp"] as Long
                        }

                        val filteredRoutePoints = mutableListOf<Map<String, Any>>()

                        for (point in incidentRoutePoints) {
                            if (filteredRoutePoints.isEmpty()) {
                                filteredRoutePoints.add(point)
                                continue
                            }

                            val previous = filteredRoutePoints.last()

                            val previousLatitude = previous["latitude"] as Double
                            val previousLongitude = previous["longitude"] as Double
                            val currentLatitude = point["latitude"] as Double
                            val currentLongitude = point["longitude"] as Double

                            val previousTimestamp = previous["timestamp"] as Long
                            val currentTimestamp = point["timestamp"] as Long

                            val elapsedSeconds =
                                (currentTimestamp - previousTimestamp) / 1000.0

                            if (elapsedSeconds <= 0.0) continue

                            val results = FloatArray(1)

                            Location.distanceBetween(
                                previousLatitude,
                                previousLongitude,
                                currentLatitude,
                                currentLongitude,
                                results
                            )

                            val speedMetersPerSecond = results[0] / elapsedSeconds

                            if (speedMetersPerSecond <= 3.0) {
                                filteredRoutePoints.add(point)
                            }
                        }

                        var distanceTraveledMeters = 0.0

                        for (i in 1 until filteredRoutePoints.size) {
                            val previous = filteredRoutePoints[i - 1]
                            val current = filteredRoutePoints[i]

                            val results = FloatArray(1)

                            Location.distanceBetween(
                                previous["latitude"] as Double,
                                previous["longitude"] as Double,
                                current["latitude"] as Double,
                                current["longitude"] as Double,
                                results
                            )

                            distanceTraveledMeters += results[0].toDouble()
                        }

                        val startLatitude = current.getDouble("lastKnownLat")
                        val startLongitude = current.getDouble("lastKnownLng")

                        val distanceMeters = if (
                            startLatitude != null &&
                            startLongitude != null &&
                            startLatitude.isFinite() &&
                            startLongitude.isFinite() &&
                            startLatitude in -90.0..90.0 &&
                            startLongitude in -180.0..180.0 &&
                            !(startLatitude == 0.0 && startLongitude == 0.0)
                        ) {
                            val results = FloatArray(1)

                            Location.distanceBetween(
                                startLatitude,
                                startLongitude,
                                reentryLatitude,
                                reentryLongitude,
                                results
                            )

                            results[0].toDouble()
                        } else {
                            null
                        }
                        transaction.update(
                            incidentRef,
                            mapOf(
                                "status" to "closed",
                                "endTime" to reentryTimestamp,
                                "resolutionType" to "RETURNED_TO_SAFE_ZONE",
                                "endLatitude" to reentryLatitude,
                                "endLongitude" to reentryLongitude,
                                "displacementMeters" to distanceMeters,
                                "routePoints" to filteredRoutePoints,
                                "distanceTraveledMeters" to if (
                                    filteredRoutePoints.size >= 2
                                ) distanceTraveledMeters else null
                            )
                        )
                    }

                    null
                }.addOnSuccessListener {
                    onDone(true, "Safe Zone re-entry processed")
                }.addOnFailureListener { exception ->
                    onDone(
                        false,
                        exception.message ?: "Failed to close Safe Zone Exit"
                    )
                }
            }
            .addOnFailureListener { exception ->
                onDone(
                    false,
                    exception.message ?: "Failed to find Safe Zone Exit"
                )
            }
    }

    fun closeIncident(
        incidentId: String,
        onDone: (Boolean, String) -> Unit
    ) {
        val caregiverId = auth.currentUser?.uid

        if (caregiverId == null || incidentId.isBlank()) {
            onDone(false, "No authenticated caregiver or valid incident ID")
            return
        }

        val incidentRef = db.collection("incidents").document(incidentId)

        db.runTransaction { transaction ->
            val incident = transaction.get(incidentRef)

            if (!incident.exists() ||
                incident.getString("caregiverId") != caregiverId
            ) {
                throw IllegalStateException("Incident not found or access denied")
            }

            if (incident.getString("status") != "open") {
                throw IllegalStateException("Incident is already resolved")
            }

            transaction.update(
                incidentRef,
                mapOf(
                    "status" to "closed",
                    "endTime" to System.currentTimeMillis(),
                    "resolutionType" to "CAREGIVER_CONFIRMED_SAFE"
                )
            )

            null
        }.addOnSuccessListener {
            onDone(true, "Child marked safe")
        }.addOnFailureListener { exception ->
            onDone(false, exception.message ?: "Unable to close incident")
        }
    }

    fun getIncidentsForChild(
        childId: String,
        onSuccess: (List<IncidentRecord>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onFailure(Exception("No authenticated caregiver found."))
            return
        }

        if (childId.isBlank()) {
            onFailure(
                IllegalArgumentException("A child ID is required.")
            )
            return
        }

        firestore
            .collection("incidents")
            .whereEqualTo("caregiverId", user.uid)
            .whereEqualTo("childId", childId)
            .get()
            .addOnSuccessListener { querySnapshot ->

                val incidents = querySnapshot.documents.map { document ->

                    fun readTimestamp(field: String): com.google.firebase.Timestamp? {
                        return when (val value = document.get(field)) {
                            is com.google.firebase.Timestamp -> value
                            is Number -> com.google.firebase.Timestamp(
                                java.util.Date(value.toLong())
                            )
                            else -> null
                        }
                    }

                    IncidentRecord(
                        incidentId = document.id,
                        childId = document.getString("childId") ?: "",
                        caregiverId = document.getString("caregiverId") ?: "",
                        startTime = readTimestamp("startTime"),
                        endTime = readTimestamp("endTime"),
                        status = document.getString("status") ?: "",
                        lastKnownLatitude = document.getDouble("lastKnownLatitude")
                            ?: document.getDouble("lastKnownLat"),
                        lastKnownLongitude = document.getDouble("lastKnownLongitude")
                            ?: document.getDouble("lastKnownLng"),
                        type = document.getString("type") ?: "ELOPEMENT",
                        safeZoneId = document.getString("safeZoneId"),
                        safeZoneName = document.getString("safeZoneName"),
                        resolutionType = document.getString("resolutionType"),
                        displacementMeters = document.getDouble("displacementMeters"),
                        distanceTraveledMeters = document.getDouble("distanceTraveledMeters"),
                        routePoints = (document.get("routePoints") as? List<*>)
                            ?.mapNotNull { point ->
                                val data = point as? Map<*, *> ?: return@mapNotNull null

                                val latitude = (data["latitude"] as? Number)?.toDouble()
                                val longitude = (data["longitude"] as? Number)?.toDouble()
                                val timestamp = (data["timestamp"] as? Number)?.toLong()

                                if (
                                    latitude == null || longitude == null ||
                                    !latitude.isFinite() || !longitude.isFinite() ||
                                    latitude !in -90.0..90.0 ||
                                    longitude !in -180.0..180.0 ||
                                    timestamp == null
                                ) {
                                    null
                                } else {
                                    mapOf(
                                        "latitude" to latitude,
                                        "longitude" to longitude,
                                        "timestamp" to timestamp
                                    )
                                }
                            } ?: emptyList(),
                        endLatitude = document.getDouble("endLatitude"),
                        endLongitude = document.getDouble("endLongitude")
                    )
                }

                val sortedIncidents = incidents.sortedByDescending {
                    it.startTime?.seconds ?: Long.MIN_VALUE
                }

                onSuccess(sortedIncidents)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

}
