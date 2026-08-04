package com.papy.launcher

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AdminSectionFavorites(
    contactsGranted: Boolean,
    onManageFavorites: () -> Unit
) {
    SectionTitle("Favoris")

    AdminButton("Gérer les favoris", onManageFavorites)
    if (!contactsGranted) {
        Text(
            text = "Autorisez d'abord les contacts (section Permissions ci-dessous).",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}