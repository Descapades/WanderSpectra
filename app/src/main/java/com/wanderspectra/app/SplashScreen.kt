package com.wanderspectra.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import com.wanderspectra.app.ui.theme.TanSongbird
import com.wanderspectra.app.ui.theme.Salsa


@Composable
fun SplashScreen() {

    val backgroundCream = Color(0xFFEEE9D5)
    val primaryBlue = Color(0xFF4681B2)
    val emergencyRed = Color(0xFFC34A5B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundCream)
            .offset(y = (-25).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(R.drawable.wanderspectra_logo),
            contentDescription = "WanderSpectra Logo",
            modifier = Modifier.size(600.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .width(280.dp)
                .height(100.dp)
        ) {
            Text(
                text = "Wander",
                color = emergencyRed,
                fontFamily = TanSongbird,
                fontSize = 36.sp,
                lineHeight = 36.sp,
                modifier = Modifier.align(Alignment.TopStart)
            )

            Text(
                text = "Spectra",
                color = primaryBlue,
                fontFamily = TanSongbird,
                fontSize = 36.sp,
                lineHeight = 36.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = 50.dp,
                        y = 40.dp
                    )
            )
        }

        Spacer(modifier = Modifier.height(42.dp))

        Text(
            text = "Detect. Locate. Alert.",
            color = emergencyRed,
            fontFamily = Salsa,
            fontSize = 40.sp,
            textAlign = TextAlign.Center
        )
    }
}