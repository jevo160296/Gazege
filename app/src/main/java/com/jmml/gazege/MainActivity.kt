package com.jmml.gazege

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.jmml.gazege.core.AppDatabase
import com.jmml.gazege.core.AppRepository
import com.jmml.gazege.core.entities.recursiveFirstOrNull
import com.jmml.gazege.core.export.CreateBackupDocument
import com.jmml.gazege.extensions.livedata.observeOnce
import com.jmml.gazege.ui.Settings
import com.jmml.gazege.ui.fragments.IconVisibility
import com.jmml.gazege.ui.fragments.SplashScreenFragment
import com.jmml.gazege.ui.fragments.TextVisibility
import com.jmml.gazege.ui.navigation.MainNavHost
import com.jmml.gazege.ui.progressStatus.Status
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.widgets.GDefiniteCircularProgressIndicator
import com.jmml.gazege.ui.widgets.LargeBody
import com.jmml.gazege.ui.widgets.MediumHeadline
import kotlinx.coroutines.delay
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

    private lateinit var resultLauncherSaveData: ActivityResultLauncher<String>

    private lateinit var resultLauncherOpenDocument: ActivityResultLauncher<Array<String>>

    private lateinit var resultLauncherExportDetails: ActivityResultLauncher<String>

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            repository,
            settings,
            resultLauncherSaveData,
            resultLauncherOpenDocument,
            resultLauncherExportDetails
        )
    }

    private fun handleIntent() {
        if (intent.action == Intent.ACTION_VIEW) {
            val uri = intent?.data
            val scheme = uri?.scheme
            if (scheme != "https") {
                uri?.also {
                    contentResolver.openInputStream(uri)?.also {
                        mainViewModel.exportModule.importData(it)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultLauncherSaveData = registerForActivityResult(CreateBackupDocument()) { uri ->
            uri?.also {
                contentResolver.openOutputStream(uri)?.let { outputStream ->
                    mainViewModel.exportModule.exportData(outputStream)
                }
            }
        }
        resultLauncherOpenDocument = registerForActivityResult(OpenDocument()) { uri ->
            uri?.also {
                contentResolver.openInputStream(uri)?.also {
                    mainViewModel.exportModule.importData(it)
                }
            }
        }
        resultLauncherExportDetails = registerForActivityResult(CreateBackupDocument()) { uri ->
            mainViewModel.categoryIdToExportFlow.observeOnce(this) { categoryId ->
                mainViewModel.settingsCategoryIdToExportFlow(-1)
                mainViewModel.categoryWithSubcategoriesAndBudgetWithCalculatedData.observeOnce(this) { categories ->
                    categories
                        .recursiveFirstOrNull { it.category.category.id == categoryId }
                        ?.let { categoryToExport ->
                            uri?.let {
                                contentResolver.openOutputStream(uri)?.let { outputStream ->
                                    mainViewModel.exportModule.exportDetails(
                                        outputStream,
                                        categoryToExport
                                    )
                                }
                            }
                        }
                }
            }
        }
        setContent {
            val useDynamicColor by mainViewModel.useDynamicColor.observeAsState(initial = false)
            GazegeTheme(
                appMode = stringResource(id = R.string.APP_MODE),
                isDynamicColor = useDynamicColor
            ) {
                SetStatusBarColors()
                val importState by mainViewModel.rememberImportState()
                LaunchedEffect(key1 = Unit) {
                    handleIntent()
                }

                val showSplashScreen = mainViewModel.appInitialized()
                val splashScreenState by rememberSplashScreenState(showSplashScreen = showSplashScreen)

                val navController = rememberNavController()

                val currentState = importState.status
                Crossfade(targetState = currentState, label = "CrossFade") {
                    when (it) {
                        Status.STARTED -> {
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(
                                    dimensionResource(id = R.dimen.DefaultPadding),
                                    Alignment.CenterVertically
                                )
                            ) {
                                when (importState.type) {
                                    MainViewModel.Type.IMPORT -> MediumHeadline(text = "Importing data")
                                    MainViewModel.Type.EXPORT -> MediumHeadline(text = "Exporting data")
                                }
                                GDefiniteCircularProgressIndicator(progress = importState.progress.toFloat())
                                LargeBody(
                                    modifier = Modifier.animateContentSize(),
                                    text = importState.message
                                )
                            }
                        }

                        Status.ERROR -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(dimensionResource(id = R.dimen.DefaultPadding)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(importState.message)
                                    TextButton(onClick = {
                                        mainViewModel.updateImportStateStatus(
                                            Status.NOT_STARTED
                                        )
                                    }) {
                                        Text(text = "Skip")
                                    }
                                }
                            }
                        }

                        else -> {
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