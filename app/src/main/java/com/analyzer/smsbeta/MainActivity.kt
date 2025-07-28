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

    // Регистратор для запроса всех разрешений сразу
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        permissionsGranted = allGranted
        if (allGranted) {
            checkInternet(this)
        } else {
            // Если разрешения не даны - запросить снова
            requestPermissionsLauncher.launch(requiredPermissions)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Убираем ActionBar
        supportActionBar?.hide()

        setContent {
            val context = LocalContext.current

            // Проверка разрешений при запуске
            LaunchedEffect(Unit) {
                val hasAllPermissions = requiredPermissions.all { perm ->
                    ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
                }

                if (hasAllPermissions) {
                    permissionsGranted = true
                    checkInternet(context)
                } else {
                    requestPermissionsLauncher.launch(requiredPermissions)
                }
            }

            // Основной интерфейс
            when {
                !permissionsGranted -> {} // Пустой экран во время запроса разрешений
                internetAvailable -> WebViewContent("https://example.com")
                else -> InternetRequiredMessage()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (permissionsGranted) checkInternet(this)
    }

    private fun checkInternet(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        internetAvailable = capabilities?.let {
            it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } ?: false

        showWebView = internetAvailable
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
                    override fun shouldOverrideUrlLoading(view: WebView, url: String) = false
                }
                loadUrl(url)
            }
        }
    )
}

@Composable
fun InternetRequiredMessage() {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                loadData(
                    "<html><body style='text-align:center;padding-top:40%;font-size:24px;'>" +
                            "Требуется интернет-подключение" +
                            "</body></html>",
                    "text/html",
                    "UTF-8"
                )
            }
        }
    )
}