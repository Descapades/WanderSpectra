package com.wanderspectra.app.data

import com.google.firebase.firestore.FirebaseFirestore
import com.wanderspectra.app.model.SafetyCircleMember

/**
 * Handles Cloud Firestore storage and retrieval
 * for WanderSpectra Safety Circle members.
 */
class SafetyCircleRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val safetyCircleCollection =
        firestore.collection("safetyCircle")

    /**
     * Creates or updates a Safety Circle member.
     *
     * If the member does not already have an ID,
     * Firestore generates one automatically.
     */
    fun saveMember(
        member: SafetyCircleMember,
        onSuccess: (SafetyCircleMember) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val documentReference =
            if (member.memberId.isBlank()) {
                safetyCircleCollection.document()
            } else {
                safetyCircleCollection.document(member.memberId)
            }

        val memberToSave = member.copy(
            memberId = documentReference.id
        )

        documentReference
            .set(memberToSave)
            .addOnSuccessListener {
                onSuccess(memberToSave)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    /**
     * Retrieves one Safety Circle member
     * using the member's Firestore document ID.
     */
    fun getMember(
        memberId: String,
        onSuccess: (SafetyCircleMember?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        safetyCircleCollection
            .document(memberId)
            .get()
            .addOnSuccessListener { document ->
                val member = document.toObject(
                    SafetyCircleMember::class.java
                )

                onSuccess(member)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    /**
     * Retrieves Safety Circle members associated
     * with a specific caregiver and child.
     */
    fun getMembersForChild(
        caregiverId: String,
        childId: String,
        onSuccess: (List<SafetyCircleMember>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        safetyCircleCollection
            .whereEqualTo("caregiverId", caregiverId)
            .whereEqualTo("childId", childId)
            .get()
            .addOnSuccessListener { querySnapshot ->

                val members = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(
                        SafetyCircleMember::class.java
                    )
                }

                onSuccess(members)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}
