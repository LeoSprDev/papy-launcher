# Spec — Contacts favoris avec photo

**Date :** 2026-08-01
**Projet :** papy-launcher
**Auteur :** brainstorming opencode + utilisateur

## Contexte

Papy Launcher a un écran d'accueil avec 8 raccourcis configurables + un bouton SOS. Aujourd'hui, le bouton « Appels » ouvre le dialer natif Android — un mur pour un senior de 85 ans (clavier numérique, onglets, recherche). Papa veut juste appeler ses proches. La reconnaissance par photo est un raccourci cognitif massif : un senior reconnaît un visage avant de lire un nom.

## Objectif

Ajouter un 9e raccourci « Favoris » sur l'écran d'accueil, configurable par l'admin (on/off comme les 8 autres), qui ouvre un écran dédié affichant les contacts favoris en grandes tuiles carrées avec photo. Tap = appel direct. L'admin sélectionne les favoris depuis le carnet de contacts du téléphone.

## Décisions prises

| Décision | Choix | Raison |
|---|---|---|
| Accès | Nouveau bouton « Favoris » sur l'accueil (9e raccourci configurable) | Cohérent avec le système Shortcuts + AdminScreen existant. Papa a un accès direct. Respecte « un écran, un job ». |
| Nombre de favoris | Illimité, grille 2 colonnes scrollable | Flexible pour l'admin. Le scroll est acceptable ici (liste dédiée, pas l'accueil). |
| Action au tap | Appel direct (`ACTION_CALL`) | Le plus rapide, le moins de gestes. Cohérent avec « un écran, un job ». |
| Config par l'admin | Sélection depuis le carnet de contacts du téléphone (`READ_CONTACTS`) | Plus pratique que la saisie manuelle. Photos récupérées automatiquement. |
| Stockage | Référence `lookupKey` (pas de snapshot) | Toujours à jour si le contact change de photo/numéro. Les `lookupKey` sont stables même après sync. |
| Suppression | Bouton « Retirer » rouge dans la section admin | Un favori doit pouvoir être retiré. Pas de confirmation (YAGNI — réajoutable en 2 taps). |
| Couleur du bouton | Indigo Favoris (#3949AB) | Indigo profond, distinct de l'Indigo Mécanique admin (#1A237E) et du Bleu Message (#1565C0). |

## Architecture

### 1. Shortcuts.kt — nouveau raccourci

Ajout à l'enum `ShortcutId` et à la liste `Shortcuts.all` en 9e position :

- `ShortcutId.FAVORIS` → label `R.string.btn_favoris`, couleur `Color(0xFF3949AB)` (Indigo Favoris), `defaultEnabled = true`

Ordre dans `Shortcuts.all` : APPELS, SMS, WHATSAPP, MAIL, PHOTOS, APPLIS, APPAREIL_PHOTO, PROUT, FAVORIS. Le bouton apparaît en fin de grille si activé.

### 2. strings.xml — nouveau libellé

```xml
<string name="btn_favoris">Favoris</string>
```

### 3. Prefs.kt — stockage des favoris

Liste ordonnée de `lookupKey` (String) en SharedPreferences, clé `favorites_list`, sérialisée en JSON via `org.json.JSONArray` (pas de dépendance externe).

Nouvelles fonctions :
- `getFavorites(context): List<String>` — retourne la liste ordonnée des `lookupKey`
- `addFavorite(context, lookupKey: String)` — ajoute à la fin si absent (dédup)
- `removeFavorite(context, lookupKey: String)` — retire de la liste

L'ordre est préservé (l'admin réorganise en supprimant/réajoutant — pas de drag-and-drop, YAGNI).

### 4. ContactsHelper.kt — factorisation des queries

Nouveau fichier `ContactsHelper.kt` contenant les queries `ContactsContract` partagées entre `FavoritesScreen` et `AdminScreen` :

- `data class Contact(lookupKey: String, displayName: String, phoneNumber: String?, photoUri: Uri?)`
- `fun getContactByLookupKey(context: Context, lookupKey: String): Contact?` — résout le `lookupKey` en `Uri` via `ContactsContract.Contacts.getLookupUri`, query le prénom (`DISPLAY_NAME`) et la photo (`PHOTO_URI`). Récupère le numéro via `getPhoneNumber`. Retourne `null` si le contact est introuvable (supprimé). Retourne un `Contact` avec `phoneNumber = null` si le contact existe mais n'a pas de numéro.
- `private fun getPhoneNumber(context: Context, contactId: Long): String?` — query `ContactsContract.CommonDataKinds.Phone` filtré sur `CONTACT_ID`. Retourne le premier numéro, mobile privilégié si `TYPE_MOBILE` présent, sinon le premier disponible. Privée car appelée uniquement par `getContactByLookupKey`.

