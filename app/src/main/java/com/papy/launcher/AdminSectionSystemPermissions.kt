package com.papy.launcher

import android.os.Build
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun AdminSectionSystemPermissions(
    notifListenerEnabled: Boolean,
    overlayEnabled: Boolean,
    kioskRunning: Boolean,
    writeSettingsEnabled: Boolean,
    isLauncherDefault: Boolean,
    onOpenNotifSettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenWriteSettings: () -> Unit,
    onOpenHomeSettings: () -> Unit
) {
    SectionTitle("Autorisations système")

    AdminLinkButton(
        label = "Accès aux notifications",
        active = notifListenerEnabled,
        hint = "Pastilles SMS, mail, WhatsApp",
        onClick = onOpenNotifSettings
    )
    Spacer(modifier = Modifier.height(8.dp))

    AdminLinkButton(
        label = "Affichage au-dessus des autres applis",
        active = overlayEnabled,
        hint = "Bouton Accueil flottant",
        onClick = onOpenOverlaySettings
    )
    Spacer(modifier = Modifier.height(8.dp))

    AdminLinkButton(
        label = "Service d'accessibilité (kiosque)",
        active = kioskRunning,
        hint = "Empêche de quitter le launcher",
        onClick = onOpenAccessibilitySettings
    )
    Spacer(modifier = Modifier.height(8.dp))

    AdminLinkButton(
        label = "Modification des réglages",
        active = writeSettingsEnabled,
        hint = "Slider luminosité",
        onClick = onOpenWriteSettings
    )
    Spacer(modifier = Modifier.height(8.dp))

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        AdminLinkButton(
            label = "Launcher par défaut",
            active = isLauncherDefault,
            hint = "Papy Launcher s'ouvre au bouton Home",
            onClick = onOpenHomeSettings
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}