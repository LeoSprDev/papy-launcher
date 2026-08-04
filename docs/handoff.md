# Papy Launcher — Handoff

**Date :** 2026-08-04
**Repo GitHub :** https://github.com/LeoSprDev/papy-launcher
**APK debug :** `app/build/outputs/apk/debug/app-debug.apk` (~19 Mo)

---

## État du projet

Papy Launcher est un launcher Android simple et accessible pour seniors (85 ans), développé en Kotlin + Jetpack Compose. Il tourne sur Android 6.0+ (minSdk 23), testé sur Pixel 6a (Android 16), ciblé pour Samsung Galaxy A13 4G (Android 13).

### Ce qui marche (validé sur device)

| Feature | Statut |
|---|---|
| Écran d'accueil avec gros boutons colorés (grille 2 colonnes, taille fixe) | ✅ |
| Appels (dialer) | ✅ |
| SMS (appli native) | ✅ |
| WhatsApp (lancement + détection) | ✅ |
| Mail (appli native) | ✅ |
| Photos (visionneur intégré — grille + plein écran, lib Coil) | ✅ |
| Liste des applis installées (icônes + noms) | ✅ |
| Bouton SOS (appel direct, numéro configurable) | ✅ |
| Mode admin (PIN 4 chiffres, trigger : 7 clics sur l'horloge) | ✅ |
| Pastilles notif (appels manqués, SMS, mail, WhatsApp) | ✅ |
| Bouton Home flottant au-dessus des autres applis (centre-droite) | ✅ |
| Heure + date en gros sur l'écran d'accueil | ✅ |
| Indicateur batterie (pourcentage + icône, couleur selon niveau) | ✅ |
| Thème clair forcé (lisibilité) | ✅ |
| Mode kiosque (AccessibilityService — best effort, liste blanche dynamique) | ✅ |
| Config admin : SOS on/off + numéro | ✅ |
| Config admin : changement de PIN | ✅ |
| Config admin : activer/désactiver raccourcis (9 boutons) | ✅ |
| Config admin : réglages rapides (Wi-Fi, données, Bluetooth, affichage, son) | ✅ |
| Config admin : slider luminosité | ✅ |
| Config admin : liens directs vers autorisations système (notifications, overlay, accessibilité, write settings, launcher par défaut) | ✅ |
| Config admin : indicateurs d'état des autorisations (pastilles vertes/grises + coche) | ✅ |
| Config admin : boutons redemander permissions runtime (CALL_PHONE, READ_CALL_LOG, photos, READ_CONTACTS) | ✅ |
| Contacts favoris (photo + appel direct) | ✅ |
| Gestion des favoris (écran dédié : ajouter/retirer) | ✅ |
| Tuiles d'applis dynamiques (admin choisit les applis, icône native, couleur Bleu Acier) | ✅ |
| Gestion des applis dynamiques (écran dédié : ajouter/retirer) | ✅ |
| Écran admin scrollable | ✅ |
| Génération APK debug | ✅ |
| Git local + remote à jour (main) | ✅ |

### Boutons raccourcis (9 configurables)

Ordre d'affichage sur l'écran d'accueil (haut-gauche → bas-droite, 2 par ligne) :

| Position | ID | Label | Couleur | Icône | Action |
|---|---|---|---|---|---|
| 1 | APPELS | Appels | Vert Appel #2E7D32 | Phone | Dialer natif |
| 2 | FAVORIS | Favoris | Indigo Favoris #3949AB | Star | Écran favoris (grille photo + appel direct) |
| 3 | SMS | SMS | Bleu Message #1565C0 | Sms | Appli SMS native |
| 4 | WHATSAPP | WhatsApp | Vert WhatsApp #075E54 | Chat | Lance WhatsApp |
| 5 | MAIL | Mail | Orange Courrier #EF6C00 | Email | Appli mail native |
| 6 | PHOTOS | Photos | Violet Album #6A1B9A | Photo | Visionneur photos intégré |
| 7 | APPLIS | Applis | Gris Réglages #455A64 | Apps | Liste applis installées |
| 8 | APPAREIL_PHOTO | Appareil photo | Teal Caméra #00695C | PhotoCamera | Caméra native (Intent) |
| 9 | PROUT | Prout | Violet Prout #8E63BC | SentimentVerySatisfied | Son synthétisé via AudioTrack |

> **Grille fusionnée :** les raccourcis fixes et les tuiles d'applis dynamiques sont fusionnés en une seule liste avant le chunking par 2. La première tuile dynamique remplit le trou de la dernière rangée fixe si le nombre de raccourcis fixes est impair.

### Tuiles d'applis dynamiques (illimitées)

| Caractéristique | Valeur |
|---|---|
| Couleur | Bleu Acier #546E7A |
| Icône | Icône native de l'appli (PackageManager.getApplicationIcon) dans un médaillon blanc 40dp |
| Label | Nom de l'appli (résolu via PackageManager à chaque affichage) |
| Action | `getLaunchIntentForPackage` + `safeStartActivity` |
| Ordre | Après les 9 raccourcis fixes, dans l'ordre d'ajout |
| Stockage | `dynamic_apps_list` en JSON dans SharedPreferences (packageName + label) |
| Sélection | Écran `AppPickerScreen` — liste des applis installées (reuse de `loadInstalledApps`) |
| Gestion | Écran `ManageAppsScreen` — ajouter/retirer, détection appli désinstallée |

### Ce qui reste à faire

| Feature | Priorité | Notes |
|---|---|---|
| Raccourcis réglages simplifiés sur l'écran d'accueil (pour papa) | Moyenne | Actuellement les réglages sont admin-only |
| Device Owner + Lock Task Mode | Futur | Kiosque total (bloque vraiment le swipe notifs). Nécessite factory reset |
| APK signé (release) | Basse | Pour publication Play Store éventuelle |
| Icône d'appli personnalisée | Basse | Actuellement icône Android par défaut |
| Tests unitaires / instrumentés | Basse | Squelettes présents mais vides |
| Internationalisation (EN) | Basse | Tout est en FR dur dans strings.xml + code |
| Mode Urgence + SMS location | Futur | Feature #2 du brainstorming (non démarrée) |
| Rappels visuels/vocaux + alerte aidant | Futur | Feature #3 du brainstorming (non démarrée) |

---

## Comment reprendre le projet

### 1. Ouvrir dans Android Studio
- File → Open → sélectionner le dossier du repo
- Attendre l'indexation + sync Gradle (~1-2 min)

### 2. Brancher un téléphone
- Activer débogage USB (Paramètres → Options de développement)
- Brancher en USB → accepter "Autoriser le débogage USB"

### 3. Lancer
- Sélectionner le device dans le menu déroulant (en haut)
- **Run ▶️** (`Shift+F10`)

### 4. Générer un APK
```bash
./gradlew assembleDebug
```
APK → `app/build/outputs/apk/debug/app-debug.apk` (~19 Mo)

### 5. Installer l'APK sur un téléphone
- Copier `app-debug.apk` sur le téléphone (USB, mail, Drive, etc.)
- Sur le téléphone, ouvrir le fichier → Installer
- Activer "Sources inconnues" si demandé

### 6. Paramétrages obligatoires sur le téléphone
→ Voir `docs/parametrage-telephone.md` (12 étapes dans l'ordre)

---

## Architecture du code

```
app/src/main/java/com/papy/launcher/
├── MainActivity.kt              — Activity principale, navigation 9 écrans (sealed class Screen), UI accueil, lancement applis, permissions runtime, trigger admin (7 clics horloge), grille fusionnée (fixes + dynamiques), batterie, HomeTile sealed class
├── Screen.kt                    — sealed class Screen (navigation type-safe : Home, Pin, Admin, ManageFavorites, ManageApps, AppPicker, AppList, Photos, Favorites)
├── Prefs.kt                     — SharedPreferences (PIN, SOS, kiosque, home button, raccourcis, favoris JSON, dynamic apps JSON)
├── Shortcuts.kt                — Modèle de données des raccourcis (enum ShortcutId 9 entrées + data class Shortcut)
├── DynamicApp.kt               — Data class DynamicApp + helpers Prefs (getDynamicApps, addDynamicApp, removeDynamicApp)
├── PinScreen.kt                — Écran saisie PIN (pavé numérique grand)
├── AdminScreen.kt              — Écran admin composeur (state hoisting + lifecycle observer + orchestration des sections). ~257 lignes (était 732).
├── AdminButtons.kt             — Composants UI admin réutilisables (SectionTitle, AdminButton, AdminLinkButton, PermissionButton)
├── AdminSectionSos.kt           — Section SOS (switch afficher + champ numéro)
├── AdminSectionPin.kt           — Section changement PIN
├── AdminSectionKiosk.kt         — Section mode kiosque (switch + warning + redirect accessibilité)
├── AdminSectionShortcuts.kt     — Section raccourcis affichés (boucle sur Shortcuts.all)
├── AdminSectionSystemPermissions.kt — Section autorisations système (5 AdminLinkButton via callbacks)
├── AdminSectionAppPermissions.kt — Section permissions runtime (4 PermissionButton)
├── AdminSectionFavorites.kt     — Section favoris (bouton + warning contacts)
├── AdminSectionApps.kt         — Section applis (bouton gérer)
├── AdminSectionQuickSettings.kt — Section réglages rapides (5 boutons + slider luminosité)
├── Permissions.kt              — Helpers permissions (isGranted, isPhotosPermissionGranted, isNotifListenerEnabled, isDefaultLauncher) — internal
├── ManageFavoritesScreen.kt    — Écran dédié gestion favoris (ajouter via picker contacts, retirer, liste avec photo + nom + numéro)
├── ManageAppsScreen.kt         — Écran dédié gestion applis dynamiques (ajouter/retirer, détection désinstallée)
├── AppPickerScreen.kt          — Écran de sélection d'applis (liste des applis installées, tap = ajout)
├── FavoritesScreen.kt          — Écran favoris (grille LazyVerticalGrid 2 colonnes, photo + prénom, appel direct)
├── ContactsHelper.kt           — Queries ContactsContract (getContactByLookupKey par LOOKUP_KEY, getPhoneNumber)
├── AppListScreen.kt            — Liste applis installées (icônes + noms) + loadInstalledApps + AppRow + AppIcon (réutilisés)
├── PhotosScreen.kt             — Visionneur photos (grille LazyVerticalGrid + plein écran, Coil)
├── ProutSound.kt               — Synthèse son prout via AudioTrack
├── PapyNotificationListener.kt  — NotificationListenerService (badges SMS/mail/WhatsApp)
├── MissedCalls.kt               — Compteur appels manqués (query CallLog)
├── PapyKioskService.kt         — AccessibilityService mode kiosque (liste blanche de base + applis dynamiques)
├── HomeButtonService.kt         — Service foreground bouton Home flottant (WindowManager overlay, centre-droite)
├── ui/components/
│   └── ScreenHeader.kt          — Header réutilisable (bouton Retour bleu + titre 28sp) — utilisé par 6 écrans
└── ui/theme/
    ├── Theme.kt                 — Thème clair forcé
    ├── Color.kt                 — Couleurs (constantes Papy* + Material3 Purple/Pink pour Theme.kt)
    └── Type.kt                  — Typographie
```

### Navigation (AppNavigation dans MainActivity)
```
Screen.Home  ←→  Screen.Pin  ←→  Screen.Admin  ←→  Screen.ManageFavorites
                                          ←→  Screen.ManageApps  ←→  Screen.AppPicker
Screen.Home  ←→  Screen.AppList
Screen.Home  ←→  Screen.Photos
Screen.Home  ←→  Screen.Favorites
```

> Navigation type-safe via `sealed class Screen` (Screen.kt). Le `when` dans `AppNavigation()` est exhaustif — le compilateur vérifie tous les cas, plus de string literals.

### Flux du mode admin
1. 7 clics sur l'horloge (dans les 2 secondes) → `Screen.Pin`
2. PIN correct → `Screen.Admin`
3. "Retour" → `Screen.Home`
4. Admin → "Gérer les favoris" → `Screen.ManageFavorites` → "Retour" → `Screen.Admin`
5. Admin → "Gérer les applis" → `Screen.ManageApps` → "Ajouter une appli" → `Screen.AppPicker` → "Retour" → `Screen.ManageApps` → "Retour" → `Screen.Admin`

### Préférences stockées (Prefs.kt → SharedPreferences `papy_prefs`)
| Clé | Type | Défaut |
|---|---|---|
| `admin_pin` | String | "0000" |
| `sos_number` | String | "112" |
| `sos_visible` | Boolean | true |
| `kiosk_enabled` | Boolean | false |
| `home_button_enabled` | Boolean | true |
| `shortcut_<ID>` | Boolean | true (par raccourci) |
| `favorites_list` | String (JSON) | null (liste vide) |
| `dynamic_apps_list` | String (JSON) | null (liste vide) |

### Section « Autorisations système » (admin)
Boutons avec indicateurs d'état (pastille verte + coche si actif, pastille grise sinon) :
- Accès aux notifications → `ACTION_NOTIFICATION_LISTENER_SETTINGS`
- Affichage au-dessus des autres applis → `ACTION_MANAGE_OVERLAY_PERMISSION`
- Service d'accessibilité (kiosque) → `ACTION_ACCESSIBILITY_SETTINGS`
- Modification des réglages → `ACTION_MANAGE_WRITE_SETTINGS`
- Launcher par défaut → `ACTION_HOME_SETTINGS` (API 24+)

### Section « Permissions de l'application » (admin)
Boutons « Redemander » (boîte système runtime, fallback page d'infos appli si refus définitif) :
- Appels téléphoniques (SOS) → `CALL_PHONE`
- Journal d'appels (appels manqués) → `READ_CALL_LOG`
- Photos (visionneur) → `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE`
- Contacts (favoris) → `READ_CONTACTS`

### Section « Favoris » (admin)
Bouton unique « Gérer les favoris » → navigue vers `ManageFavoritesScreen`. Si la permission `READ_CONTACTS` n'est pas accordée, un message d'avertissement s'affiche sous le bouton.

### Section « Applis » (admin)
Bouton unique « Gérer les applis » → navigue vers `ManageAppsScreen`.

### Indicateur batterie (écran d'accueil)
- Icône Material Battery (niveau dynamique : 0-5 barres) + pourcentage
- Couleur : vert >30%, orange ≤30%, rouge ≤15% (pas d'icône éclair, même en charge)
- Rafraîchi toutes les 30 secondes via `ACTION_BATTERY_CHANGED` sticky broadcast
- Position : haut-droite, superposé à l'horloge (ne décentre pas l'horloge)

---

## Permissions requises

### Manifeste
| Permission | Rôle | Runtime ? |
|---|---|---|
| `CALL_PHONE` | SOS + favoris (appel direct) | Oui (boîte dialogue) |
| `READ_CALL_LOG` | Pastille appels manqués | Oui (multiPermission au démarrage) |
| `READ_MEDIA_IMAGES` (Android 13+) | Visionneur photos | Oui (multiPermission au démarrage) |
| `READ_EXTERNAL_STORAGE` (Android ≤12) | Visionneur photos | Oui |
| `READ_CONTACTS` | Favoris (lookupKey → photo/numéro/prénom) | Oui (à la demande, pas au démarrage) |
| `QUERY_ALL_PACKAGES` | Visibilité de toutes les applis installées (AppPickerScreen) | Non (permission normal) |
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
- `androidx.compose.material:material-icons-extended`
- `androidx.compose.ui:ui`, `ui-graphics`, `ui-tooling-preview`

### Externe
- `io.coil-kt:coil-compose` 2.7.0 — chargement d'images (visionneur photos + photos favoris)

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
| Bouton Home clignote avec applis système (ex: Paramètres) | `onWindowFocusChanged` transitoire lors du lancement d'applis système | Contournement : ne pas ajouter d'applis système comme tuiles dynamiques (cas non prévu pour papa) |
| Pas d'icône d'appli perso | Icône Android par défaut | À faire |
| `getActiveNotifications()` depuis l'activity | Ne marche que depuis le service | Contourné : service bind lui-même dans BadgeStore |
| `LocalLifecycleOwner` déprécié | Déplacé vers `lifecycle-runtime-compose` (non ajouté au projet) | Warning bénin, fonctionne toujours |

### Bugs résolus (historique)

| Problème | Cause | Solution | Issue |
|---|---|---|---|
| Favoris vides | `getLookupUri(-1L, lookupKey)` générait une URI `.../lookup/<key>/-1` rejetée par le provider contacts (IllegalArgumentException) | Query directe sur `Contacts.CONTENT_URI` avec `WHERE LOOKUP_KEY = ?` | #1 ✅ |
| Pas d'UI retirer favori | Conséquence du bug #1 : la liste admin était vide | Corrigé automatiquement avec #1 | #2 ✅ |
| Kiosque bloque les applis dynamiques | `ALLOWED_PACKAGES` fixe ne incluait pas les applis ajoutées dynamiquement | `allowedPackages(context)` fusionne `BASE_ALLOWED_PACKAGES` + `Prefs.getDynamicApps` | — ✅ |
| Bouton Home disparaît (onResume transitoire) | `onResume`/`onPause` transitoires lors du lancement d'applis | Déplacement de la gestion vers `onWindowFocusChanged` | — ✅ |
| Liste applis incomplète (22 au lieu de 82) | Android 11+ restreint la visibilité des applis sans `QUERY_ALL_PACKAGES` | Ajout de `QUERY_ALL_PACKAGES` au manifeste | — ✅ |
| Tuiles dynamiques ne remplissent pas le trou de la dernière rangée fixe | Deux boucles séparées (raccourcis fixes + applis dynamiques) chunked séparément | Fusion en une seule liste `HomeTile` (Fixed + Dynamic) avant chunking | — ✅ |

---

## Documentation

| Fichier | Contenu |
|---|---|
| `README.md` | Présentation complète (features, install, compatibilité, architecture) |
| `docs/2026-07-31-journal-dev.md` | Journal initial : contexte, install, code, bugs, décisions |
| `docs/parametrage-telephone.md` | Paramétrages téléphone étape par étape (12 étapes) |
| `docs/handoff.md` | Ce fichier — état du projet, architecture, reprise |
| `docs/superpowers/specs/2026-08-01-appareil-photo-prout-design.md` | Spec boutons Appareil photo + Prout |
| `docs/superpowers/specs/2026-08-01-favoris-contacts-design.md` | Spec contacts favoris avec photo |
| `docs/superpowers/specs/2026-08-02-tuiles-applis-dynamiques-design.md` | Spec tuiles d'applis dynamiques |
| `docs/superpowers/plans/2026-08-01-favoris-contacts.md` | Plan d'implémentation favoris (7 tâches) |
| `docs/superpowers/plans/2026-08-02-tuiles-applis-dynamiques.md` | Plan d'implémentation tuiles applis (7 tâches) |
| `DESIGN.md` | Design system complet (couleurs, typo, layout, composants) |
| `PRODUCT.md` | Document produit (plateforme, utilisateurs, principes) |

---

## Évolutions de la session 2026-08-01 / 2026-08-03

| Évolution | Fichiers touchés | Détail |
|---|---|---|
| Bouton Home centre-droite | `HomeButtonService.kt` | `Gravity.CENTER_VERTICAL or Gravity.END` au lieu de `TOP or END` |
| Imports icônes Material | `MainActivity.kt` | Imports explicites `Icons.Filled.*` (FQN ne marche pas pour extensions) |
| Section Autorisations système | `AdminScreen.kt` | 5 boutons liens Settings + indicateurs d'état (pastilles vertes/grises) |
| Section Permissions appli | `AdminScreen.kt` | 4 boutons Redemander (CALL_PHONE, READ_CALL_LOG, photos, READ_CONTACTS) |
| Trigger admin 7 clics | `MainActivity.kt` | Remplace long appui 2s — 7 clics sur horloge dans 2s |
| Toast kiosque + warning | `AdminScreen.kt` | Toast « Trouvez Papy Launcher » + warning rouge si kiosque on mais service inactif |
| Boutons Appareil photo + Prout | `Shortcuts.kt`, `MainActivity.kt`, `ProutSound.kt`, `AndroidManifest.xml`, `strings.xml`, `DESIGN.md` | Spec + implémentation (caméra native + son synthétisé AudioTrack) |
| Contacts favoris | `Shortcuts.kt`, `Prefs.kt`, `ContactsHelper.kt`, `FavoritesScreen.kt`, `ManageFavoritesScreen.kt`, `MainActivity.kt`, `AdminScreen.kt`, `AndroidManifest.xml`, `strings.xml`, `DESIGN.md` | Spec + plan + implémentation. Fix bug affichage (query par LOOKUP_KEY). Gestion via écran dédié. |
| Tuiles d'applis dynamiques | `DynamicApp.kt`, `ManageAppsScreen.kt`, `AppPickerScreen.kt`, `MainActivity.kt`, `AdminScreen.kt`, `PapyKioskService.kt`, `AndroidManifest.xml`, `DESIGN.md` | Spec + plan + implémentation. BigButton refactor (icône native Drawable + médaillon blanc). Kiosque liste blanche dynamique. HomeButton via onWindowFocusChanged. QUERY_ALL_PACKAGES. |
| Réordonnancement des tuiles | `Shortcuts.kt` | Ordre : Appels, Favoris, SMS, WhatsApp, Mail, Photos, Applis, Appareil photo, Prout |
| Grille fusionnée | `MainActivity.kt` | `sealed class HomeTile` (Fixed + Dynamic) — une seule liste chunked par 2, les tuiles dynamiques remplissent le trou de la dernière rangée fixe |
| Icône Favoris | `MainActivity.kt` | `Icons.Filled.Star` (était `null`) |
| Indicateur batterie | `MainActivity.kt` | Icône Material Battery + pourcentage, couleur vert/orange/rouge, superposé haut-droite (ne décentre pas l'horloge) |
| README complet | `README.md` | Présentation, features, install, compatibilité, architecture, principes |
| Suppression branches feature | git | `feature/favoris-contacts` et `feature/tuiles-applis-dynamiques` fusionnées dans `main` |

---

## Évolutions de la session 2026-08-04 — Refactoring thermo-nucléaire

Audit de qualité code sévère, puis refactoring parallèle (4 agents) ciblant les points 1, 2, 3 et 6 de l'audit. Build vérifié vert après intégration.

| Évolution | Fichiers touchés | Détail |
|---|---|---|
| Header écran réutilisable | `ui/components/ScreenHeader.kt` (nouveau), `ManageFavoritesScreen.kt`, `ManageAppsScreen.kt`, `AppListScreen.kt`, `AppPickerScreen.kt`, `PhotosScreen.kt`, `FavoritesScreen.kt` | Extrait `ScreenHeader(title, onBack)` — 6 copies du header "Retour + titre" supprimées. Unification via `Spacer.weight(1f)` (titre poussé à droite). |
| Navigation type-safe | `Screen.kt` (nouveau), `MainActivity.kt` | `sealed class Screen` (9 object singletons) remplace les string literals. `when` exhaustif dans `AppNavigation()` — le compilateur valide tous les cas. |
| Constantes couleurs | `ui/theme/Color.kt` | 14 constantes `Papy*` définies (`PapyBlue`, `PapyRed`, `PapyTextDark`, `PapyTextGray`, `PapyTextGrayAlt`, `PapyTextLight`, `PapyTextBlueGray`, `PapySurfaceLight`, `PapySurfaceMuted`, `PapyBorder`, `PapyGreen`, `PapyGreenLight`, `PapyRedLight`, `PapyOrange`). Couleurs Material3 (`Purple80/Pink40`) conservées pour `Theme.kt`. **Usages pas encore remplacés** — les `Color(0xFF...)` hardcodés restent dans le code (sweep à faire). |
| Split `AdminScreen.kt` | `AdminScreen.kt` (732 → 257 lignes), `AdminButtons.kt`, `AdminSectionSos.kt`, `AdminSectionPin.kt`, `AdminSectionKiosk.kt`, `AdminSectionShortcuts.kt`, `AdminSectionSystemPermissions.kt`, `AdminSectionAppPermissions.kt`, `AdminSectionFavorites.kt`, `AdminSectionApps.kt`, `AdminSectionQuickSettings.kt`, `Permissions.kt` | 10 sections extraites en composables `internal` avec state hoisting (params + callbacks). Composants UI partagés (`SectionTitle`, `AdminButton`, `AdminLinkButton`, `PermissionButton`) dans `AdminButtons.kt`. Helpers permissions (`isGranted`, `isPhotosPermissionGranted`, `isNotifListenerEnabled`, `isDefaultLauncher`) dans `Permissions.kt` (`internal`). Lifecycle observer et state hoisting restent dans `AdminScreen` (orchestrateur). Couleurs hardcodées conservées (sweep à faire). |

### Conflits inter-agents résolus à la main
- Agent `Color.kt` avait supprimé `Purple80/PurpleGrey80/Pink80/Purple40/PurpleGrey40/Pink40` que `Theme.kt` référence → restaurées
- Agent `ScreenHeader` avait supprimé l'import `sp` de `PhotosScreen.kt` à tort (le `fontSize = 22.sp` du texte "Aucune photo trouvée" en avait encore besoin) → restauré

### Points d'audit thermo-nucléaire non traités (backlog)
Ces points ont été identifiés lors de l'audit mais pas traités cette session :
- Extraire `HomeTile`, `shortcutIcon`, `launchXxx` hors de `MainActivity.kt` (item 5)
- Helper `rememberOnResume` pour supprimer 3 copies du lifecycle observer (item 4)
- `Permissions.kt` simplifié + `PermissionButton` allégé (item 7)
- Cache `allowedPackages()` dans `PapyKioskService` (item 8)
- Receiver batterie unique au lieu du polling 30s (item 9)
- `PinScreen.trySubmit()` local (item 10)
- `DynamicApp` membres de `Prefs` (cohérence avec favoris) (item 11)
- Fix bug `AdminLinkButton` fond identique + supprimer `launchSos` mort + `onResume/onPause` vides (item 12)
- Bug `requestSosCall` race condition (3.1)
- `loadInstalledApps` hors main thread (3.8)
- `PapyKioskService.enabled` mutable global (3.4)
- Couleurs hardcodées → remplacer par les constantes `Papy*` (sweep post-Color.kt)

---

## Contact / contexte

- **Demandeur :** Fils de papa (85 ans)
- **Téléphone cible :** Samsung Galaxy A13 4G (Android 13)
- **Téléphone de test :** Google Pixel 6a (Android 16)
- **OS de dev :** Linux + Android Studio
- **Objectif :** Launcher simple, privacy-first, gros boutons, mode kiosque, admin PIN