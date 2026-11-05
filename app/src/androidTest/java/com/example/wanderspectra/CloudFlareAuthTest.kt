package com.wanderspectra.app

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import java.net.HttpURLConnection
import java.net.URL

object CloudflareAuthTest {

    fun run() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Log.e("CloudflareTest", "No Firebase user signed in")
            return
        }

        user.getIdToken(false)
            .addOnSuccessListener { result ->
                val token = result.token ?: return@addOnSuccessListener

                Thread {
                    try {
                        val url = URL(
                            "https://wanderspectra-alerts.destin-champlin.workers.dev/submit-sighting"
                        )

                        val connection =
                            url.openConnection() as HttpURLConnection

                        connection.requestMethod = "POST"
                        connection.setRequestProperty(
                            "Authorization",
                            "Bearer $token"
                        )
                        connection.connectTimeout = 10000
                        connection.readTimeout = 10000

                        val status = connection.responseCode

                        connection.disconnect()

                    } catch (e: Exception) {
                        Log.e(
                            "CloudflareTest",
                            "Connection failed",
                            e
                        )
                    }
                }.start()
            }
            .addOnFailureListener {
                Log.e(
                    "CloudflareTest",
                    "Unable to get Firebase token",
                    it
                )
            }
    }
}