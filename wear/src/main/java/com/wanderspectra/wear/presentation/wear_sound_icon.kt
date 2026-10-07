package com.wanderspectra.wear.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.compose.ui.unit.sp
import com.wanderspectra.wear.R

private val SoundBlue = Color(0xFFA9C8D9)
private val SecondaryBlue = Color(0xFF1C4B76)

@Composable
fun PlayingSoundScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundBlue),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.wear_sound_icon),
            contentDescription = "Playing sound",
            modifier = Modifier
                .size(120.dp)
                .offset(y = (-25).dp)
        )

        Text(
            text = "Playing Sound...",
            color = SecondaryBlue,
            fontSize = 18.sp,
            modifier = Modifier.offset(y = 50.dp)
        )
    }
}

@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true
)
@Composable
fun PlayingSoundScreenPreview() {
    PlayingSoundScreen()
}