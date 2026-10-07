package com.wanderspectra.app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.LaunchedEffect

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

    fun getChildProfiles(
        onSuccess: (List<ChildProfile>) -> Unit,
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
            .get()
            .addOnSuccessListener { documents ->
                val children = documents.mapNotNull { document ->
                    document.toObject(ChildProfile::class.java)
                }

                onSuccess(children)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun updateChildProfile(
        childProfile: ChildProfile,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onFailure(Exception("No authenticated caregiver found."))
            return
        }

        if (childProfile.childId.isBlank()) {
            onFailure(Exception("Child profile ID is missing."))
            return
        }

        val updatedChild = childProfile.copy(
            caregiverUid = user.uid
        )

        firestore
            .collection("caregivers")
            .document(user.uid)
            .collection("children")
            .document(childProfile.childId)
            .set(updatedChild)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}