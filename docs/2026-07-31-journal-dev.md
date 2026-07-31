# Journal de développement — Papy Launcher

**Date :** 2026-07-31
**Auteur :** Développement collaboratif (opencode + utilisateur)
**Cible :** Samsung Galaxy A13 4G (Android 13) — téléphone de papa (85 ans)
**Device de test :** Google Pixel 6a (Android 16)

---

## 1. Contexte et objectifs

### Le besoin
Créer un launcher Android ultra-simple pour une personne âgée (85 ans) qui a des difficultés avec :
- Les gestuelles complexes (slide, swipe, clic long)
- La complexité d'Android (multiples écrans, notifications, publicités)
- La mémoire (oublie les procédures 3 jours après)

### Fonctionnalités requises
- Passer et recevoir un appel
- WhatsApp vidéo
- Lire et envoyer des SMS
- Lire ses mails
- Lancer quelques applis
- Voir les photos
- Bouton SOS (masquable par admin)
- Mode Admin (PIN) pour configurer le launcher
- Privacy first
- Léger, compatible vieux Android (minSdk 23)
- Icones gros et faciles à comprendre

### Décisions prises
| Décision | Choix | Raison |
|---|---|---|
| Base de départ | Partir de zéro | BaldPhone (301★, Java) atteint 55-60% du besoin mais code legacy lourd ; senioren-launcher exige Android 14+ |
| Langage | Kotlin + Jetpack Compose | Moderne, lisible, maintenable |
| Langue UI | Français | |
| Mode Admin | Code PIN à 4 chiffres | |
| Accès réglages | Raccourcis simplifiés dans le launcher, admin only | |
| Bouton SOS | Affichable/masquable par admin, numéro configurable | |
| Photos | Visionneur intégré léger (à venir) | |
| OS de dev | Windows | |

---

## 2. Installation des outils

### Git
- **Déjà installé** : git 2.45.0.windows.1
- Identité non configurée au départ → à configurer avec `git config user.name` / `user.email`

### GitHub CLI (gh)
- **Non installé** au départ
- Tentative d'installation via `winget` → winget non disponible (PowerShell 5.1)
- Tentative via MSI silencieux → échec (probablement blocage antivirus/SES Evolution)
- Tentative via Chocolatey → échec (droits admin requis, proxy)
- **Décision : création du repo à la main via github.com**

