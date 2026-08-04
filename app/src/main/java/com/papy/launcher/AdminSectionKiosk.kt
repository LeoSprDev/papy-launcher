package com.papy.launcher

import com.papy.launcher.ui.theme.PapyRed
import com.papy.launcher.ui.theme.PapyTextGray
import android.widget.Toast
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AdminSectionKiosk(
    kioskEnabled: Boolean,
    kioskRunning: Boolean,
    onKioskToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
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
                        onKioskToggle(true)
                    }
                } else {
                    onKioskToggle(false)
                }
            }
        )
    }
    Text(
        text = "Empêche de quitter le launcher par erreur (swipe, récents, etc.). À activer une fois dans les paramètres d'accessibilité.",
        fontSize = 16.sp,
        color = PapyTextGray,
        modifier = Modifier.padding(top = 8.dp)
    )
    if (kioskEnabled && !kioskRunning) {
        Text(
            text = "⚠ À activer dans les paramètres d'accessibilité (Papy Launcher n'est pas encore actif)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = PapyRed,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}