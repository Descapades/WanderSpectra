package com.wanderspectra.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa


@Composable
fun EmergencyProfileContent(
    profile: ChildProfile?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (profile == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No child profile is currently available.",
                color = PrimaryBlue,
                fontFamily = Salsa,
                fontSize = 16.sp
            )
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            ViewChildProfile(
                child = profile
            )
        }
    }
}