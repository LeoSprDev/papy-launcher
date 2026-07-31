# Papy Launcher — Handoff

**Date :** 2026-07-31
**Repo GitHub :** https://github.com/LeoSprDev/papy-launcher
**Chemin local :** `C:\yo\01_Dev\02_Applis\05_Android_launcher\papy-launcher`
**APK debug :** `app\build\outputs\apk\debug\app-debug.apk` (12.2 Mo)

---

## État du projet

Papy Launcher est un launcher Android simple et accessible pour seniors (85 ans), développé en Kotlin + Jetpack Compose. Il tourne sur Android 6.0+ (minSdk 23), testé sur Pixel 6a (Android 16), ciblé pour Samsung Galaxy A13 4G (Android 13).

### Ce qui marche (validé sur device)

| Feature | Statut |
|---|---|
| Écran d'accueil avec gros boutons colorés | ✅ |
| Appels (dialer) | ✅ |
| SMS (appli native) | ✅ |
| WhatsApp (lancement + détection) | ✅ |
| Mail (appli native) | ✅ |
| Photos (visionneur intégré — grille + plein écran, lib Coil) | ✅ |
| Liste des applis installées (icônes + noms) | ✅ |
| Bouton SOS (appel direct, numéro configurable) | ✅ |
| Mode admin (PIN 4 chiffres, trigger : long appui sur l'heure) | ✅ |
| Pastilles notif (appels manqués, SMS, mail, WhatsApp) | ✅ |
| Bouton Home flottant au-dessus des autres applis | ✅ |
| Heure + date en gros sur l'écran d'accueil | ✅ |
| Thème clair forcé (lisibilité) | ✅ |
| Mode kiosque (AccessibilityService — best effort) | ✅ |
| Config admin : SOS on/off + numéro | ✅ |
| Config admin : changement de PIN | ✅ |
| Config admin : activer/désactiver raccourcis | ✅ |
| Config admin : réglages rapides (Wi-Fi, données, Bluetooth, affichage, son) | ✅ |
| Config admin : slider luminosité | ✅ |
| Écran admin scrollable | ✅ |
| Génération APK debug | ✅ |
| Git local + remote à jour | ✅ |

### Ce qui reste à faire

| Feature | Priorité | Notes |
|---|---|---|
| Raccourcis réglages simplifiés sur l'écran d'accueil (pour papa) | Moyenne | Actuellement les réglages sont admin-only. Si papa doit pouvoir activer/désactiver Wi-Fi lui-même, il faudrait des boutons simplifiés sur l'accueil (ou pas, selon choix) |
| Device Owner + Lock Task Mode | Futur | Kiosque total (bloque vraiment le swipe notifs). Nécessite factory reset ou téléphone sans compte Google. Voir journal de dev section 11 |
| APK signé (release) | Basse | Pour publication Play Store éventuelle. `keytool` + `build.gradle.kts` signingConfig |
| Icône d'appli personnalisée | Basse | Actuellement icône Android par défaut |
| Tests unitaires / instrumentés | Basse | Squelettes présents (ExampleUnitTest, ExampleInstrumentedTest) mais vides |
| Internationalisation (EN) | Basse | Tout est en FR dur dans strings.xml + code |

---

## Comment reprendre le projet

### 1. Ouvrir dans Android Studio
- File → Open → sélectionner `C:\yo\01_Dev\02_Applis\05_Android_launcher\papy-launcher`
- Attendre l'indexation + sync Gradle (~1-2 min)

### 2. Brancher un téléphone
- Activer débogage USB (Paramètres → Options de développement)
- Brancher en USB → accepter "Autoriser le débogage USB"

### 3. Lancer
- Sélectionner le device dans le menu déroulant (en haut)
- **Run ▶️** (`Shift+F10`)

### 4. Générer un APK
```powershell
cd C:\yo\01_Dev\02_Applis\05_Android_launcher\papy-launcher
.\gradlew.bat assembleDebug
```
APK → `app\build\outputs\apk\debug\app-debug.apk`

### 5. Installer l'APK sur un téléphone
- Copier `app-debug.apk` sur le téléphone (USB, Drive, etc.)
- Sur le téléphone, ouvrir le fichier → Installer
- Activer "Sources inconnues" si demandé

### 6. Paramétrages obligatoires sur le téléphone
→ Voir `docs/parametrage-telephone.md` (11 étapes dans l'ordre)

---

## Architecture du code

```
app/src/main/java/com/papy/launcher/
├── MainActivity.kt          — Activity principale, navigation 5 écrans, UI accueil, lancement applis, permissions runtime
├── Prefs.kt                — SharedPreferences (PIN, SOS, kiosque, home button, raccourcis)
├── Shortcuts.kt             — Modèle de données des raccourcis (enum ShortcutId + data class Shortcut)
├── PinScreen.kt            — Écran saisie PIN (pavé numérique grand)
├── AdminScreen.kt           — Écran admin (SOS, PIN, kiosque, raccourcis, réglages rapides, luminosité) — scrollable
├── AppListScreen.kt         — Liste applis installées (icônes + noms)
├── PhotosScreen.kt          — Visionneur photos (grille LazyVerticalGrid + plein écran, Coil)
├── PapyNotificationListener.kt — NotificationListenerService (badges SMS/mail/WhatsApp)
├── MissedCalls.kt           — Compteur appels manqués (query CallLog)
├── PapyKioskService.kt     — AccessibilityService mode kiosque (liste blanche applis)
├── HomeButtonService.kt    — Service foreground bouton Home flottant (WindowManager overlay)
└── ui/theme/
    ├── Theme.kt             — Thème clair forcé
    ├── Color.kt             — Couleurs
    └── Type.kt              — Typographie
```

### Navigation (AppNavigation dans MainActivity)
```
"home"  ←→  "pin"  ←→  "admin"
"home"  ←→  "applist"
"home"  ←→  "photos"
```

### Flux du mode admin
1. Long appui (2s) sur l'heure → `"pin"`
2. PIN correct → `"admin"`
3. "Retour" → `"home"`

### Préférences stockées (Prefs.kt → SharedPreferences `papy_prefs`)
| Clé | Type | Défaut |
|---|---|---|
| `admin_pin` | String | "0000" |
| `sos_number` | String | "112" |
| `sos_visible` | Boolean | true |
| `kiosk_enabled` | Boolean | false |
| `home_button_enabled` | Boolean | true |
| `shortcut_<ID>` | Boolean | true (par raccourci) |

---

## Permissions requises

### Manifeste
| Permission | Rôle | Runtime ? |
|---|---|---|
| `CALL_PHONE` | SOS (appel direct) | Oui (boîte dialogue) |
| `READ_CALL_LOG` | Pastille appels manqués | Oui (multiPermission au démarrage) |
| `READ_MEDIA_IMAGES` (Android 13+) | Visionneur photos | Oui (multiPermission au démarrage) |
| `READ_EXTERNAL_STORAGE` (Android ≤12) | Visionneur photos | Oui |
| `SYSTEM_ALERT_WINDOW` | Bouton Home flottant | Oui (manuel, Paramètres) |
| `FOREGROUND_SERVICE` | HomeButtonService | Non |
| `FOREGROUND_SERVICE_SPECIAL_USE` | HomeButtonService (Android 14+) | Non |
| `WRITE_SETTINGS` | Slider luminosité | Oui (manuel, Paramètres) |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Badges notif | Oui (manuel, Paramètres) |
| `BIND_ACCESSIBILITY_SERVICE` | Mode kiosque | Oui (manuel, Paramètres) |

### Activation manuelle obligatoire (voir parametrage-telephone.md)
- Accès aux notifications
- Affichage au-dessus des autres applis
- Service d'accessibilité (kiosque)
- Écriture des réglages (luminosité)

---

## Dépendances

### Internes (AndroidX + Compose)
- `androidx.core:core-ktx`
- `androidx.lifecycle:lifecycle-runtime-ktx`
- `androidx.activity:activity-compose`
- `androidx.compose:compose-bom` (BOM 2026.02.01)
- `androidx.compose.material3:material3`
- `androidx.compose.ui:ui`, `ui-graphics`, `ui-tooling-preview`

### Externe
- `io.coil-kt:coil-compose` 2.7.0 — chargement d'images (visionneur photos)

### Versions clés (gradle/libs.versions.toml)
| Dépendance | Version |
|---|---|
| AGP | 9.3.1 |
| Kotlin | 2.2.10 |
| Compose BOM | 2026.02.01 |
| Coil | 2.7.0 |
| minSdk | 23 |
| targetSdk | 37 |

---

## Bugs connus / limitations

| Problème | Cause | Statut |
|---|---|---|
| Swipe notifications non bloqué | AccessibilityService trop lent, immersive sticky recache mais ne bloque pas | Limitation Android sans root. Alternative : Device Owner + Lock Task Mode |
| Bouton Home flottant peut disparaître | Android peut tuer le service foreground sous pression mémoire | Relancer l'app si besoin |
| Pas d'icône perso | Icône Android par défaut | À faire |
| `getActiveNotifications()` depuis l'activity | Ne marche que depuis le service | Contourné : service bind lui-même dans BadgeStore |

---

## Commandes utiles

### Build
```powershell
cd C:\yo\01_Dev\02_Applis\05_Android_launcher\papy-launcher
.\gradlew.bat assembleDebug          # APK debug
.\gradlew.bat assembleRelease         # APK release (nécessite signing config)
```

### Git
```powershell
git add -A
git commit -m "description"
git push
```

### ADB (installer l'APK via USB)
```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"
```

---

## Documentation

| Fichier | Contenu |
|---|---|
| `docs/2026-07-31-journal-dev.md` | Journal complet : contexte, install, code, bugs, décisions |
| `docs/parametrage-telephone.md` | Paramétrages téléphone étape par étape (11 étapes) |
| `docs/handoff.md` | Ce fichier — état du projet, architecture, reprise |

---

## Contact / contexte

- **Demandeur :** Fils de papa (85 ans)
- **Téléphone cible :** Samsung Galaxy A13 4G (Android 13)
- **Téléphone de test :** Google Pixel 6a (Android 16)
- **OS de dev :** Windows + Android Studio
- **Objectif :** Launcher simple, privacy-first, gros boutons, mode kiosque, admin PIN