package com.wanderspectra.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.ButtonRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.SecondaryRed

@Composable
fun ChildMissingContent(
    childName: String = "Jimothy",
    statusText: String = "",
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("CHILD IS MISSING", color = SecondaryRed, fontFamily = Salsa, fontSize = 22.sp)
        Text("CONFIRMATION", color = PrimaryBlue, fontFamily = Salsa, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Is $childName missing?",
            color = SecondaryRed,
            fontFamily = Salsa,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (statusText.isNotBlank()) {
            Text(
                text = statusText,
                color = SecondaryRed,
                fontFamily = Salsa,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonRed,
                contentColor = SecondaryRed
            )
        ) {
            Text("Elopement Mode", fontFamily = Salsa)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonBlue,
                contentColor = SecondaryBlue
            )
        ) {
            Text("Cancel", fontFamily = Salsa)
        }
    }
}