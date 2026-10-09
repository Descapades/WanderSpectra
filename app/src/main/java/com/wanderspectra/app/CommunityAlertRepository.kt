package com.wanderspectra.app

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CommunityAlertRepository {

    private val db = FirebaseFirestore.getInstance()
    private val alerts = db.collection("communityAlerts")

    // Save a new community alert
    suspend fun createAlert(alert: CommunityAlert): String {
        require(alert.incidentId.isNotBlank()) {
            "An incident ID is required."
        }
        require(alert.caregiverId.isNotBlank()) {
            "A caregiver ID is required."
        }
        require(alert.childId.isNotBlank()) {
            "A child ID is required."
        }

        val document = alerts.document()
        val newAlert = alert.copy(id = document.id)

        document.set(newAlert).await()

        return document.id
    }

    // Retrieve all community alerts
    suspend fun getAlerts(): List<CommunityAlert> {
        return alerts.get().await().documents.mapNotNull {
            it.toObject(CommunityAlert::class.java)
        }
    }

    // Retrieve alerts for a specific incident
    suspend fun getAlertsForIncident(
        incidentId: String
    ): List<CommunityAlert> {
        return alerts
            .whereEqualTo("incidentId", incidentId)
            .get()
            .await()
            .toObjects(CommunityAlert::class.java)
    }

    // Mark an alert as resolved
    suspend fun resolveAlert(alertId: String) {
        alerts.document(alertId).update(
            mapOf(
                "status" to "RESOLVED",
                "resolvedAt" to com.google.firebase.Timestamp.now(),
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
        ).await()
    }
}

