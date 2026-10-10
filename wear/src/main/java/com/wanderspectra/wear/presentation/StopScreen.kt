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
import androidx.compose.ui.unit.dp
import com.wanderspectra.wear.R
import androidx.compose.foundation.layout.width
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

private val StopPink = Color(0xFFE2A0A2)

@Composable
fun StopScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StopPink),
        contentAlignment = Alignment.Center
    ) {
        CirculatingLightRing()

        Image(
            painter = painterResource(id = R.drawable.wear_stop_sign),
            contentDescription = "Stop",
            modifier = Modifier
                .size(100.dp)
                .offset(y = (-30).dp)
        )

        Image(
            painter = painterResource(id = R.drawable.wear_stop_text),
            contentDescription = "Stop and wait",
            modifier = Modifier
                .width(125.dp)
                .offset(y = 38.dp)
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    device = androidx.wear.tooling.preview.devices.WearDevices.SMALL_ROUND,
    showSystemUi = true
)
@Composable
fun StopScreenPreview() {
    StopScreen()
}

@Composable
fun CirculatingLightRing() {
    val infiniteTransition =
        rememberInfiniteTransition(label = "stopRingAnimation")

    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "stopRingRotation"
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val strokeWidth = 12.dp.toPx()
        val padding = 6.dp.toPx()

        drawArc(
            color = Color.White,
            startAngle = rotation.value,
            sweepAngle = 70f,
            useCenter = false,
            topLeft = Offset(padding, padding),
            size = Size(
                size.width - padding * 2,
                size.height - padding * 2
            ),
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            )
        )
    }
}