---
name: Papy Launcher
description: Launcher Android pour seniors — tuiles domestiques colorées, gros boutons, contraste fort, thème clair forcé.
colors:
  indigo-mecanique: "#1A237E"
  rouge-sos-familial: "#C62828"
  vert-appel: "#2E7D32"
  bleu-message: "#1565C0"
  vert-whatsapp: "#075E54"
  orange-courrier: "#EF6C00"
  violet-album: "#6A1B9A"
  gris-reglages: "#455A64"
  teal-camera: "#00695C"
  violet-prout: "#8E63BC"
  texte-principal: "#333333"
  texte-secondaire: "#555555"
  texte-aide: "#666666"
  texte-legende: "#888888"
  contour-clair: "#CCCCCC"
  surface-pale: "#EEEEEE"
  surface-card: "#F5F5F5"
  blanc: "#FFFFFF"
  noir: "#000000"
typography:
  horloge:
    fontFamily: "Roboto, sans-serif"
    fontSize: "48sp"
    fontWeight: 700
    lineHeight: 1.1
  titre-ecran:
    fontFamily: "Roboto, sans-serif"
    fontSize: "32sp"
    fontWeight: 700
    lineHeight: 1.2
  titre-section:
    fontFamily: "Roboto, sans-serif"
    fontSize: "24sp"
    fontWeight: 700
    lineHeight: 1.25
  bouton-tuile:
    fontFamily: "Roboto, sans-serif"
    fontSize: "26sp"
    fontWeight: 700
    lineHeight: 1.2
  bouton-sos:
    fontFamily: "Roboto, sans-serif"
    fontSize: "36sp"
    fontWeight: 700
    lineHeight: 1.2
  bouton-admin:
    fontFamily: "Roboto, sans-serif"
    fontSize: "20sp"
    fontWeight: 700
    lineHeight: 1.2
  pave-pin:
    fontFamily: "Roboto, sans-serif"
    fontSize: "32sp"
    fontWeight: 700
    lineHeight: 1.2
  libelle-tuile:
    fontFamily: "Roboto, sans-serif"
    fontSize: "22sp"
    fontWeight: 700
    lineHeight: 1.2
  corps:
    fontFamily: "Roboto, sans-serif"
    fontSize: "20sp"
    fontWeight: 400
    lineHeight: 1.4
  secondaire:
    fontFamily: "Roboto, sans-serif"
    fontSize: "16sp"
    fontWeight: 400
    lineHeight: 1.4
  badge:
    fontFamily: "Roboto, sans-serif"
    fontSize: "14sp"
    fontWeight: 700
    lineHeight: 1
rounded:
  tuile: "24dp"
  bouton-admin: "16dp"
  vignette-photo: "8dp"
  pastille-couleur: "6dp"
  cercle: "50%"
spacing:
  padding-ecran: "16dp"
  padding-ecran-admin: "24dp"
  gap-tuile: "16dp"
  gap-ligne: "8dp"
  interne-bouton: "16dp"
  interne-tuile: "4dp"
components:
  tuile-raccourci:
    backgroundColor: "{colors.vert-appel}"
    textColor: "{colors.blanc}"
    rounded: "{rounded.tuile}"
    padding: "{spacing.interne-tuile}"
    height: "aspectRatio 1.6"
  tuile-sos:
    backgroundColor: "{colors.rouge-sos-familial}"
    textColor: "{colors.blanc}"
    rounded: "{rounded.tuile}"
    height: "100dp"
  bouton-admin:
    backgroundColor: "{colors.indigo-mecanique}"
    textColor: "{colors.blanc}"
    rounded: "{rounded.bouton-admin}"
    padding: "{spacing.interne-bouton}"
    height: "56dp"
  bouton-retour:
    backgroundColor: "{colors.indigo-mecanique}"
    textColor: "{colors.blanc}"
    rounded: "{rounded.bouton-admin}"
    padding: "24dp 16dp"
  touche-pin:
    backgroundColor: "{colors.indigo-mecanique}"
    textColor: "{colors.blanc}"
    rounded: "{rounded.cercle}"
    size: "80dp"
  touche-pin-action:
    backgroundColor: "{colors.surface-pale}"
    textColor: "{colors.rouge-sos-familial}"
    rounded: "{rounded.cercle}"
    size: "80dp"
  rangee-appli:
    backgroundColor: "{colors.surface-card}"
    textColor: "{colors.texte-principal}"
    rounded: "{rounded.bouton-admin}"
    padding: "12dp"
  badge:
    backgroundColor: "{colors.rouge-sos-familial}"
    textColor: "{colors.blanc}"
    rounded: "{rounded.cercle}"
    size: "32dp"
