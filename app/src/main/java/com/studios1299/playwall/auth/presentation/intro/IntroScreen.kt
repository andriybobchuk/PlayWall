package com.studios1299.playwall.auth.presentation.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studios1299.playwall.R
import com.studios1299.playwall.core.presentation.components.Buttons
import com.studios1299.playwall.core.presentation.designsystem.PlayWallTheme

@Composable
fun IntroScreenRoot(
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onContentPolicyClick: () -> Unit
) {
    IntroScreen(
        onAction = { action ->
            when(action) {
                IntroAction.OnSignInClick -> onSignInClick()
                IntroAction.OnSignUpClick -> onSignUpClick()
                IntroAction.onTermsClick -> onTermsClick()
                IntroAction.onPrivacyClick -> onPrivacyClick()
                IntroAction.onContentPolicyClick -> onContentPolicyClick()
            }
        }
    )
}

@Composable
fun IntroScreen(
    onAction: (IntroAction) -> Unit
) {
    Scaffold {
        it
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                LogoVertical()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 48.dp)
            ) {
//                Text(
//                    text = stringResource(R.string.welcome_to_playwall),
//                    color = MaterialTheme.colorScheme.onBackground,
//                    fontSize = 20.sp
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = stringResource(R.string.an_incredible_app_that_allows_you_to_send_wallpapers),
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSecondaryContainer
//                )
                Spacer(modifier = Modifier.height(32.dp))
                Buttons.Primary(
                    text = stringResource(id = R.string.login),
                    isLoading = false,
                    onClick = {
                        onAction(IntroAction.OnSignInClick)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Buttons.Outlined(
                    text = stringResource(id = R.string.register),
                    isLoading = false,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onAction(IntroAction.OnSignUpClick)
                    }
                )
            }
        }
    }
}

@Composable
private fun LogoVertical(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_pw),
            contentDescription = null,
            modifier = Modifier.size(320.dp)
        )
    }
}

@Preview
@Composable
private fun IntroScreenPreview() {
    PlayWallTheme {
        IntroScreen(
            onAction = {}
        )
    }
}