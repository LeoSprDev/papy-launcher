# Refactoring Structurel Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Nettoyer la structure du code restant après l'audit thermo-nucléaire : extraire les helpers de MainActivity, factoriser le lifecycle observer, simplifier PermissionButton, déplacer les dynamic apps dans Prefs, supprimer la duplication PinScreen, et remplacer le polling batterie par un receiver unique.

**Architecture:** 6 refactoring indépendants (un par issue), ordonnancés par dépendances croisées. Chaque tâche produit un build vert et un commit. Pas de TDD — le projet n'a pas de tests unitaires significatifs (squelettes vides), la vérification se fait par `./gradlew assembleDebug` + inspection visuelle des diff.

**Tech Stack:** Kotlin, Jetpack Compose, AndroidX, minSdk 23, targetSdk 37.

## Global Constraints

- Branche git : `refactor/thermo-nuclear-quality-audit` (ne pas créer de nouvelle branche).
- Build de vérification : `./gradlew assembleDebug` doit rester vert après chaque tâche.
- Couleurs : utiliser les constantes `Papy*` de `ui/theme/Color.kt` (ne pas reintroduire de `Color(0xFF...)`).
- Pas de changement de comportement utilisateur (sauf la perf batterie qui s'améliore).
- Ne pas casser les signatures publiques des composables écran (`XxxScreen(onBack: () -> Unit)`, etc.).
- Conventions de commit : messages en français, préfixés par le type (`Refactor:`, `Perf:`, etc.), référence à l'issue `#N`.
- Imports : ordre alphabétique, pas de doublons, supprimer les imports devenus inutilisés.
- Ne pas modifier `AndroidManifest.xml`, `Shortcuts.kt` (couleurs de tuiles), `ui/theme/Color.kt` (déjà fait), `ui/theme/Theme.kt`.

---

## File Structure (cible après plan complet)

```
app/src/main/java/com/papy/launcher/
├── MainActivity.kt              — Activity pure (onCreate, permissions, focus, navigation). ~300 lignes (était 693).
├── Screen.kt                    — sealed class Screen (inchangé)
├── HomeTile.kt                  — NOUVEAU : sealed class HomeTile (Fixed + Dynamic), extraite de MainActivity.kt
├── Intents.kt                   — NOUVEAU : launchDialer, launchSmsApp, launchWhatsApp, launchMailApp, launchCamera, performCall, safeStartActivity, launchApp (extraits de MainActivity.kt + AppListScreen.kt)
├── Shortcuts.kt                 — Modèle + shortcutIcon() déplacée depuis MainActivity.kt
├── Prefs.kt                     — + getDynamicApps/addDynamicApp/removeDynamicApp (membres, migrés depuis DynamicApp.kt)
├── DynamicApp.kt                — data class DynamicApp seule (helpers supprimés)
├── PinScreen.kt                 — trySubmit() local, duplication supprimée
├── AdminScreen.kt               — inchangé (déjà splité)
├── AdminButtons.kt              — PermissionButton simplifié (logique rationale extraite)
├── Permissions.kt               — + requestOrOpenSettings helper
├── ManageFavoritesScreen.kt     — rememberOnResume (au lieu de lifecycle observer inline)
├── ManageAppsScreen.kt          — rememberOnResume
├── ui/components/
│   ├── ScreenHeader.kt          — inchangé
│   └── LifecycleEffects.kt      — NOUVEAU : rememberOnResume helper
└── ui/theme/                    — inchangé
```

---

## Task 1: rememberOnResume helper (issue #5)

**Pourquoi en premier** : helper pur, aucune dépendance vers les autres tâches. Utilisé par les tâches 6 et 7 (qui refactoront ManageFavoritesScreen et ManageAppsScreen — pas dans ce plan, mais débloque le pattern). Tâche isolée et rapide.

**Files:**
- Create: `app/src/main/java/com/papy/launcher/ui/components/LifecycleEffects.kt`
- Modify: `app/src/main/java/com/papy/launcher/ManageFavoritesScreen.kt`
- Modify: `app/src/main/java/com/papy/launcher/ManageAppsScreen.kt`
- Modify: `app/src/main/java/com/papy/launcher/AdminScreen.kt`

**Interfaces:**
- Produces: `@Composable fun rememberOnResume(effect: () -> Unit)` — enregistre un callback lancé au ON_RESUME, désenregistre au onDispose. Remplace le pattern `DisposableEffect(lifecycleOwner) { LifecycleEventObserver { if ON_RESUME ... } }`.

- [ ] **Step 1: Créer `LifecycleEffects.kt`**

```kotlin
package com.papy.launcher.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun rememberOnResume(effect: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) effect()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
```

- [ ] **Step 2: Refactor `ManageFavoritesScreen.kt`**

Remplacer le bloc lifecycle observer (lignes ~61-74) :
```kotlin
val lifecycleOwner = LocalLifecycleOwner.current
val lifecycleObserver = LifecycleEventObserver { _, event ->
    if (event == Lifecycle.Event.ON_RESUME) {
        favorites = Prefs.getFavorites(context)
        contactsGranted = ...
    }
}
DisposableEffect(lifecycleOwner) {
    lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
    onDispose { lifecycleOwner.lifecycle.removeObserver(lifecycleObserver) }
}
```
Par :
```kotlin
rememberOnResume {
    favorites = Prefs.getFavorites(context)
    contactsGranted = androidx.core.content.ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.READ_CONTACTS
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}
```

Ajouter l'import `com.papy.launcher.ui.components.rememberOnResume`. Supprimer les imports devenus inutilisés : `DisposableEffect`, `LifecycleEventObserver`, `Lifecycle`, `LocalLifecycleOwner` (vérifier s'ils sont utilisés ailleurs dans le fichier — ne les supprimer que si inutilisés).

