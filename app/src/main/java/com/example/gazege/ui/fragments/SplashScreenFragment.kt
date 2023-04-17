package com.example.gazege.ui.fragments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOutCirc
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

enum class IconVisibility {
    VISIBLE,
    HIDDEN,
    FADE_IN,
    FADE_OUT
}

enum class TextVisibility {
    VISIBLE,
    HIDDEN,
    FADE_IN
}

@Composable
fun SplashScreenFragment(
    modifier: Modifier = Modifier,
    transitionDuration: Int,
    iconVisibility: IconVisibility = IconVisibility.FADE_IN,
    textVisibility: TextVisibility = TextVisibility.HIDDEN
) {
    val iconTransition: Pair<Boolean, Boolean> = when (iconVisibility) {
        IconVisibility.VISIBLE -> Pair(true, true)
        IconVisibility.HIDDEN -> Pair(false, false)
        IconVisibility.FADE_IN -> Pair(false, true)
        IconVisibility.FADE_OUT -> Pair(true, false)
    }
    val textTransition: Pair<Boolean, Boolean> = when (textVisibility) {
        TextVisibility.VISIBLE -> Pair(true, true)
        TextVisibility.HIDDEN -> Pair(false, false)
        TextVisibility.FADE_IN -> Pair(false, true)
    }

    val iconVisibilityState =
        remember { MutableTransitionState(initialState = iconTransition.first) }
            .apply { targetState = iconTransition.second }
    val textVisibilityState =
        remember { MutableTransitionState(initialState = textTransition.first) }
            .apply { targetState = textTransition.second }

    Column(
        modifier
            .fillMaxSize()
            .animateContentSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visibleState = iconVisibilityState,
            modifier = Modifier,
            enter = fadeIn(tween(transitionDuration, easing = EaseOutCirc)),
            exit = fadeOut(tween(transitionDuration, easing = EaseOutCirc)),
            label = "A"
        ) {
            Image(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.ic_launcher_background)),
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "AppIcon"
            )
        }
        AnimatedVisibility(
            visibleState = textVisibilityState,
            modifier = Modifier,
            enter = (fadeIn(tween(transitionDuration, easing = EaseOutCirc)) +
                    expandVertically(
                        tween(transitionDuration, easing = EaseOutCirc),
                        clip = false
                    ) { 0 }
                    ),
            exit = fadeOut(tween(transitionDuration, easing = EaseOutCirc)),
            label = "B"
        ) {
            MediumHeadline(
                stringResource(id = R.string.app_name),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Preview
@Composable
fun PreviewSplashScreen() {
    GazegeTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            SplashScreenFragment(
                iconVisibility = IconVisibility.VISIBLE,
                textVisibility = TextVisibility.VISIBLE,
                transitionDuration = 500
            )
        }
    }
}