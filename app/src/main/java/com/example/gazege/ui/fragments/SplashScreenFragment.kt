package com.example.gazege.ui.fragments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.R
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.MediumHeadline
import kotlinx.coroutines.delay

@Composable
fun SplashScreenFragment(
    onNavigateToInitialScreen: () -> Unit
) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .clip(CircleShape)
                .background(colorResource(id = R.color.ic_launcher_background)),
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "AppIcon"
        )
        MediumHeadline(stringResource(id = R.string.app_name))
    }
    LaunchedEffect("splashScreen") {
        delay(500L)
        onNavigateToInitialScreen()
    }
}

@Preview
@Composable
fun PreviewSplashScreen() {
    GazegeTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            SplashScreenFragment {

            }
        }
    }
}