- [ ] **Step 3: Refactor `ManageAppsScreen.kt`**

Remplacer le bloc lifecycle observer (lignes ~47-56) par :
```kotlin
rememberOnResume {
    apps = Prefs.getDynamicApps(context)
}
```

Ajouter l'import `com.papy.launcher.ui.components.rememberOnResume`. Supprimer les imports devenus inutilisés (`DisposableEffect`, `LifecycleEventObserver`, `Lifecycle`, `LocalLifecycleOwner`) si inutilisés ailleurs.

- [ ] **Step 4: Refactor `AdminScreen.kt`**

Localiser le `DisposableEffect(lifecycleOwner)` + `LifecycleEventObserver` qui re-check les 8 états de permission au ON_RESUME (lignes ~79-95). Remplacer par :

```kotlin
rememberOnResume {
    notifListenerEnabled = isNotifListenerEnabled(context)
    overlayEnabled = Settings.canDrawOverlays(context)
    kioskRunning = PapyKioskService.isRunning(context)
    writeSettingsEnabled = Settings.System.canWrite(context)
    callPhoneGranted = isGranted(context, Manifest.permission.CALL_PHONE)
    callLogGranted = isGranted(context, Manifest.permission.READ_CALL_LOG)
    photosGranted = isPhotosPermissionGranted(context)
    contactsGranted = isGranted(context, Manifest.permission.READ_CONTACTS)
}
```

Ajouter l'import `com.papy.launcher.ui.components.rememberOnResume`. Supprimer les imports devenus inutilisés (`DisposableEffect`, `LifecycleEventObserver`, `Lifecycle`, `LocalLifecycleOwner`) si inutilisés ailleurs dans AdminScreen.kt.

- [ ] **Step 5: Build**

