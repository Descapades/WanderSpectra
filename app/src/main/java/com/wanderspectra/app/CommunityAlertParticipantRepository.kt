package com.wanderspectra.app

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class CommunityAlertParticipantRepository(
    private val context: Context
) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun registerParticipant(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onFailure("You must be signed in to participate.")
            return
        }

        if (!CommunityAlertPreferences.isOptedIn(context)) {
            onFailure("Community Alert participation is disabled.")
            return
        }

        CommunityAlertLocationManager(context).getCurrentLocation(
            onSuccess = { latitude, longitude ->

                // Recheck consent in case the user disabled participation
                // while the location request was running.
                if (!CommunityAlertPreferences.isOptedIn(context)) {
                    onFailure("Community Alert participation was disabled.")
                    return@getCurrentLocation
                }

                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->

                        if (!CommunityAlertPreferences.isOptedIn(context)) {
                            onFailure("Community Alert participation was disabled.")
                            return@addOnSuccessListener
                        }

                        val participant = CommunityAlertParticipant(
                            userId = user.uid,
                            optedIn = true,
                            fcmToken = token,
                            latitude = latitude,
                            longitude = longitude,
                            locationUpdatedAt = Timestamp.now()
                        )

                        db.collection("communityAlertParticipants")
                            .document(user.uid)
                            .set(participant)
                            .addOnSuccessListener {
                                onSuccess()
                            }
                            .addOnFailureListener { exception ->
                                onFailure(
                                    exception.message
                                        ?: "Unable to register for Community Alerts."
                                )
                            }
                    }
                    .addOnFailureListener { exception ->
                        onFailure(
                            exception.message ?: "Unable to retrieve notification token."
                        )
                    }
            },
            onFailure = onFailure
        )
    }

    fun unregisterParticipant(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onFailure("You must be signed in.")
            return
        }

        db.collection("communityAlertParticipants")
            .document(user.uid)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(
                    exception.message ?: "Unable to disable Community Alerts."
                )
            }
    }
}