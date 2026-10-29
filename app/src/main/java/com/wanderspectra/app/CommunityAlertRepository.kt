package com.wanderspectra.app

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

class CommunityAlertRepository {

    private val db = FirebaseFirestore.getInstance()
    private val alerts = db.collection("communityAlerts")
    private val publicAlerts = db.collection("publicCommunityAlerts")
    private val publishWorkerUrl =
        "https://wanderspectra-alerts.destin-champlin.workers.dev/publish-community-alert"

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
        require(
            alert.latitude != null &&
                    alert.longitude != null &&
                    alert.latitude.isFinite() &&
                    alert.longitude.isFinite() &&
                    alert.latitude in -90.0..90.0 &&
                    alert.longitude in -180.0..180.0 &&
                    !(alert.latitude == 0.0 && alert.longitude == 0.0)
        ) {
            "A valid last-known wearable location is required to publish a Community Alert."
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

        val document = alerts.document(alert.incidentId)
        val now = com.google.firebase.Timestamp.now()

        val newAlert = alert.copy(
            id = document.id,
            status = "ACTIVE",
            createdAt = now,
            updatedAt = now,
            resolvedAt = null
        )

        document.set(newAlert).await()

        check(publishAlert(document.id)) {
            "Community Alert was saved, but public publishing failed."
        }

        return document.id
    }

    private suspend fun publishAlert(alertId: String): Boolean =
        withContext(Dispatchers.IO) {
            val user = FirebaseAuth.getInstance().currentUser
                ?: return@withContext false

            val token = try {
                com.google.android.gms.tasks.Tasks.await(
                    user.getIdToken(false)
                ).token ?: return@withContext false
            } catch (e: Exception) {
                return@withContext false
            }

            var connection: HttpURLConnection? = null

            try {
                connection = URL(publishWorkerUrl)
                    .openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.doOutput = true

                val body = JSONObject()
                    .put("alertId", alertId)
                    .toString()

                connection.outputStream.use {
                    it.write(body.toByteArray(Charsets.UTF_8))
                }

                connection.responseCode in 200..299
            } catch (e: Exception) {
                android.util.Log.e(
                    "CommunityAlert",
                    "Unable to publish Community Alert",
                    e
                )
                false
            } finally {
                connection?.disconnect()
            }
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

