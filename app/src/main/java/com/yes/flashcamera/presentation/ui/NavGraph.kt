package com.yes.flashcamera.presentation.ui

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun Nav(
    context: Context,
    navController: NavHostController
) {

    NavHost(navController = navController, startDestination = "Camera") {
        composable("Camera") {
            CameraScreen(
                context,
                onButtonClick = {
                    navController.navigate("Settings")
                }
            )
        }
        composable("Settings") {
            SettingsScreen()
        }


    }
}
