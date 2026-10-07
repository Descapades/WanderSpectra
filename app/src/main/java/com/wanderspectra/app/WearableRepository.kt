package com.wanderspectra.app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.text.get
import kotlin.text.set

class WearableRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun wearableDocument(childId: String) =
        firestore.collection("caregivers")
            .document(auth.currentUser?.uid ?: "")
            .collection("children")
            .document(childId)
            .collection("wearable")
            .document("device")

    fun saveWearable(
        childId: String,
        wearable: Wearable,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        android.util.Log.d(
            "WearableFirestore",
            "ABOUT TO SAVE: $wearable"
        )

        wearableDocument(childId)
            .set(wearable)
            .addOnSuccessListener {
                android.util.Log.d(
                    "WearableFirestore",
                    "SAVED TO FIRESTORE: $wearable"
                )
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getWearable(
        childId: String,
        onSuccess: (Wearable?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        wearableDocument(childId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onSuccess(
                        document.toObject(Wearable::class.java)
                    )
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}



