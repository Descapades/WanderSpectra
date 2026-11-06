package com.wanderspectra.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.ButtonRed
import com.wanderspectra.app.ui.theme.CardCream
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.SecondaryRed
import com.wanderspectra.app.ui.theme.TanSongbird

@Composable
fun IncidentNotesScreen(
    initialNotes: String,
    initialPossibleTrigger: String,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit
) {
    var notes by remember(initialNotes) {
        mutableStateOf(initialNotes)
    }

    var possibleTrigger by remember(initialPossibleTrigger) {
        mutableStateOf(initialPossibleTrigger)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (initialNotes.isBlank()) "Add Notes" else "Edit Notes",
            color = EmergencyRed,
            fontFamily = TanSongbird,
            fontSize = 20.sp
        )
        OutlinedTextField(
            value = possibleTrigger,
            onValueChange = { possibleTrigger = it },
            label = {
                Text(
                    text = "Possible Trigger",
                    fontFamily = Salsa
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .offset(y = (-8).dp),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(
                fontFamily = Salsa,
                fontSize = 20.sp,
                color = PrimaryBlue
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardCream,
                unfocusedContainerColor = CardCream,
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = PrimaryBlue,
                disabledBorderColor = PrimaryBlue,
                errorBorderColor = PrimaryBlue,
                focusedLabelColor = PrimaryBlue,
                unfocusedLabelColor = PrimaryBlue,
                cursorColor = PrimaryBlue
            )
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = {
                Text(
                    text = "Incident Notes",
                    fontFamily = Salsa
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .offset(y = (-8).dp),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(
                fontFamily = Salsa,
                fontSize = 20.sp,
                color = PrimaryBlue
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardCream,
                unfocusedContainerColor = CardCream,
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = PrimaryBlue,
                disabledBorderColor = PrimaryBlue,
                errorBorderColor = PrimaryBlue,
                focusedLabelColor = PrimaryBlue,
                unfocusedLabelColor = PrimaryBlue,
                cursorColor = PrimaryBlue
            )
        )

        Button(
            onClick = { onSave(notes, possibleTrigger) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonRed,
                contentColor = EmergencyRed
            ),
            border = BorderStroke(1.dp, SecondaryRed)
        ) {
            Text(
                text = "Save Notes",
                color = SecondaryRed,
                fontFamily = Salsa,
                fontSize = 20.sp
            )
        }

        TextButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonBlue,
                contentColor = SecondaryBlue
            ),
            border = BorderStroke(1.dp, SecondaryBlue)
        ) {
            Text(
                text = "Cancel",
                color = SecondaryBlue,
                fontFamily = Salsa,
                fontSize = 20.sp
            )
        }
    }
}
