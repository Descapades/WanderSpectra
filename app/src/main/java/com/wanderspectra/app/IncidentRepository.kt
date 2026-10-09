package com.wanderspectra.app

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

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
            "lastKnownLng" to incident.lastKnownLng
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

    fun closeIncident(incidentId: String, onDone: (Boolean, String) -> Unit) {
        db.collection("incidents")
            .document(incidentId)
            .update(
                mapOf(
                    "status" to "closed",
                    "endTime" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener { onDone(true, "Child marked safe") }
            .addOnFailureListener { error ->
                onDone(false, error.message ?: "Close failed")
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
                            ?: document.getDouble("lastKnownLng")
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
