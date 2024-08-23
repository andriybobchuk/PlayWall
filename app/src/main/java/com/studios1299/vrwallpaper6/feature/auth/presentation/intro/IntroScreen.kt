package com.studios1299.vrwallpaper6.feature.auth.presentation.intro

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studios1299.vrwallpaper6.core.presentation.components.Buttons
import com.studios1299.vrwallpaper6.core.presentation.designsystem.PlayWallTheme
import com.studios1299.vrwallpaper6.R

@Composable
fun IntroScreenRoot(
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    IntroScreen(
        onAction = { action ->
            when(action) {
                IntroAction.OnSignInClick -> onSignInClick()
                IntroAction.OnSignUpClick -> onSignUpClick()
            }
        }
    )
}

@Composable
fun IntroScreen(
    onAction: (IntroAction) -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            RuniqueLogoVertical()
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
               // text = stringResource(id = R.string.welcome_to_runique),
                text = "Welcome to app!",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                //text = stringResource(id = R.string.runique_description),
                text = "An incredible app that allows you to send wallpapers",
                style = MaterialTheme.typography.bodySmall
            )
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

@Composable
private fun RuniqueLogoVertical(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        Icon(
//            imageVector = LogoIcon,
//            contentDescription = "Logo",
//            tint = MaterialTheme.colorScheme.onBackground
//        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
           // text = stringResource(id = R.string.runique),
            text = "PlayWall",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
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