package com.analyzer.smsbeta

// 1. MainActivity.kt
// MainActivity.kt
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.ANSWER_PHONE_CALLS
        )
        private const val PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Настройка WebView
        val webView: WebView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://google.com") // Замените на нужный URL

        // Проверка разрешений
        checkPermissions()
    }

    private fun checkPermissions() {
        val missingPermissions = mutableListOf<String>()

        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission)
            }
        }

        if (missingPermissions.isNotEmpty()) {
            // Запрашиваем только недостающие разрешения
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            handlePermissionResult(permissions, grantResults)
        }
    }

    private fun handlePermissionResult(
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val deniedPermissions = mutableListOf<String>()
        var showRationale = false

        for (i in permissions.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permissions[i])

                // Проверяем, стоит ли флаг "больше не спрашивать"
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    showRationale = true
                }
            }
        }

        if (deniedPermissions.isEmpty()) {
            // Все разрешения предоставлены
            return
        }

        if (showRationale) {
            // Пользователь выбрал "больше не спрашивать"
            showSettingsDialog()
        } else {
            // Повторно запрашиваем только отклоненные разрешения
            showRetryDialog(deniedPermissions)
        }
    }

    private fun showRetryDialog(permissions: List<String>) {
        AlertDialog.Builder(this)
            .setTitle("Требуются разрешения")
            .setMessage("Для работы приложения необходимы следующие разрешения: ${permissions.joinToString(", ")}")
            .setPositiveButton("Повторить") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    permissions.toTypedArray(),
                    PERMISSION_REQUEST_CODE
                )
            }
            .setCancelable(false)
            .show()
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Требуются разрешения")
            .setMessage("Вы отклонили некоторые разрешения с отметкой \"больше не спрашивать\". Пожалуйста, предоставьте их вручную в настройках.")
            .setPositiveButton("Настройки") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("Выйти") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // При возвращении из настроек проверяем разрешения снова
        checkPermissions()
    }
}