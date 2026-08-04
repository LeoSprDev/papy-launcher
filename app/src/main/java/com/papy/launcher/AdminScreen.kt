package com.papy.launcher

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminScreen(
    onExit: () -> Unit,
    onManageFavorites: () -> Unit,
    onManageApps: () -> Unit
) {
    val context = LocalContext.current
    var sosNumber by remember { mutableStateOf(Prefs.getSosNumber(context)) }
    var sosVisible by remember { mutableStateOf(Prefs.isSosVisible(context)) }
    var newPin by remember { mutableStateOf("") }
    var kioskEnabled by remember { mutableStateOf(Prefs.isKioskEnabled(context)) }

    // États des autorisations/permissions — lus à l'affichage et après retour Settings
    var notifListenerEnabled by remember { mutableStateOf(isNotifListenerEnabled(context)) }
    var overlayEnabled by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var kioskRunning by remember { mutableStateOf(PapyKioskService.isRunning(context)) }
    var writeSettingsEnabled by remember { mutableStateOf(Settings.System.canWrite(context)) }
    var callPhoneGranted by remember { mutableStateOf(isGranted(context, Manifest.permission.CALL_PHONE)) }
    var callLogGranted by remember { mutableStateOf(isGranted(context, Manifest.permission.READ_CALL_LOG)) }
    var photosGranted by remember { mutableStateOf(isPhotosPermissionGranted(context)) }
    var contactsGranted by remember { mutableStateOf(isGranted(context, Manifest.permission.READ_CONTACTS)) }

    // Re-vérifie les états quand l'admin revient au premier plan (après un Intent Settings)
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    val lifecycleObserver = androidx.lifecycle.LifecycleEventObserver { _, event ->
        if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
            notifListenerEnabled = isNotifListenerEnabled(context)
            overlayEnabled = Settings.canDrawOverlays(context)
            kioskRunning = PapyKioskService.isRunning(context)
            writeSettingsEnabled = Settings.System.canWrite(context)
            callPhoneGranted = isGranted(context, Manifest.permission.CALL_PHONE)
            callLogGranted = isGranted(context, Manifest.permission.READ_CALL_LOG)
            photosGranted = isPhotosPermissionGranted(context)
            contactsGranted = isGranted(context, Manifest.permission.READ_CONTACTS)
        }
    }
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose { lifecycleOwner.lifecycle.removeObserver(lifecycleObserver) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Administration",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E)
        )
        Spacer(modifier = Modifier.height(24.dp))

        AdminSectionSos(
            sosNumber = sosNumber,
            onSosNumberChange = {
                sosNumber = it
                Prefs.setSosNumber(context, it)
            },
            sosVisible = sosVisible,
            onSosVisibleChange = {
                sosVisible = it
                Prefs.setSosVisible(context, it)
            }
        )
        Spacer(modifier = Modifier.height(32.dp))

        AdminSectionPin(
            newPin = newPin,
            onNewPinChange = { newPin = it },
            onSavePin = {
                if (newPin.length == 4) {
                    Prefs.setPin(context, newPin)
                    newPin = ""
                }
            }
        )
        Spacer(modifier = Modifier.height(32.dp))

        AdminSectionKiosk(
            kioskEnabled = kioskEnabled,
            kioskRunning = kioskRunning,
            onKioskToggle = {
                if (it) {
                    Prefs.setKioskEnabled(context, true)
                    PapyKioskService.enabled = true
                    kioskEnabled = true
                } else {
                    Prefs.setKioskEnabled(context, false)
                    PapyKioskService.enabled = false
                    kioskEnabled = false
                }
            }
        )
        Spacer(modifier = Modifier.height(32.dp))

        AdminSectionShortcuts(
            onShortcutToggle = { id, enabled ->
                Prefs.setShortcutEnabled(context, id, enabled)
            },
            isShortcutEnabled = { id ->
                Prefs.isShortcutEnabled(context, id)
            }
        )
        Spacer(modifier = Modifier.height(32.dp))

        AdminSectionSystemPermissions(
            notifListenerEnabled = notifListenerEnabled,
            overlayEnabled = overlayEnabled,
            kioskRunning = kioskRunning,
            writeSettingsEnabled = writeSettingsEnabled,
            isLauncherDefault = isDefaultLauncher(context),
            onOpenNotifSettings = {
                Toast.makeText(
                    context,
                    "Trouvez Papy Launcher dans la liste et activez-le",
                    Toast.LENGTH_LONG
                ).show()
                context.startActivity(
                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
            },
            onOpenOverlaySettings = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.startActivity(
                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                            data = Uri.parse("package:${context.packageName}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    )
                }
            },
            onOpenAccessibilitySettings = {
                Toast.makeText(
                    context,
                    "Trouvez Papy Launcher dans la liste et activez-le",
                    Toast.LENGTH_LONG
                ).show()
                context.startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
            },
            onOpenWriteSettings = {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
            },
            onOpenHomeSettings = {
                try {
                    context.startActivity(
                        Intent(Settings.ACTION_HOME_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    )
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Appuyez sur le bouton Home et choisissez Papy Launcher",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))

        AdminSectionAppPermissions(
            callPhoneGranted = callPhoneGranted,
            callLogGranted = callLogGranted,
            photosGranted = photosGranted,
            contactsGranted = contactsGranted,
            context = context,
            onCallPhoneResult = { callPhoneGranted = it },
            onCallLogResult = { callLogGranted = it },
            onPhotosResult = { photosGranted = it },
            onContactsResult = { contactsGranted = it }
        )
        Spacer(modifier = Modifier.height(32.dp))

        AdminSectionFavorites(
            contactsGranted = contactsGranted,
            onManageFavorites = onManageFavorites
        )
        Spacer(modifier = Modifier.height(32.dp))

        AdminSectionApps(onManageApps = onManageApps)
        Spacer(modifier = Modifier.height(32.dp))

        AdminSectionQuickSettings(
            onOpenWifi = {
                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            },
            onOpenDataUsage = {
                context.startActivity(Intent(Settings.ACTION_DATA_USAGE_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            },
            onOpenBluetooth = {
                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            },
            onOpenDisplay = {
                context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            },
            onOpenSound = {
                context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        )
        Spacer(modifier = Modifier.height(32.dp))

        AdminButton("Retour à l'écran d'accueil", onExit)
    }
}