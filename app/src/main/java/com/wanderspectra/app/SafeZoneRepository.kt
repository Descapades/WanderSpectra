package com.wanderspectra.app

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class SafeZoneRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun save(zone: SafeZone, onDone: (Boolean, String) -> Unit) {
        val caregiverId = FirebaseAuth.getInstance().currentUser?.uid

        if (caregiverId == null) {
            onDone(false, "You must be signed in to save a Safe Zone.")
            return
        }

        val data = hashMapOf(
            "caregiverId" to caregiverId,
            "name" to zone.name,
            "childId" to zone.childId,
            "radius" to zone.radius,
            "monitoringEnabled" to zone.monitoringEnabled,
            "lat" to zone.lat,
            "lng" to zone.lng
        )

        db.collection("safeZones")
            .add(data)
            .addOnSuccessListener { onDone(true, "Saved to Firestore") }
            .addOnFailureListener { error ->
                onDone(false, error.message ?: "Save failed")
            }
    }
}