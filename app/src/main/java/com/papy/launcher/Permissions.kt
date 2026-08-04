package com.papy.launcher

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

internal fun isGranted(context: Context, permission: String): Boolean =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

internal fun isPhotosPermissionGranted(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        isGranted(context, Manifest.permission.READ_MEDIA_IMAGES)
    else
        isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)

internal fun isNotifListenerEnabled(context: Context): Boolean {
    val flat = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    ) ?: return false
    return flat.split(":").any { it.startsWith(context.packageName + "/") }
}

internal fun isDefaultLauncher(context: Context): Boolean {
    val roleHeld = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? android.app.role.RoleManager
        roleManager?.isRoleHeld(android.app.role.RoleManager.ROLE_HOME) == true
    } else false
    if (roleHeld) return true
    val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    val resolvers = context.packageManager.queryIntentActivities(homeIntent, 0)
    if (resolvers.size != 1) return false
    return resolvers[0].activityInfo.packageName == context.packageName
}