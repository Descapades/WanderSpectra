package com.wanderspectra.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestoreException
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa

/** Shared feedback for data-backed caregiver screens. */
@Composable
fun DataStateFeedback(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (loading) CircularProgressIndicator(color = PrimaryBlue)
        Text(title, color = PrimaryBlue, fontFamily = Salsa)
        Text(message, color = PrimaryBlue)
        onRetry?.let { retry ->
            TextButton(onClick = retry) { Text("Retry", color = PrimaryBlue) }
        }
    }
}

/** Permission failures are distinct from empty data and connectivity failures. */
fun caregiverDataError(error: Exception): String = when (
    (error as? FirebaseFirestoreException)?.code
) {
    FirebaseFirestoreException.Code.PERMISSION_DENIED ->
        "Your account does not have permission to access this data. Contact the project team if this continues."
    FirebaseFirestoreException.Code.UNAUTHENTICATED ->
        "Your session could not be verified. Sign in again and retry."
    FirebaseFirestoreException.Code.UNAVAILABLE,
    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
        "The service could not be reached. Check your connection and retry."
    else -> "The request could not be completed. Please retry."
}
