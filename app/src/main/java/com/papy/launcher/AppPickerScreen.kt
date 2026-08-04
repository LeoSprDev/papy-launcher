package com.papy.launcher

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.papy.launcher.ui.components.ScreenHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppPickerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var existingApps by remember { mutableStateOf<Set<String>>(emptySet()) }
    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.Default) { loadInstalledApps(context) }
        existingApps = Prefs.getDynamicApps(context).map { it.packageName }.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ScreenHeader(title = "Ajouter une appli", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))

        for (app in apps) {
            AppRow(app = app) {
                if (existingApps.contains(app.packageName)) {
                    Toast.makeText(context, "Déjà ajoutée", Toast.LENGTH_SHORT).show()
                    onBack()
                } else {
                    Prefs.addDynamicApp(context, app.packageName, app.label)
                    Toast.makeText(context, "${app.label} ajoutée", Toast.LENGTH_SHORT).show()
                    onBack()
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}