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

        getLatest { existing ->
            val data = hashMapOf(
                "name" to zone.name,
                "childId" to zone.childId,
                "caregiverId" to caregiverId,
                "radius" to zone.radius,
                "monitoringEnabled" to zone.monitoringEnabled,
                "lat" to zone.lat,
                "lng" to zone.lng,
                "savedAt" to System.currentTimeMillis()
            )

            if (existing != null && existing.id.isNotBlank()) {
                db.collection("safeZones")
                    .document(existing.id)
                    .set(data)
                    .addOnSuccessListener { onDone(true, "Updated existing zone") }
                    .addOnFailureListener { error ->
                        onDone(false, error.message ?: "Update failed")
                    }
            } else {
                db.collection("safeZones")
                    .add(data)
                    .addOnSuccessListener { onDone(true, "Saved to Firestore") }
                    .addOnFailureListener { error ->
                        onDone(false, error.message ?: "Save failed")
                    }
            }
        }
    }

    fun getLatest(onDone: (SafeZone?) -> Unit) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
        db.collection("safeZones")
            .whereEqualTo("caregiverId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val doc = snapshot.documents.maxByOrNull { it.getLong("savedAt") ?: 0L }
                if (doc == null) {
                    onDone(null)
                } else {
                    onDone(
                        SafeZone(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            childId = doc.getString("childId") ?: "",
                            radius = doc.getLong("radius")?.toInt() ?: 500,
                            monitoringEnabled = doc.getBoolean("monitoringEnabled") ?: true,
                            lat = doc.getDouble("lat") ?: 0.0,
                            lng = doc.getDouble("lng") ?: 0.0
                        )
                    )
                }
            }
            .addOnFailureListener { onDone(null) }
    }
}