Cycle de vie des contacts : un `lookupKey` est stable même si le contact est modifié (changement de numéro, de photo). Si le contact est supprimé du carnet, `getContactByLookupKey` retourne `null` et la tuile est ignorée à l'affichage. Pas de nettoyage proactif de la liste — un contact réajouté avec le même lookup réapparaîtra automatiquement.

Performances : query à chaque affichage de `FavoritesScreen` (onResume) et à chaque render de la section admin. Pour N favoris (usage réaliste : 5-15), N queries `ContactsContract` — négligeable (<50ms total). Pas de cache (YAGNI — les contacts changent peu, la fraîcheur prime sur la perf).

Gestion des erreurs :
- `SecurityException` si `READ_CONTACTS` révoquée entre-temps → catch, affiche « Autorisez l'accès aux contacts » + bouton vers `PermissionButton`.
- `Cursor` fermé systématiquement via `use { }`.
- Pas de crash si le carnet de contacts est vide.

### 5. FavoritesScreen.kt — écran dédié

Nouveau fichier `FavoritesScreen.kt`. Header standard identique à `AppListScreen` et `PhotosScreen` : `Row` avec bouton « Retour » indigo à gauche, titre « Favoris » à droite.

Corps : `LazyVerticalGrid` 2 colonnes, vignettes carrées (`aspectRatio 1f`), espacement 8dp, scrollable vertical.

