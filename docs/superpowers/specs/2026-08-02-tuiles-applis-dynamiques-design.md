# Spec — Tuiles d'applis dynamiques

**Date :** 2026-08-02
**Projet :** papy-launcher
**Auteur :** brainstorming opencode + utilisateur

## Contexte

Papy Launcher a 9 raccourcis fixes sur l'écran d'accueil + une tuile "Applis" qui ouvre une liste de toutes les applis installées. Pour un senior, cette liste déversante est un mur. L'admin veut choisir des applis spécifiques qui apparaissent comme des tuiles dédiées sur l'accueil — comme les favoris contacts, mais pour les applis.

## Objectif

Permettre à l'admin d'ajouter/retirer des applis installées comme tuiles dédiées sur l'écran d'accueil. L'admin sélectionne les applis depuis la liste des applis installées. Les tuiles d'applis apparaissent après les 9 raccourcis fixes. La tuile "Applis" actuelle (liste complète) est conservée (désactivable par l'admin).

## Décisions prises

| Décision | Choix | Raison |
|---|---|---|
| Option | A — tuiles dynamiques sur l'accueil | 1 tap pour papa, cohérent avec le pattern Favoris |
| Couleur des tuiles d'applis | Bleu Acier #546E7A | Neutre, distinct du Gris Réglages (#455A64) de la tuile Applis. Les tuiles se distinguent par leur icône native, pas par leur couleur. |
| Icône | Icône native de l'appli (PackageManager.getApplicationIcon) | Papa reconnaît le logo WhatsApp, Maps, etc. |
| Nombre d'applis | Illimité | L'admin est raisonnable. La grille s'étend vers le bas (scroll géré par SpaceEvenly). |
| Ordre | Après les 9 raccourcis fixes, dans l'ordre d'ajout | Simple, prévisible. Pas de drag & drop. |
| Sélection | Liste des applis installées (reuse de loadInstalledApps) | Simple, réutilise l'existant. |
| Tuile "Applis" actuelle | Conservée (désactivable, défaut désactivé) | Code et écran AppListScreen conservés pour un besoin futur. |
| Affichage de l'icône native | Cercle blanc 40dp (médaillon) sur la tuile Bleu Acier | Les icônes natives colorées ne sont pas lisibles sur fond foncé. Le médaillon blanc fait office de fond neutre. |

## Architecture

### 1. DynamicApp — modèle de données

Nouveau concept : les `DynamicApp` — des tuiles d'applis stockées en Prefs, affichées après les raccourcis fixes. `ShortcutId` reste un enum fixe de 9 valeurs (on ne peut pas étendre un enum à l'exécution).

- `data class DynamicApp(packageName: String, label: String)` — le `label` est résolu au moment de l'ajout via `PackageManager.getApplicationLabel`, mais le `packageName` est la source de vérité (le label peut changer si l'appli est mise à jour).
- Stocké en JSON dans SharedPreferences, clé `dynamic_apps_list`, sérialisé via `org.json.JSONArray` (pas de dépendance externe).
- L'ordre d'ajout est préservé.

### 2. Prefs.kt — stockage des DynamicApps

Nouvelles fonctions (pattern identique aux favoris) :
- `getDynamicApps(context): List<DynamicApp>` — retourne la liste ordonnée
- `addDynamicApp(context, packageName: String, label: String)` — ajoute à la fin si absent (dédup par packageName)
- `removeDynamicApp(context, packageName: String)` — retire de la liste

### 3. BigButton — refactor pour icône native

Actuellement `BigButton` accepte `icon: ImageVector?` (icône Material). Ajout d'un paramètre `drawableIcon: Drawable? = null`. Les deux sont mutuellement exclusifs :

