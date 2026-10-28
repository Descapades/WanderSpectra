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

    fun create(
        zone: SafeZone,
        onDone: (Boolean, String, SafeZone?) -> Unit
    ) {
        val caregiverId = FirebaseAuth.getInstance().currentUser?.uid

        if (caregiverId == null) {
            onDone(false, "You must be signed in to create a Safe Zone.", null)
            return
        }

        val data = hashMapOf(
            "name" to zone.name,
            "childId" to zone.childId,
            "childMonitoring" to zone.childMonitoring,
            "caregiverId" to caregiverId,
            "radius" to zone.radius,
            "monitoringEnabled" to zone.monitoringEnabled,
            "lat" to zone.lat,
            "lng" to zone.lng,
            "savedAt" to System.currentTimeMillis()
        )

        db.collection("safeZones")
            .add(data)
            .addOnSuccessListener { documentReference ->
                val savedZone = zone.copy(id = documentReference.id)

                onDone(
                    true,
                    "New Safe Zone created.",
                    savedZone
                )
            }
            .addOnFailureListener { error ->
                onDone(false, error.message ?: "Unable to create Safe Zone.", null)
            }
    }

    fun update(
        zone: SafeZone,
        onDone: (Boolean, String, SafeZone?) -> Unit
    ) {
        val caregiverId = FirebaseAuth.getInstance().currentUser?.uid

        if (caregiverId == null) {
            onDone(false, "You must be signed in to edit a Safe Zone.", null)
            return
        }

        if (zone.id.isBlank()) {
            onDone(false, "Safe Zone ID is missing.", null)
            return
        }

        val updates = mapOf(
            "name" to zone.name,
            "childId" to zone.childId,
            "childMonitoring" to zone.childMonitoring,
            "radius" to zone.radius,
            "monitoringEnabled" to zone.monitoringEnabled,
            "lat" to zone.lat,
            "lng" to zone.lng,
            "savedAt" to System.currentTimeMillis()
        )

        val document = db.collection("safeZones").document(zone.id)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(document)

            if (!snapshot.exists() ||
                snapshot.getString("caregiverId") != caregiverId
            ) {
                throw IllegalStateException("Safe Zone not found or access denied.")
            }

            transaction.update(document, updates)
        }
            .addOnSuccessListener {
                onDone(true, "Safe Zone updated.", zone)
            }
            .addOnFailureListener { error ->
                onDone(false, error.message ?: "Unable to update Safe Zone.", null)
            }
    }

    fun delete(
        zoneId: String,
        onDone: (Boolean, String) -> Unit
    ) {
        val caregiverId = FirebaseAuth.getInstance().currentUser?.uid

        if (caregiverId == null) {
            onDone(false, "You must be signed in to delete a Safe Zone.")
            return
        }

        if (zoneId.isBlank()) {
            onDone(false, "Safe Zone ID is missing.")
            return
        }

        val document = db.collection("safeZones").document(zoneId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(document)

            if (!snapshot.exists() ||
                snapshot.getString("caregiverId") != caregiverId
            ) {
                throw IllegalStateException("Safe Zone not found or access denied.")
            }

            transaction.delete(document)
        }
            .addOnSuccessListener {
                onDone(true, "Safe Zone deleted.")
            }
            .addOnFailureListener { error ->
                onDone(false, error.message ?: "Unable to delete Safe Zone.")
            }
    }

    fun getAll(onDone: (List<SafeZone>) -> Unit) {
        val caregiverId = FirebaseAuth.getInstance().currentUser?.uid

        if (caregiverId == null) {
            onDone(emptyList())
            return
        }

        db.collection("safeZones")
            .whereEqualTo("caregiverId", caregiverId)
            .get()
            .addOnSuccessListener { snapshot ->
                val zones = snapshot.documents.map { doc ->
                    SafeZone(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        childId = doc.getString("childId") ?: "",
                        childMonitoring = (doc.get("childMonitoring") as? Map<*, *>)
                            ?.entries
                            ?.mapNotNull { (key, value) ->
                                if (key is String && value is Boolean) {
                                    key to value
                                } else {
                                    null
                                }
                            }
                            ?.toMap()
                            ?: emptyMap(),
                        radius = doc.getLong("radius")?.toInt() ?: 500,
                        monitoringEnabled = doc.getBoolean("monitoringEnabled") ?: true,
                        lat = doc.getDouble("lat") ?: 0.0,
                        lng = doc.getDouble("lng") ?: 0.0
                    )
                }

                onDone(zones)
            }
            .addOnFailureListener {
                onDone(emptyList())
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

    fun getById(
        zoneId: String,
        onResult: (SafeZone?) -> Unit
    ) {
        val caregiverId = FirebaseAuth.getInstance().currentUser?.uid

        if (caregiverId == null || zoneId.isBlank()) {
            onResult(null)
            return
        }

        db.collection("safeZones")
            .document(zoneId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists() ||
                    document.getString("caregiverId") != caregiverId
                ) {
                    onResult(null)
                    return@addOnSuccessListener
                }

                val zone = SafeZone(
                    id = document.id,
                    caregiverId = document.getString("caregiverId").orEmpty(),
                    childId = document.getString("childId").orEmpty(),
                    name = document.getString("name").orEmpty(),
                    radius = document.getLong("radius")?.toInt() ?: 500,
                    monitoringEnabled = document.getBoolean("monitoringEnabled") ?: true,
                    childMonitoring = (document.get("childMonitoring") as? Map<*, *>)
                        ?.entries
                        ?.mapNotNull { (key, value) ->
                            if (key is String && value is Boolean) {
                                key to value
                            } else {
                                null
                            }
                        }
                        ?.toMap()
                        ?: emptyMap(),
                    lat = document.getDouble("lat") ?: 0.0,
                    lng = document.getDouble("lng") ?: 0.0
                )

                onResult(zone)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}