package com.wanderspectra.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.BackgroundCream
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.ButtonRed
import com.wanderspectra.app.ui.theme.ButtonYellow
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.SecondaryRed
import com.wanderspectra.app.ui.theme.TanSongbird

@Composable
fun ChildConfirmation(
    children: List<ChildProfile>,
    onAddAnotherChild: () -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
    ) {

        // WanderSpectra Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 32.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Wander",
                color = EmergencyRed,
                fontFamily = TanSongbird,
                fontSize = 22.sp
            )

            Text(
                text = "Spectra",
                color = PrimaryBlue,
                fontFamily = TanSongbird,
                fontSize = 22.sp
            )

            Image(
                painter = painterResource(
                    R.drawable.wanderspectra_logo
                ),
                contentDescription = "WanderSpectra Logo",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(60.dp)
            )
        }

        // Outer yellow card
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    start = 28.dp,
                    end = 28.dp,
                    top = 95.dp,
                    bottom = 24.dp
                )
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = PrimaryBlue,
                    shape = RoundedCornerShape(14.dp)
                )
                .background(
                    color = ButtonYellow,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(14.dp)
        ) {

            // Inner cream card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = PrimaryBlue,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        color = BackgroundCream,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
            ) {

                Text(
                    text = "Child Profiles",
                    color = SecondaryBlue,
                    fontFamily = Salsa,
                    fontSize = 24.sp
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "Children associated with your account",
                    color = PrimaryBlue,
                    fontFamily = Salsa,
                    fontSize = 12.sp
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                children.forEach { child ->

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = PrimaryBlue,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(
                                color = BackgroundCream,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(12.dp)
                    ) {

                        Text(
                            text = child.fullName,
                            color = SecondaryBlue,
                            fontFamily = Salsa,
                            fontSize = 18.sp
                        )

                        if (child.preferredName.isNotBlank()) {
                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text = "Preferred Name: ${child.preferredName}",
                                color = PrimaryBlue,
                                fontFamily = Salsa,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }

                Button(
                    onClick = onAddAnotherChild,
                    modifier = Modifier
                        .fillMaxWidth()
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
                        text = "Add Another Child",
                        fontFamily = Salsa,
                        fontSize = 12.sp,
                        color = SecondaryBlue
                    )
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(1f)
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
                            text = "Back",
                            fontFamily = Salsa,
                            fontSize = 12.sp,
                            color = SecondaryBlue
                        )
                    }

                    Button(
                        onClick = onFinish,
                        modifier = Modifier
                            .weight(1f)
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
                            text = "Finish",
                            fontFamily = Salsa,
                            fontSize = 12.sp,
                            color = SecondaryRed
                        )
                    }
                }
            }
        }
    }
}