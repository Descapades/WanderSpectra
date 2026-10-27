package com.wanderspectra.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.wanderspectra.app.ui.theme.Salsa

/** Opens the system dialer; the caregiver must initiate the call. */
@Composable
fun EmergencyServicesShortcut(
    modifier: Modifier = Modifier,
    emergencyNumber: String = "911"
) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = {
                error = null
                try {
                    context.startActivity(
                        Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", emergencyNumber, null))
                    )
                } catch (_: ActivityNotFoundException) {
                    error = "No calling app is available. Use another phone to contact emergency services."
                } catch (_: SecurityException) {
                    error = "Unable to open the calling app. Open your phone app to contact emergency services."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Emergency Services ($emergencyNumber)", fontFamily = Salsa)
        }
        Text("Opens your phone dialer. You choose when to call.", fontFamily = Salsa)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
