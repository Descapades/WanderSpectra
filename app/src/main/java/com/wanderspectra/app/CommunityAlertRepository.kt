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

        val incidentDocument = db.collection("incidents")
            .document(alert.incidentId)
            .get()
            .await()

        require(incidentDocument.exists()) {
            "The associated incident does not exist."
        }

        require(incidentDocument.getString("status") == "open") {
            "Community Alerts can only be created for active incidents."
        }

        require(incidentDocument.getString("caregiverId") == alert.caregiverId) {
            "The incident does not belong to the specified caregiver."
        }

        require(incidentDocument.getString("childId") == alert.childId) {
            "The incident does not belong to the specified child."
        }

        val document = alerts.document()
        val now = com.google.firebase.Timestamp.now()

        val newAlert = alert.copy(
            id = document.id,
            status = "ACTIVE",
            createdAt = now,
            updatedAt = now,
            resolvedAt = null
        )

        document.set(newAlert).await()

        return document.id
    }

    // Retrieve all community alerts
    suspend fun getAlerts(): List<CommunityAlert> {
        return db.collection("publicCommunityAlerts")
            .whereEqualTo("status", "ACTIVE")
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(CommunityAlert::class.java)
                    ?.copy(id = document.id)
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