### Android Studio
- Téléchargé depuis https://developer.android.com/studio
- Installé avec options par défaut (`C:\Program Files\Android\Android Studio`)
- Embarque son propre JDK 17 (le Java 8 du système n'est pas utilisé)
- SDK Android téléchargé au premier lancement (~3 Go)

---

## 3. Création du repo GitHub

### Étapes manuelles
1. Création sur https://github.com/new
   - Nom : `papy-launcher`
   - Visibilité : **Private**
   - Initialisation : README + License Apache 2.0
2. Clonage en local :
   ```
   cd C:\yo\01_Dev\02_Applis\05_Android_launcher
   git clone https://github.com/<pseudo>/papy-launcher.git
   ```
3. Authentification via Personal Access Token (compte GitHub avec 2FA)
4. Sauvegarde temporaire de README.md et LICENSE hors du dossier pour qu'Android Studio accepte de créer le projet dans le dossier du repo (il veut un dossier vide)

### Chemin du projet
```
C:\yo\01_Dev\02_Applis\05_Android_launcher\papy-launcher
```

---

## 4. Création du projet Android Studio

### Paramètres du projet
| Paramètre | Valeur |
|---|---|
| Template | Empty Activity (Jetpack Compose) |
| Name | Papy Launcher |
| Package name | com.papy.launcher |
| Save location | C:\yo\01_Dev\02_Applis\05_Android_launcher\papy-launcher |
| Language | Kotlin |
| Minimum SDK | API 23 (Android 6.0) |
| Build configuration | Kotlin DSL (build.gradle.kts) |

### Structure créée
```
app/
├── build.gradle.kts
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/papy/launcher/
│   │   │   ├── MainActivity.kt
│   │   │   └── ui/theme/ (Color.kt, Theme.kt, Type.kt)
│   │   └── res/
│   │       ├── values/ (strings.xml, themes.xml, colors.xml)
│   │       ├── drawable/ (ic_launcher...)
│   │       └── xml/ (backup_rules.xml, data_extraction_rules.xml)
│   ├── androidTest/
│   └── test/
build.gradle.kts
settings.gradle.kts
```

### Configuration build.gradle.kts
- `applicationId = "com.papy.launcher"`
- `minSdk = 23`
- `targetSdk = 37`
- Compose activé (`buildFeatures { compose = true }`)

---

## 5. Préparation du téléphone de test (Pixel 6a)

### Activation du mode développeur
1. **Paramètres** → **À propos du téléphone**
2. Taper **7 fois** sur **Numéro de version** → "Vous êtes maintenant développeur"
3. **Paramètres** → **Options de développement**
4. Activer **Débogage USB**

### Connexion au PC
- Brancher en USB
- Sur l'écran du téléphone : accepter **"Autoriser le débogage USB"** (cocher "Toujours")

### Premier déploiement
- Android Studio détecte le Pixel 6a dans le menu device
- Bouton **Run ▶️** → l'appli se compile, s'installe, s'affiche avec "Hello Android!"

### Pour le téléphone final (Galaxy A13 de papa)
Mêmes étapes à reproduire sur le A13 :
1. Activer mode développeur + débogage USB
2. Brancher en USB au PC
3. Android Studio déploie l'APK directement

---

## 6. Transformation en launcher

### Modification du manifeste
Ajout des catégories `HOME` et `DEFAULT` dans l'intent-filter de MainActivity :
```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
    <category android:name="android.intent.category.HOME" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```
→ Android propose Papy Launcher comme écran d'accueil quand on appuie sur le bouton Home.

### Écran d'accueil avec gros boutons
Création d'une grille 2×3 de boutons colorés + bouton SOS en pleine largeur :
- **Appels** (vert)
- **SMS** (bleu)
- **WhatsApp** (vert foncé)
- **Mail** (orange)
- **Photos** (violet)
- **Applis** (gris)
- **SOS** (rouge, pleine largeur en bas)

Tailles : boutons en `aspectRatio(1.6f)`, texte 26sp, SOS 36sp, cibles tactiles larges.

### Fonctions de lancement d'applis
| Bouton | Fonction | Technique |
|---|---|---|
| Appels | `launchDialer()` | `Intent.ACTION_DIAL` |
| SMS | `launchSmsApp()` | `Intent.ACTION_MAIN` + `CATEGORY_APP_MESSAGING` |
| WhatsApp | `launchWhatsApp()` | `packageManager.getLaunchIntentForPackage("com.whatsapp")` |
| Mail | `launchMailApp()` | `Intent.ACTION_MAIN` + `CATEGORY_APP_EMAIL` |
| Photos | `launchGallery()` | `Intent.ACTION_VIEW` type `image/*` |
| Applis | `launchAllApps()` | Toast "Bientôt disponible" (à remplacer) |
| SOS | `launchSos()` | `Intent.ACTION_CALL` + permission runtime |

### Bugs corrigés
1. **`Modifier.weight()` inaccessible** : `BigButton` transformé en `RowScope.BigButton` (extension)
2. **Boutons non cliquables** : ajout de `Modifier.clickable(onClick = onClick)`
3. **Imports inutilisés** : `Icon`, `ImageVector`, `size` retirés
4. **`CATEGORY_APP_LAUNCHER` unresolved** : remplacé par Toast temporaire

---

## 7. Permissions runtime

### CALL_PHONE (bouton SOS)
- Manifeste : `<uses-permission android:name="android.permission.CALL_PHONE" />`
- Runtime : `ActivityResultContracts.RequestPermission()` dans MainActivity
- `requestSosCall()` vérifie la permission avant d'appeler `ACTION_CALL`
- Si non accordée → boîte de dialogue système → puis appel

### READ_CALL_LOG (pastilles appels manqués)
- Manifeste : `<uses-permission android:name="android.permission.READ_CALL_LOG" />`
- Runtime : `ActivityResultContracts.RequestMultiplePermissions()` au démarrage
- Voir section 9 pour le détail

### SYSTEM_ALERT_WINDOW (bouton Home flottant)
- Manifeste : `<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />`
- Runtime : demande via `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` (à faire manuellement sur le téléphone)
- Voir section 11 pour le détail

### FOREGROUND_SERVICE + FOREGROUND_SERVICE_SPECIAL_USE
- Manifeste :
  ```xml
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
  ```
- Nécessaire pour `HomeButtonService` (Android 14+)

---

## 8. Visibilité des autres applis (queries)

### Problème
Depuis Android 11, une appli ne "voit" plus les autres applis par défaut. WhatsApp n'était pas détecté.

### Solution
Ajout d'un bloc `<queries>` dans le manifeste :
```xml
<queries>
    <package android:name="com.whatsapp" />
    <package android:name="com.whatsapp.w4b" />
    <intent>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.APP_EMAIL" />
    </intent>
    <intent>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.APP_MESSAGING" />
    </intent>
    <intent>
        <action android:name="android.intent.action.VIEW" />
        <data android:mimeType="image/*" />
    </intent>
</queries>
```

---

## 9. Mode Admin + PIN

### Architecture
- **`Prefs.kt`** : stockage SharedPreferences (PIN, numéro SOS, visibilité SOS, kiosque, bouton home)
- **`PinScreen.kt`** : écran de saisie avec pavé numérique grand (cercles 80dp, texte 32sp)
- **`AdminScreen.kt`** : écran de configuration (SOS, PIN, kiosque)
- **`MainActivity.kt`** : navigation entre 3 écrans (`home` → `pin` → `admin`)

### Trigger du mode admin
- **Long appui** (2 secondes) sur l'heure en haut de l'écran d'accueil
- Discret : papa ne le fera jamais par accident

### PIN par défaut
- `0000` (modifiable dans l'écran admin)

### Écran Admin — fonctionnalités
- **Bouton SOS** : afficher/masquer + numéro configurable
- **Code admin** : changement du PIN (4 chiffres)
- **Mode kiosque** : activer/désactiver (ouvre les paramètres d'accessibilité)
- **Bouton retour** vers l'écran d'accueil

### Préférences stockées (Prefs.kt)
| Clé | Type | Défaut |
|---|---|---|
| `admin_pin` | String | "0000" |
| `sos_number` | String | "112" |
| `sos_visible` | Boolean | true |
| `kiosk_enabled` | Boolean | false |
| `home_button_enabled` | Boolean | true |

---

## 10. Pastilles de notifications (badges)

### Objectif
Afficher une pastille rouge avec compteur sur les boutons Appels, SMS, Mail, WhatsApp.

### Appels manqués
- **Fichier** : `MissedCalls.kt`
- **Technique** : query `CallLog.Calls.CONTENT_URI` filtré sur `MISSED_TYPE` + `NEW = 1`
- **Permission** : `READ_CALL_LOG` (runtime)
- **State** : `MutableStateFlow<Int>` rafraîchi à chaque affichage de `HomeScreen`

### SMS / Mail / WhatsApp
- **Fichier** : `PapyNotificationListener.kt`
- **Technique** : `NotificationListenerService` qui compte les notifications actives par package
- **Packages surveillés** :
  - SMS : `com.google.android.apps.messaging`, `com.samsung.android.app.messaging`, `com.android.mms`, `com.android.messaging`
  - Mail : `com.google.android.gm`, `com.samsung.android.app.email`, `com.android.email`
  - WhatsApp : `com.whatsapp`, `com.whatsapp.w4b`
- **State** : `MutableStateFlow<Int>` par catégorie, rafraîchi à chaque notification postée/supprimée

### Bug critique corrigé
- `getActiveNotifications()` ne peut être appelé **que depuis le NotificationListenerService lui-même**, pas depuis l'activity
- Solution : le service bind une référence de lui-même dans `BadgeStore`, et `countNotifications()` est appelé depuis le service

### Activation manuelle sur le téléphone (OBLIGATOIRE)
Le `NotificationListenerService` ne s'active pas via une boîte de dialogue. Il faut :
1. **Paramètres** → **Notifications** → **Accès aux notifications** (ou "Notifications d'apps" → "Accès aux notifications")
2. Trouver **Papy Launcher** dans la liste → **activer**

### Composable Badge
- Cercle rouge 32dp avec bordure blanche 2dp
- Texte 14sp blanc, "99+" si > 99
- Position : `Alignment.TopEnd` sur chaque bouton

---

## 11. Mode kiosque (blocage de sortie)

### Objectif
Empêcher papa de quitter le launcher par erreur (swipe notifications, bouton récents, ouverture d'applis non autorisées).

### Technique utilisée : AccessibilityService
- **Fichier** : `PapyKioskService.kt`
- **Config** : `res/xml/kiosk_service_config.xml`
- Écoute `TYPE_WINDOW_STATE_CHANGED` + `TYPE_WINDOWS_CHANGED`
- Si une appli non autorisée s'ouvre → `performGlobalAction(GLOBAL_ACTION_HOME)`
- Si SystemUI (panneau notifications) s'ouvre → `GLOBAL_ACTION_HOME`
- Flag `enabled` contrôlé par l'admin (on/off)

### Applis autorisées (liste blanche)
```
com.papy.launcher (launcher lui-même)
com.android.dialer, com.google.android.dialer, com.samsung.android.dialer
com.android.mms, com.samsung.android.app.messaging, com.google.android.apps.messaging, com.android.messaging
com.google.android.gm, com.samsung.android.app.email, com.android.email
com.whatsapp, com.whatsapp.w4b
com.google.android.apps.photos, com.samsung.android.gallery, com.android.gallery, com.sec.android.gallery3d
com.android.camera, com.sec.android.app.camera, com.google.android.GoogleCamera
android, com.android.settings
com.google.android.contacts, com.samsung.android.app.contacts, com.android.contacts
```

### Mode immersive
- `hideSystemBars()` dans MainActivity
- Flags : `IMMERSIVE_STICKY | FULLSCREEN | HIDE_NAVIGATION | LAYOUT_*`
- Appelé dans `onCreate` et `onWindowFocusChanged`

### Limitations constatées
- Le swipe vers le bas pour ouvrir les notifications n'est **pas parfaitement bloqué** par l'AccessibilityService (réaction trop lente, mode immersive sticky recache mais ne bloque pas le geste)
- **Solution robuste alternative** : Device Owner + Lock Task Mode (mode kiosque enterprise) — nécessite un factory reset ou un téléphone sans compte Google
- **Décision** : on en reste à l'AccessibilityService comme "best effort" pour l'instant

### Activation manuelle sur le téléphone (OBLIGATOIRE)
1. Mode admin → activer l'interrupteur "Mode kiosque"
2. Android ouvre **Paramètres** → **Accessibilité**
3. Trouver **Papy Launcher** → **activer**

---

## 12. Bouton Home flottant

### Objectif
Un bouton "Accueil" toujours visible par-dessus les autres applis pour que papa revienne au launcher.

### Technique
- **Fichier** : `HomeButtonService.kt`
- Service foreground qui affiche un `Button` via `WindowManager` en overlay
- Type de fenêtre : `TYPE_APPLICATION_OVERLAY` (Android 8+)
- Position : `Gravity.BOTTOM | CENTER_HORIZONTAL`, y=80
- Au clic : `Intent.ACTION_MAIN` + `CATEGORY_HOME` → revient au launcher

### Cycle de vie
- **`onPause()` (MainActivity)** : démarre le service (papa quitte le launcher)
- **`onResume()` (MainActivity)** : arrête le service (papa est sur le launcher)

### Service foreground
- Notification persistante (canal `home_button`, importance LOW)
- Icône : `android.R.drawable.star_on`
- Type : `specialUse` (requis Android 14+)

### Permission manuelle sur le téléphone (OBLIGATOIRE)
1. **Paramètres** → **Applications** → **Papy Launcher** → **Afficher au-dessus des autres applications**
2. → **Autoriser**
(ou : Paramètres → Affichage → Afficher au-dessus des autres applis → Papy Launcher)

---

## 13. Heure et date sur l'écran d'accueil

### Composable `ClockHeader`
- Heure en **48sp** bleu foncé (`0xFF1A237E`), format `HH:mm`
- Date en **22sp** gris foncé (`0xFF333333`), format `EEEE d MMMM` (ex : "Vendredi 31 juillet")
- Locale : `Locale.FRANCE` pour la date
- Mise à jour toutes les 1 secondes via `LaunchedEffect` + `delay(1000L)`
- Long appui sur l'heure → ouvre le mode admin (PIN)

---

## 14. Thème — forçage en mode clair

### Problème
Le thème Compose suivait le système (sombre sur le Pixel 6a) → heure et date illisibles (bleu foncé et gris sur fond noir).

### Solution
- `PapyLauncherTheme` : `darkTheme = false`, `dynamicColor = false` → force `LightColorScheme`
- `Surface` dans MainActivity : `color = Color.White` explicite
- Date : couleur `0xFF333333` (gris foncé) pour meilleur contraste

---

## 15. Écran de configuration admin — raccourcis affichés

### Objectif
L'admin peut activer/désactiver chaque raccourci sur l'écran d'accueil.

### Architecture
- **`Shortcuts.kt`** : enum `ShortcutId` + data class `Shortcut` (id, label, couleur, défaut activé)
- **`Prefs.kt`** : `isShortcutEnabled()` / `setShortcutEnabled()` / `getEnabledShortcuts()` (stocké par clé `shortcut_<ID>`)
- **`HomeScreen`** : grille dynamique construite à partir des raccourcis activés (`Prefs.getEnabledShortcuts()`), groupés par rangées de 2
- **`AdminScreen`** : section "Raccourcis affichés" avec interrupteur on/off par raccourci + pastille de couleur

### Comportement
- Désactiver un raccourci → il disparaît de l'écran d'accueil immédiatement
- Réactiver → il réapparaît
- L'ordre des boutons suit l'ordre de `Shortcuts.all`

### Scroll de l'écran admin
- `AdminScreen` utilise `verticalScroll(rememberScrollState())` car le contenu dépasse l'écran

---

## 16. Liste d'applis custom

### Objectif
Remplacer le Toast "Bientôt disponible" par un écran qui liste toutes les applis installées.

### Fichier
- **`AppListScreen.kt`**

### Fonctionnement
- `loadInstalledApps()` : query `PackageManager.getInstalledApplications()` + filtre celles avec `getLaunchIntentForPackage` non null + exclut Papy Launcher lui-même + tri alphabétique
- `AppListScreen` : colonne scrollable avec bouton "Retour" + titre "Applications" + liste des applis
- `AppRow` : ligne avec icône 48dp (via `AndroidView` + `ImageView`) + nom 20sp
- Tap sur une appli → `launchApp()` via `getLaunchIntentForPackage`

### Navigation
- `AppNavigation` : état `"applist"` → `AppListScreen(onBack = { screen = "home" })`
- `HomeScreen` : `onApplis` callback → `screen = "applist"`

---

## 17. Visionneur photos intégré

### Objectif
Un visionneur de photos léger intégré au launcher (sans pub, sans menus complexes).

### Fichier
- **`PhotosScreen.kt`**

### Technique
- **Chargement des photos** : `MediaStore.Images.Media.EXTERNAL_CONTENT_URI` query (id + name), tri par `DATE_ADDED DESC`
- **Affichage des images** : lib **Coil** (`coil-compose` 2.7.0) — `AsyncImage` pour charger les URI
- **Grille** : `LazyVerticalGrid` avec 3 colonnes, vignettes carrées (`aspectRatio(1f)`)
- **Plein écran** : tap sur une vignette → `FullscreenPhoto` (fond noir, `ContentScale.Fit`, tap pour fermer)

### Permissions photos (multi-version)
| Version Android | Permission runtime | Permission manifeste |
|---|---|---|
| Android 13+ (API 33+, TIRAMISU) | `READ_MEDIA_IMAGES` | ✅ |
| Android 12 et avant (API ≤ 32) | `READ_EXTERNAL_STORAGE` | ✅ (`maxSdkVersion="32"`) |

- `requestEssentialPermissions()` dans MainActivity : branche sur `Build.VERSION.SDK_INT >= TIRAMISU`
- `multiPermissionLauncher` demande toutes les permissions d'un coup au démarrage

### Dépendance Coil
- `gradle/libs.versions.toml` : `coil = "2.7.0"` + `coil-compose`
- `app/build.gradle.kts` : `implementation(libs.coil.compose)`

### Navigation
- `AppNavigation` : état `"photos"` → `PhotosScreen(onBack = { screen = "home" })`
- `HomeScreen` : `onPhotos` callback → `screen = "photos"`
- Le bouton Photos de l'écran d'accueil lance maintenant `PhotosScreen` au lieu de la galerie externe

---

## 18. Fichiers créés / modifiés (MÀJ)

### Fichiers créés
| Fichier | Rôle |
|---|---|
| `Prefs.kt` | Stockage SharedPreferences (PIN, SOS, kiosque, home button, raccourcis) |
| `PinScreen.kt` | Écran de saisie du code admin (pavé numérique) |
| `AdminScreen.kt` | Écran de configuration admin (SOS, PIN, kiosque, raccourcis) — scrollable |
| `PapyNotificationListener.kt` | Service d'écoute des notifications (badges SMS/mail/WhatsApp) |
| `MissedCalls.kt` | Compteur d'appels manqués (query CallLog) |
| `PapyKioskService.kt` | AccessibilityService mode kiosque |
| `HomeButtonService.kt` | Bouton Home flottant (overlay) |
| `Shortcuts.kt` | Modèle de données des raccourcis (enum + data class) |
| `AppListScreen.kt` | Liste des applis installées avec icônes |
| `PhotosScreen.kt` | Visionneur photos (grille + plein écran) |
| `res/xml/kiosk_service_config.xml` | Config du service d'accessibilité |

### Fichiers modifiés
| Fichier | Modifications |
|---|---|
| `AndroidManifest.xml` | Permissions (CALL_PHONE, READ_CALL_LOG, READ_MEDIA_IMAGES, READ_EXTERNAL_STORAGE, SYSTEM_ALERT_WINDOW, FOREGROUND_SERVICE, FOREGROUND_SERVICE_SPECIAL_USE), queries, intent-filter HOME, services (notif listener, kiosk, home button) |
| `MainActivity.kt` | UI Compose, navigation 5 écrans, lancement d'applis, permissions runtime (multi-version), immersive mode, services, ClockHeader |
| `AdminScreen.kt` | Scroll vertical, section raccourcis |
| `strings.xml` | Libellés FR, description du service kiosque |
| `ui/theme/Theme.kt` | Forçage thème clair |
| `gradle/libs.versions.toml` | Ajout Coil 2.7.0 |
| `app/build.gradle.kts` | Dépendance coil-compose |

---

## 19. Bugs corrigés (chronologie MÀJ)

| Bug | Cause | Solution |
|---|---|---|
| WhatsApp "non installé" | Android 11+ visibility filtering | Ajout bloc `<queries>` dans manifeste |
| SOS crash | `CALL_PHONE` permission runtime non demandée | `ActivityResultContracts.RequestPermission()` |
| `Modifier.weight()` unresolved | `BigButton` hors `RowScope` | Extension `RowScope.BigButton` |
| `CATEGORY_APP_LAUNCHER` unresolved | N'existe pas dans l'API | Toast temporaire puis AppListScreen |
| `ExperimentalComposeUiApi` unresolved | Mauvais import | `ExperimentalFoundationApi` pour `combinedClickable` |
| `contentResolver` unresolved | Appelé sur `NotificationManager` au lieu de `Context` | Extension `Context.isNotificationListenerAccessGranted()` |
| `onListenerDestroyed` overrides nothing | Mauvaise méthode | `onListenerDisconnected` |
| Pastilles SMS ne s'affichaient pas | `getActiveNotifications()` appelé depuis l'activity au lieu du service | Refactor : le service bind lui-même dans `BadgeStore` |
| `ic_menu_home` unresolved | Ressource inexistante | `android.R.drawable.star_on` |
| `Notification.Builder.build()` unresolved | API level | Branchement `if (Build.VERSION.SDK_INT >= O)` |
| `startForeground()` crash | `foregroundServiceType` manquant | Ajout `android:foregroundServiceType="specialUse"` + permission `FOREGROUND_SERVICE_SPECIAL_USE` |
| Heure/date illisible | Thème sombre suivi du système | Forçage `LightColorScheme` + `Color.White` |
| Écran admin tronqué | Pas de scroll | `verticalScroll(rememberScrollState())` |
| `stringResource` unresolved dans AdminScreen | Import manquant | Ajout `import androidx.compose.ui.res.stringResource` |
| "Aucune photo trouvée" | Permission photos non demandée au runtime | `requestEssentialPermissions()` branche sur `READ_MEDIA_IMAGES` (Android 13+) ou `READ_EXTERNAL_STORAGE` (≤ 12) |

---

## 20. Paramétrages manuels sur le téléphone (RÉCAPITULATIF MÀJ)

### À faire une seule fois (par l'admin / toi)

| # | Paramètre | Chemin | Action |
|---|---|---|---|
| 1 | Mode développeur | Paramètres → À propos du téléphone → taper 7× sur "Numéro de version" | Activer |
| 2 | Débogage USB | Paramètres → Options de développement | Activer |
| 3 | Launcher par défaut | Appuyer sur bouton Home → choisir "Papy Launcher" → "Toujours" | Sélectionner |
| 4 | Permission journal d'appels | Au premier lancement, boîte de dialogue | Accorder |
| 5 | Permission photos | Au premier lancement, boîte de dialogue | Accorder |
| 6 | Accès aux notifications | Paramètres → Notifications → Accès aux notifications → Papy Launcher | Activer |
| 7 | Affichage au-dessus des autres applis | Paramètres → Applications → Papy Launcher → Afficher au-dessus des autres applications | Autoriser |
| 8 | Service d'accessibilité (kiosque) | Paramètres → Accessibilité → Papy Launcher | Activer (si mode kiosque souhaité) |
| 9 | Permission appel (CALL_PHONE) | Au premier appui sur SOS, boîte de dialogue | Accorder |

### À faire dans l'app (mode admin)

| # | Action | Comment |
|---|---|---|
| 1 | Ouvrir le mode admin | Long appui (2s) sur l'heure en haut de l'écran |
| 2 | Saisir le PIN | `0000` par défaut |
| 3 | Configurer le SOS | Numéro + affichage on/off |
| 4 | Changer le PIN | 4 chiffres |
| 5 | Activer le kiosque | Interrupteur (ouvre les paramètres d'accessibilité) |
| 6 | Activer/désactiver les raccourcis | Section "Raccourcis affichés" — interrupteurs par bouton |

---

## 21. Roadmap (MÀJ)

| Priorité | Feature | Statut |
|---|---|---|
| Haute | Mode admin + PIN | ✅ Fait |
| Haute | Pastilles de notif (appels, SMS, mail, WhatsApp) | ✅ Fait |
| Haute | Mode kiosque (AccessibilityService) | ✅ Fait (best effort) |
| Haute | Heure et date sur l'écran d'accueil | ✅ Fait |
| Haute | Bouton Home flottant | ✅ Fait |
| Haute | Écran de configuration admin : choisir les raccourcis | ✅ Fait |
| Moyenne | Liste d'applis custom | ✅ Fait |
| Moyenne | Visionneur photos intégré léger | ✅ Fait |
| Moyenne | Raccourcis réglages simplifiés (Wi-Fi, données, luminosité) — admin only | À faire |
| Basse | Restore README.md et LICENSE dans le repo + premier commit | À faire |
| Futur | Device Owner + Lock Task Mode (kiosque total) | Étude |

---

## 22. Notes techniques (MÀJ)

- **minSdk 23** : compatible Android 6.0+ → tourne sur le Galaxy A13 (Android 13) et le Pixel 6a (Android 16)
- **Dépendance externe unique** : Coil 2.7.0 (chargement d'images) — tout le reste est AndroidX + Compose
- **Pas de réseau** : aucune donnée ne quitte le téléphone (Coil charge uniquement des URI locales)
- **Kotlin + Jetpack Compose** : UI déclarative, moderne, lisible
- **StateFlow** : mise à jour réactive des badges et compteurs
- **SharedPreferences** : stockage local simple (PIN, préférences, raccourcis)
- **Permissions gérées par version** : photos (READ_MEDIA_IMAGES vs READ_EXTERNAL_STORAGE), foreground service (specialUse)