---

# Design System: Papy Launcher

## Overview

**Creative North Star: "La Porte Couleur"**

Chaque bouton est une porte peinte d'une couleur qu'on reconnaît. La navigation devient mémoire spatiale, pas lecture de texte : papa sait que « la verte » c'est les appels, « la rouge en bas » c'est le secours, sans avoir à déchiffrer un label. La couleur est l'identifiant, le texte est la confirmation.

Le système est un ensemble de **tuiles domestiques** : objets du quotidien peints en couleurs pleines, formes simples, robustesse tactile. Le blanc dominant est la pièce claire où les tuiles sont posées ; les couleurs profondes (pas de primaires criards) distinguent chaque fonction sans alarmer. Les coins sont arrondis doux (24dp sur les tuiles), les surfaces sont mates et pleines, il n'y a ni ombre, ni dégradé, ni mouvement décoratif. La matière est uniforme — seul le ton change.

La lisibilité sert la reconnaissance immédiate : 48sp pour l'horloge, 36sp pour le SOS, 26sp sur les tuiles, rien en dessous de 14sp. Le thème clair est forcé (`darkTheme = false`, `dynamicColor = false`) par construction — le senior ne bascule jamais en mode sombre illisible. L'Indigo Mécanique est la couleur de l'aidant (admin, PIN, retour) ; le Rouge SOS Familial est la seule couleur qui interrompt. Tout le reste vit dans une palette de six teintes fonctionnelles saturées mais profondes, choisies pour rester lisibles sur blanc avec un texte blanc par-dessus.

