package com.wanderspectra.app

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

object CloudinaryManager {

    private const val CLOUD_NAME = "kys4jn25"
    private const val UPLOAD_PRESET = "wanderspectra_child_profiles"

    fun initialize(context: Context) {
        val config = mapOf(
            "cloud_name" to CLOUD_NAME
        )

        try {
            MediaManager.get()
        } catch (_: IllegalStateException) {
            MediaManager.init(context, config)
        }
    }

    fun uploadChildPhoto(
        context: Context,
        photoUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        initialize(context)

        MediaManager.get()
            .upload(photoUri)
            .unsigned(UPLOAD_PRESET)
            .callback(object : UploadCallback {

                override fun onStart(requestId: String) {
                }

                override fun onProgress(
                    requestId: String,
                    bytes: Long,
                    totalBytes: Long
                ) {
                }

                override fun onSuccess(
                    requestId: String,
                    resultData: Map<*, *>
                ) {
                    val secureUrl = resultData["secure_url"] as? String

                    if (secureUrl != null) {
                        onSuccess(secureUrl)
                    } else {
                        onFailure("Cloudinary did not return an image URL.")
                    }
                }

                override fun onError(
                    requestId: String,
                    error: ErrorInfo
                ) {
                    onFailure(error.description)
                }

                override fun onReschedule(
                    requestId: String,
                    error: ErrorInfo
                ) {
                }
            })
            .dispatch()
    }
}