package com.example

import android.app.Application

class TaskFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Disable automatic FCM token retrieval and topic sync on startup before FirebaseApp auto-init
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
    }
}
