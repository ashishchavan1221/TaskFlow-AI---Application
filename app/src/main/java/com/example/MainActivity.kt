package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.ui.MainAppViewModel

class MainActivity : FragmentActivity() {
    private val viewModel: MainAppViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.sendWelcomeNotification()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Safely disable automatic FCM token retrieval on emulator startup via reflection
        try {
            val cls = Class.forName("com.google.firebase.messaging.FirebaseMessaging")
            val getInstance = cls.getMethod("getInstance")
            val instance = getInstance.invoke(null)
            val setAutoInit = cls.getMethod("setAutoInitEnabled", Boolean::class.javaPrimitiveType ?: java.lang.Boolean.TYPE)
            setAutoInit.invoke(instance, false)
        } catch (_: Exception) {}

        try {
            val iidCls = Class.forName("com.google.firebase.iid.FirebaseInstanceId")
            val getInstance = iidCls.getMethod("getInstance")
            val instance = getInstance.invoke(null)
            val setAutoInit = iidCls.getMethod("setAutoInitEnabled", Boolean::class.javaPrimitiveType ?: java.lang.Boolean.TYPE)
            setAutoInit.invoke(instance, false)
        } catch (_: Exception) {}

        // Notification runtime permission for Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.sendWelcomeNotification()
            }
        } else {
            viewModel.sendWelcomeNotification()
        }

        // Enforce startup security check via BiometricPrompt with 4-digit PIN fallback
        viewModel.secureSessionManager.triggerStartupSecurityCheck(this)

        setContent {
            MainAppContent(viewModel)
        }
    }
}
