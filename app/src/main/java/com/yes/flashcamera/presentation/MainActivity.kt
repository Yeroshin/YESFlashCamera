package com.yes.flashcamera.presentation

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.yes.camera.presentation.contract.CameraContract
import com.yes.camera.presentation.ui.CameraScreen
import com.yes.camera.presentation.vm.CameraViewModel
import com.yes.flashcamera.presentation.ui.theme.FlashCameraTheme
import com.yes.settings.presentation.contract.SettingsContract
import com.yes.settings.presentation.ui.SettingsScreen
import com.yes.settings.presentation.wm.SettingsViewModel
import com.yes.billing.presentation.contract.BillingContract
import com.yes.billing.presentation.ui.BillingScreen
import com.yes.billing.presentation.vm.BillingViewModel
import com.yes.shared.presentation.ui.theme.AppColors

private const val PERMISSIONS_REQUEST_CODE = 10

class MainActivity : ComponentActivity() {

    private val mBackgroundThread: HandlerThread = HandlerThread("CameraThread").apply { start() }
    private val mBackgroundHandler: Handler = Handler(mBackgroundThread.looper)

    private val cameraViewModel: CameraViewModel by viewModels {
        (application as CameraViewModel.DependencyResolver)
            .resolveCameraDependency()
        .viewModelFactory
    }
    private val settingsViewModel: SettingsViewModel by viewModels {
        (application as SettingsViewModel.DependencyResolver)
            .resolveSettingsDependency()
            .viewModelFactory
    }
    private val billingViewModel: BillingViewModel by viewModels {
        (application as BillingViewModel.DependencyResolver)
            .resolveBillingDependency()
        .viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            cameraViewModel.uiState.value.state !is CameraContract.CameraState.Success
        }

        setContent {
            val settingsState by settingsViewModel.uiState.collectAsState()
            val billingState by billingViewModel.uiState.collectAsState()
            val isSubscribed = (billingState.billingState as? BillingContract.BillingState.Success)?.billing?.isActiveSubscription == true

            val themeValue = (settingsState.state as? SettingsContract.SettingsState.Success)?.settings?.themeValue ?: "Dark (Default)"
            val appColors = when (themeValue) {
                "Light", "Светлая" -> AppColors.light()
                "AMOLED Black", "AMOLED Чёрная" -> AppColors.amoled()
                "High Contrast", "Высокий контраст" -> AppColors.highContrast()
                else -> AppColors.dark()
            }

            FlashCameraTheme(colors = appColors) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "Camera") {
                    composable("Camera",
                        enterTransition = {
                            scaleIn(animationSpec = tween(300), transformOrigin = TransformOrigin(0.5f, 0.5f)) + fadeIn(tween(300))
                        },
                        exitTransition = {
                            scaleOut(animationSpec = tween(300), transformOrigin = TransformOrigin(0.5f, 0.5f)) + fadeOut(tween(300))
                        },
                        popEnterTransition = {
                            scaleIn(animationSpec = tween(300), transformOrigin = TransformOrigin(0.5f, 0.5f)) + fadeIn(tween(300))
                        },
                        popExitTransition = {
                            scaleOut(animationSpec = tween(300), transformOrigin = TransformOrigin(0.5f, 0.5f)) + fadeOut(tween(300))
                        }

                    ) {
                        CameraScreen(
                            cameraViewModel = cameraViewModel,
                            isSubscribed = isSubscribed,
                            onBillingClick = {
                                navController.navigate("Billing")
                            },
                            onSettingsClick = {
                                navController.navigate("Settings")
                            }
                        )
                    }
                    composable("Settings",
                        enterTransition = {
                            scaleIn(animationSpec = tween(300), transformOrigin = TransformOrigin(0.5f, 0.5f)) + fadeIn(tween(300))
                        },
                        exitTransition = {
                            scaleOut(animationSpec = tween(300), transformOrigin = TransformOrigin(0.5f, 0.5f)) + fadeOut(tween(300))
                        },
                        popEnterTransition = {
                            scaleIn(animationSpec = tween(300), transformOrigin = TransformOrigin(0.5f, 0.5f)) + fadeIn(tween(300))
                        },
                        popExitTransition = {
                            scaleOut(animationSpec = tween(300), transformOrigin = TransformOrigin(0.5f, 0.5f)) + fadeOut(tween(300))
                        }
                    ) {
                        SettingsScreen(
                            settingsViewModel = settingsViewModel,
                            isSubscribed = isSubscribed,
                            onBillingClick = {
                                navController.navigate("Billing")
                            },
                            onBackClick = {
                                navController.navigate("Camera")
                            }
                        )
                    }
                    composable("Billing",
                        enterTransition = {
                            scaleIn(animationSpec = tween(300), transformOrigin = TransformOrigin(0.5f, 0.5f)) + fadeIn(tween(300))
                        },
                        exitTransition = {
                            scaleOut(animationSpec = tween(300), transformOrigin = TransformOrigin(0.5f, 0.5f)) + fadeOut(tween(300))
                        },
                        popEnterTransition = {
                            scaleIn(animationSpec = tween(300), transformOrigin = TransformOrigin(0.5f, 0.5f)) + fadeIn(tween(300))
                        },
                        popExitTransition = {
                            scaleOut(animationSpec = tween(300), transformOrigin = TransformOrigin(0.5f, 0.5f)) + fadeOut(tween(300))
                        }
                    ) {
                        BillingScreen(
                            billingViewModel = billingViewModel,
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
