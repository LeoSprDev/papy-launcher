# Tuiles d'applis dynamiques — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Permettre à l'admin d'ajouter/retirer des applis installées comme tuiles dédiées sur l'écran d'accueil, après les 9 raccourcis fixes.

**Architecture:** Nouveau concept `DynamicApp` (packageName + label) stocké en JSON dans Prefs. `BigButton` refactoré pour accepter une icône native `Drawable` en plus des `ImageVector` Material. Deux nouveaux écrans : `ManageAppsScreen` (gestion) et `AppPickerScreen` (sélection). `HomeScreen` itère sur les raccourcis fixes puis les DynamicApps.

**Tech Stack:** Kotlin + Jetpack Compose, `PackageManager` (Android framework), `org.json.JSONArray` (stdlib), `AndroidView` + `ImageView` (icônes natives).

## Global Constraints

- minSdk 23, targetSdk 37.
- No new external dependencies (`org.json` is Android stdlib, `AndroidView`/`ImageView` already used in `AppListScreen`).
- Colors from DESIGN.md: `bleu-acier` = `#546E7A`, `Surface Card` = `#F5F5F5`, `Texte Principal` = `#333333`, `Texte Légende` = `#888888`, `Rouge SOS Familial` = `#C62828`, `Indigo Mécanique` = `#1A237E`.
- UI text in French (hardcoded in code, consistent with existing admin sections).
- No code comments unless explicitly requested.
- Verify build after each task: `export ANDROID_HOME=/home/yo/Android/Sdk && ./gradlew :app:compileDebugKotlin 2>&1 | tail -15` (expect BUILD SUCCESSFUL).

---

## File Structure

| Fichier | Action | Responsabilité |
|---|---|---|
| `app/src/main/java/com/papy/launcher/DynamicApp.kt` | Créer | Data class `DynamicApp` + helpers Prefs (getDynamicApps, addDynamicApp, removeDynamicApp) |
| `app/src/main/java/com/papy/launcher/Prefs.kt` | Modifier | Ajouter stockage JSON des DynamicApps (déléguer à DynamicApp.kt) |
| `app/src/main/java/com/papy/launcher/MainActivity.kt` | Modifier | Refactor `BigButton` pour accepter `drawableIcon: Drawable?` + HomeScreen itère sur DynamicApps + navigation `manage_apps`/`app_picker` |
| `app/src/main/java/com/papy/launcher/AdminScreen.kt` | Modifier | Ajouter section « Applis » avec bouton « Gérer les applis » + callback `onManageApps` |
| `app/src/main/java/com/papy/launcher/ManageAppsScreen.kt` | Créer | Écran de gestion (liste + bouton ajouter + bouton retirer) |
| `app/src/main/java/com/papy/launcher/AppPickerScreen.kt` | Créer | Écran de sélection (liste des applis installées, tap = ajout) |
| `DESIGN.md` | Modifier | Ajouter `bleu-acier` dans frontmatter + section Tertiary |

---

### Task 1: DynamicApp — modèle + stockage Prefs + couleur DESIGN.md

**Files:**
- Create: `app/src/main/java/com/papy/launcher/DynamicApp.kt`
- Modify: `DESIGN.md`

**Interfaces:**
- Produces: `data class DynamicApp(packageName: String, label: String)`, `Prefs.getDynamicApps(context): List<DynamicApp>`, `Prefs.addDynamicApp(context, packageName, label)`, `Prefs.removeDynamicApp(context, packageName)`.

- [ ] **Step 1: Créer DynamicApp.kt avec data class + helpers Prefs**

Créer `app/src/main/java/com/papy/launcher/DynamicApp.kt` :

