package com.papy.launcher

import com.papy.launcher.ui.components.ScreenHeader
import com.papy.launcher.ui.theme.PapyRed
import com.papy.launcher.ui.theme.PapySurfaceLight
import com.papy.launcher.ui.theme.PapySurfaceMuted
import com.papy.launcher.ui.theme.PapyTextDark
import com.papy.launcher.ui.theme.PapyTextGray
import com.papy.launcher.ui.theme.PapyTextLight
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun ManageAppsScreen(
    onBack: () -> Unit,
    onAddApp: () -> Unit
) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf(Prefs.getDynamicApps(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            apps = Prefs.getDynamicApps(context)
        }
    }
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose { lifecycleOwner.lifecycle.removeObserver(lifecycleObserver) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ScreenHeader(title = "Gérer les applis", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))

        AdminButton("Ajouter une appli") {
            onAddApp()
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (apps.isEmpty()) {
            Text(
                text = "Aucune appli. Touchez « Ajouter une appli ».",
                fontSize = 18.sp,
                color = PapyTextGray
            )
        } else {
            for (app in apps) {
                val pm = context.packageManager
                val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                val isInstalled = launchIntent != null
                val appLabel = if (isInstalled) {
                    try {
                        pm.getApplicationLabel(pm.getApplicationInfo(app.packageName, 0)).toString()
                    } catch (e: Exception) {
                        app.label
                    }
                } else {
                    app.label
                }
                val appIcon = if (isInstalled) {
                    try {
                        pm.getApplicationIcon(app.packageName)
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PapySurfaceLight)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (appIcon != null) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PapySurfaceMuted),
                            contentAlignment = Alignment.Center
                        ) {
                            AppIcon(icon = appIcon, size = 32)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PapySurfaceMuted)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        if (isInstalled) {
                            Text(
                                text = appLabel,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = PapyTextDark
                            )
                        } else {
                            Text(
                                text = "$appLabel (désinstallée)",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = PapyRed
                            )
                        }
                        Text(
                            text = app.packageName,
                            fontSize = 16.sp,
                            color = PapyTextLight
                        )
                    }
                    Text(
                        text = "Retirer",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PapyRed,
                        modifier = Modifier.clickable {
                            Prefs.removeDynamicApp(context, app.packageName)
                            apps = Prefs.getDynamicApps(context)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}