package com.papy.launcher

import androidx.compose.runtime.Composable

@Composable
internal fun AdminSectionApps(
    onManageApps: () -> Unit
) {
    SectionTitle("Applis")

    AdminButton("Gérer les applis", onManageApps)
}