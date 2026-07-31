package com.papy.launcher

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

class PapyKioskService : AccessibilityService() {

    companion object {
        const val LAUNCHER_PACKAGE = "com.papy.launcher"

        val ALLOWED_PACKAGES = setOf(
            LAUNCHER_PACKAGE,
            "com.android.dialer",
            "com.google.android.dialer",
            "com.samsung.android.dialer",
            "com.android.mms",
            "com.samsung.android.app.messaging",
            "com.google.android.apps.messaging",
            "com.android.messaging",
            "com.google.android.gm",
            "com.samsung.android.app.email",
            "com.android.email",
            "com.whatsapp",
            "com.whatsapp.w4b",
            "com.google.android.apps.photos",
            "com.samsung.android.gallery",
            "com.android.gallery",
            "com.sec.android.gallery3d",
            "com.android.camera",
            "com.sec.android.app.camera",
            "com.google.android.GoogleCamera",
            "android",
            "com.android.settings",
            "com.google.android.contacts",
            "com.samsung.android.app.contacts",
            "com.android.contacts"
        )

        var enabled = false

        fun isRunning(context: Context): Boolean {
            val flat = Settings.Secure.getString(
                context.contentResolver,
                "enabled_accessibility_services"
            ) ?: return false
            return flat.contains("com.papy.launcher/com.papy.launcher.PapyKioskService")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!enabled) return
        if (event == null) return

        val pkg = event.packageName?.toString() ?: return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (pkg == "com.android.systemui") {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    return
                }
                if (pkg != LAUNCHER_PACKAGE && pkg !in ALLOWED_PACKAGES) {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                }
            }
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> {
                if (pkg == "com.android.systemui") {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                }
            }
        }
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
    }
}