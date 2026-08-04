package com.papy.launcher

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Battery4Bar
import androidx.compose.material.icons.filled.Battery3Bar
import androidx.compose.material.icons.filled.Battery2Bar
import androidx.compose.material.icons.filled.Battery1Bar
import androidx.compose.material.icons.filled.Battery0Bar
import androidx.compose.material.icons.filled.Sms
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
import androidx.compose.ui.viewinterop.AndroidView
import com.papy.launcher.ui.theme.PapyBlue
import com.papy.launcher.ui.theme.PapyGreen
import com.papy.launcher.ui.theme.PapyLauncherTheme
import com.papy.launcher.ui.theme.PapyOrange
import com.papy.launcher.ui.theme.PapyRed
import com.papy.launcher.ui.theme.PapyTextBlueGray
import com.papy.launcher.ui.theme.PapyTextDark
import com.papy.launcher.ui.theme.PapyTextLight

sealed class HomeTile {
    data class Fixed(val shortcut: Shortcut) : HomeTile()
    data class Dynamic(val app: DynamicApp) : HomeTile()
}

class MainActivity : ComponentActivity() {
    private var pendingSosNumber: String? = null
    private var sosPermissionPending = false

    private val callPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingSosNumber?.let { performCall(this, it) }
        } else {
            Toast.makeText(this, "Permission d'appel refusée", Toast.LENGTH_SHORT).show()
        }
        pendingSosNumber = null
        sosPermissionPending = false
    }

    private val multiPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        MissedCalls.refresh(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestEssentialPermissions()
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
    }

    override fun onPause() {
        super.onPause()
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
        if (hasFocus) {
            stopHomeButtonService()
        } else {
            if (Prefs.isHomeButtonEnabled(this)) {
                startHomeButtonService()
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun AppNavigation() {
        var screen by remember { mutableStateOf<Screen>(Screen.Home) }
        when (screen) {
            Screen.Home -> HomeScreen(
                onAdminTrigger = { screen = Screen.Pin },
                onApplis = { screen = Screen.AppList },
                onPhotos = { screen = Screen.Photos },
                onFavorites = { screen = Screen.Favorites }
            )
            Screen.Pin -> PinScreen(
                onSuccess = { screen = Screen.Admin },
                onCancel = { screen = Screen.Home }
            )
            Screen.Admin -> AdminScreen(
                onExit = { screen = Screen.Home },
                onManageFavorites = { screen = Screen.ManageFavorites },
                onManageApps = { screen = Screen.ManageApps }
            )
            Screen.ManageFavorites -> ManageFavoritesScreen(
                onBack = { screen = Screen.Admin }
            )
            Screen.ManageApps -> ManageAppsScreen(
                onBack = { screen = Screen.Admin },
                onAddApp = { screen = Screen.AppPicker }
            )
            Screen.AppPicker -> AppPickerScreen(
                onBack = { screen = Screen.ManageApps }
            )
            Screen.AppList -> AppListScreen(
                onBack = { screen = Screen.Home }
            )
            Screen.Photos -> PhotosScreen(
                onBack = { screen = Screen.Home }
            )
            Screen.Favorites -> FavoritesScreen(
                onBack = { screen = Screen.Home }
            )
        }
    }

    fun requestSosCall(number: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            performCall(this, number)
        } else {
            if (sosPermissionPending) return
            pendingSosNumber = number
            sosPermissionPending = true
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onAdminTrigger: () -> Unit,
    onApplis: () -> Unit,
    onPhotos: () -> Unit,
    onFavorites: () -> Unit
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
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        var tapCount by remember { mutableStateOf(0) }
        var lastTapTime by remember { mutableStateOf(0L) }
        ClockHeader(
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp)
                .combinedClickable(
                    onClick = {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime > 2000L) tapCount = 0
                        lastTapTime = now
                        tapCount++
                        if (tapCount >= 7) {
                            tapCount = 0
                            onAdminTrigger()
                        }
                    },
                    onLongClick = {}
                )
        )

        // Grille : fusion des raccourcis fixes + applis dynamiques, chunked par 2
        val enabledShortcuts = remember { Prefs.getEnabledShortcuts(context) }
        val dynamicApps = remember { Prefs.getDynamicApps(context) }

        val allTiles: List<HomeTile> = enabledShortcuts.map { HomeTile.Fixed(it) } +
            dynamicApps.map { HomeTile.Dynamic(it) }
        val rows = allTiles.chunked(2)
        for (rowTiles in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (tile in rowTiles) {
                    when (tile) {
                        is HomeTile.Fixed -> {
                            val sc = tile.shortcut
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
                                ShortcutId.APPAREIL_PHOTO -> { ctx -> launchCamera(ctx) }
                                ShortcutId.PROUT -> { ctx -> playFartSound(ctx) }
                                ShortcutId.FAVORIS -> { ctx -> onFavorites() }
                            }
                            BigButton(
                                label = stringResource(sc.labelRes),
                                color = sc.color,
                                icon = shortcutIcon(sc.id),
                                badge = badge
                            ) { action(context) }
                        }
                        is HomeTile.Dynamic -> {
                            val app = tile.app
                            val pm = context.packageManager
                            val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                            val appLabel = try {
                                pm.getApplicationLabel(pm.getApplicationInfo(app.packageName, 0)).toString()
                            } catch (e: Exception) {
                                app.label
                            }
                            val appIcon = try {
                                pm.getApplicationIcon(app.packageName)
                            } catch (e: Exception) {
                                null
                            }
                            BigButton(
                                label = appLabel,
                                color = PapyTextBlueGray,
                                drawableIcon = appIcon,
                                badge = 0
                            ) {
                                if (launchIntent != null) {
                                    launchApp(context, app.packageName)
                                } else {
                                    Toast.makeText(context, "$appLabel n'est plus installée", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
                if (rowTiles.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        if (Prefs.isSosVisible(context)) {
            BigSosButton(
                label = stringResource(R.string.btn_sos),
                color = PapyRed
            ) {
                val number = Prefs.getSosNumber(context)
                (context as? MainActivity)?.requestSosCall(number)
            }
        }
    }
}

@Composable
fun ClockHeader(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var batteryLevel by remember { mutableStateOf(0) }
    var isCharging by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            val intent = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            if (intent != null) {
                val level = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    batteryLevel = (level * 100) / scale
                }
                val status = intent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
                isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == android.os.BatteryManager.BATTERY_STATUS_FULL
            }
            delay(30000L)
        }
    }
    val timeStr = remember(currentTime) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(currentTime))
    }
    val dateStr = remember(currentTime) {
        SimpleDateFormat("EEEE d MMMM", Locale.FRANCE).format(Date(currentTime))
    }
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        BatteryIndicator(
            level = batteryLevel,
            isCharging = isCharging,
            modifier = Modifier.align(Alignment.TopEnd)
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Papy Launcher",
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = PapyTextLight
            )
            Text(
                text = timeStr,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = PapyBlue
            )
            Text(
                text = dateStr.replaceFirstChar { it.uppercase() },
                fontSize = 22.sp,
                color = PapyTextDark,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun BatteryIndicator(level: Int, isCharging: Boolean, modifier: Modifier = Modifier) {
    val batteryColor = when {
        level <= 15 -> PapyRed
        level <= 30 -> PapyOrange
        else -> PapyGreen
    }
    val batteryIcon = when {
        level >= 90 -> Icons.Filled.BatteryFull
        level >= 60 -> Icons.Filled.Battery5Bar
        level >= 40 -> Icons.Filled.Battery4Bar
        level >= 20 -> Icons.Filled.Battery3Bar
        level >= 10 -> Icons.Filled.Battery2Bar
        level >= 5 -> Icons.Filled.Battery1Bar
        else -> Icons.Filled.Battery0Bar
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(top = 8.dp)
    ) {
        androidx.compose.material3.Icon(
            imageVector = batteryIcon,
            contentDescription = null,
            tint = batteryColor,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = "$level%",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = batteryColor
        )
    }
}

@Composable
fun RowScope.BigButton(
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    drawableIcon: Drawable? = null,
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (drawableIcon != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            android.widget.ImageView(ctx).apply {
                                setImageDrawable(drawableIcon)
                                layoutParams = android.view.ViewGroup.LayoutParams(32, 32)
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            } else if (icon != null) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = label,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(4.dp)
            )
        }
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
            .background(PapyRed)
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

fun launchCamera(context: Context) {
    val intent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
    } else {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    }.apply {
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

@Composable
fun shortcutIcon(id: ShortcutId): androidx.compose.ui.graphics.vector.ImageVector? =
    when (id) {
        ShortcutId.APPELS -> Icons.Filled.Phone
        ShortcutId.SMS -> Icons.Filled.Sms
        ShortcutId.WHATSAPP -> Icons.Filled.Chat
        ShortcutId.MAIL -> Icons.Filled.Email
        ShortcutId.PHOTOS -> Icons.Filled.Photo
        ShortcutId.APPLIS -> Icons.Filled.Apps
        ShortcutId.APPAREIL_PHOTO -> Icons.Filled.PhotoCamera
        ShortcutId.PROUT -> Icons.Filled.SentimentVerySatisfied
        ShortcutId.FAVORIS -> Icons.Filled.Star
    }