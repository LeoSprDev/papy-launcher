package com.papy.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AdminSectionShortcuts(
    onShortcutToggle: (ShortcutId, Boolean) -> Unit,
    isShortcutEnabled: (ShortcutId) -> Boolean
) {
    val context = LocalContext.current
    SectionTitle("Raccourcis affichés")

    for (sc in Shortcuts.all) {
        var enabled by remember { mutableStateOf(isShortcutEnabled(sc.id)) }
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
                    onShortcutToggle(sc.id, it)
                }
            )
        }
    }
}