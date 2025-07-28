package com.analyzer.smsbeta

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : ComponentActivity() {

    private val requiredPermissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    )

    private var permissionsGranted by mutableStateOf(false)
    private var internetAvailable by mutableStateOf(false)

    // Регистратор для запроса разрешений
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            permissionsGranted = true
            checkInternet(this)
        } else {
            requestPermissionsLauncher.launch(requiredPermissions)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Убираем ActionBar и делаем полноэкранный режим
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        WindowCompat.setDecorFitsSystemWindows(window, false)

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
            if (permissionsGranted && internetAvailable) {
                WebViewContent("https://example.com")
            } else if (permissionsGranted) {
                InternetRequiredMessage()
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
    }
}

@Composable
fun WebViewContent(url: String) {
    AndroidWebView(url)
}

@Composable
fun InternetRequiredMessage() {
    AndroidTextView()
}

// Нативные Android View без Compose
fun ComponentActivity.AndroidWebView(url: String) {
    val webView = WebView(this).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        settings.javaScriptEnabled = true
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String) = false
        }
        loadUrl(url)
    }

    setContentView(webView)
}

fun ComponentActivity.AndroidTextView() {
    val textView = TextView(this).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        text = "Требуется интернет-подключение"
        setTextColor(Color.BLACK)
        textSize = 24f
        gravity = android.view.Gravity.CENTER
    }

    setContentView(textView)
}