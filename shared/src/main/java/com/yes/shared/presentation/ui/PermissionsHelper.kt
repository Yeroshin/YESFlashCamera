package com.yes.shared.presentation.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext

@Composable
fun PermissionManager(
    permissions: Array<String>,  // Подайте разрешения, например, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
    onPermissionsGranted: @Composable () -> Unit,
    onPermissionsDenied: @Composable (isPermanent: Boolean) -> Unit = { isPermanent ->
        if (isPermanent) {
            Text("Permissions denied permanently. Go to settings to enable them.")
        } else {
            Text("Permissions Denied")
        }
    }
) {
    val context = LocalContext.current
    val arePermissionsGranted = remember { mutableStateOf(false) }
    val requestAttempts = remember { mutableStateOf(0) }  // Счётчик попыток запроса (начинается с 0)

    // Функция для проверки разрешений
    fun checkPermissions(): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Вычисление hasRationale (важно для определения состояния)
    val activity = context as? Activity
    val hasRationale = activity?.let { act ->
        permissions.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(act, permission)
        }
    } ?: false

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { results ->
            val allGranted = results.values.all { it }
            if (allGranted) {
                arePermissionsGranted.value = true
            } else {
                requestAttempts.value += 1  // Увеличиваем счётчик при отказе
                Toast.makeText(context, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // При старте: проверка и первый запрос
    LaunchedEffect(Unit) {
        arePermissionsGranted.value = checkPermissions()
        if (!arePermissionsGranted.value && requestAttempts.value == 0) {
            // Первый запрос
            launcher.launch(permissions)
        }
    }

    Column {
        if (!arePermissionsGranted.value) {
            val isPermanentDeny = requestAttempts.value >= 2  // После двух попыток — permanent
            onPermissionsDenied(isPermanentDeny)

            if (isPermanentDeny) {
                // Кнопка для перехода в настройки
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                    context.startActivity(intent)
                }) {
                    Text("Go to Settings")
                }
            } else {
                // После первого отказа: rationale и кнопка для повторного запроса
                Text(
                    "These permissions are needed for the app to function properly. " +
                            "Please grant them when prompted."
                )
                Button(onClick = { launcher.launch(permissions) }) {
                    Text("Request Permissions")
                }
            }
        } else {
            onPermissionsGranted()
        }
    }
}




