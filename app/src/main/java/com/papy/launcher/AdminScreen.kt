package com.papy.launcher

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

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

        // Section SOS
        SectionTitle("Bouton SOS")

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Afficher le bouton SOS",
                fontSize = 20.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = sosVisible,
                onCheckedChange = {
                    sosVisible = it
                    Prefs.setSosVisible(context, it)
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = sosNumber,
            onValueChange = {
                sosNumber = it
                Prefs.setSosNumber(context, it)
            },
            label = { Text("Numéro SOS") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Section PIN
        SectionTitle("Changer le code admin")

        OutlinedTextField(
            value = newPin,
            onValueChange = {
                if (it.all { c -> c.isDigit() } && it.length <= 4) {
                    newPin = it
                }
            },
            label = { Text("Nouveau code (4 chiffres)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        AdminButton("Enregistrer le code") {
            if (newPin.length == 4) {
                Prefs.setPin(context, newPin)
                newPin = ""
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section Kiosque
        SectionTitle("Mode kiosque")

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bloquer la sortie du launcher",
                fontSize = 20.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = kioskEnabled,
                onCheckedChange = {
                    if (it) {
                        if (!PapyKioskService.isRunning(context)) {
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
                        } else {
                            Prefs.setKioskEnabled(context, true)
                            PapyKioskService.enabled = true
                            kioskEnabled = true
                        }
                    } else {
                        Prefs.setKioskEnabled(context, false)
                        PapyKioskService.enabled = false
                        kioskEnabled = false
                    }
                }
            )
        }
        Text(
            text = "Empêche de quitter le launcher par erreur (swipe, récents, etc.). À activer une fois dans les paramètres d'accessibilité.",
            fontSize = 16.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(top = 8.dp)
        )
        if (kioskEnabled && !kioskRunning) {
            Text(
                text = "⚠ À activer dans les paramètres d'accessibilité (Papy Launcher n'est pas encore actif)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section Raccourcis
        SectionTitle("Raccourcis affichés")

        for (sc in Shortcuts.all) {
            var enabled by remember { mutableStateOf(Prefs.isShortcutEnabled(context, sc.id)) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(sc.color)
                ) {}
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(sc.labelRes),
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                        Prefs.setShortcutEnabled(context, sc.id, it)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section Autorisations système (à activer manuellement une fois)
        SectionTitle("Autorisations système")

        AdminLinkButton(
            label = "Accès aux notifications",
            active = notifListenerEnabled,
            hint = "Pastilles SMS, mail, WhatsApp"
        ) {
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
        }
        Spacer(modifier = Modifier.height(8.dp))

        AdminLinkButton(
            label = "Affichage au-dessus des autres applis",
            active = overlayEnabled,
            hint = "Bouton Accueil flottant"
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        AdminLinkButton(
            label = "Service d'accessibilité (kiosque)",
            active = kioskRunning,
            hint = "Empêche de quitter le launcher"
        ) {
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
        }
        Spacer(modifier = Modifier.height(8.dp))

        AdminLinkButton(
            label = "Modification des réglages",
            active = writeSettingsEnabled,
            hint = "Slider luminosité"
        ) {
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            AdminLinkButton(
                label = "Launcher par défaut",
                active = isDefaultLauncher(context),
                hint = "Papy Launcher s'ouvre au bouton Home"
            ) {
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
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section Permissions appli (boîte système / page d'infos)
        SectionTitle("Permissions de l'application")

        PermissionButton(
            label = "Appels téléphoniques (SOS)",
            granted = callPhoneGranted,
            permission = Manifest.permission.CALL_PHONE,
            context = context,
            onResult = { callPhoneGranted = it }
        )
        Spacer(modifier = Modifier.height(8.dp))

        PermissionButton(
            label = "Journal d'appels (appels manqués)",
            granted = callLogGranted,
            permission = Manifest.permission.READ_CALL_LOG,
            context = context,
            onResult = { callLogGranted = it }
        )
        Spacer(modifier = Modifier.height(8.dp))

        PermissionButton(
            label = "Photos (visionneur)",
            granted = photosGranted,
            permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_IMAGES
            else
                Manifest.permission.READ_EXTERNAL_STORAGE,
            context = context,
            onResult = { photosGranted = it }
        )
        Spacer(modifier = Modifier.height(8.dp))

        PermissionButton(
            label = "Contacts (favoris)",
            granted = contactsGranted,
            permission = Manifest.permission.READ_CONTACTS,
            context = context,
            onResult = { contactsGranted = it }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Section Favoris (bouton vers écran dédié)
        SectionTitle("Favoris")

        AdminButton("Gérer les favoris") {
            onManageFavorites()
        }
        if (!contactsGranted) {
            Text(
                text = "Autorisez d'abord les contacts (section Permissions ci-dessous).",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        SectionTitle("Applis")

        AdminButton("Gérer les applis") {
            onManageApps()
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section Réglages rapides
        SectionTitle("Réglages rapides")

        AdminButton("Réglages Wi-Fi") {
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
        Spacer(modifier = Modifier.height(8.dp))

        AdminButton("Réglages données mobiles") {
            context.startActivity(Intent(Settings.ACTION_DATA_USAGE_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
        Spacer(modifier = Modifier.height(8.dp))

        AdminButton("Réglages Bluetooth") {
            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
        Spacer(modifier = Modifier.height(8.dp))

        AdminButton("Réglages affichage") {
            context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
        Spacer(modifier = Modifier.height(8.dp))

        AdminButton("Réglages son") {
            context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Slider de luminosité
        Text(
            text = "Luminosité",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        var brightness by remember {
            mutableFloatStateOf(
                try {
                    Settings.System.getInt(
                        context.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS
                    ).toFloat()
                } catch (e: Exception) {
                    128f
                }
            )
        }
        Slider(
            value = brightness,
            onValueChange = { brightness = it },
            valueRange = 0f..255f,
            onValueChangeFinished = {
                if (Settings.System.canWrite(context)) {
                    Settings.System.putInt(
                        context.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS,
                        brightness.toInt()
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Pour modifier la luminosité, activez « Modification des réglages » dans la section Autorisations",
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(32.dp))

        AdminButton("Retour à l'écran d'accueil") {
            onExit()
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF333333),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    )
}

@Composable
fun AdminButton(
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A237E))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun AdminLinkButton(
    label: String,
    active: Boolean,
    hint: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) Color(0xFF1A237E) else Color(0xFF1A237E))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (active) Color(0xFF2E7D32) else Color(0xFFCCCCCC)),
            contentAlignment = Alignment.Center
        ) {
            if (active) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = hint,
                fontSize = 14.sp,
                color = Color(0xFFCCCCCC)
            )
        }
        Text(
            text = if (active) "Activé" else "À activer",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) Color(0xFF81C784) else Color(0xFFFFCDD2)
        )
    }
}

@Composable
fun PermissionButton(
    label: String,
    granted: Boolean,
    permission: String,
    context: Context,
    onResult: (Boolean) -> Unit
) {
    val activity = context as? Activity
    val activityResult = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { g ->
        onResult(g)
        if (g) Toast.makeText(context, "Permission accordée", Toast.LENGTH_SHORT).show()
        else Toast.makeText(context, "Permission refusée — ouvrez les réglages de l'app", Toast.LENGTH_LONG).show()
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A237E))
            .clickable {
                if (granted) {
                    Toast.makeText(
                        context,
                        "Onglet « Permissions » → désactivez « $label »",
                        Toast.LENGTH_LONG
                    ).show()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    Toast.makeText(
                        context,
                        "Onglet « Permissions » → activez « $label »",
                        Toast.LENGTH_LONG
                    ).show()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } else {
                    val canRequest = activity?.let {
                        !it.shouldShowRequestPermissionRationale(permission)
                    } != false
                    if (canRequest) {
                        activityResult.launch(permission)
                    } else {
                        Toast.makeText(
                            context,
                            "Onglet « Permissions » → activez « $label »",
                            Toast.LENGTH_LONG
                        ).show()
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                }
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (granted) Color(0xFF2E7D32) else Color(0xFFCCCCCC)),
            contentAlignment = Alignment.Center
        ) {
            if (granted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (granted) "Accordée" else "À accorder",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (granted) Color(0xFF81C784) else Color(0xFFFFCDD2)
        )
    }
}

private fun isGranted(context: Context, permission: String): Boolean =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

private fun isPhotosPermissionGranted(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        isGranted(context, Manifest.permission.READ_MEDIA_IMAGES)
    else
        isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)

private fun isNotifListenerEnabled(context: Context): Boolean {
    val flat = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    ) ?: return false
    return flat.split(":").any { it.startsWith(context.packageName + "/") }
}

private fun isDefaultLauncher(context: Context): Boolean {
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