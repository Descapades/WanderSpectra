
package com.wanderspectra.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.BackgroundCream
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.ButtonRed
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.SecondaryRed
import com.wanderspectra.app.ui.theme.TanSongbird
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@Composable
fun ElopementConfirmationContent(
    childName: String = "Jimothy",
    statusText: String = "",
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    isBusy: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("CHILD IS MISSING", color = EmergencyRed, fontFamily = TanSongbird, fontSize = 18.sp)
        Text("CONFIRMATION", color = EmergencyRed, fontFamily = Salsa, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Is $childName missing?",
            color = SecondaryRed,
            fontFamily = Salsa,
            fontSize = 34.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = EmergencyRed)) {
                    append("Activating ")
                }
                withStyle(SpanStyle(color = SecondaryRed)) {
                    append("ELOPEMENT MODE")
                }
                withStyle(SpanStyle(color = EmergencyRed)) {
                    append(
                        " will prioritize $childName's location " +
                                "and give you access to emergency response tools."
                    )
                }
            },
            fontFamily = Salsa,
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Elopement Mode will:",
            color = SecondaryRed,
            fontFamily = Salsa,
            fontSize = 22.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "• Continue monitoring $childName's wearable location\n" +
                    "• Provide quick access to Play Sound and emergency services\n" +
                    "• Allow you to notify your Safety Circle\n" +
                    "• Allow you to issue a Community Alert",
            color = EmergencyRed,
            fontFamily = Salsa,
            fontSize = 20.sp,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(48.dp))


        Button(
            onClick = onConfirm,
            enabled = !isBusy,
            modifier = Modifier.fillMaxWidth(0.85f)
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
                text = "ELOPEMENT MODE",
                fontFamily = Salsa,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onCancel,
            enabled = !isBusy,
            modifier = Modifier.fillMaxWidth(0.85f)
                .height(48.dp)
                .border(
                    width = 1.dp,
                    color = PrimaryBlue,
                    shape = RoundedCornerShape(10.dp)
                ),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonBlue,
                contentColor = SecondaryBlue
            )
        ) {
            Text(
                text = "Cancel",
                fontFamily = Salsa,
                fontSize = 16.sp
            )
        }
    }
}

