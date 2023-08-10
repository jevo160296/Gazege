package com.example.gazege

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.rememberNavController
import com.example.gazege.core.AppDatabase
import com.example.gazege.core.AppRepository
import com.example.gazege.core.export.writeCsv
import com.example.gazege.ui.Settings
import com.example.gazege.ui.fragments.IconVisibility
import com.example.gazege.ui.fragments.SplashScreenFragment
import com.example.gazege.ui.fragments.TextVisibility
import com.example.gazege.ui.navigation.MainNavHost
import com.example.gazege.ui.theme.GazegeTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

class MainActivity : ComponentActivity() {
    private val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    private val repository: AppRepository by lazy {
        AppRepository(
            personDao = database.personDao(),
            accountDao = database.accountDao(),
            transactionDao = database.transactionDao(),
            categoryDao = database.categoryDao(),
            budgetDao = database.budgetDao()
        )
    }
    private val settings by lazy { Settings(this) }

    private lateinit var resultLauncher: ActivityResultLauncher<String>

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(repository, settings, resultLauncher)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultLauncher = registerForActivityResult(CreateDocument("txt/csv")) { uri ->
            mainViewModel.viewModelScope.launch {
                mainViewModel.getExportedTransactions { exportTransactions ->
                    uri?.also { uri ->
                        contentResolver.openOutputStream(uri)?.use {
                            writeCsv(it, exportTransactions)
                        }
                    }
                }
            }
        }
        setContent {
            GazegeTheme(appMode = stringResource(id = R.string.APP_MODE)) {
                SetStatusBarColors()

                val showSplashScreen = mainViewModel.appInitialized()
                val splashScreenState by rememberSplashScreenState(showSplashScreen = showSplashScreen)

                val navController = rememberNavController()

                SplashScreenLayout(splashScreenState) {
                    MainNavHost(
                        navController = navController,
                        mainViewModel = mainViewModel,
                        onCloseApp = { this@MainActivity.finish() },
                        onDataLoaded = { splashScreenState.hideAndShowContent() }
                    )
                }
            }
        }
    }
}

/**
Updates all of the system bar colors to be transparent, and use dark icons
if we're in light theme.
 */
@Composable
private fun SetStatusBarColors() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    LaunchedEffect(systemUiController, useDarkIcons) {
        // Update all of the system bar colors to be transparent, and use
        // dark icons if we're in light theme
        systemUiController.setStatusBarColor(
            color = Color.Transparent, darkIcons = useDarkIcons
        )

        // setStatusBarColor() and setNavigationBarColor() also exist
    }
}

class SplashScreenState(showSplashScreen: Boolean) {
    var showInitialSplashScreen = mutableStateOf(showSplashScreen.not())
    val showLoadingSplashScreen = MutableTransitionState(showSplashScreen.not())

    fun loadContentWhileShowing() {
        showInitialSplashScreen.value = false
    }

    fun hideAndShowContent() {
        loadContentWhileShowing()
        showLoadingSplashScreen.targetState = false
    }
}

@Composable
fun rememberSplashScreenState(showSplashScreen: Boolean) =
    remember { mutableStateOf(SplashScreenState(showSplashScreen)) }

@Composable
private fun SplashScreenLayout(
    splashScreenState: SplashScreenState,
    content: @Composable () -> Unit
) {
    val showInitialSplashScreen by rememberSaveable { splashScreenState.showInitialSplashScreen }
    val showLoadingSplashScreen = remember { splashScreenState.showLoadingSplashScreen }

    if (showInitialSplashScreen) {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            SplashScreenFragment(
                transitionDuration = 800,
                iconVisibility = IconVisibility.FADE_IN,
                textVisibility = TextVisibility.FADE_IN
            )
        }
    } else {
        val timeMillis = 1500L
        val transitionDuration = timeMillis
            .times(70L)
            .div(100L)
            .coerceAtMost(min(timeMillis, 1000L))
            .toInt()
        AnimatedVisibility(
            modifier = Modifier.zIndex(1f),
            visibleState = showLoadingSplashScreen,
            exit = fadeOut(tween(500, 100))
        ) {
            Box(Modifier.background(MaterialTheme.colorScheme.background)) {
                SplashScreenFragment(
                    transitionDuration = transitionDuration,
                    iconVisibility = IconVisibility.VISIBLE,
                    textVisibility = TextVisibility.VISIBLE
                )
            }
        }
        content()
    }

    LaunchedEffect(key1 = Unit) {
        delay(500L)
        splashScreenState.loadContentWhileShowing()
    }
    LaunchedEffect(key1 = Unit) {
        delay(3000)
        splashScreenState.hideAndShowContent()
    }
}