package com.wanderspectra.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.ButtonRed
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryRed
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.padding

/** Opens the system dialer; the caregiver must initiate the call. */
@Composable
fun EmergencyServicesShortcut(
    modifier: Modifier = Modifier,
    emergencyNumber: String = "911"
) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Call Emergency Services",
            color = SecondaryRed,
            fontFamily = Salsa,
            fontSize = 24.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Provides access to emergency services and " +
                    "$emergencyNumber calling through your phone dialer.",
            color = EmergencyRed,
            fontFamily = Salsa,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                error = null
                try {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_DIAL,
                            Uri.fromParts("tel", emergencyNumber, null)
                        )
                    )
                } catch (_: ActivityNotFoundException) {
                    error = "No calling app is available. Use another phone to contact emergency services."
                } catch (_: SecurityException) {
                    error = "Unable to open the calling app. Open your phone app to contact emergency services."
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(48.dp)
                .border(
                    width = 1.dp,
                    color = SecondaryRed,
                    shape = RoundedCornerShape(10.dp)
                ),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonRed,
                contentColor = SecondaryRed
            )
        ) {
            Text(
                text = "Emergency Services",
                fontFamily = Salsa,
                fontSize = 18.sp
            )
        }

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontFamily = Salsa
            )
        }
    }
}
