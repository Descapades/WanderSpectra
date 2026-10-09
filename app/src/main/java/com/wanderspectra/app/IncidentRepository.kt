package com.wanderspectra.app

import com.google.firebase.firestore.FirebaseFirestore

class IncidentRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
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
}