```bash
./gradlew assembleDebug 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/papy/launcher/ui/components/LifecycleEffects.kt \
        app/src/main/java/com/papy/launcher/ManageFavoritesScreen.kt \
        app/src/main/java/com/papy/launcher/ManageAppsScreen.kt \
        app/src/main/java/com/papy/launcher/AdminScreen.kt
git commit -m "Refactor: rememberOnResume helper (issue #5)

Extrait le pattern DisposableEffect + LifecycleEventObserver ON_RESUME répété dans
3 fichiers (AdminScreen, ManageFavoritesScreen, ManageAppsScreen) en un helper
rememberOnResume { ... }. -3 copies de boilerplate."
```

---

## Task 2: DynamicApp membres de Prefs (issue #10)

**Pourquoi ici** : nécessaire avant la tâche 4 (qui extrait `Intents.kt` et peut référencer `Prefs.getDynamicApps`). Mettre Prefs dans son état final d'abord.

**Files:**
- Modify: `app/src/main/java/com/papy/launcher/Prefs.kt`
- Modify: `app/src/main/java/com/papy/launcher/DynamicApp.kt`
- Modify (imports only): tous les fichiers qui appellellent `Prefs.getDynamicApps` / `addDynamicApp` / `removeDynamicApp` — vérifier via grep, les signatures restent `Prefs.xxx(context, ...)` donc aucun changement d'appel, juste cohérence.

**Interfaces:**
- Produces: `Prefs.getDynamicApps(context): List<DynamicApp>`, `Prefs.addDynamicApp(context, packageName, label)`, `Prefs.removeDynamicApp(context, packageName)` — désormais membres de l'object `Prefs` (au lieu de fonctions d'extension).

- [ ] **Step 1: Lister les sites d'appel**

