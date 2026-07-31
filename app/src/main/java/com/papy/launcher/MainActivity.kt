package com.papy.launcher

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.papy.launcher.ui.theme.PapyLauncherTheme

class MainActivity : ComponentActivity() {
    private var pendingSosNumber: String? = null

    private val callPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingSosNumber?.let { performCall(this, it) }
        } else {
            Toast.makeText(this, "Permission d'appel refusée", Toast.LENGTH_SHORT).show()
        }
        pendingSosNumber = null
    }

    private val multiPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        MissedCalls.refresh(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestEssentialPermissions()
        PapyKioskService.enabled = Prefs.isKioskEnabled(this) && PapyKioskService.isRunning(this)
        hideSystemBars()
        setContent {
            PapyLauncherTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    AppNavigation()
                }
            }
        }
    }

    private fun requestEssentialPermissions() {
        val needed = mutableListOf(Manifest.permission.READ_CALL_LOG)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            needed.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            needed.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        val toRequest = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (toRequest.isNotEmpty()) {
            multiPermissionLauncher.launch(toRequest.toTypedArray())
        }
    }

    override fun onResume() {
        super.onResume()
        stopHomeButtonService()
    }

    override fun onPause() {
        super.onPause()
        if (Prefs.isHomeButtonEnabled(this)) {
            startHomeButtonService()
        }
    }

    private fun startHomeButtonService() {
        val intent = Intent(this, HomeButtonService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopHomeButtonService() {
        stopService(Intent(this, HomeButtonService::class.java))
    }

    private fun hideSystemBars() {
        if (Prefs.isKioskEnabled(this)) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                    or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && Prefs.isKioskEnabled(this)) {
            hideSystemBars()
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun AppNavigation() {
        var screen by remember { mutableStateOf("home") }
        when (screen) {
            "home" -> HomeScreen(
                onAdminTrigger = { screen = "pin" },
                onApplis = { screen = "applist" },
                onPhotos = { screen = "photos" }
            )
            "pin" -> PinScreen(
                onSuccess = { screen = "admin" },
                onCancel = { screen = "home" }
            )
            "admin" -> AdminScreen(
                onExit = { screen = "home" }
            )
            "applist" -> AppListScreen(
                onBack = { screen = "home" }
            )
            "photos" -> PhotosScreen(
                onBack = { screen = "home" }
            )
        }
    }

    fun requestSosCall(number: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            performCall(this, number)
        } else {
            pendingSosNumber = number
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onAdminTrigger: () -> Unit,
    onApplis: () -> Unit,
    onPhotos: () -> Unit
) {
    val context = LocalContext.current

    // Rafraîchit les appels manqués à chaque affichage de l'écran
    MissedCalls.refresh(context)
    BadgeStore.refreshAll()

    // Collecte les compteurs en temps réel
    val missed by MissedCalls.count.collectAsState()
    val smsBadge by BadgeStore.sms.collectAsState()
    val mailBadge by BadgeStore.mail.collectAsState()
    val waBadge by BadgeStore.whatsapp.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        ClockHeader(
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = { onAdminTrigger() }
                )
        )

        // Grille dynamique construite à partir des raccourcis activés
        val enabledShortcuts = remember { Prefs.getEnabledShortcuts(context) }
        val rows = enabledShortcuts.chunked(2)
        for (rowShortcuts in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (sc in rowShortcuts) {
                    val badge = when (sc.id) {
                        ShortcutId.APPELS -> missed
                        ShortcutId.SMS -> smsBadge
                        ShortcutId.WHATSAPP -> waBadge
                        ShortcutId.MAIL -> mailBadge
                        else -> 0
                    }
                    val action: (Context) -> Unit = when (sc.id) {
                        ShortcutId.APPELS -> { ctx -> launchDialer(ctx) }
                        ShortcutId.SMS -> { ctx -> launchSmsApp(ctx) }
                        ShortcutId.WHATSAPP -> { ctx -> launchWhatsApp(ctx) }
                        ShortcutId.MAIL -> { ctx -> launchMailApp(ctx) }
                        ShortcutId.PHOTOS -> { ctx -> onPhotos() }
                        ShortcutId.APPLIS -> { ctx -> onApplis() }
                    }
                    BigButton(
                        label = stringResource(sc.labelRes),
                        color = sc.color,
                        badge = badge
                    ) { action(context) }
                    if (rowShortcuts.indexOf(sc) == 0 && rowShortcuts.size > 1) {
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
            }
        }

        if (Prefs.isSosVisible(context)) {
            BigSosButton(
                label = stringResource(R.string.btn_sos),
                color = Color(0xFFC62828)
            ) {
                val number = Prefs.getSosNumber(context)
                (context as? MainActivity)?.requestSosCall(number)
            }
        }
    }
}

@Composable
fun ClockHeader(modifier: Modifier = Modifier) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTime = System.currentTimeMillis()
        }
    }
    val timeStr = remember(currentTime) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(currentTime))
    }
    val dateStr = remember(currentTime) {
        SimpleDateFormat("EEEE d MMMM", Locale.FRANCE).format(Date(currentTime))
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Papy Launcher",
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF888888)
        )
        Text(
            text = timeStr,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E)
        )
        Text(
            text = dateStr.replaceFirstChar { it.uppercase() },
            fontSize = 22.sp,
            color = Color(0xFF333333),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun RowScope.BigButton(
    label: String,
    color: Color,
    badge: Int = 0,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1.6f)
            .clip(RoundedCornerShape(24.dp))
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(4.dp)
        )
        if (badge > 0) {
            Badge(
                count = badge,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
fun Badge(
    count: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(8.dp)
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0xFFC62828))
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun BigSosButton(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

fun launchDialer(context: Context) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    safeStartActivity(context, intent)
}

fun launchSmsApp(context: Context) {
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_APP_MESSAGING)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    safeStartActivity(context, intent)
}

fun launchWhatsApp(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
    if (intent != null) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "WhatsApp non installé", Toast.LENGTH_SHORT).show()
    }
}

fun launchMailApp(context: Context) {
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_APP_EMAIL)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    safeStartActivity(context, intent)
}

fun launchSos(context: Context) {
    val number = context.getString(R.string.sos_default_number)
    (context as? MainActivity)?.requestSosCall(number)
}

fun performCall(context: Context, number: String) {
    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    safeStartActivity(context, intent)
}

fun safeStartActivity(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Aucune application trouvée", Toast.LENGTH_SHORT).show()
    }
}