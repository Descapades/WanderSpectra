package com.wanderspectra.app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChildProfileRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun createChildProfile(
        childProfile: ChildProfile,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onFailure(Exception("No authenticated caregiver found."))
            return
        }

        val childDocument = firestore
            .collection("caregivers")
            .document(user.uid)
            .collection("children")
            .document()

        val childWithIds = childProfile.copy(
            childId = childDocument.id,
            caregiverUid = user.uid
        )

        childDocument
            .set(childWithIds)
            .addOnSuccessListener {
                onSuccess(childDocument.id)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getChildProfile(
        childId: String,
        onSuccess: (ChildProfile?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onFailure(Exception("No authenticated caregiver found."))
            return
        }

        firestore
            .collection("caregivers")
            .document(user.uid)
            .collection("children")
            .document(childId)
            .get()
            .addOnSuccessListener { document ->
                val childProfile =
                    document.toObject(ChildProfile::class.java)

                onSuccess(childProfile)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}