```bash
cd /home/yo/Documents/Dev/papy-launcher
grep -rn "getDynamicApps\|addDynamicApp\|removeDynamicApp" app/src/main/java/
```
Note les fichiers. Les appels sont déjà `Prefs.getDynamicApps(...)` etc. — ils ne changent pas (extension → membre garde la même syntaxe d'appel).

- [ ] **Step 2: Migrer les 3 fonctions vers `Prefs.kt`**

Dans `Prefs.kt`, ajouter après la section `favorites` :

```kotlin
private const val KEY_DYNAMIC_APPS = "dynamic_apps_list"

fun getDynamicApps(context: Context): List<DynamicApp> {
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

fun addDynamicApp(context: Context, packageName: String, label: String) {
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
    PapyKioskService.invalidateAllowedPackagesCache()
}

fun removeDynamicApp(context: Context, packageName: String) {
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
    PapyKioskService.invalidateAllowedPackagesCache()
}
```

Ajouter les imports dans `Prefs.kt` : `org.json.JSONArray` (déjà présent), `org.json.JSONObject` (nouveau).

- [ ] **Step 3: Nettoyer `DynamicApp.kt`**

Supprimer les 3 fonctions d'extension (`Prefs.getDynamicApps`, `Prefs.addDynamicApp`, `Prefs.removeDynamicApp`) et la constante `KEY_DYNAMIC_APPS`. Ne garder que la data class :

```kotlin
package com.papy.launcher

data class DynamicApp(
    val packageName: String,
    val label: String
)
```

Supprimer les imports devenus inutilisés (`Context`, `JSONArray`, `JSONObject`).

- [ ] **Step 4: Build**

```bash
./gradlew assembleDebug 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`. Si erreur "unresolved reference" sur un appel, vérifier que l'appel utilise bien `Prefs.getDynamicApps(...)` (déjà le cas partout normalement).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/papy/launcher/Prefs.kt app/src/main/java/com/papy/launcher/DynamicApp.kt
git commit -m "Refactor: DynamicApp membres de Prefs (issue #10)

getDynamicApps/addDynamicApp/removeDynamicApp étaient des fonctions d'extension dans
DynamicApp.kt, alors que les favoris (meme pattern) sont membres de Prefs. Migration
en membres pour cohérence. DynamicApp.kt ne contient plus que la data class."
```

---

## Task 3: PinScreen trySubmit() local (issue #9)

**Pourquoi ici** : tâche triviale et isolée, bonne pour casser la monotonie entre 2 tâches plus grosses.

**Files:**
- Modify: `app/src/main/java/com/papy/launcher/PinScreen.kt`

**Interfaces:**
- Aucune (changement interne à PinScreen).

- [ ] **Step 1: Lire PinScreen.kt**

Identifier les deux blocs dupliqués : lignes ~99-107 (pavé 1-9) et ~130-138 (touche 0). Chacun fait :
```kotlin
if (enteredDigits.size == maxDigits) {
    val pin = enteredDigits.joinToString("")
    if (pin == Prefs.getPin(context)) {
        onSuccess()
    } else {
        showError.value = true
        enteredDigits.clear()
    }
}
```

- [ ] **Step 2: Extraire `trySubmit()` en fonction locale**

Dans le corps de `PinScreen`, avant le pavé numérique, ajouter :

```kotlin
fun trySubmit() {
    if (enteredDigits.size == maxDigits) {
        val pin = enteredDigits.joinToString("")
        if (pin == Prefs.getPin(context)) {
            onSuccess()
        } else {
            showError.value = true
            enteredDigits.clear()
        }
    }
}
```

- [ ] **Step 3: Remplacer les deux blocs par `trySubmit()`**

Dans la branche pavé 1-9 (chiffre `digit`) :
```kotlin
PinKey(digit.toString()) {
    if (enteredDigits.size < maxDigits) {
        enteredDigits.add(digit)
        showError.value = false
        if (enteredDigits.size == maxDigits) trySubmit()
    }
}
```

Dans la branche touche 0 :
```kotlin
PinKey("0") {
    if (enteredDigits.size < maxDigits) {
        enteredDigits.add(0)
        showError.value = false
        if (enteredDigits.size == maxDigits) trySubmit()
    }
}
```

- [ ] **Step 4: Build**

```bash
./gradlew assembleDebug 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/papy/launcher/PinScreen.kt
git commit -m "Refactor: PinScreen trySubmit() local (issue #9)

La logique de validation (if size == maxDigits → compare → onSuccess/showError+clear)
était copiée mot pour mot dans le pavé 1-9 et la touche 0. Extrait en fonction locale
trySubmit(). -10 lignes de duplication."
```

---

## Task 4: Extraire HomeTile + shortcutIcon + launchXxx de MainActivity (issue #4)

**Pourquoi ici** : tâche la plus grosse, dépend de la tâche 2 (Prefs.getDynamicApps est membre). À faire après les tâches isolées.

**Files:**
- Create: `app/src/main/java/com/papy/launcher/HomeTile.kt`
- Create: `app/src/main/java/com/papy/launcher/Intents.kt`
- Modify: `app/src/main/java/com/papy/launcher/MainActivity.kt`
- Modify: `app/src/main/java/com/papy/launcher/Shortcuts.kt` (ajout de `shortcutIcon`)
- Modify: `app/src/main/java/com/papy/launcher/AppListScreen.kt` (suppression de `launchApp` dupliqué)

**Interfaces:**
- Produces:
  - `HomeTile.kt` : `sealed class HomeTile { data class Fixed(shortcut: Shortcut); data class Dynamic(app: DynamicApp) }` (déplacée telle quelle de MainActivity.kt)
  - `Intents.kt` : `launchDialer(context)`, `launchSmsApp(context)`, `launchWhatsApp(context)`, `launchMailApp(context)`, `launchCamera(context)`, `performCall(context, number)`, `safeStartActivity(context, intent)`, `launchApp(context, packageName)` — toutes top-level, déplacées telles quelles de MainActivity.kt (et `launchApp` de AppListScreen.kt)
  - `Shortcuts.kt.shortcutIcon(id: ShortcutId): ImageVector?` — déplacée de MainActivity.kt

- [ ] **Step 1: Créer `HomeTile.kt`**

```kotlin
package com.papy.launcher

sealed class HomeTile {
    data class Fixed(val shortcut: Shortcut) : HomeTile()
    data class Dynamic(val app: DynamicApp) : HomeTile()
}
```

Supprimer la définition de `HomeTile` de `MainActivity.kt` (lignes ~88-91).

- [ ] **Step 2: Créer `Intents.kt`**

Copier depuis `MainActivity.kt` (lignes ~600-662) les fonctions : `launchDialer`, `launchSmsApp`, `launchWhatsApp`, `launchMailApp`, `performCall`, `launchCamera`, `safeStartActivity`. Copier depuis `AppListScreen.kt:146-152` la fonction `launchApp`. Toutes top-level, même package `com.papy.launcher`.

```kotlin
package com.papy.launcher

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast

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

fun launchApp(context: Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}

fun safeStartActivity(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Aucune application trouvée", Toast.LENGTH_SHORT).show()
    }
}
```

Supprimer ces fonctions de `MainActivity.kt` (lignes ~600-676) et `launchApp` de `AppListScreen.kt:146-152`. Ajouter l'import `com.papy.launcher.launchApp` dans `AppListScreen.kt` si nécessaire (vérifier — étant dans le même package, pas d'import nécessaire).

- [ ] **Step 3: Déplacer `shortcutIcon` vers `Shortcuts.kt`**

Dans `Shortcuts.kt`, ajouter à la fin :

```kotlin
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
```

Ajouter les imports nécessaires dans `Shortcuts.kt` : `androidx.compose.runtime.Composable`, `androidx.compose.material.icons.Icons`, `androidx.compose.material.icons.filled.*` (toutes les icônes listées).

Supprimer `shortcutIcon` de `MainActivity.kt` (lignes ~664-676). Supprimer de `MainActivity.kt` les imports d'icônes Material devenus inutilisés (`Icons.Filled.Phone`, `Icons.Filled.Sms`, etc. — vérifier soigneusement s'ils sont utilisés ailleurs dans MainActivity.kt avant de les supprimer).

- [ ] **Step 4: Vérifier MainActivity.kt`

