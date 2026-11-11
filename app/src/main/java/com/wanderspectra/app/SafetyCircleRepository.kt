package com.wanderspectra.app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


class SafetyCircleRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val safetyCircleCollection =
        firestore.collection("safetyCircle")

    private fun memberDocument(memberId: String) =
        safetyCircleCollection.document(memberId)


    fun saveMemberWithAssignments(
        member: SafetyCircleMember,
        selectedChildIds: Set<String>,
        onSuccess: (SafetyCircleMember) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val caregiverId = FirebaseAuth.getInstance().currentUser?.uid

        if (caregiverId.isNullOrBlank() ||
            member.caregiverId != caregiverId ||
            selectedChildIds.isEmpty() ||
            selectedChildIds.any { it.isBlank() }
        ) {
            onError(IllegalArgumentException("Invalid member or child assignments."))
            return
        }

        val document = if (member.memberId.isBlank()) {
            safetyCircleCollection.document()
        } else {
            memberDocument(member.memberId)
        }

        val primaryChildId = member.childId
            .takeIf { it in selectedChildIds }
            ?: selectedChildIds.min()

        val memberToSave = member.copy(
            memberId = document.id,
            childId = primaryChildId
        )


        if (member.memberId.isBlank()) {
            val batch = firestore.batch()

            batch.set(document, memberToSave)

            selectedChildIds.forEach { childId ->
                batch.set(
                    document.collection("assignedChildren").document(childId),
                    mapOf(
                        "childId" to childId,
                        "assignedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            batch.commit()
                .addOnSuccessListener { onSuccess(memberToSave) }
                .addOnFailureListener { onError(it) }

            return
        }


        document.collection("assignedChildren")
            .get()
            .addOnSuccessListener { snapshot ->
                val existingIds = snapshot.documents.map { it.id }.toSet()
                val batch = firestore.batch()

                batch.set(document, memberToSave)

                (selectedChildIds - existingIds).forEach { childId ->
                    batch.set(
                        document.collection("assignedChildren").document(childId),
                        mapOf(
                            "childId" to childId,
                            "assignedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                (existingIds - selectedChildIds).forEach { childId ->
                    batch.delete(
                        document.collection("assignedChildren").document(childId)
                    )
                }

                batch.commit()
                    .addOnSuccessListener { onSuccess(memberToSave) }
                    .addOnFailureListener { onError(it) }
            }
            .addOnFailureListener { onError(it) }
    }

    fun getMembersForCaregiver(
        caregiverId: String,
        onSuccess: (List<SafetyCircleMember>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (caregiverId.isBlank()) {
            onError(IllegalArgumentException("Caregiver ID is required."))
            return
        }

        safetyCircleCollection
            .whereEqualTo("caregiverId", caregiverId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val members = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(SafetyCircleMember::class.java)
                }

                onSuccess(members)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun getAssignedChildIds(
        memberId: String,
        onSuccess: (Set<String>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (memberId.isBlank()) {
            onError(
                IllegalArgumentException("Member ID is required.")
            )
            return
        }

        safetyCircleCollection
            .document(memberId)
            .collection("assignedChildren")
            .get()
            .addOnSuccessListener { snapshot ->
                val childIds = snapshot.documents
                    .map { it.id }
                    .toSet()

                onSuccess(childIds)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }


    fun isMemberAssignedToChild(
        member: SafetyCircleMember,
        childId: String,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        memberDocument(member.memberId)
            .collection("assignedChildren")
            .get()
            .addOnSuccessListener { snapshot ->
                val assignedIds = snapshot.documents.map { it.id }

                val isAssigned = if (assignedIds.isEmpty()) {
                    member.childId == childId
                } else {
                    childId in assignedIds
                }

                onSuccess(isAssigned)
            }
            .addOnFailureListener { onError(it) }
    }

    fun removeMember(
        memberId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val memberRef = memberDocument(memberId)
        val assignmentsRef = memberRef.collection("assignedChildren")

        assignmentsRef.get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()

                snapshot.documents.forEach { document ->
                    batch.delete(document.reference)
                }

                batch.delete(memberRef)

                batch.commit()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it) }
            }
            .addOnFailureListener { onError(it) }
    }

}


