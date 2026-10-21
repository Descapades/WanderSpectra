package com.wanderspectra.app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChildSafeZoneMonitoringRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun get(
        childId: String,
        onDone: (ChildSafeZoneMonitoring?) -> Unit
    ) {
        val caregiverId = FirebaseAuth.getInstance().currentUser?.uid

        if (caregiverId == null || childId.isBlank()) {
            onDone(null)
            return
        }

        db.collection("childSafeZoneMonitoring")
            .document(childId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists() ||
                    document.getString("caregiverId") != caregiverId
                ) {
                    onDone(null)
                } else {
                    onDone(
                        ChildSafeZoneMonitoring(
                            childId = childId,
                            caregiverId = caregiverId,
                            activeSafeZoneId = document.getString("activeSafeZoneId") ?: "",
                            monitoringEnabled = document.getBoolean("monitoringEnabled") ?: false
                        )
                    )
                }
            }
            .addOnFailureListener {
                onDone(null)
            }

    }
    fun setMonitoring(
        childId: String,
        safeZoneId: String,
        enabled: Boolean,
        onDone: (Boolean, String) -> Unit
    ) {
        val caregiverId = FirebaseAuth.getInstance().currentUser?.uid

        if (caregiverId == null || childId.isBlank()) {
            onDone(false, "Caregiver or child is missing.")
            return
        }

        if (enabled && safeZoneId.isBlank()) {
            onDone(false, "Select a Safe Zone first.")
            return
        }

        val monitoringRef = db.collection("childSafeZoneMonitoring")
            .document(childId)

        val zoneRef = if (safeZoneId.isNotBlank()) {
            db.collection("safeZones").document(safeZoneId)
        } else {
            null
        }

        db.runTransaction { transaction ->
            if (zoneRef != null) {
                val zone = transaction.get(zoneRef)

                if (!zone.exists() ||
                    zone.getString("caregiverId") != caregiverId
                ) {
                    throw IllegalStateException(
                        "Safe Zone not found or access denied."
                    )
                }
            }

            val data = mapOf(
                "childId" to childId,
                "caregiverId" to caregiverId,
                "activeSafeZoneId" to if (enabled) safeZoneId else "",
                "monitoringEnabled" to enabled
            )

            transaction.set(monitoringRef, data)
        }
            .addOnSuccessListener {
                onDone(true, "Child monitoring updated.")
            }
            .addOnFailureListener { error ->
                onDone(false, error.message ?: "Monitoring update failed.")
            }
    }

}