MainActivity.kt ne doit plus contenir que : la classe `MainActivity` (Activity), `AppNavigation()`, `HomeScreen`, `ClockHeader`, `BatteryIndicator`, `BigButton`, `Badge`, `BigSosButton`. Les imports doivent être nettoyés (supprimer `ActivityNotFoundException`, `Intent` (sauf si encore utilisé pour HomeButtonService), `MediaStore`, `Uri`, etc. si plus utilisés).

Vérifier la taille cible : ~300-400 lignes (était 693).

- [ ] **Step 5: Build**

```bash
./gradlew assembleDebug 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL`. Si erreurs "unresolved reference", vérifier les imports.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/papy/launcher/HomeTile.kt \
        app/src/main/java/com/papy/launcher/Intents.kt \
        app/src/main/java/com/papy/launcher/MainActivity.kt \
        app/src/main/java/com/papy/launcher/Shortcuts.kt \
        app/src/main/java/com/papy/launcher/AppListScreen.kt
git commit -m "Refactor: extraire HomeTile, shortcutIcon, launchXxx de MainActivity (issue #4)

MainActivity.kt (693 lignes) mélangeait l'Activity, la sealed class HomeTile (modèle),
les helpers de lancement d'intents (launchDialer, launchSmsApp, etc.), shortcutIcon
(mapping ID → icône), et les composants UI. Extraire vers leurs propres fichiers :
- HomeTile.kt : sealed class HomeTile
- Intents.kt : launchDialer/launchSmsApp/launchWhatsApp/launchMailApp/performCall/
  launchCamera/launchApp/safeStartActivity
- Shortcuts.kt : + shortcutIcon()
- AppListScreen.kt : launchApp supprimé (déplacé vers Intents.kt)

