package com.wanderspectra.app

import com.google.firebase.firestore.FirebaseFirestore

class IncidentRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun startIncident(incident: Incident, onDone: (Boolean, String) -> Unit) {
        val data = hashMapOf(
            "childId" to incident.childId,
            "caregiverId" to incident.caregiverId,
            "status" to incident.status,
            "startTime" to incident.startTime,
            "endTime" to incident.endTime,
            "lastKnownLat" to incident.lastKnownLat,
            "lastKnownLng" to incident.lastKnownLng
        )

        db.collection("incidents")
            .add(data)
            .addOnSuccessListener { onDone(true, "Incident saved") }
            .addOnFailureListener { error ->
                onDone(false, error.message ?: "Incident save failed")
            }
    }
}