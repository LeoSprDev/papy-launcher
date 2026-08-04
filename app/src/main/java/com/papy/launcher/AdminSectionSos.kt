package com.papy.launcher

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AdminSectionSos(
    sosNumber: String,
    onSosNumberChange: (String) -> Unit,
    sosVisible: Boolean,
    onSosVisibleChange: (Boolean) -> Unit
) {
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
            onCheckedChange = onSosVisibleChange
        )
    }
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = sosNumber,
        onValueChange = onSosNumberChange,
        label = { Text("Numéro SOS") },
        modifier = Modifier.fillMaxWidth()
    )
}