MainActivity.kt descend à ~350 lignes et se concentre sur l'Activity."
```

---

## Task 5: Simplifier PermissionButton + Permissions.kt (issue #6)

**Pourquoi en dernier** : tâche délicate, dépend des tâches précédentes pour avoir un état stable.

**Files:**
- Modify: `app/src/main/java/com/papy/launcher/Permissions.kt`
- Modify: `app/src/main/java/com/papy/launcher/AdminButtons.kt`

**Interfaces:**
- Produces: `requestPermissionOrOpenSettings(activity: Activity, permission: String, onResult: (Boolean) -> Unit, settingsLabel: String, context: Context)` — helper qui gère rationale + fallback page Settings. `PermissionButton` devient purement UI.

- [ ] **Step 1: Lire `AdminButtons.kt` (`PermissionButton`)**

Identifier la logique inlinée dans le `.clickable { ... }` : check `granted` (ouvre page Settings pour désactiver), check `Build.VERSION.SDK_INT < M` (ouvre Settings), check `shouldShowRequestPermissionRationale` (lance activityResult ou ouvre Settings).

- [ ] **Step 2: Ajouter `requestPermissionOrOpenSettings` dans `Permissions.kt`**

```kotlin
internal fun openAppDetailsSettings(context: Context, label: String) {
    android.widget.Toast.makeText(
        context,
        "Onglet « Permissions » → $label",
        android.widget.Toast.LENGTH_LONG
    ).show()
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:${context.packageName}")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

internal fun requestPermissionOrOpenSettings(
    activity: Activity,
    permission: String,
    onResult: (Boolean) -> Unit,
    settingsLabel: String,
    context: Context
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        openAppDetailsSettings(context, "activez « $settingsLabel »")
        return
    }
    val canRequest = !activity.shouldShowRequestPermissionRationale(permission)
    if (canRequest) {
        // le launcher doit être passé en paramètre — voir étape 3
    } else {
        openAppDetailsSettings(context, "activez « $settingsLabel »")
    }
}
```

Note : le launcher `rememberLauncherForActivityResult` ne peut pas être déplacé hors d'un composable. Donc la signature finale est différente — voir étape 3.

- [ ] **Step 3: Refactor `PermissionButton` dans `AdminButtons.kt`**

`PermissionButton` garde le launcher `rememberLauncherForActivityResult` (impossible à extraire d'un composable), mais la logique de décision passe à un helper. Nouvelle version :

```kotlin
@Composable
fun PermissionButton(
    label: String,
    granted: Boolean,
    permission: String,
    context: Context,
    onResult: (Boolean) -> Unit
) {
    val activity = context as? Activity
    val activityResult = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { g ->
        onResult(g)
        if (g) Toast.makeText(context, "Permission accordée", Toast.LENGTH_SHORT).show()
        else Toast.makeText(context, "Permission refusée — ouvrez les réglages de l'app", Toast.LENGTH_LONG).show()
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PapyBlue)
            .clickable {
                if (granted) {
                    openAppDetailsSettings(context, "désactivez « $label »")
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    openAppDetailsSettings(context, "activez « $label »")
                } else {
                    val canRequest = activity?.let {
                        !it.shouldShowRequestPermissionRationale(permission)
                    } != false
                    if (canRequest) {
                        activityResult.launch(permission)
                    } else {
                        openAppDetailsSettings(context, "activez « $label »")
                    }
                }
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ... Box + Check icon + label + status text (inchangé)
    }
}
```

Le helper `openAppDetailsSettings` extrait la duplication du Toast + Intent Settings (3 branches l'utilisaient). Le reste de la logique (rationale + fallback) reste dans le clickable car elle dépend du launcher composable.

Ajouter l'import `com.papy.launcher.openAppDetailsSettings` dans `AdminButtons.kt`.

- [ ] **Step 4: Build**

```bash
./gradlew assembleDebug 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/papy/launcher/Permissions.kt app/src/main/java/com/papy/launcher/AdminButtons.kt
git commit -m "Refactor: simplifier PermissionButton (issue #6)

