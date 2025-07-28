package com.analyzer.smsbeta

import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_PERMISSION = 100 // Код запроса разрешений
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAndRequestPermissions()
    }

    /**
     * Метод проверяет наличие необходимых разрешений и запрашивает их,
     * если они ещё не предоставлены.
     */
    private fun checkAndRequestPermissions() {
        if (!hasAllRequiredPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_PHONE_STATE
                ),
                REQUEST_CODE_PERMISSION
            )
        }
    }

    /**
     * Возвращает true, если приложение имеет все требуемые разрешения.
     */
    private fun hasAllRequiredPermissions(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

    /**
     * Реакция на результат запроса разрешений.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Все разрешения получены успешно
                    println("Все разрешения были предоставлены.")
                } else {
                    // Некоторые или все разрешения отклонены
                    println("Отсутствуют некоторые важные разрешения.")
                }
            }
        }
    }
}