package com.wanderspectra.app

import com.google.firebase.firestore.FirebaseFirestore

class SafeZoneRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun save(zone: SafeZone, onDone: (Boolean) -> Unit) {
        val data = hashMapOf(
            "name" to zone.name,
            "childId" to zone.childId,
            "radius" to zone.radius,
            "monitoringEnabled" to zone.monitoringEnabled,
            "lat" to zone.lat,
            "lng" to zone.lng
        )

        db.collection("safeZones")
            .add(data)
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }
}