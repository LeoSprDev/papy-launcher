# Papy Launcher — Handoff

**Date :** 2026-08-02
**Repo GitHub :** https://github.com/LeoSprDev/papy-launcher
**APK debug :** `app/build/outputs/apk/debug/app-debug.apk`

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
| Thème clair forcé (lisibilité) | ✅ |
| Mode kiosque (AccessibilityService — best effort) | ✅ |
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

> **Grille fixe :** si un nombre impair de tuiles est activé, la dernière tuile garde la même taille que les autres et reste à gauche (un `Spacer` avec `weight(1f)` occupe la place restante à droite).

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
APK → `app/build/outputs/apk/debug/app-debug.apk`

### 5. Installer l'APK sur un téléphone
- Copier `app-debug.apk` sur le téléphone (USB, Drive, etc.)
- Sur le téléphone, ouvrir le fichier → Installer
- Activer "Sources inconnues" si demandé

### 6. Paramétrages obligatoires sur le téléphone
→ Voir `docs/parametrage-telephone.md` (12 étapes dans l'ordre)

---

## Architecture du code

```
app/src/main/java/com/papy/launcher/
├── MainActivity.kt              — Activity principale, navigation 7 écrans, UI accueil, lancement applis, permissions runtime, trigger admin (7 clics horloge), grille fixe
├── Prefs.kt                    — SharedPreferences (PIN, SOS, kiosque, home button, raccourcis, favoris JSON)
├── Shortcuts.kt                — Modèle de données des raccourcis (enum ShortcutId 9 entrées + data class Shortcut)
├── PinScreen.kt                — Écran saisie PIN (pavé numérique grand)
├── AdminScreen.kt              — Écran admin (SOS, PIN, kiosque, raccourcis, autorisations, permissions, réglages rapides, luminosité, bouton "Gérer les favoris") — scrollable
├── ManageFavoritesScreen.kt    — Écran dédié gestion favoris (ajouter via picker contacts, retirer, liste avec photo + nom + numéro)
├── FavoritesScreen.kt          — Écran favoris (grille LazyVerticalGrid 2 colonnes, photo + prénom, appel direct)
├── ContactsHelper.kt           — Queries ContactsContract (getContactByLookupKey par LOOKUP_KEY, getPhoneNumber)
├── AppListScreen.kt            — Liste applis installées (icônes + noms)
├── PhotosScreen.kt             — Visionneur photos (grille LazyVerticalGrid + plein écran, Coil)
├── ProutSound.kt               — Synthèse son prout via AudioTrack
├── PapyNotificationListener.kt  — NotificationListenerService (badges SMS/mail/WhatsApp)
├── MissedCalls.kt               — Compteur appels manqués (query CallLog)
├── PapyKioskService.kt         — AccessibilityService mode kiosque (liste blanche applis)
├── HomeButtonService.kt         — Service foreground bouton Home flottant (WindowManager overlay, centre-droite)
└── ui/theme/
    ├── Theme.kt                 — Thème clair forcé
    ├── Color.kt                 — Couleurs
    └── Type.kt                  — Typographie
```

### Navigation (AppNavigation dans MainActivity)
```
"home"  ←→  "pin"  ←→  "admin"  ←→  "manage_favorites"
"home"  ←→  "applist"
"home"  ←→  "photos"
"home"  ←→  "favorites"
```

### Flux du mode admin
1. 7 clics sur l'horloge (dans les 2 secondes) → `"pin"`
2. PIN correct → `"admin"`
3. "Retour" → `"home"`
4. Admin → "Gérer les favoris" → `"manage_favorites"` → "Retour" → `"admin"`

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
| Pas d'icône d'appli perso | Icône Android par défaut | À faire |
| `getActiveNotifications()` depuis l'activity | Ne marche que depuis le service | Contourné : service bind lui-même dans BadgeStore |
| `LocalLifecycleOwner` déprécié | Déplacé vers `lifecycle-runtime-compose` (non ajouté au projet) | Warning bénin, fonctionne toujours |

### Bugs résolus (historique)

| Problème | Cause | Solution | Issue |
|---|---|---|---|
| Favoris vides | `getLookupUri(-1L, lookupKey)` générait une URI `.../lookup/<key>/-1` rejetée par le provider contacts (IllegalArgumentException) | Query directe sur `Contacts.CONTENT_URI` avec `WHERE LOOKUP_KEY = ?` | #1 ✅ |
| Pas d'UI retirer favori | Conséquence du bug #1 : la liste admin était vide | Corrigé automatiquement avec #1 | #2 ✅ |

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
| `docs/superpowers/plans/2026-08-01-favoris-contacts.md` | Plan d'implémentation favoris (7 tâches) |
| `DESIGN.md` | Design system complet (couleurs, typo, layout, composants) |
| `PRODUCT.md` | Document produit (plateforme, utilisateurs, principes) |

---

## Évolutions de la session 2026-08-01 / 2026-08-02

| Évolution | Fichiers touchés | Détail |
|---|---|---|
| Bouton Home centre-droite | `HomeButtonService.kt` | `Gravity.CENTER_VERTICAL or Gravity.END` au lieu de `TOP or END` |
| Imports icônes Material | `MainActivity.kt` | Imports explicites `Icons.Filled.*` (FQN ne marche pas pour extensions) |
| Section Autorisations système | `AdminScreen.kt` | 5 boutons liens Settings + indicateurs d'état (pastilles vertes/grises) |
| Section Permissions appli | `AdminScreen.kt` | 4 boutons Redemander (CALL_PHONE, READ_CALL_LOG, photos, READ_CONTACTS) |
| Trigger admin 7 clics | `MainActivity.kt` | Remplace long appui 2s — 7 clics sur horloge dans 2s |
| Toast kiosque + warning | `AdminScreen.kt` | Toast « Trouvez Papy Launcher » + warning rouge si kiosque on mais service inactif |
| Boutons Appareil photo + Prout | `Shortcuts.kt`, `MainActivity.kt`, `ProutSound.kt`, `AndroidManifest.xml`, `strings.xml`, `DESIGN.md` | Spec + implémentation (caméra native + son synthétisé AudioTrack) |
| Contacts favoris | `Shortcuts.kt`, `Prefs.kt`, `ContactsHelper.kt`, `FavoritesScreen.kt`, `MainActivity.kt`, `AdminScreen.kt`, `AndroidManifest.xml`, `strings.xml`, `DESIGN.md` | Spec + plan + implémentation. Fix bug affichage (query par LOOKUP_KEY). Gestion via écran dédié `ManageFavoritesScreen`. |
| Réordonnancement des tuiles | `Shortcuts.kt` | Ordre : Appels, Favoris, SMS, WhatsApp, Mail, Photos, Applis, Appareil photo, Prout |
| Grille fixe (taille des tuiles) | `MainActivity.kt` | `Spacer(weight 1f)` quand rangée incomplète — la dernière tuile ne s'étire pas |
| Icône Favoris | `MainActivity.kt` | `Icons.Filled.Star` (était `null`) |
| README complet | `README.md` | Présentation, features, install, compatibilité, architecture, principes |
| Suppression branche feature | git | `feature/favoris-contacts` fusionnée dans `main` puis supprimée (local + remote) |

---

## Contact / contexte

- **Demandeur :** Fils de papa (85 ans)
- **Téléphone cible :** Samsung Galaxy A13 4G (Android 13)
- **Téléphone de test :** Google Pixel 6a (Android 16)
- **OS de dev :** Linux + Android Studio
- **Objectif :** Launcher simple, privacy-first, gros boutons, mode kiosque, admin PIN