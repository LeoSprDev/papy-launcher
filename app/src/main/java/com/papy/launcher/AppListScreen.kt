package com.papy.launcher

import com.papy.launcher.ui.components.ScreenHeader
import com.papy.launcher.ui.theme.PapyTextDark
import com.papy.launcher.ui.theme.PapySurfaceLight
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable
)

fun loadInstalledApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    val all = pm.getInstalledApplications(PackageManager.GET_META_DATA)
    val result = mutableListOf<AppInfo>()
    for (app in all) {
        if (app.packageName == "com.papy.launcher") continue
        val intent = pm.getLaunchIntentForPackage(app.packageName) ?: continue
        result.add(
            AppInfo(
                packageName = app.packageName,
                label = pm.getApplicationLabel(app).toString(),
                icon = pm.getApplicationIcon(app)
            )
        )
    }
    return result.sortedBy { it.label.lowercase() }
}

@Composable
fun AppListScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.Default) { loadInstalledApps(context) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ScreenHeader(title = "Applications", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))

        for (app in apps) {
            AppRow(app = app) { launchApp(context, app.packageName) }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun AppRow(
    app: AppInfo,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PapySurfaceLight)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(icon = app.icon, size = 48)
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = app.label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = PapyTextDark
        )
    }
}

@Composable
fun AppIcon(icon: Drawable, size: Int) {
    AndroidView(
        factory = { ctx ->
            ImageView(ctx).apply {
                setImageDrawable(icon)
                layoutParams = android.view.ViewGroup.LayoutParams(size, size)
            }
        },
        modifier = Modifier.size(size.dp)
    )
}

fun launchApp(context: Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}