**Key Characteristics:**
- Tuiles pleines, coins 24dp, surface mate, aucune ombre.
- Couleurs fonctionnelles profondes (jamais de primaires criards), blanc dominant.
- Texte blanc gras sur tuiles colorées ; texte foncé (#333333) sur surfaces claires.
- Thème clair forcé — le mode sombre est un non-sens produit, pas un défaut.
- Tailles de texte élevées (14sp minimum, 48sp pour l'horloge) ; pas de hiérarchie fine, des sauts nets.
- Toutes les tailles en `sp`, tous les espacements en `dp` (suivre le réglagluaires de police système).
- Indigo Mécanique = couleur de l'admin ; Rouge SOS Familial = seule couleur d'alerte.
- Pas d'effet au survol (tactile pur) ; pas de mouvement décoratif.

## Colors

Palette de tuiles domestiques : six teintes fonctionnelles profondes pour les raccourcis, un indigo institutionnel pour l'admin, un rouge familial pour l'alerte, et une rampe de neutres du blanc au gris foncé pour les surfaces et le texte.

### Primary
- **Indigo Mécanique** (#1A237E) : couleur de l'aidant. Titres d'écrans admin (« Administration », « Mode Admin »), boutons de retour, pavé numérique PIN, boutons d'action admin, légende « Papy Launcher ». Signale « endroit sérieux » sans aggressivité. Jamais utilisée sur l'écran d'accueil senior.

### Secondary
- **Rouge SOS Familial** (#C62828) : seule couleur d'alerte. Bouton SOS pleine largeur, message « Code incorrect », touche d'effacement du pavé PIN, pastilles de notifications. Rouge direct mais pas sanitaire — un bouton d'appel maison, pas une alarme d'incendie.

### Tertiary (couleurs fonctionnelles des raccourcis)
- **Vert Appel** (#2E7D32) : bouton Appels. Vert sapin profond, lisible sur blanc.
- **Bleu Message** (#1565C0) : bouton SMS. Bleu ciel profond.
- **Vert WhatsApp** (#075E54) : bouton WhatsApp. Exception marque — teinte officielle de WhatsApp, reconnaissable.
- **Orange Courrier** (#EF6C00) : bouton Mail. Orange cuivre.
- **Violet Album** (#6A1B9A) : bouton Photos. Violet aubergine.
- **Gris Réglages** (#455A64) : bouton Applis. Gris ardoise.
- **Teal Caméra** (#00695C) : bouton Appareil photo. Teal foncé, distinct du Vert WhatsApp (#075E54) et du Vert Appel (#2E7D32).
- **Violet Prout** (#8E63BC) : bouton Prout. Violet plus clair que Violet Album (#6A1B9A), humoristique.

### Neutral
- **Blanc** (#FFFFFF) : surface dominante. Fond de l'application, texte sur tuiles colorées.
- **Surface Pale** (#EEEEEE) : fonds neutres secondaires (touches PIN d'action, vignettes photo).
- **Surface Card** (#F5F5F5) : rangées d'applis dans la liste.
- **Texte Principal** (#333333) : texte foncé sur surfaces claires (libellés, titres de section admin, noms d'applis).
- **Texte Secondaire** (#555555) : libellés d'accompagnement (« Entrez le code »).
- **Texte Aide** (#666666) : textes d'aide et captions (« Aucune photo trouvée », descriptions kiosque, note luminosité).
- **Texte Légende** (#888888) : « Papy Launcher » au-dessus de l'horloge, éléments discrets.
- **Contour Clair** (#CCCCCC) : indicateurs PIN vides.
- **Noir** (#000000) : fond plein écran photo.

### Named Rules
**The One Alert Rule.** Le Rouge SOS Familial est la seule couleur d'alerte autorisée. Il apparaît sur le bouton SOS, les pastilles de notification, les messages d'erreur et la touche d'effacement. Aucune autre touche de rouge. Une pastille rouge n'est pas de la décoration — c'est un signal.

**The Aidant Color Rule.** L'Indigo Mécanique est réservé à l'admin et à la navigation de retour. Il n'apparaît jamais sur l'écran d'accueil senior. La séparation des couleurs reflète la séparation des rôles : le senior vit dans les tuiles, l'aidant vit dans l'indigo.

**The Full Tile Rule.** Les couleurs de raccourcis remplissent toute la tuile ; le texte est blanc gras par-dessus. Jamais de tuile blanche avec bordure colorée, jamais de fond clair avec icône colorée. La couleur est l'identifiant.

## Typography

**Display Font:** Roboto (system default, sans-serif)
**Body Font:** Roboto (system default, sans-serif)

**Character:** Roboto系统, lisible à toutes les tailles, familier à tout utilisateur Android. Pas de fonte de marque — la reconnaissance vient de la taille et du poids, pas de la personnalité typographique. Toutes les tailles en `sp` pour suivre le réglage de police système (utile si un aidant augmente la taille système).

### Hierarchy
- **Horloge** (Bold, 48sp, 1.1) : heure en haut de l'écran d'accueil. La plus grande typo de l'app — papa la lit à 2 mètres.
- **Titre Écran** (Bold, 32sp, 1.2) : titres d'écrans admin (« Administration », « Mode Admin »).
- **Titre Section** (Bold, 24sp, 1.25) : titres de sections dans l'admin (« Bouton SOS », « Mode kiosque »).
- **Bouton Tuile** (Bold, 26sp, 1.2) : labels des raccourcis sur l'écran d'accueil.
- **Bouton SOS** (Bold, 36sp, 1.2) : label du bouton SOS — plus grand que les tuiles pour signaler l'exception.
- **Bouton Admin** (Bold, 20sp, 1.2) : boutons d'action admin (« Enregistrer le code », « Réglages Wi-Fi »).
- **Pavé PIN** (Bold, 32sp, 1.2) : chiffres du pavé numérique admin.
- **Libellé Tuile** (Bold, 22sp, 1.2) : titres secondaires (« Applications », « Photos » en haut de liste), label « Entrez le code ».
- **Corps** (Normal, 20sp, 1.4) : libellés de réglages admin, noms d'applis dans la liste, texte de bouton retour.
- **Secondaire** (Normal, 16sp, 1.4) : descriptions, notes d'aide. `bodyLarge` du Material theme.
- **Badge** (Bold, 14sp, 1) : compteurs dans les pastilles rouges. Minimum absolu — jamais en dessous.

### Named Rules
**The Size Jump Rule.** Pas de hiérarchie fine. Les tailles sautent par paliers nets (14 → 20 → 22 → 26 → 32 → 36 → 48). Un senior ne distingue pas 16sp de 18sp ; il distingue 20 de 26.

**The White-On-Color Rule.** Tout texte sur tuile colorée est blanc gras. Tout texte sur surface claire est foncé (#333333). Jamais de texte coloré sur couleur, jamais de texte clair sur blanc.

## Layout

Portrait uniquement (`android:screenOrientation="portrait"` verrouillé sur MainActivity). L'écran d'accueil est une colonne verticale : header horloge en haut, grille de tuiles 2-par-2 au centre (chaque tuile `aspectRatio 1.6f`, `weight 1f` par rangée, espacement 16dp), bouton SOS pleine largeur en bas. `Arrangement.SpaceEvenly` distribue verticalement.

L'écran admin est une colonne scrollable (`verticalScroll(rememberScrollState())`, padding 24dp) — sections empilées : SOS, PIN, Kiosque, Raccourcis, Réglages rapides, Luminosité, Retour. Les sections sont séparées par `Spacer 32dp`.

La liste d'applis et les photos sont des colonnes scrollables avec un header `Row` (bouton Retour à gauche, titre à droite). La grille photo est `LazyVerticalGrid` 3 colonnes, vignettes carrées (`aspectRatio 1f`), espacement 4dp.

Padding écran : 16dp sur l'accueil et les listes, 24dp sur l'admin (plus aéré pour la configuration). Toutes les tuiles et boutons respectent les cibles tactiles ≥48dp (les tuiles font bien plus).

## Elevation & Depth

Le système est **plat par construction**. Aucune ombre, aucun dégradé, aucun effet de profondeur. Les surfaces se distinguent par la couleur pleine, pas par l'élévation. Les tuiles sont des blocs colorés mats ; les rangées d'applis sont des cartes gris clair (#F5F5F5) sans ombre ; les boutons admin sont des bandes indigo pleines.

La profondeur visuelle vient uniquement du contraste de couleur : une tuile colorée sur fond blanc se détache par saturation, pas par élévation. Le plein écran photo est la seule surface noire — il « recule » par contraste avec le blanc dominant.

### Named Rules
**The Flat-By-Design Rule.** Pas d'ombre. Pas de `elevation`. Pas de tonal elevation Material. La hiérarchie visuelle est portée par la couleur et la taille, jamais par la profondeur. Une ombre sur une tuile est un bug.

## Shapes

Form language : coins arrondis doux, jamais aigus. Les tuiles (raccourcis et SOS) utilisent `RoundedCornerShape 24dp` — assez rond pour sembler amical, assez droit pour rester robuste. Les boutons admin et les rangées d'applis utilisent `16dp`. Les vignettes photo utilisent `8dp`. Les pastilles de couleur dans l'admin (indicateurs de raccourci) utilisent `6dp`.

Le cercle complet (`CircleShape`) est réservé à deux usages : les touches du pavé PIN (80dp, signaux d'entrée) et les pastilles de notification (32dp, badges). Le cercle signale « élément ponctuel », le rectangle arrondi signale « surface tactile ».

Pas de bordures. Les éléments se distinguent par couleur de fond pleine, pas par contour. L'unique exception est la pastille de notification qui a une bordure blanche 2dp pour se détacher sur la tuile colorée.

## Components

Philosophie : **boutons robustes et directs**. Gros, lisibles, sans fioritures. Le contraste et la taille font tout le travail.

### Tuile Raccourci (BigButton)
- **Shape:** `RoundedCornerShape 24dp`, `aspectRatio 1.6f`, `weight 1f` dans sa rangée.
- **Fond:** couleur fonctionnelle pleine (Vert Appel, Bleu Message, etc.).
- **Texte:** blanc, Bold 26sp, centré, padding 4dp.
- **Badge:** cercle rouge 32dp en `TopEnd` si > 0, bordure blanche 2dp, texte blanc 14sp (« 99+ » si > 99).
- **État:** aucun état visuel au survol (tactile pur). `Modifier.clickable` uniquement.
- **Variante:** aucune. La couleur et le label changent, la forme ne change pas.

### Tuile SOS (BigSosButton)
- **Shape:** `RoundedCornerShape 24dp`, `fillMaxWidth`, hauteur fixe 100dp.
- **Fond:** Rouge SOS Familial plein.
- **Texte:** blanc, Bold 36sp, centré. Plus grand que les tuiles raccourcis pour signaler l'exception.
- **État:** aucun. Tap → appel direct.

### Bouton Admin (AdminButton)
- **Shape:** `RoundedCornerShape 16dp`, `fillMaxWidth`, hauteur 56dp.
- **Fond:** Indigo Mécanique plein.
- **Texte:** blanc, Bold 20sp, centré horizontalement, padding horizontal 16dp.
- **Usage:** actions de l'écran admin (« Enregistrer le code », « Réglages Wi-Fi », etc.) et boutons « Retour » des écrans liste.

### Touche PIN (PinKey)
- **Shape:** `CircleShape`, 80dp.
- **Fond:** Indigo Mécanique plein pour les chiffres ; Surface Pale (#EEEEEE) pour les actions (C, X).
- **Texte:** blanc Bold 32sp sur chiffre ; Rouge SOS Familial Bold 32sp sur action.
- **État:** aucun. Tap → saisie ou action.

### Rangée Appli (AppRow)
- **Shape:** `RoundedCornerShape 16dp`, `fillMaxWidth`, padding 12dp.
- **Fond:** Surface Card (#F5F5F5) plein.
- **Contenu:** icône 48dp (AndroidView ImageView) + `Spacer 16dp` + nom appli Texte Principal Medium 20sp.
- **État:** `clickable` pur, aucun feedback visuel.

### Badge Pastille
- **Shape:** `CircleShape`, 32dp, bordure blanche 2dp, padding 8dp depuis le bord de la tuile.
- **Fond:** Rouge SOS Familial.
- **Texte:** blanc Bold 14sp, « 99+ » si > 99.
- **Position:** `Alignment.TopEnd` sur la tuile.

### Section Title (admin)
- **Texte:** Texte Principal Bold 24sp, `fillMaxWidth`, padding bas 12dp.
- **Usage:** séparateur visuel entre sections de l'écran admin.

### Header Horloge (ClockHeader)
- **Légende:** « Papy Launcher » Texte Légende 18sp Normal.
- **Heure:** Indigo Mécanique Bold 48sp, format `HH:mm`, mise à jour 1s.
- **Date:** Texte Principal 22sp Normal, format `EEEE d MMMM` (Locale.FRANCE), première lettre capitalisée.
- **Trigger admin:** `combinedClickable` — `onLongClick` (2s) ouvre le mode admin. Aucun feedback visuel du long appui.

### Composants Material utilisés tels quels
- **Switch** : toggles admin (SOS visible, kiosque, raccourcis). Style Material 3 par défaut.
- **OutlinedTextField** : champ « Numéro SOS », champ « Nouveau code ». Style Material 3 par défaut.
- **Slider** : luminosité (0–255). Style Material 3 par défaut.

## Do's and Don'ts

### Do:
- **Do** verrouiller le portrait (`android:screenOrientation="portrait"`) — le paysage casse la grille et n'a pas de sens pour un senior.
- **Do** forcer le thème clair (`darkTheme = false`, `dynamicColor = false`) — la lisibilité senior est non-négociable.
- **Do** utiliser des tailles en `sp` et des espacements en `dp` pour suivre le réglage de police système.
- **Do** remplir toute la tuile avec la couleur fonctionnelle ; texte blanc gras par-dessus.
- **Do** garder l'Indigo Mécanique pour l'admin et le Rouge SOS Familial pour l'alerte — la séparation des couleurs reflète la séparation des rôles.
- **Do** sauter par paliers nets de taille de texte (14 → 20 → 22 → 26 → 32 → 36 → 48) ; pas de hiérarchie fine.
- **Do** viser des cibles tactiles ≥ 48dp (les tuiles font bien plus, les touches PIN 80dp).
- **Do** faire défiler l'écran admin verticalement (`verticalScroll`) — le contenu dépasse l'écran.

### Don't:
- **Don't** ajouter d'ombres, d'élévation ou de dégradés. Le système est plat par construction.
- **Don't** utiliser des primaires criards (#F44336, #2196F3) — les couleurs sont profondes (#C62828, #1565C0) pour la lisibilité sans alarme.
- **Don't** mettre du texte coloré sur une tuile colorée, ni du texte clair sur fond blanc. Blanc sur couleur, foncé sur clair.
- **Don't** ajouter des effets au survol, des animations décoratives ou des transitions complexes. Tactile pur, mouvement nul.
- **Don't** utiliser l'Indigo Mécanique sur l'écran d'accueil senior — c'est la couleur de l'aidant.
- **Don't** utiliser une autre couleur que le Rouge SOS Familial pour alerter. Une pastille verte ou orange est un bug.
- **Don't** faire des tuiles blanches avec bordure colorée. La couleur est l'identifiant — elle remplit.
- **Don't** descendre en dessous de 14sp pour du texte lisible. Le senior ne le lira pas.
- **Don't** ajouter un mode sombre. C'est un non-sens produit, pas un défaut manquant.