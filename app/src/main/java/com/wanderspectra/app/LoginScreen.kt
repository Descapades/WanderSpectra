package com.wanderspectra.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import com.wanderspectra.app.ui.theme.TanSongbird
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.ButtonYellow
import com.wanderspectra.app.ui.theme.CardCream
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.PrimaryBlue
import androidx.compose.foundation.border
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.wanderspectra.app.ui.theme.BackgroundCream
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.ButtonRed
import com.wanderspectra.app.ui.theme.SecondaryRed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts


@Composable
fun LoginScreen(
    callbackManager: CallbackManager,
    onCreateAccountClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {

    val backgroundCream = Color(0xFFEEE9D5)
    val primaryBlue = Color(0xFF4681B2)
    val emergencyRed = Color(0xFFC34A5B)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginMessage by remember { mutableStateOf<String?>(null) }
    var isLoggingIn by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val credentialManager = CredentialManager.create(context)
    val coroutineScope = rememberCoroutineScope()



    fun signInCaregiver() {
        loginMessage = null

        when {
            email.isBlank() -> {
                loginMessage = "Please enter your email address."
            }

            !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> {
                loginMessage = "Please enter a valid email address."
            }

            password.isBlank() -> {
                loginMessage = "Please enter your password."
            }

            else -> {
                isLoggingIn = true

                auth.signInWithEmailAndPassword(
                    email.trim(),
                    password
                ).addOnCompleteListener { task ->
                    isLoggingIn = false

                    if (task.isSuccessful) {
                        loginMessage = null
                        onLoginSuccess()
                    } else {
                        loginMessage = "Invalid email or password."
                    }
                }
            }
        }
    }

    fun signInWithGoogle() {
        coroutineScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(
                        context.getString(R.string.default_web_client_id)
                    )
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                val credential = result.credential

                if (
                    credential.type ==
                    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)

                    val firebaseCredential = GoogleAuthProvider.getCredential(
                        googleIdTokenCredential.idToken,
                        null
                    )

                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                loginMessage = null
                                onLoginSuccess()
                            } else {
                                loginMessage =
                                    "Google sign in failed. Please try again."
                            }
                        }
                } else {
                    loginMessage =
                        "Google sign in failed. Please try again."
                }

            } catch (e: GetCredentialException) {
                loginMessage =
                    "Google sign in was cancelled or could not be completed."
            } catch (e: Exception) {
                loginMessage =
                    "Google sign in failed. Please try again."
            }
        }
    }

    fun handleFacebookAccessToken(accessToken: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(accessToken.token)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loginMessage = null
                    onLoginSuccess()
                } else {
                    loginMessage =
                        "Facebook sign in failed. Please try again."
                }
            }
    }

    LoginManager.getInstance().registerCallback(
        callbackManager,
        object : FacebookCallback<LoginResult> {

            override fun onSuccess(result: LoginResult) {
                handleFacebookAccessToken(result.accessToken)
            }

            override fun onCancel() {
                loginMessage = "Facebook sign in was cancelled."
            }

            override fun onError(error: FacebookException) {
                loginMessage =
                    "Facebook sign in failed. Please try again."
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundCream)
    ) {

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
                color = emergencyRed,
                fontFamily = TanSongbird,
                fontSize = 22.sp
            )

            Text(
                text = "Spectra",
                color = primaryBlue,
                fontFamily = TanSongbird,
                fontSize = 22.sp
            )

            Image(
                painter = painterResource(R.drawable.wanderspectra_logo),
                contentDescription = "WanderSpectra Logo",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(60.dp)
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    start = 28.dp,
                    end = 28.dp,
                    top = 95.dp
                )
                .fillMaxWidth()
                .background(
                    color = ButtonYellow,
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 1.dp,
                    color = PrimaryBlue,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Inner cream login card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = BackgroundCream,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = PrimaryBlue,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 28.dp,
                        bottom = 28.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Welcome to",
                    color = SecondaryBlue,
                    fontFamily = Salsa,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(color = EmergencyRed)
                        ) {
                            append("Wander")
                        }

                        withStyle(
                            style = SpanStyle(color = PrimaryBlue)
                        ) {
                            append("Spectra!")
                        }
                    },
                    fontFamily = Salsa,
                    fontSize = 24.sp,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text = "Please log in or create an\naccount to get started!",
                    color = SecondaryBlue,
                    fontFamily = Salsa,
                    fontSize = 20.sp,
                    lineHeight = 25.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(
                    modifier = Modifier.height(28.dp)
                )
                Button(
                    onClick = {
                        signInWithGoogle()
                    },
                    modifier = Modifier
                        .width(230.dp)
                        .height(48.dp)
                        .border(
                            width = 1.dp,
                            color = PrimaryBlue,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardCream,
                        contentColor = SecondaryBlue
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 14.dp,
                        vertical = 0.dp
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Log in with Google        ",
                            fontFamily = Salsa,
                            fontSize = 14.sp,
                            color = SecondaryBlue
                        )

                        Image(
                            painter = painterResource(R.drawable.google_logo),
                            contentDescription = "Google",
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        LoginManager.getInstance().logInWithReadPermissions(
                            context as androidx.activity.ComponentActivity,
                            callbackManager,
                            listOf("email", "public_profile")
                        )
                    },
                    modifier = Modifier
                        .width(230.dp)
                        .height(48.dp)
                        .border(
                            width = 1.dp,
                            color = PrimaryBlue,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardCream,
                        contentColor = SecondaryBlue
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 14.dp,
                        vertical = 0.dp
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Log in with Facebook        ",
                            fontFamily = Salsa,
                            fontSize = 14.sp,
                            color = SecondaryBlue
                        )

                        Image(
                            painter = painterResource(R.drawable.facebook_logo),
                            contentDescription = "Facebook",
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Log in with Email:",
                    modifier = Modifier
                        .width(230.dp),
                    color = SecondaryBlue,
                    fontFamily = Salsa,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Enter Username/Email",
                    modifier = Modifier
                        .width(230.dp),
                    color = SecondaryBlue,
                    fontFamily = Salsa,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(5.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .width(230.dp),
                    placeholder = {
                        Text(
                            text = "username",
                            fontFamily = Salsa,
                            fontSize = 12.sp
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = PrimaryBlue,
                        focusedContainerColor = CardCream,
                        unfocusedContainerColor = CardCream
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Enter Password",
                    modifier = Modifier
                        .width(230.dp),
                    color = SecondaryBlue,
                    fontFamily = Salsa,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(5.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .width(230.dp),
                    placeholder = {
                        Text(
                            text = "password",
                            fontFamily = Salsa,
                            fontSize = 12.sp
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = PrimaryBlue,
                        focusedContainerColor = CardCream,
                        unfocusedContainerColor = CardCream
                    )
                )
                Spacer(modifier = Modifier.height(18.dp))

                loginMessage?.let { message ->
                    Text(
                        text = message,
                        fontFamily = Salsa,
                        fontSize = 12.sp,
                        color = EmergencyRed,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        signInCaregiver()
                    },
                    enabled = !isLoggingIn,
                    modifier = Modifier
                        .width(230.dp)
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
                        text = "Log In",
                        fontFamily = Salsa,
                        fontSize = 16.sp,
                        color = SecondaryBlue
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Or",
                    color = SecondaryBlue,
                    fontFamily = Salsa,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onCreateAccountClick,
                    modifier = Modifier
                        .width(230.dp)
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
                        text = "Create Account",
                        fontFamily = Salsa,
                        fontSize = 16.sp,
                        color = SecondaryRed
                    )
                }
                // Login controls will go here next
            }
        }
    }
}