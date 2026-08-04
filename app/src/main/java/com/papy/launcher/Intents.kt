package com.papy.launcher

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast

fun launchDialer(context: Context) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    safeStartActivity(context, intent)
}

fun launchSmsApp(context: Context) {
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_APP_MESSAGING)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    safeStartActivity(context, intent)
}

fun launchWhatsApp(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
    if (intent != null) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "WhatsApp non installé", Toast.LENGTH_SHORT).show()
    }
}

fun launchMailApp(context: Context) {
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_APP_EMAIL)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    safeStartActivity(context, intent)
}

fun performCall(context: Context, number: String) {
    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    safeStartActivity(context, intent)
}

fun launchCamera(context: Context) {
    val intent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
    } else {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    }.apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    safeStartActivity(context, intent)
}

fun launchApp(context: Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}

fun safeStartActivity(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Aucune application trouvée", Toast.LENGTH_SHORT).show()
    }
}