Composable `FavoriteTile` :
- Tuile carrée arrondie 16dp, fond `Surface Card` (#F5F5F5)
- Photo du contact occupant ~75% de la tuile (cercle centré via Coil `AsyncImage` + `CircleShape`)
- Prénom en dessous en Bold 20sp `Texte Principal` (#333333), tronqué à 1 ligne avec ellipsis
- Tap → appel direct `ACTION_CALL` sur le numéro du contact (vérifie `CALL_PHONE`, demande si manquant, `performCall`)

Gestion des cas :
- Contact sans photo : cercle gris `Surface Pale` (#EEEEEE) avec initiales du prénom (Bold 28sp, texte foncé #333333) — lisible, dégradé minimal.
- Contact sans numéro : tuile désactivée (opacité 50%), tap → Toast « Aucun numéro pour ce contact ».
- Contact supprimé du carnet : ignoré au query, non affiché.
- Liste vide : message centré « Aucun favori. Ajoutez-en depuis l'administration. ».

### 6. MainActivity.kt — navigation

`AppNavigation` gagne `"favorites" -> FavoritesScreen(onBack = { screen = "home" })`.

`HomeScreen` gagne `onFavorites` callback → `screen = "favorites"`.

Le `when (sc.id)` des actions raccourcis ajoute `ShortcutId.FAVORIS -> { ctx -> onFavorites() }`.

Pas de badge pour ce bouton (`else -> 0` déjà géré).

### 7. AdminScreen.kt — section « Favoris »

Nouvelle section après « Raccourcis affichés », avant « Réglages rapides ». Contenu vertical :

1. **Bouton « Ajouter un favori »** — indigo, ouvre `Intent.ACTION_PICK` sur `ContactsContract.Contacts.CONTENT_URI` via `rememberLauncherForActivityResult`. L'admin pick un contact → on récupère son `lookupKey` → `Prefs.addFavorite`. Si déjà dans la liste → Toast « Déjà dans les favoris ». Bouton désactivé (opacité 50%) tant que `READ_CONTACTS` n'est pas accordée.

2. **Liste des favoris actuels** — pour chaque favori, une `Row` (fond `Surface Card` #F5F5F5, arrondi 16dp, padding 12dp) :
   - Photo du contact (cercle 40dp via Coil `AsyncImage`, fallback initiales si pas de photo)
   - Prénom (Bold 20sp #333333) + numéro (16sp gris #666666) en colonne
   - Bouton « Retirer » rouge (`Rouge SOS Familial` #C62828, texte 18sp) à droite → `Prefs.removeFavorite` + re-render de la liste via `mutableStateOf`

3. **Permission** — au-dessus de la section, un `PermissionButton` (composant existant) pour « Contacts (favoris) » → `READ_CONTACTS`, avec indicateur pastille verte/grise. Tap → boîte runtime ou page d'infos appli selon l'état. Ajouté dans la section « Permissions de l'application » existante (aux côtés de CALL_PHONE, READ_CALL_LOG, photos).

### 8. AndroidManifest.xml — permission

Ajout `<uses-permission android:name="android.permission.READ_CONTACTS" />`. Pas de `WRITE_CONTACTS`. Aucune query spécifique à ajouter dans `<queries>` (ContactsContract est un provider système, toujours visible).

### 9. DESIGN.md — nouvelle couleur

Ajout dans le frontmatter `colors` :
- `indigo-favoris: "#3949AB"` — Indigo Favoris (indigo profond)

Ajout dans la section Tertiary des couleurs documentées :
- **Indigo Favoris** (#3949AB) : bouton Favoris. Indigo profond, plus clair que l'Indigo Mécanique (#1A237E) — distinct pour ne pas confondre avec l'admin, mais dans la même famille bleutée.

## Permissions

- `READ_CONTACTS` (runtime, dangereuse) — lecture du carnet de contacts pour : (a) résoudre les `lookupKey` stockés en photo/numéro/prénom à l'affichage de `FavoritesScreen`, (b) récupérer le `lookupKey` du contact pické par l'admin.
- `CALL_PHONE` (déjà présente) — appel direct au tap sur un favori.
- Pas de `WRITE_CONTACTS` (on ne modifie pas le carnet).

Demande au premier lancement : `requestEssentialPermissions()` dans `MainActivity.onCreate` n'inclut pas `READ_CONTACTS` — la permission n'est demandée que lorsque l'admin tente d'ajouter un favori ou interagit avec la section « Contacts (favoris) » des permissions. Cohérent avec « on ne demande que ce qu'on utilise ».

## Compatibilité

- `READ_CONTACTS` disponible depuis API 1, `ContactsContract` depuis API 5. `getLookupUri` stable depuis API 5.
- `rememberLauncherForActivityResult` Compose.
- Aucun souci sur minSdk 23. Compatible targetSdk 37.

## Risques

- **Google Play `READ_CONTACTS`** : politique stricte, justification requise. La feature est légitime (afficher les favoris pour les appeler). Si publication Play Store un jour, déclarer l'usage dans le Play Console.
- **9e raccourci sur l'accueil** : si tous activés, grille plus dense. Mitigation : l'admin peut désactiver les boutons non essentiels. Pas de changement de layout nécessaire.
- **Contact sans photo** : fallback initiales — dégradé minimal mais fonctionnel. Le senior reconnaît le prénom + l'emplacement spatial.
- **Contact supprimé entre-temps** : tuile ignorée silencieusement. Pas d'erreur visible. Le `lookupKey` reste dans la liste (réapparaîtra si le contact est restauré). Pas de nettoyage proactif.
- **Performance avec beaucoup de favoris** : N queries `ContactsContract` à chaque affichage. Négligeable pour N < 50 (usage réaliste : 5-15). Pas de cache.

## Tests

- Pas de test unitaire automatisable pour les queries `ContactsContract` (nécessite un carnet de contacts mock).
- Test manuel :
  1. Admin → section Favoris → autoriser Contacts → bouton « Ajouter un favori » → pick un contact avec photo → apparaît dans la liste admin avec photo + prénom + numéro.
  2. Pick le même contact → Toast « Déjà dans les favoris ».
  3. Bouton « Retirer » → le favori disparaît de la liste admin.
  4. Écran d'accueil → bouton « Favoris » → écran dédié s'ouvre, vignettes photo affichées.
  5. Tap sur une vignette → appel direct du numéro.
  6. Tap sur une vignette sans numéro → Toast « Aucun numéro pour ce contact ».
  7. Contact sans photo → initiales affichées dans un cercle gris.
  8. Désactiver le raccourci « Favoris » dans l'admin → bouton disparaît de l'accueil.
  9. Supprimer un contact du carnet → sa vignette n'apparaît plus dans `FavoritesScreen`.