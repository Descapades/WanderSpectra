package com.wanderspectra.app

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import android.content.Context
import android.location.Geocoder
import java.util.Locale

class CommunityAlertRepository {

    private val db = FirebaseFirestore.getInstance()
    private val alerts = db.collection("communityAlerts")
    private val publishWorkerUrl =
        "https://wanderspectra-alerts.destin-champlin.workers.dev/publish-community-alert"
    private val resolvedAlertsWorkerUrl =
        "https://wanderspectra-alerts.destin-champlin.workers.dev/resolved-community-alerts"


    private suspend fun getGeneralLocation(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String = withContext(Dispatchers.IO) {
        try {
            @Suppress("DEPRECATION")
            val address = Geocoder(context, Locale.getDefault())
                .getFromLocation(latitude, longitude, 1)
                ?.firstOrNull()

            listOfNotNull(
                address?.locality?.takeIf { it.isNotBlank() },
                address?.adminArea?.takeIf { it.isNotBlank() }
            ).joinToString(", ")
        } catch (e: Exception) {
            ""
        }
    }


    suspend fun createAlert(
        context: Context,
        alert: CommunityAlert
    ): String {
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

        val generalLocation = getGeneralLocation(
            context,
            alert.latitude!!,
            alert.longitude!!
        )

        val newAlert = alert.copy(
            id = document.id,
            status = "ACTIVE",
            lastKnownArea = generalLocation,
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


    private suspend fun getResolvedAlerts(): List<CommunityAlert> =
        withContext(Dispatchers.IO) {
            val user = FirebaseAuth.getInstance().currentUser
                ?: throw IllegalStateException("User is not signed in")

            val token = user.getIdToken(false).await().token
                ?: throw IllegalStateException("Unable to get Firebase token")

            val connection = URL(resolvedAlertsWorkerUrl)
                .openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.setRequestProperty(
                    "Authorization",
                    "Bearer $token"
                )
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val responseCode = connection.responseCode

                if (responseCode !in 200..299) {
                    throw IllegalStateException(
                        "Unable to retrieve resolved alerts ($responseCode)"
                    )
                }

                val response = connection.inputStream
                    .bufferedReader()
                    .use { it.readText() }

                val alertsArray = JSONObject(response).getJSONArray("alerts")

                (0 until alertsArray.length()).map { index ->
                    val alert = alertsArray.getJSONObject(index)

                    CommunityAlert(
                        id = alert.optString("id"),
                        incidentId = alert.optString("incidentId"),
                        childName = alert.optString("childName"),
                        photoUrl = alert.optString("photoUrl"),
                        lastKnownArea = alert.optString("lastKnownArea"),
                        status = "RESOLVED",
                        createdAt = alert.optString("createdAt")
                            .takeIf { it.isNotBlank() && it != "null" }
                            ?.let { com.google.firebase.Timestamp(
                                java.util.Date.from(java.time.Instant.parse(it))
                            ) },
                        resolvedAt = alert.optString("resolvedAt")
                            .takeIf { it.isNotBlank() && it != "null" }
                            ?.let { com.google.firebase.Timestamp(
                                java.util.Date.from(java.time.Instant.parse(it))
                            ) }
                    )
                }
            } finally {
                connection.disconnect()
            }
        }


    suspend fun getAlerts(): List<CommunityAlert> {
        val activeAlerts = alerts
            .whereEqualTo("status", "ACTIVE")
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(CommunityAlert::class.java)
                    ?.copy(id = document.id)
            }

        val resolvedAlerts = getResolvedAlerts()

        return (activeAlerts + resolvedAlerts)
            .sortedWith(
                compareBy<CommunityAlert> {
                    it.status != "ACTIVE"
                }.thenByDescending {
                    it.createdAt?.seconds ?: 0L
                }
            )
    }


    suspend fun resolveAlert(alertId: String) {
        val success = withContext(Dispatchers.IO) {
            val user = FirebaseAuth.getInstance().currentUser
                ?: throw IllegalStateException("Caregiver is not signed in")

            val token = user.getIdToken(false).await().token
                ?: throw IllegalStateException("Unable to get Firebase authentication token")

            val connection = (
                    URL("https://wanderspectra-alerts.destin-champlin.workers.dev/resolve-community-alert")
                        .openConnection() as HttpURLConnection
                    )

            try {
                connection.requestMethod = "POST"
                connection.setRequestProperty(
                    "Authorization",
                    "Bearer $token"
                )
                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.doOutput = true

                val body = JSONObject().apply {
                    put("alertId", alertId)
                }

                connection.outputStream.use { output ->
                    output.write(body.toString().toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode

                if (responseCode !in 200..299) {
                    val errorBody = connection.errorStream
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        .orEmpty()

                    throw IllegalStateException(
                        "Community Alert resolution failed ($responseCode): $errorBody"
                    )
                }

                val response = connection.inputStream
                    .bufferedReader()
                    .use { it.readText() }

                JSONObject(response).optBoolean("success", false)
            } finally {
                connection.disconnect()
            }
        }

        check(success) {
            "Cloudflare Worker did not confirm Community Alert resolution"
        }
    }

}

