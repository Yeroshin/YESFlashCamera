package com.yes.shared.presentation.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat



@Composable
fun PermissionsHelper(
    permissions: List<String>,
    onPermissionsResult: (granted: List<String>, denied: List<String>) -> Unit,
    rationaleMessage: String,
    requestCounter: Int
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity ?: run {
        Log.e("PermissionsHelper", "Context is not AppCompatActivity. Context: $context")
        return
    }

    // SharedPreferences для отслеживания, запрашивались ли разрешения ранее
    val sharedPrefs: SharedPreferences = remember { context.getSharedPreferences("permissions_prefs", Context.MODE_PRIVATE) }
    var hasRequestedPermissions by remember { mutableStateOf(sharedPrefs.getBoolean("hasRequestedPermissions", false)) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        Log.d("PermissionsHelper", "Launcher result: $result")
        val granted = result.filterValues { it }.keys.toList()
        val denied = result.filterValues { !it }.keys.toList()
        // После любого запроса устанавливаем флаг, что запрос был сделан (persistent через SharedPrefs)
        if (!hasRequestedPermissions) {
            sharedPrefs.edit().putBoolean("hasRequestedPermissions", true).apply()
            hasRequestedPermissions = true
        }
        onPermissionsResult(granted, denied)
    }

    var showRationaleDialog by remember { mutableStateOf(false) }
    var showBlockedDialog by remember { mutableStateOf(false) }
    var deniedPermissions by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(requestCounter) {
        Log.d("PermissionsHelper", "LaunchedEffect(requestCounter=$requestCounter, hasRequestedPermissions=$hasRequestedPermissions)")
        val allGranted = permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        Log.d("PermissionsHelper", "AllGranted: $allGranted")
        if (!allGranted) {
            val shouldShowRationale = permissions.any { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }
            Log.d("PermissionsHelper", "ShouldShowRationale: $shouldShowRationale")
            deniedPermissions = permissions.filterNot { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

            if (shouldShowRationale) {
                // Пользователь отказывал ранее (без "Не спрашивать"): показать rationale, потом запрос
                Log.d("PermissionsHelper", "Showing rationale because shouldShowRationale=true")
                showRationaleDialog = true
            } else if (hasRequestedPermissions) {
                // Не первый запрос, но shouldShowRationale=false: лицендтор, разрешения заблокированы, показать диалог с настройками
                Log.d("PermissionsHelper", "Permissions blocked, showing blocked dialog")
                showBlockedDialog = true
            } else {
                // Первый запрос (новая установка или первая попытка): сразу системный диалог без rationale
                Log.d("PermissionsHelper", "First request, launching permissions directly")
                launcher.launch(permissions.toTypedArray())
            }
        } else {
            Log.d("PermissionsHelper", "All permissions already granted, no action needed")
            onPermissionsResult(permissions, emptyList())
        }
    }

    // Rationale диалог (только после отказа без блокировки)
    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showRationaleDialog = false
                    Log.d("PermissionsHelper", "Launching permissions after rationale")
                    launcher.launch(permissions.toTypedArray())
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) {
                    Text("Отмена")
                }
            },
            title = { Text("Необходимые разрешения") },
            text = { Text(rationaleMessage) }
        )
    }

    // Диалог для заблокированных разрешений
    if (showBlockedDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showBlockedDialog = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }) {
                    Text("Перейти в настройки")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockedDialog = false }) {
                    Text("Отмена")
                }
            },
            title = { Text("Разрешения заблокированы") },
            text = {
                Text("Эти разрешения (${deniedPermissions.joinToString(", ") { it.split(".").last() }}) необходимы, но были заблокированы. Перейдите в настройки приложения, чтобы включить их вручную. Без этого функция не будет работать.")
            }
        )
    }

    // Тест-кнопка (можно убрать в продакшене, или оставить для ручного теста)
    Button(onClick = {
        Log.d("PermissionsHelper", "Manual button launch")
        launcher.launch(permissions.toTypedArray())
    }) {
        Text("Тест-запрос разрешений вручную")
    }
}

