package com.wanderspectra.app


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

class SightingReportRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val sightingWorkerUrl =
        "https://wanderspectra-alerts.destin-champlin.workers.dev/submit-sighting"

    fun submitReport(
        report: SightingReport,
        onDone: (Boolean, String) -> Unit
    ) {
        val reporterId = FirebaseAuth.getInstance().currentUser?.uid

        if (reporterId == null) {
            onDone(false, "You must be signed in to report a sighting.")
            return
        }

        if (report.communityAlertId.isBlank()) {
            onDone(false, "No Community Alert was selected.")
            return
        }

        if (report.sightingTime == null) {
            onDone(false, "Please select when the sighting occurred.")
            return
        }

        if (
            !report.latitude.isFinite() ||
            !report.longitude.isFinite() ||
            report.latitude !in -90.0..90.0 ||
            report.longitude !in -180.0..180.0
        ) {
            onDone(false, "Please select a valid sighting location.")
            return
        }

        if (report.direction.isBlank()) {
            onDone(false, "Please select a direction.")
            return
        }

        // Check the public alert, not the caregiver's private alert.
        val alertRef = db.collection("publicCommunityAlerts")
            .document(report.communityAlertId)

        alertRef.get()
            .addOnSuccessListener { snapshot ->

                val alert = snapshot.toObject(CommunityAlert::class.java)

                if (!snapshot.exists() || alert?.status != "ACTIVE") {
                    onDone(
                        false,
                        "This Community Alert is no longer active."
                    )
                    return@addOnSuccessListener
                }

                val reportRef = db.collection("sightingReports").document()

                val reportData = report.copy(
                    id = reportRef.id,
                    reporterId = reporterId,
                    submittedAt = null
                )

                val data = mutableMapOf<String, Any?>(
                    "id" to reportData.id,
                    "communityAlertId" to reportData.communityAlertId,
                    "reporterId" to reportData.reporterId,
                    "latitude" to reportData.latitude,
                    "longitude" to reportData.longitude,
                    "locationDescription" to reportData.locationDescription,
                    "sightingTime" to reportData.sightingTime,
                    "direction" to reportData.direction,
                    "details" to reportData.details,
                    "submittedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )

                reportRef.set(data)
                    .addOnSuccessListener {
                        notifyWorker(reportRef.id) { notified ->
                            if (!notified) {
                                android.util.Log.w(
                                    "SightingReport",
                                    "Sighting saved, but caregiver notification failed."
                                )
                            }

                            onDone(
                                true,
                                if (notified) {
                                    "Sighting submitted successfully."
                                } else {
                                    "Sighting saved, but caregiver notification could not be confirmed."
                                }
                            )
                        }
                    }
                    .addOnFailureListener { error ->
                        onDone(
                            false,
                            error.message ?: "Submission failed."
                        )
                    }
            }
            .addOnFailureListener { error ->
                onDone(
                    false,
                    error.message ?: "Unable to verify Community Alert."
                )
            }
    }

    private fun notifyWorker(reportId: String, onResult: (Boolean) -> Unit) {
        FirebaseAuth.getInstance().currentUser
            ?.getIdToken(false)
            ?.addOnSuccessListener { tokenResult ->
                val idToken = tokenResult.token

                if (idToken == null) {
                    onResult(false)
                    return@addOnSuccessListener
                }

                Thread {
                    var connection: HttpURLConnection? = null

                    try {
                        connection = URL(sightingWorkerUrl)
                            .openConnection() as HttpURLConnection

                        connection.requestMethod = "POST"
                        connection.setRequestProperty(
                            "Authorization",
                            "Bearer $idToken"
                        )
                        connection.setRequestProperty(
                            "Content-Type",
                            "application/json"
                        )
                        connection.connectTimeout = 10000
                        connection.readTimeout = 10000
                        connection.doOutput = true

                        val body = JSONObject()
                            .put("reportId", reportId)
                            .toString()

                        connection.outputStream.use {
                            it.write(body.toByteArray(Charsets.UTF_8))
                        }

                        val success = connection.responseCode in 200..299

                        android.os.Handler(
                            android.os.Looper.getMainLooper()
                        ).post {
                            onResult(success)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e(
                            "SightingReport",
                            "Worker notification request failed",
                            e
                        )

                        android.os.Handler(
                            android.os.Looper.getMainLooper()
                        ).post {
                            onResult(false)
                        }
                    } finally {
                        connection?.disconnect()
                    }
                }.start()
            }
            ?.addOnFailureListener {
                onResult(false)
            }
    }
}