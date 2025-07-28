package com.analyzer.smsbeta

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.Settings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val requiredPermissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    )

    private var permissionsGranted by mutableStateOf(false)
    private var internetAvailable by mutableStateOf(false)
    private var showWebView by mutableStateOf(false)

    // Регистратор для запроса разрешений
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Повторный запрос ТОГО ЖЕ разрешения при отказе
            requestPermissionLauncher.launch(requiredPermissions[currentPermissionIndex])
        } else if (currentPermissionIndex < requiredPermissions.size - 1) {
            // Переход к следующему разрешению
            currentPermissionIndex++
            requestPermissionLauncher.launch(requiredPermissions[currentPermissionIndex])
        } else {
            // Все разрешения получены
            permissionsGranted = true
            checkInternet(this)
        }
    }

    private var currentPermissionIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Убираем ActionBar (шторку)
        actionBar?.hide()

        setContent {
            val context = LocalContext.current

            // Фон приложения
            Surface(modifier = Modifier.fillMaxSize()) {
                // Проверка разрешений при запуске
                LaunchedEffect(Unit) {
                    if (requiredPermissions.all { perm ->
                            ContextCompat.checkSelfPermission(
                                context,
                                perm
                            ) == PackageManager.PERMISSION_GRANTED
                        }) {
                        permissionsGranted = true
                        checkInternet(context)
                    } else {
                        requestPermissionLauncher.launch(requiredPermissions[currentPermissionIndex])
                    }
                }

                // Отображение веб-вью
                if (showWebView) {
                    WebViewContent("https://example.com")
                }

                // Окно отсутствия интернета
                if (!internetAvailable && permissionsGranted) {
                    InternetRequiredMessage()
                }
            }
        }
    }

    private fun checkInternet(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        internetAvailable = capabilities?.let {
            it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } ?: false

        showWebView = internetAvailable && permissionsGranted
    }
}

@Composable
fun WebViewContent(url: String) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
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
        }
    )
}

@Composable
fun InternetRequiredMessage() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = android.graphics.Color.WHITE
    ) {
        androidx.compose.material.Text(
            text = "Требуется интернет-подключение",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}