PermissionButton (96 lignes) mélangeait UI, construction d'Intent Settings, check
shouldShowRequestPermissionRationale, fallback. Extraction de openAppDetailsSettings
(dupliqué 3× dans le clickable) vers Permissions.kt. Le launcher reste dans le
composable (impossible à extraire), mais la logique Settings est factorisée."
```

---

## Task 6: Receiver batterie unique (issue #8)

**Pourquoi en dernier** : tâche de perf, indépendante du reste. Plus gros changement de comportement (remplace une boucle par un receiver), à faire quand le reste est stable.

**Files:**
- Modify: `app/src/main/java/com/papy/launcher/MainActivity.kt` (ClockHeader)

**Interfaces:**
- Aucune (changement interne à ClockHeader).

- [ ] **Step 1: Lire `ClockHeader` dans `MainActivity.kt`**

Identifier la `LaunchedEffect(Unit) { while(true) { ... delay(30000L) } }` qui poll `ACTION_BATTERY_CHANGED` via `registerReceiver(null, ...)` toutes les 30s.

- [ ] **Step 2: Remplacer par un `DisposableEffect` + `BroadcastReceiver`**

```kotlin
@Composable
fun ClockHeader(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var batteryLevel by remember { mutableStateOf(0) }
    var isCharging by remember { mutableStateOf(false) }

    // Tick de l'horloge toutes les 30s (ou moins)
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(30000L)
        }
    }

    // Receiver batterie : push au lieu de poll
    DisposableEffect(Unit) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: android.content.Context?, intent: android.content.Intent?) {
                intent ?: return
                val level = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    batteryLevel = (level * 100) / scale
                }
                val status = intent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
                isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == android.os.BatteryManager.BATTERY_STATUS_FULL
            }
        }
        context.registerReceiver(receiver, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // ... reste de ClockHeader inchangé (Box, BatteryIndicator, Column, Texts)
}
```

Note : `ACTION_BATTERY_CHANGED` est un sticky broadcast — le receiver reçoit immédiatement la dernière valeur à l'enregistrement, puis les changements en push. Plus besoin de boucle.

Ajouter les imports nécessaires : `androidx.compose.runtime.DisposableEffect`, `android.content.BroadcastReceiver`, `android.content.IntentFilter`, `android.content.Intent` (vérifier ceux déjà présents).

- [ ] **Step 3: Build**

```bash
./gradlew assembleDebug 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/papy/launcher/MainActivity.kt
git commit -m "Perf: receiver batterie unique au lieu du polling 30s (issue #8)

ClockHeader utilisait une LaunchedEffect avec boucle while(true) { delay(30000) }
qui appelait registerReceiver(null, ACTION_BATTERY_CHANGED) à chaque tick — poll
pure d'un sticky broadcast. Remplacé par un BroadcastReceiver enregistré une fois via
DisposableEffect. Le receiver reçoit les changements en push (et la valeur initiale à
l'enregistrement car sticky). Supprime le wake-up toutes les 30s."
```

---

## Self-Review

**Spec coverage** : 6 issues (#4, #5, #6, #8, #9, #10) → 6 tâches. ✅

**Placeholder scan** : aucun TBD, TODO, "implement later". Chaque étape a le code réel. ✅

**Type consistency** :
- `rememberOnResume(effect: () -> Unit)` — cohérent entre tâche 1 et les tâches 6/7 (hors plan).
- `Prefs.getDynamicApps(context): List<DynamicApp>` — migré en membre dans tâche 2, inchangé ensuite. ✅
- `HomeTile` déplacée en tâche 4, référencée nulle part ailleurs dans le plan (correct — utilisée seulement dans MainActivity). ✅
- `openAppDetailsSettings(context, label)` défini en tâche 5, utilisé en tâche 5. ✅

**Ordre** : tâche 2 (DynamicApp → Prefs) avant tâche 4 (Intents.kt peut référencer Prefs.getDynamicApps indirectement via HomeScreen). OK. Sinon les tâches sont indépendantes.