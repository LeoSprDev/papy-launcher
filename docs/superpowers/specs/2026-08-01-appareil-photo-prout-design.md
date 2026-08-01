# Spec — Boutons Appareil photo et Prout

**Date :** 2026-08-01
**Projet :** papy-launcher
**Auteur :** brainstorming opencode + utilisateur

## Contexte

Papy Launcher a un écran d'accueil avec 6 raccourcis configurables (Appels, SMS, WhatsApp, Mail, Photos, Applis) + un bouton SOS. L'utilisateur demande l'ajout de 2 boutons : un déclencheur d'appareil photo (lance l'appli caméra native du téléphone) et un bouton "prout" qui joue un son humoristique. Les 2 boutons doivent être configurables par l'admin (on/off comme les 6 raccourcis existants).

## Objectif

Ajouter 2 raccourcis à l'écran d'accueil, configurables dans l'admin, qui :
1. **Appareil photo** — ouvre l'appli caméra native du téléphone via Intent.
2. **Prout** — joue un son de prout synthétisé en code (aucun fichier audio embarqué).

## Décisions prises

| Décision | Choix | Raison |
|---|---|---|
| Son prout | Synthétisé en code via AudioTrack | Zéro fichier, zéro licence, zéro dépendance. Cohérent avec privacy-first/léger. Freesound et Pixabay bloquent les téléchargements directs ; un fichier fourni par l'utilisateur est une option future. |
| Appareil photo | Lance l'appli caméra native (Intent) | Pas de gestion de photo dans le launcher ; papa voit sa photo dans la galerie native. |
| Visibilité | Configurable admin (on/off) | Cohérent avec les 6 raccourcis existants. |
| Label caméra | "Appareil photo" | Évite l'ambiguïté avec le bouton "Photos" (galerie). |

## Architecture

### 1. Shortcuts.kt — 2 nouveaux IDs

Ajouter 2 entrées à l'enum `ShortcutId` et à la liste `Shortcuts.all` :

- `ShortcutId.APPAREIL_PHOTO` → label `R.string.btn_appareil_photo`, couleur `Color(0xFF00695C)` (teal foncé, distinct du Vert WhatsApp #075E54 et du Vert Appel #2E7D32)
- `ShortcutId.PROUT` → label `R.string.btn_prout`, couleur `Color(0xFF8E63BC)` (violet plus clair que Violet Album #6A1B9A, humoristique)

Ordre dans `Shortcuts.all` : APPELS, SMS, WHATSAPP, MAIL, PHOTOS, APPLIS, APPAREIL_PHOTO, PROUT. Les 2 nouveaux boutons apparaissent en fin de grille (donc en bas, potentiellement sur une 4e rangée si tous les raccourcis sont activés).

### 2. strings.xml — 2 nouveaux libellés

```xml
<string name="btn_appareil_photo">Appareil photo</string>
<string name="btn_prout">Prout</string>
```

### 3. MainActivity.kt — actions de lancement

Dans le `when (sc.id)` des actions de `HomeScreen`, ajouter :

- `ShortcutId.APPAREIL_PHOTO -> { ctx -> launchCamera(ctx) }`
- `ShortcutId.PROUT -> { ctx -> playFartSound(ctx) }`

Pas de badge pour ces 2 boutons (`else -> 0` déjà géré).

### 4. Nouveau fichier ProutSound.kt — synthèse du son

Fichier dédié `ProutSound.kt` contenant `fun playFartSound(context: Context)`.

**Technique** : `AudioTrack` en mode static, bruit brun filtré avec une enveloppe ADSR.

- Sample rate : 44100 Hz
- Durée : ~180ms (~8000 samples)
- Enveloppe : attaque 5ms (montée rapide), decay 50ms (descente à 60%), sustain 60ms (à 60%), release 65ms (descente à 0)
- Fréquence de base : ~80-120 Hz (fréquence fondamentale d'un prout, pitch descendant)
- Filtre : passe-bas simple (moyenne mobile sur 4-8 samples) pour adoucir le bruit brun
- Lecture sur un thread background (async, fire-and-forget) pour ne pas bloquer l'UI
- AudioTrack en mode `PERFORMANCE_MODE_LOW_LATENCY` si disponible (API 26+), fallback `MODE_STATIC`

**Comportement** : tap → son joué immédiatement. Pas de feedback visuel (tactile pur, comme les autres tuiles). Tap répété pendant que le son joue → coupe le son en cours et rejoue (nouvel AudioTrack à chaque tap).

### 5. MainActivity.kt — launchCamera

Ajouter `fun launchCamera(context: Context)` :

- Android 10+ (API 29+) : `Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)`
- Fallback Android < 10 : `Intent(MediaStore.ACTION_IMAGE_CAPTURE)`
- Flags : `Intent.FLAG_ACTIVITY_NEW_TASK`
- Appel via `safeStartActivity(context, intent)` (existant, gère `ActivityNotFoundException`)

### 6. AndroidManifest.xml — queries caméra

Ajouter dans le bloc `<queries>` :

```xml
<intent>
    <action android:name="android.media.action.STILL_IMAGE_CAMERA" />
</intent>
<intent>
    <action android:name="android.media.action.IMAGE_CAPTURE" />
</intent>
```

Permet à Papy Launcher de détecter l'appli caméra sur Android 11+ (visibility filtering).

### 7. AdminScreen.kt — pas de modification

La section "Raccourcis affichés" itère sur `Shortcuts.all`, donc les 2 nouveaux boutons apparaissent automatiquement avec interrupteur on/off.

### 8. DESIGN.md — 2 nouvelles couleurs

Ajouter dans le frontmatter `colors` :

- `teal-camera: "#00695C"` — Teal Caméra (teal foncé, distinct des autres verts)
- `violet-prout: "#8E63BC"` — Violet Prout (violet plus clair, humoristique)

Ajouter dans la section Tertiary des couleurs documentées.

### 9. .impeccable/design.json — mise à jour sidecar

Ajouter `teal-camera` et `violet-prout` dans `colorMeta` avec `tonalRamp` 8 steps.

## Permissions

- Aucune permission runtime nécessaire.
- Appareil photo : l'appli caméra native gère ses propres permissions. Papy Launcher ne fait que lancer l'Intent.
- Prout : `AudioTrack` ne nécessite pas `RECORD_AUDIO` (le son est généré, pas enregistré). Pas de permission manifeste.

## Compatibilité

- minSdk 23 : `AudioTrack` disponible depuis API 1. `PERFORMANCE_MODE_LOW_LATENCY` (API 26+) est optionnel avec fallback. `INTENT_ACTION_STILL_IMAGE_CAMERA` (API 29+) a un fallback `ACTION_IMAGE_CAPTURE`.
- targetSdk 37 : compatible.

## Tests

- Pas de test unitaire automatisable pour le son (sortie audio) ni pour le lancement d'Intent (activité externe).
- Test manuel : tap sur "Appareil photo" → caméra s'ouvre. Tap sur "Prout" → son joué. Tap répéré → son rejoué.
- Test admin : désactiver "Appareil photo" dans l'admin → bouton disparaît de l'accueil. Réactiver → réapparaît.

## Risques

- **Son synthétisé peu réaliste** : le bruit brun filtré peut sonner plus "bzz" que "prout". Mitigation : enveloppe ADSR bien réglée + pitch descendant. Si insuffisant, l'utilisateur peut fournir un fichier audio plus tard (remplacement de `ProutSound.kt` par `MediaPlayer.create(res.raw.prout)`).
- **8 boutons si tous activés** : grille 2×4 = 4 rangées de tuiles. L'écran d'accueil utilise `Arrangement.SpaceEvenly` vertical, donc les tuiles se répartissent. Sur un petit téléphone, les tuiles pourraient être tassées. Mitigation : l'admin peut désactiver les boutons non essentiels. Pas de changement de layout nécessaire pour l'instant.