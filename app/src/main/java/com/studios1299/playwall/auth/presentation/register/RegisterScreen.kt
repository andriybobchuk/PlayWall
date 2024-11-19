package com.studios1299.playwall.auth.presentation.register

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.studios1299.playwall.R
import com.studios1299.playwall.core.presentation.components.Buttons
import com.studios1299.playwall.core.presentation.components.TextFields
import com.studios1299.playwall.core.presentation.designsystem.CheckIcon
import com.studios1299.playwall.core.presentation.designsystem.CrossIcon
import com.studios1299.playwall.core.presentation.designsystem.ERROR_RED
import com.studios1299.playwall.core.presentation.designsystem.EmailIcon
import com.studios1299.playwall.core.presentation.designsystem.PlayWallTheme
import com.studios1299.playwall.core.presentation.designsystem.SUCCESS_GREEN
import com.studios1299.playwall.core.presentation.designsystem.poppins
import com.studios1299.playwall.auth.domain.PasswordValidationState
import com.studios1299.playwall.auth.data.UserDataValidator
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.designsystem.PersonIcon

private const val REQUEST_ID_TOKEN = "247858897065-pavj6enbck6erkubdlqo6p6vovne3utg.apps.googleusercontent.com"

@Composable
fun RegisterScreenRoot(
    onSignInClick: () -> Unit,
    onSuccessfulRegistration: () -> Unit,
    onTermsOfServiceClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    viewModel: RegisterViewModel
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val account = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val result = account.getResult(ApiException::class.java)
                val credentials = GoogleAuthProvider.getCredential(result.idToken, null)
                viewModel.googleRegister(credentials, context)
                Toast.makeText(
                    context,
                    R.string.registration_successful,
                    Toast.LENGTH_LONG
                ).show()
                onSuccessfulRegistration()
            } catch (it: ApiException) {
                print(it)
            }
        }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is RegisterEvent.Error -> {
                keyboardController?.hide()
                Toast.makeText(
                    context,
                    event.error.asString(context),
                    Toast.LENGTH_LONG
                ).show()
            }

            RegisterEvent.RegistrationSuccess -> {
                keyboardController?.hide()
                Toast.makeText(
                    context,
                    R.string.registration_successful,
                    Toast.LENGTH_LONG
                ).show()
                onSuccessfulRegistration()
            }
        }
    }

    RegisterScreen(
        state = viewModel.state,
        onAction = { action ->
            when(action) {
                is RegisterAction.OnLoginClick -> onSignInClick()
                is RegisterAction.OnTermsOfServiceClick -> onTermsOfServiceClick()
                is RegisterAction.OnPrivacyPolicyClick -> onPrivacyPolicyClick()
                else -> Unit
            }
            viewModel.onAction(action)
        },
        launcher = launcher
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RegisterScreen(
    state: RegisterState,
    onAction: (RegisterAction) -> Unit,
    launcher: ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>
) {
    val context = LocalContext.current
    Scaffold {
        it
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(vertical = 32.dp)
                .padding(top = 16.dp)
        ) {
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = stringResource(id = R.string.create_account),
                style = MaterialTheme.typography.titleLarge
            )
            val annotatedString = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontFamily = poppins,
                        color = Color.Gray
                    )
                ) {
                    append(stringResource(id = R.string.already_have_an_account) + " ")
                    pushStringAnnotation(
                        tag = "clickable_text",
                        annotation = stringResource(id = R.string.login)
                    )
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = poppins
                        )
                    ) {
                        append(stringResource(id = R.string.login))
                    }
                }
            }
            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(
                        tag = "clickable_text",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let {
                        onAction(RegisterAction.OnLoginClick)
                    }
                }
            )
            Spacer(modifier = Modifier.height(30.dp))
            TextFields.Primary(
                state = state.username,
                startIcon = PersonIcon,
                endIcon = if (state.isUsernameValid) {
                    CheckIcon
                } else null,
                hint = stringResource(id = R.string.example_username),
                title = stringResource(id = R.string.username),
                modifier = Modifier.fillMaxWidth(),
                additionalInfo = stringResource(id = R.string.must_be_a_valid_username),
                keyboardType = KeyboardType.Text
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextFields.Primary(
                state = state.email,
                startIcon = EmailIcon,
                endIcon = if (state.isEmailValid) {
                    CheckIcon
                } else null,
                hint = stringResource(id = R.string.example_email),
                title = stringResource(id = R.string.email),
                modifier = Modifier.fillMaxWidth(),
                additionalInfo = stringResource(id = R.string.must_be_a_valid_email),
                keyboardType = KeyboardType.Email
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextFields.Password(
                state = state.password,
                isPasswordVisible = state.isPasswordVisible,
                onTogglePasswordVisibility = {
                    onAction(RegisterAction.OnTogglePasswordVisibilityClick)
                },
                hint = stringResource(id = R.string.password),
                title = stringResource(id = R.string.password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            PasswordRequirement(
                text = stringResource(
                    id = R.string.at_least_x_characters,
                    UserDataValidator.MIN_PASSWORD_LENGTH
                ),
                isValid = state.passwordValidationState.hasMinLength
            )
            Spacer(modifier = Modifier.height(4.dp))
            PasswordRequirement(
                text = stringResource(
                    id = R.string.at_least_one_number,
                ),
                isValid = state.passwordValidationState.hasNumber
            )
            Spacer(modifier = Modifier.height(4.dp))
            PasswordRequirement(
                text = stringResource(
                    id = R.string.contains_lowercase_char,
                ),
                isValid = state.passwordValidationState.hasLowerCaseCharacter
            )
            Spacer(modifier = Modifier.height(4.dp))
            PasswordRequirement(
                text = stringResource(
                    id = R.string.contains_uppercase_char,
                ),
                isValid = state.passwordValidationState.hasUpperCaseCharacter
            )
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.isTermsAccepted,
                    onCheckedChange = {
                        onAction(RegisterAction.OnToggleTermsAcceptance)
                    }
                )
                val termsAndPolicyText = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontFamily = poppins,
                            color = Color.Gray
                        )
                    ) {
                        append(stringResource(R.string.i_agree_to_the))
                        pushStringAnnotation(tag = "terms", annotation = "terms_of_service")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)) {
                            append(stringResource(R.string.terms_of_service))
                        }
                        pop()
                        append(stringResource(R.string.and))
                        pushStringAnnotation(tag = "privacy", annotation = "privacy_policy")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)) {
                            append(stringResource(R.string.privacy_policy))
                        }
                    }
                }
                ClickableText(
                    text = termsAndPolicyText,
                    onClick = { offset ->
                        termsAndPolicyText.getStringAnnotations(tag = "terms", start = offset, end = offset).firstOrNull()?.let {
                            onAction(RegisterAction.OnTermsOfServiceClick)
                        }
                        termsAndPolicyText.getStringAnnotations(tag = "privacy", start = offset, end = offset).firstOrNull()?.let {
                            onAction(RegisterAction.OnPrivacyPolicyClick)
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Buttons.Primary(
                text = stringResource(id = R.string.register),
                isLoading = state.isRegistering,
                enabled = state.canRegister,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onAction(RegisterAction.OnRegisterClick(context))
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                modifier = Modifier
                    .padding(
                        top = 15.dp,
                    )
                    .align(Alignment.CenterHorizontally),
                text = stringResource(id = R.string.or_connect_with),
                fontWeight = FontWeight.Normal, fontFamily = poppins
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(15.dp)
                        .alpha(if (state.isRegistering) 1f else 0f),
                    strokeWidth = 1.5.dp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestIdToken(REQUEST_ID_TOKEN)
                        .build()
                    val googleSingInClient = GoogleSignIn.getClient(context, gso)
                    launcher.launch(googleSingInClient.signInIntent)
                }) {
                    Icon(
                        modifier = Modifier.size(50.dp),
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google Icon", tint = Color.Unspecified
                    )
                }
            }
        }
    }
}

@Composable
fun PasswordRequirement(
    text: String,
    isValid: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isValid) {
                CheckIcon
            } else {
                CrossIcon
            },
            contentDescription = null,
            tint = if (isValid) SUCCESS_GREEN else ERROR_RED
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}