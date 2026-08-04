package com.papy.launcher

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun AdminSectionPin(
    newPin: String,
    onNewPinChange: (String) -> Unit,
    onSavePin: () -> Unit
) {
    SectionTitle("Changer le code admin")

    OutlinedTextField(
        value = newPin,
        onValueChange = {
            if (it.all { c -> c.isDigit() } && it.length <= 4) {
                onNewPinChange(it)
            }
        },
        label = { Text("Nouveau code (4 chiffres)") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(12.dp))

    AdminButton("Enregistrer le code", onSavePin)
}