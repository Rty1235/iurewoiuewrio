package com.analyzer.smsbeta

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    // Список необходимых разрешений
    private val requiredPermissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG
    )

    // Состояния UI
    private var showPermissionDialog by mutableStateOf(false)
    private var showInternetDialog by mutableStateOf(false)
    private var permissionsGranted by mutableStateOf(false)
    private var internetAvailable by mutableStateOf(false)

    // Регистрация запроса разрешений
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.all { it.value }
        if (!permissionsGranted) showPermissionDialog = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current

            // Проверка разрешений при запуске
            LaunchedEffect(Unit) {
                checkPermissions(context)
            }

            // Проверка интернета после получения разрешений
            LaunchedEffect(permissionsGranted) {
                if (permissionsGranted) {
                    checkInternet(context)
                }
            }

            // Главный интерфейс
            when {
                !permissionsGranted -> PermissionRequestScreen()
                !internetAvailable -> InternetRequestScreen()
                else -> WebContentScreen("https://example.com") // Ваш URL здесь
            }

            // Диалог разрешений
            if (showPermissionDialog) {
                AlertDialog(
                    onDismissRequest = { showPermissionDialog = false },
                    title = { Text("Требуются разрешения") },
                    text = { Text("Для работы приложения необходимы все разрешения") },
                    confirmButton = {
                        Button(onClick = {
                            requestPermissionLauncher.launch(requiredPermissions)
                            showPermissionDialog = false
                        }) {
                            Text("Запросить снова")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            openAppSettings(context)
                            showPermissionDialog = false
                        }) {
                            Text("Настройки")
                        }
                    }
                )
            }

            // Диалог интернета
            if (showInternetDialog) {
                AlertDialog(
                    onDismissRequest = { showInternetDialog = false },
                    title = { Text("Требуется интернет") },
                    text = { Text("Для работы приложения необходимо интернет-соединение") },
                    confirmButton = {
                        Button(onClick = {
                            openInternetSettings(context)
                            showInternetDialog = false
                        }) {
                            Text("Настройки сети")
                        }
                    }
                )
            }
        }
    }

    // Проверка разрешений
    private fun checkPermissions(context: Context) {
        permissionsGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionsGranted) {
            requestPermissionLauncher.launch(requiredPermissions)
        }
    }

    // Проверка интернета
    private fun checkInternet(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: run {
            internetAvailable = false
            showInternetDialog = true
            return
        }

        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: run {
            internetAvailable = false
            showInternetDialog = true
            return
        }

        internetAvailable = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }

        if (!internetAvailable) showInternetDialog = true
    }

    // Открытие настроек приложения
    private fun openAppSettings(context: Context) {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            context.startActivity(this)
        }
    }

    // Открытие настроек сети
    private fun openInternetSettings(context: Context) {
        Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(this)
        }
    }

    // Проверка при возвращении в приложение
    override fun onResume() {
        super.onResume()
        if (permissionsGranted) checkInternet(this)
    }
}

@Composable
fun WebContentScreen(url: String) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = object : WebViewClient() {
                    // Обработка кнопки "Назад"
                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        return false
                    }
                }
                loadUrl(url)
            }
        },
        update = { webView -> webView.loadUrl(url) }
    )
}

@Composable
fun PermissionRequestScreen() {
    Text("Запрос разрешений...")
}

@Composable
fun InternetRequestScreen() {
    Text("Проверка интернет-соединения...")
}