```kotlin
package com.papy.launcher

import android.content.Context
import org.json.JSONArray

data class DynamicApp(
    val packageName: String,
    val label: String
)

private const val KEY_DYNAMIC_APPS = "dynamic_apps_list"

fun Prefs.getDynamicApps(context: Context): List<DynamicApp> {
    val json = prefs(context).getString(KEY_DYNAMIC_APPS, null) ?: return emptyList()
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).mapNotNull { i ->
            val obj = arr.optJSONObject(i) ?: return@mapNotNull null
            val pkg = obj.optString("package", null) ?: return@mapNotNull null
            val label = obj.optString("label", pkg)
            DynamicApp(pkg, label)
        }
    } catch (e: Exception) {
        emptyList()
    }
}

fun Prefs.addDynamicApp(context: Context, packageName: String, label: String) {
    val current = getDynamicApps(context).toMutableList()
    if (current.any { it.packageName == packageName }) return
    current.add(DynamicApp(packageName, label))
    val arr = JSONArray()
    for (app in current) {
        arr.put(org.json.JSONObject().apply {
            put("package", app.packageName)
            put("label", app.label)
        })
    }
    prefs(context).edit().putString(KEY_DYNAMIC_APPS, arr.toString()).apply()
}

fun Prefs.removeDynamicApp(context: Context, packageName: String) {
    val current = getDynamicApps(context).toMutableList()
    if (current.none { it.packageName == packageName }) return
    current.removeAll { it.packageName == packageName }
    val arr = JSONArray()
    for (app in current) {
        arr.put(org.json.JSONObject().apply {
            put("package", app.packageName)
            put("label", app.label)
        })
    }
    prefs(context).edit().putString(KEY_DYNAMIC_APPS, arr.toString()).apply()
}
```

Note: `Prefs` is an `object`, and `prefs(context)` is a `private fun` inside it. These extension functions need access to it. Change `prefs(context)` from `private fun` to `internal fun` in `Prefs.kt` so the extensions in `DynamicApp.kt` can call it.

- [ ] **Step 2: Rendre prefs(context) internal dans Prefs.kt**

Modifier `app/src/main/java/com/papy/launcher/Prefs.kt`, ligne 15 : changer `private fun prefs` en `internal fun prefs`.

- [ ] **Step 3: Ajouter la couleur dans DESIGN.md**

Dans le frontmatter `colors:`, après `indigo-favoris: "#3949AB"` :
```yaml
  bleu-acier: "#546E7A"
```

Dans la section `### Tertiary`, après `**Indigo Favoris**` :
```markdown
- **Bleu Acier** (#546E7A) : tuiles d'applis dynamiques. Neutre, distinct du Gris Réglages (#455A64) de la tuile Applis. Les tuiles d'applis se distinguent par leur icône native, pas par leur couleur.
```

- [ ] **Step 4: Vérifier le build**

Run: `export ANDROID_HOME=/home/yo/Android/Sdk && ./gradlew :app:compileDebugKotlin 2>&1 | tail -15`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/papy/launcher/DynamicApp.kt app/src/main/java/com/papy/launcher/Prefs.kt DESIGN.md
git commit -m "Applis dynamiques: DynamicApp data class + stockage Prefs + couleur DESIGN.md"
```

---

### Task 2: Refactor BigButton pour icône native Drawable

**Files:**
- Modify: `app/src/main/java/com/papy/launcher/MainActivity.kt:360-406` (BigButton composable)

**Interfaces:**
- Produces: `RowScope.BigButton(label, color, icon: ImageVector? = null, drawableIcon: Drawable? = null, badge: Int = 0, onClick)`.

- [ ] **Step 1: Modifier BigButton pour accepter drawableIcon**

Dans `MainActivity.kt`, remplacer la fonction `RowScope.BigButton` (lignes 360-406) :

```kotlin
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
```

- [ ] **Step 2: Ajouter les imports manquants dans MainActivity.kt**

Vérifier/ajouter en haut de `MainActivity.kt` :
```kotlin
import android.graphics.drawable.Drawable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.viewinterop.AndroidView
```

- [ ] **Step 3: Vérifier le build**

Run: `export ANDROID_HOME=/home/yo/Android/Sdk && ./gradlew :app:compileDebugKotlin 2>&1 | tail -15`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/papy/launcher/MainActivity.kt
git commit -m "Applis dynamiques: BigButton accepte icone native Drawable (medaillon blanc)"
```

---

### Task 3: HomeScreen — affichage des tuiles d'applis

**Files:**
- Modify: `app/src/main/java/com/papy/launcher/MainActivity.kt` (HomeScreen, après la boucle des raccourcis fixes)

**Interfaces:**
- Consumes: `Prefs.getDynamicApps(context): List<DynamicApp>`, `BigButton` avec `drawableIcon`, `launchApp(context, packageName)` de `AppListScreen.kt:146`.
- Produces: HomeScreen affiche les raccourcis fixes + les tuiles d'applis dynamiques.

- [ ] **Step 1: Ajouter l'itération sur les DynamicApps dans HomeScreen**

