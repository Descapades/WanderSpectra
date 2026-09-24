package com.wanderspectra.app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CaregiverRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun createCaregiver(
        caregiver: Caregiver,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onFailure(Exception("No authenticated caregiver found."))
            return
        }

        val caregiverWithUid = caregiver.copy(
            uid = user.uid,
            email = user.email ?: caregiver.email
        )

        firestore.collection("caregivers")
            .document(user.uid)
            .set(caregiverWithUid)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getCurrentCaregiver(
        onSuccess: (Caregiver?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onFailure(Exception("No authenticated caregiver found."))
            return
        }

        firestore.collection("caregivers")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                val caregiver = document.toObject(Caregiver::class.java)
                onSuccess(caregiver)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}