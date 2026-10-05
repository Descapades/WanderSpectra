package com.wanderspectra.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.ButtonRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryRed

@Composable
fun HomeScreen(
    onSignOut: () -> Unit
) {
    AppShell(onSignOut = onSignOut) {}
}

@Composable
fun HomeContent(
    onChildMissing: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Home", color = PrimaryBlue, fontFamily = Salsa, fontSize = 24.sp)
        Button(
            onClick = onChildMissing,
            modifier = Modifier.padding(top = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ButtonRed, contentColor = SecondaryRed)
        ) {
            Text("Child Is Missing", fontFamily = Salsa)
        }
    }
}