Dans `MainActivity.kt`, dans `HomeScreen`, après la boucle `for (rowShortcuts in rows)` (qui itère sur les raccourcis fixes) et avant le bloc `if (Prefs.isSosVisible(context))`, ajouter :

```kotlin
        val dynamicApps = remember { Prefs.getDynamicApps(context) }
        val appRows = dynamicApps.chunked(2)
        for (rowApps in appRows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (app in rowApps) {
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
                        color = Color(0xFF546E7A),
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
                if (rowApps.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
```

Note: `launchApp` est déjà défini dans `AppListScreen.kt:146` (fonction publique package-level). Vérifier l'import si nécessaire — il est dans le même package `com.papy.launcher`, donc accessible sans import.

- [ ] **Step 2: Vérifier le build**

Run: `export ANDROID_HOME=/home/yo/Android/Sdk && ./gradlew :app:compileDebugKotlin 2>&1 | tail -15`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/papy/launcher/MainActivity.kt
git commit -m "Applis dynamiques: HomeScreen affiche les tuiles d'applis après les raccourcis fixes"
```

---

### Task 4: ManageAppsScreen — écran de gestion

**Files:**
- Create: `app/src/main/java/com/papy/launcher/ManageAppsScreen.kt`

**Interfaces:**
- Consumes: `Prefs.getDynamicApps(context)`, `Prefs.removeDynamicApp(context, packageName)`, `loadInstalledApps(context)` (from `AppListScreen.kt`), `AppIcon(icon, size)` (from `AppListScreen.kt`).
- Produces: `@Composable fun ManageAppsScreen(onBack: () -> Unit, onAddApp: () -> Unit)`.

- [ ] **Step 1: Créer ManageAppsScreen.kt**

Créer `app/src/main/java/com/papy/launcher/ManageAppsScreen.kt` :

```kotlin
package com.papy.launcher

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A237E))
                    .clickable { onBack() }
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Retour",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Gérer les applis",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        AdminButton("Ajouter une appli") {
            onAddApp()
        }
        Spacer(modifier = Modifier.height(16.dp)

        if (apps.isEmpty()) {
            Text(
                text = "Aucune appli. Touchez « Ajouter une appli ».",
                fontSize = 18.sp,
                color = Color(0xFF666666)
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
                        .background(Color(0xFFF5F5F5))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (appIcon != null) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEEEEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            AppIcon(icon = appIcon, size = 32)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEEEEE))
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        if (isInstalled) {
                            Text(
                                text = appLabel,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                        } else {
                            Text(
                                text = "$appLabel (désinstallée)",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                        }
                        Text(
                            text = app.packageName,
                            fontSize = 16.sp,
                            color = Color(0xFF888888)
                        )
                    }
                    Text(
                        text = "Retirer",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828),
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
```

- [ ] **Step 2: Vérifier le build**

Run: `export ANDROID_HOME=/home/yo/Android/Sdk && ./gradlew :app:compileDebugKotlin 2>&1 | tail -15`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/papy/launcher/ManageAppsScreen.kt
git commit -m "Applis dynamiques: ecran ManageAppsScreen (liste + bouton ajouter + retirer)"
```

---

### Task 5: AppPickerScreen — écran de sélection

**Files:**
- Create: `app/src/main/java/com/papy/launcher/AppPickerScreen.kt`

**Interfaces:**
- Consumes: `loadInstalledApps(context): List<AppInfo>` (from `AppListScreen.kt:42`), `AppRow(app, onClick)` (from `AppListScreen.kt:108`), `Prefs.getDynamicApps(context)`, `Prefs.addDynamicApp(context, packageName, label)`.
- Produces: `@Composable fun AppPickerScreen(onBack: () -> Unit)`.

- [ ] **Step 1: Créer AppPickerScreen.kt**

Créer `app/src/main/java/com/papy/launcher/AppPickerScreen.kt` :

```kotlin
package com.papy.launcher

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppPickerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val apps = remember { loadInstalledApps(context) }
    val existingApps = remember { Prefs.getDynamicApps(context).map { it.packageName }.toSet() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A237E))
                    .clickable { onBack() }
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Retour",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Ajouter une appli",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
        }
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
```

- [ ] **Step 2: Vérifier le build**

Run: `export ANDROID_HOME=/home/yo/Android/Sdk && ./gradlew :app:compileDebugKotlin 2>&1 | tail -15`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/papy/launcher/AppPickerScreen.kt
git commit -m "Applis dynamiques: ecran AppPickerScreen (selection depuis applis installees)"
```

---

### Task 6: Navigation + AdminScreen

**Files:**
- Modify: `app/src/main/java/com/papy/launcher/MainActivity.kt` (AppNavigation + AdminScreen call)
- Modify: `app/src/main/java/com/papy/launcher/AdminScreen.kt` (ajout section Applis + callback)

**Interfaces:**
- Consumes: `ManageAppsScreen(onBack, onAddApp)`, `AppPickerScreen(onBack)`.

- [ ] **Step 1: Ajouter la navigation dans AppNavigation**

Dans `MainActivity.kt`, dans `AppNavigation`, après le bloc `"manage_favorites"` :

```kotlin
            "manage_apps" -> ManageAppsScreen(
                onBack = { screen = "admin" },
                onAddApp = { screen = "app_picker" }
            )
            "app_picker" -> AppPickerScreen(
                onBack = { screen = "manage_apps" }
            )
```

- [ ] **Step 2: Ajouter onManageApps à l'appel AdminScreen**

Dans `AppNavigation`, modifier l'appel `"admin" -> AdminScreen(...)` :

```kotlin
            "admin" -> AdminScreen(
                onExit = { screen = "home" },
                onManageFavorites = { screen = "manage_favorites" },
                onManageApps = { screen = "manage_apps" }
            )
```

- [ ] **Step 3: Ajouter le callback onManageApps à la signature AdminScreen**

Dans `AdminScreen.kt`, modifier la signature :

```kotlin
fun AdminScreen(
    onExit: () -> Unit,
    onManageFavorites: () -> Unit,
    onManageApps: () -> Unit
) {
```

- [ ] **Step 4: Ajouter la section « Applis » dans AdminScreen**

Dans `AdminScreen.kt`, après la section « Favoris » (le bouton « Gérer les favoris ») et avant la ligne `Spacer(modifier = Modifier.height(32.dp))` qui précède « Réglages rapides », ajouter :

```kotlin
        Spacer(modifier = Modifier.height(32.dp))

        // Section Applis
        SectionTitle("Applis")

        AdminButton("Gérer les applis") {
            onManageApps()
        }
```

- [ ] **Step 5: Vérifier le build**

Run: `export ANDROID_HOME=/home/yo/Android/Sdk && ./gradlew :app:compileDebugKotlin 2>&1 | tail -15`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/papy/launcher/MainActivity.kt app/src/main/java/com/papy/launcher/AdminScreen.kt
git commit -m "Applis dynamiques: navigation + section Applis dans AdminScreen"
```

---

### Task 7: Build APK final + push

**Files:**
- Aucune modification de code.

- [ ] **Step 1: Build APK debug complet**

Run: `export ANDROID_HOME=/home/yo/Android/Sdk && ./gradlew :app:assembleDebug 2>&1 | tail -20`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Push**

```bash
git push
```

---

## Self-Review

**Spec coverage :**
- Section 1 (DynamicApp + Prefs + couleur) → Task 1 ✅
- Section 3 (BigButton refactor) → Task 2 ✅
- Section 7 (HomeScreen itération) → Task 3 ✅
- Section 4 (ManageAppsScreen) → Task 4 ✅
- Section 5 (AppPickerScreen) → Task 5 ✅
- Section 6 (AdminScreen + navigation) → Task 6 ✅
- Section 9 (DESIGN.md couleur) → Task 1 ✅
- Section 8 (Navigation) → Task 6 ✅

**Placeholder scan :** aucun TODO/TBD. Tous les steps contiennent le code réel.

**Type consistency :** `DynamicApp(packageName, label)` cohérent entre Task 1 (définition), Task 3 (HomeScreen), Task 4 (ManageAppsScreen), Task 5 (AppPickerScreen). `Prefs.getDynamicApps/addDynamicApp/removeDynamicApp` cohérents. `ManageAppsScreen(onBack, onAddApp)` et `AppPickerScreen(onBack)` cohérents entre définition (Tasks 4-5) et navigation (Task 6). `BigButton(label, color, icon, drawableIcon, badge, onClick)` cohérent entre Task 2 (définition) et Task 3 (usage).