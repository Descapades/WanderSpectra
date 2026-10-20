package com.wanderspectra.app

import com.google.firebase.firestore.FirebaseFirestore

class CommunityEmergencyProfileRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun getEmergencyProfile(
        communityAlertId: String,
        onSuccess: (ChildProfile?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (communityAlertId.isBlank()) {
            onFailure(Exception("Community Alert ID is missing."))
            return
        }

        firestore
            .collection("publicEmergencyProfiles")
            .document(communityAlertId)
            .get()
            .addOnSuccessListener { document ->
                val profile = if (document.exists()) {
                    document.toObject(ChildProfile::class.java)
                } else {
                    null
                }

                onSuccess(profile)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}