- Si `drawableIcon != null` : `Box` cercle blanc 40dp + `AndroidView` ImageView 32dp (icône native dans un médaillon blanc)
- Si `icon != null` : `Icon` Material 40dp blanc (comme aujourd'hui)
- Le reste (label, badge, couleur de fond, aspectRatio) inchangé

Les 9 raccourcis fixes existants passent `drawableIcon = null` (inchangé). Les tuiles d'applis passent `icon = null, drawableIcon = PackageManager.getApplicationIcon(packageName)`.

### 4. ManageAppsScreen.kt — écran de gestion

Nouvel écran dédié, navigué depuis l'admin via `screen = "manage_apps"`. Pattern identique à `ManageFavoritesScreen` : header standard (bouton « Retour » indigo à gauche, titre « Gérer les applis » à droite), corps scrollable.

Contenu :
1. **Bouton « Ajouter une appli »** (AdminButton indigo) → navigue vers `screen = "app_picker"`
2. **Liste des applis ajoutées** — pour chaque `DynamicApp`, une `Row` (fond Surface Card #F5F5F5, arrondi 16dp, padding 12dp) :
   - Icône native (cercle 40dp via AndroidView + ImageView, fallback rien si désinstallée)
   - Nom de l'appli (Bold 20sp #333333) + nom du package (16sp #888888, info admin)
   - Bouton « Retirer » rouge (#C62828, 18sp) → `Prefs.removeDynamicApp` + re-render
   - Si l'appli est désinstallée (`getLaunchIntentForPackage` null) : « (désinstallée) » en rouge à la place du nom, bouton « Retirer » actif
3. **Message si vide** : « Aucune appli. Touchez « Ajouter une appli ». »

Cycle de vie : `DisposableEffect` + `LifecycleEventObserver` sur `ON_RESUME` pour rafraîchir la liste. Pas de permission runtime.

### 5. AppPickerScreen.kt — écran de sélection

Nouvel écran dédié, navigué depuis `ManageAppsScreen` via `screen = "app_picker"`. Essentiellement l'`AppListScreen` actuel avec un comportement différent au tap.

Contenu :
- Header standard (bouton « Retour » indigo → `screen = "manage_apps"`, titre « Ajouter une appli »)
- Liste scrollable des applis installées, réutilisant `loadInstalledApps(context)` de `AppListScreen.kt` (fonction publique). Query `PackageManager.getInstalledApplications`, filtre `getLaunchIntentForPackage` non null, exclut Papy Launcher, tri alphabétique.
- Chaque `AppRow` : icône 48dp + nom 20sp, tap → `Prefs.addDynamicApp(context, packageName, label)` + Toast « [nom] ajoutée » + `onBack()` (retour à ManageAppsScreen)

Dédup : si l'appli est déjà dans les tuiles dynamiques (`Prefs.getDynamicApps().any { it.packageName == selected }`), Toast « Déjà ajoutée » + retour sans ajouter.

Réutilisation : `AppPickerScreen` appelle `loadInstalledApps` et `AppRow` depuis `AppListScreen.kt` (fonctions/composables publics). Pas de duplication de logique, juste un comportement différent au tap.

### 6. AdminScreen.kt — section « Applis »

Nouvelle section après « Favoris », avant « Réglages rapides ». Un seul bouton « Gérer les applis » → `onManageApps()`. Même pattern que le bouton « Gérer les favoris ».

`AdminScreen` gagne un callback `onManageApps: () -> Unit`.

La section « Raccourcis affichés » reste inchangée (les 9 raccourcis fixes avec interrupteurs). La tuile APPLIS est désactivable comme les autres (défaut : désactivée pour ne pas surcharger l'accueil).

### 7. HomeScreen — affichage des tuiles d'applis

La grille actuelle itère sur `Prefs.getEnabledShortcuts()`. Modification : après la boucle des raccourcis fixes, ajouter une boucle sur `Prefs.getDynamicApps()`.

Chaque `DynamicApp` génère un `BigButton` avec :
- `label` = nom de l'appli (résolu via `PackageManager.getApplicationLabel` au moment de l'affichage, pas stocké — la fraîcheur prime)
- `color` = `Color(0xFF546E7A)` (Bleu Acier)
- `drawableIcon` = `PackageManager.getApplicationIcon(packageName)` (icône native)
- `icon` = `null`
- `badge` = 0
- `onClick` = `getLaunchIntentForPackage(packageName)` + `safeStartActivity`

Gestion des erreurs : si l'appli a été désinstallée (`getLaunchIntentForPackage` null), le tap affiche un Toast « [nom] n'est plus installée ». La tuile reste affichée (l'admin la retirera).

Performance : résoudre le label + l'icône pour N applis à chaque affichage de `HomeScreen` est négligeable (N réaliste : 3-10, PackageManager est rapide). Pas de cache (YAGNI, cohérent avec les favoris).

### 8. Navigation

`AppNavigation` gagne deux nouveaux états :
```
"admin" → "manage_apps" → "app_picker" → retour "manage_apps" → retour "admin"
```

- `"manage_apps" -> ManageAppsScreen(onBack = { screen = "admin" }, onAddApp = { screen = "app_picker" })`
- `"app_picker" -> AppPickerScreen(onBack = { screen = "manage_apps" })`

### 9. DESIGN.md — nouvelle couleur

Ajout dans le frontmatter `colors` :
- `bleu-acier: "#546E7A"` — Bleu Acier (tuiles d'applis dynamiques)

Ajout dans la section Tertiary :
- **Bleu Acier** (#546E7A) : tuiles d'applis dynamiques. Neutre, distinct du Gris Réglages (#455A64) de la tuile Applis. Les tuiles d'applis se distinguent par leur icône native, pas par leur couleur.

## Permissions

- Aucune permission supplémentaire. `PackageManager.getInstalledApplications` et `getLaunchIntentForPackage` sont disponibles depuis API 1. Le launcher a une exemption de visibilité sur Android 11+ (intent-filter HOME). Pas de changement manifeste.

## Compatibilité

- `Drawable`, `AndroidView`, `ImageView` déjà utilisés dans `AppListScreen`. Disponibles depuis API 1.
- `org.json.JSONArray` dans la stdlib Android.
- Pas de souci sur minSdk 23. Compatible targetSdk 37.

## Risques

- **Icône native illisible sur fond foncé** : mitigé par le médaillon blanc 40dp. L'icône colorée est sur fond blanc, lisible.
- **Appli désinstallée entre-temps** : la tuile reste affichée, tap → Toast « n'est plus installée ». L'admin la retire via `ManageAppsScreen` qui affiche « (désinstallée) ».
- **Accueil surchargé** : si l'admin ajoute beaucoup d'applis, l'accueil devient long. Mitigation : l'admin est raisonnable. Pas de limite codée (YAGNI).
- **Label d'appli qui change** : le label est résolu à chaque affichage via `PackageManager`, pas stocké. Toujours à jour.

## Tests

- Pas de test unitaire automatisable pour l'affichage des tuiles (nécessite device).
- Test manuel :
  1. Admin → section Applis → « Gérer les applis » → « Ajouter une appli » → liste des applis → tap une appli → Toast « ajoutée » → retour ManageAppsScreen → l'appli apparaît dans la liste.
  2. Ajouter la même appli → Toast « Déjà ajoutée ».
  3. Écran d'accueil → la tuile de l'appli apparaît après les raccourcis fixes, icône native dans médaillon blanc, couleur Bleu Acier.
  4. Tap sur la tuile → l'appli se lance.
  5. Bouton « Retirer » dans ManageAppsScreen → l'appli disparaît de la liste et de l'accueil.
  6. Désinstaller une appli ajoutée → tap sur sa tuile → Toast « n'est plus installée ». ManageAppsScreen affiche « (désinstallée) ».
  7. Désactiver la tuile « Applis » dans l'admin → elle disparaît de l'accueil. Réactiver → elle réapparaît.