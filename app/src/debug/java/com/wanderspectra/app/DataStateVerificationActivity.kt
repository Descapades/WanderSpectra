package com.wanderspectra.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Debug-only, read-only exercise of the actual History loading/retry UI. */
class DataStateVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialChildId = intent.getStringExtra("childId").orEmpty()
        setContent {
            MaterialTheme {
                val scope = rememberCoroutineScope()
                var testChildId by remember { mutableStateOf(initialChildId) }
                var children by remember { mutableStateOf<List<ChildProfile>>(emptyList()) }
                var profileMessage by remember { mutableStateOf("Loading test profiles...") }
                LaunchedEffect(Unit) {
                    ChildProfileRepository().getChildProfiles(
                        onSuccess = { children = it; profileMessage = if (it.isEmpty()) "No child profiles found." else "Select the child used for testing:" },
                        onFailure = { profileMessage = "Sign in to WanderSpectra first, then reopen this test." }
                    )
                }
                var scenario by remember { mutableStateOf("Failure then retry") }
                var run by remember { mutableStateOf(0) }
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Text("SCRUM-59 verification — simulated states")
                    Text("Read-only test. Retry after simulated failure loads real History.")
                    Row {
                        TextButton(onClick = { scenario = "Failure then retry"; run++ }) { Text("Failure/retry") }
                        TextButton(onClick = { scenario = "Empty"; run++ }) { Text("Empty") }
                        TextButton(onClick = { scenario = "Loading"; run++ }) { Text("Loading") }
                    }
                    TextButton(onClick = { finish() }) { Text("Return to app") }
                    if (testChildId.isBlank()) {
                        Text(profileMessage)
                        children.forEach { child ->
                            TextButton(onClick = { testChildId = child.childId }) {
                                Text(child.preferredName.ifBlank { child.fullName } + " — " + child.childId.takeLast(6))
                            }
                        }
                    } else key(scenario, run) {
                        var attempts by remember { mutableStateOf(0) }
                        val loader: (String, (List<IncidentRecord>) -> Unit, (Exception) -> Unit) -> Unit = remember {
                            { id, success, failure ->
                                attempts++
                                val attempt = attempts
                                scope.launch {
                                    delay(if (scenario == "Loading") 8000L else 1500L)
                                    when {
                                        scenario == "Empty" -> success(emptyList())
                                        scenario == "Failure then retry" && attempt == 1 -> failure(
                                            FirebaseFirestoreException("Simulated unavailable service", FirebaseFirestoreException.Code.UNAVAILABLE)
                                        )
                                        else -> IncidentRepository().getIncidentsForChild(id, success, failure)
                                    }
                                }
                            }
                        }
                        IncidentHistoryScreen(
                            childId = testChildId,
                            childName = "Test (verification)",
                            onViewReport = {},
                            modifier = Modifier.weight(1f),
                            loadIncidents = loader
                        )
                    }
                }
            }
        }
    }
}
