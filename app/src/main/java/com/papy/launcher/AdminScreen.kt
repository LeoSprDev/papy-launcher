package com.papy.launcher

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminScreen(
    onExit: () -> Unit
) {
    val context = LocalContext.current
    var sosNumber by remember { mutableStateOf(Prefs.getSosNumber(context)) }
    var sosVisible by remember { mutableStateOf(Prefs.isSosVisible(context)) }
    var newPin by remember { mutableStateOf("") }
    var kioskEnabled by remember { mutableStateOf(Prefs.isKioskEnabled(context)) }

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
            text = "Pour modifier la luminosité, autorisez d'abord l'écriture des réglages",
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )
        if (!Settings.System.canWrite(context)) {
            Spacer(modifier = Modifier.height(4.dp))
            AdminButton("Autoriser la modification des réglages") {
                context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        }

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