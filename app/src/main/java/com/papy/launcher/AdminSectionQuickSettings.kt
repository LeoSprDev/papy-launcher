package com.papy.launcher

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AdminSectionQuickSettings(
    onOpenWifi: () -> Unit,
    onOpenDataUsage: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onOpenDisplay: () -> Unit,
    onOpenSound: () -> Unit
) {
    val context = LocalContext.current
    SectionTitle("Réglages rapides")

    AdminButton("Réglages Wi-Fi", onOpenWifi)
    Spacer(modifier = Modifier.height(8.dp))

    AdminButton("Réglages données mobiles", onOpenDataUsage)
    Spacer(modifier = Modifier.height(8.dp))

    AdminButton("Réglages Bluetooth", onOpenBluetooth)
    Spacer(modifier = Modifier.height(8.dp))

    AdminButton("Réglages affichage", onOpenDisplay)
    Spacer(modifier = Modifier.height(8.dp))

    AdminButton("Réglages son", onOpenSound)
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
}