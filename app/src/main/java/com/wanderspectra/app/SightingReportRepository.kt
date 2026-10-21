package com.wanderspectra.app


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SightingReportRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

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
                    childId = alert.childId,
                    submittedAt = null
                )

                val data = mutableMapOf<String, Any?>(
                    "id" to reportData.id,
                    "communityAlertId" to reportData.communityAlertId,
                    "reporterId" to reportData.reporterId,
                    "childId" to reportData.childId,
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
                        onDone(
                            true,
                            "Sighting submitted successfully."
                        )
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
}