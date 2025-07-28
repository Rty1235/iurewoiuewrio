package com.analyzer.smsbeta

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    // Только необходимые разрешения для SMS
    private val requiredPermissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    )

    private var permissionsGranted by mutableStateOf(false)
    private var internetAvailable by mutableStateOf(false)
    private var showWebView by mutableStateOf(false)
    private var showInternetDialog by mutableStateOf(false)

    // Для запроса одного конкретного разрешения
    private var currentPermissionIndex by mutableStateOf(0)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (currentPermissionIndex < requiredPermissions.size - 1) {
                currentPermissionIndex++
                requestPermissionLauncher.launch(requiredPermissions[currentPermissionIndex])
            } else {
                permissionsGranted = true
            }
        } else {
            // Повторно запрашиваем то же самое разрешение
            requestPermissionLauncher.launch(requiredPermissions[currentPermissionIndex])
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current

            // Запуск проверки разрешений
            LaunchedEffect(Unit) {
                if (requiredPermissions.all {
                        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                    }) {
                    permissionsGranted = true
                } else {
                    requestPermissionLauncher.launch(requiredPermissions[currentPermissionIndex])
                }
            }

            // Проверка интернета при получении разрешений
            LaunchedEffect(permissionsGranted) {
                if (permissionsGranted) {
                    checkInternet(context)
                }
            }

            // Показ веб-страницы при выполнении условий
            if (showWebView) {
                WebViewContent("https://example.com") // Замените на ваш URL
            }

            // Показ диалога интернета
            if (showInternetDialog) {
                InternetRequiredDialog {
                    openInternetSettings(context)
                    showInternetDialog = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Повторная проверка интернета при возвращении в приложение
        if (permissionsGranted && !internetAvailable) {
            checkInternet(this)
        }
    }

    private fun checkInternet(context: Context) {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        internetAvailable = capabilities?.run {
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } ?: false

        if (internetAvailable) {
            showWebView = true
            showInternetDialog = false
        } else {
            showInternetDialog = true
            showWebView = false
        }
    }

    private fun openInternetSettings(context: Context) {
        Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(this)
        }
    }
}

@Composable
fun WebViewContent(url: String) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        url: String
                    ): Boolean {
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
fun InternetRequiredDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Требуется интернет") },
        text = { Text("Для работы приложения необходимо подключение к интернету") },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}