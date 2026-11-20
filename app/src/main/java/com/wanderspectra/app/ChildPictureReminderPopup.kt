package com.wanderspectra.app

import android.content.Context
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.wanderspectra.app.ui.theme.CardCream
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.White

object ChildPictureReminderPreferences {
    private const val PREFS_NAME = "child_picture_reminder_prefs"
    private const val KEY_LAST_SHOWN_PREFIX = "last_reminder_timestamp_"
    const val ONE_MONTH_MS = 30L * 24 * 60 * 60 * 1000L // 30 days in milliseconds

    fun shouldShowReminder(context: Context, userId: String, currentTime: Long = System.currentTimeMillis()): Boolean {
        if (userId.isBlank()) return false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastShownTimestamp = prefs.getLong(KEY_LAST_SHOWN_PREFIX + userId, 0L)
        return (lastShownTimestamp == 0L) || ((currentTime - lastShownTimestamp) >= ONE_MONTH_MS)
    }

    fun recordReminderShown(context: Context, userId: String, currentTime: Long = System.currentTimeMillis()) {
        if (userId.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putLong(KEY_LAST_SHOWN_PREFIX + userId, currentTime)
        }
    }
}

@Composable
fun ChildPictureReminderPopup() {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser ?: return

    val userId = currentUser.uid
    var showPopup by remember(userId) { mutableStateOf(value = false) }

    LaunchedEffect(userId) {
        if (ChildPictureReminderPreferences.shouldShowReminder(context, userId)) {
            showPopup = true
        }
    }

    if (showPopup) {
        val dismissAction = {
            ChildPictureReminderPreferences.recordReminderShown(context, userId)
            showPopup = false
        }

        AlertDialog(
            onDismissRequest = dismissAction,
            title = {
                Text(
                    text = "Update Child Profile Picture",
                    fontFamily = Salsa,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                )
            },
            text = {
                Text(
                    text = "Please remember to update the picture used for your child's profile to keep their safety profile current.",
                    fontFamily = Salsa,
                    color = SecondaryBlue,
                )
            },
            confirmButton = {
                Button(
                    onClick = dismissAction,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                ) {
                    Text(
                        text = "OK",
                        fontFamily = Salsa,
                        color = White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            containerColor = CardCream,
            shape = RoundedCornerShape(14.dp),
        )
    }
}
