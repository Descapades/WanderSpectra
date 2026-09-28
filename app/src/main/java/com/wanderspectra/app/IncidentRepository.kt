
package com.wanderspectra.app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * SCRUM-44: Retrieves previously recorded elopement incidents.
 *
 * Uses the top-level incidents collection specified in the
 * WanderSpectra Design Document.
 *
 * This repository is read-only. Incident creation and updating
 * are responsibilities of the team's incident-recording workflow.
 */
class IncidentRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Retrieves incidents associated with the authenticated caregiver
     * and the selected child.
     *
     * Results are sorted newest first after retrieval.
     * Records without a start time appear last.
     */
    fun getIncidentsForChild(
        childId: String,
        onSuccess: (List<IncidentRecord>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onFailure(Exception("No authenticated caregiver found."))
            return
        }

        if (childId.isBlank()) {
            onFailure(
                IllegalArgumentException("A child ID is required.")
            )
            return
        }

        firestore
            .collection("incidents")
            .whereEqualTo("caregiverId", user.uid)
            .whereEqualTo("childId", childId)
            .get()
            .addOnSuccessListener { querySnapshot ->

                val incidents = querySnapshot.documents.mapNotNull { document ->
                    document
                        .toObject(IncidentRecord::class.java)
                        ?.copy(incidentId = document.id)
                }

                val sortedIncidents = incidents.sortedByDescending {
                    it.startTime?.seconds ?: Long.MIN_VALUE
                }

                onSuccess(sortedIncidents)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
