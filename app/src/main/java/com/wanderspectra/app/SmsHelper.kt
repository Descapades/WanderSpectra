
package com.wanderspectra.app

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import java.util.UUID

object SmsHelper {

    fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun sendSms(
        context: Context,
        phoneNumber: String,
        message: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (!hasSmsPermission(context)) {
            onFailure("SMS permission has not been granted.")
            return
        }

        if (phoneNumber.isBlank()) {
            onFailure("The recipient's phone number is missing.")
            return
        }

        if (message.isBlank()) {
            onFailure("The SMS message cannot be empty.")
            return
        }

        val appContext = context.applicationContext

        val smsManager = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                appContext.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            } ?: run {
                onFailure("SMS service is unavailable.")
                return
            }
        } catch (exception: Exception) {
            onFailure(exception.localizedMessage ?: "SMS service is unavailable.")
            return
        }

        val messageParts = smsManager.divideMessage(message)

        if (messageParts.isEmpty()) {
            onFailure("Unable to prepare the SMS message.")
            return
        }

        val action = "${appContext.packageName}.SMS_SENT.${UUID.randomUUID()}"
        var completedParts = 0
        var finished = false

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (finished) return

                if (resultCode != Activity.RESULT_OK) {
                    finished = true
                    try {
                        appContext.unregisterReceiver(this)
                    } catch (_: IllegalArgumentException) {
                    }

                    onFailure("SMS sending failed (code $resultCode).")
                    return
                }

                completedParts++

                if (completedParts == messageParts.size) {
                    finished = true
                    try {
                        appContext.unregisterReceiver(this)
                    } catch (_: IllegalArgumentException) {
                    }

                    onSuccess()
                }
            }
        }

        try {
            val filter = IntentFilter(action)

            ContextCompat.registerReceiver(
                appContext,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )

            val sentIntents = ArrayList<PendingIntent>()

            messageParts.forEachIndexed { index, _ ->
                val intent = Intent(action)
                    .setPackage(appContext.packageName)
                    .putExtra("partIndex", index)

                val pendingIntent = PendingIntent.getBroadcast(
                    appContext,
                    (action + index).hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE
                )

                sentIntents.add(pendingIntent)
            }

            if (messageParts.size == 1) {
                smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    sentIntents.first(),
                    null
                )
            } else {
                smsManager.sendMultipartTextMessage(
                    phoneNumber,
                    null,
                    messageParts,
                    sentIntents,
                    null
                )
            }

        } catch (exception: Exception) {
            if (!finished) {
                finished = true

                try {
                    appContext.unregisterReceiver(receiver)
                } catch (_: IllegalArgumentException) {
                }

                onFailure(
                    exception.localizedMessage ?: "Unable to send SMS."
                )
            }
        }
    }
}


