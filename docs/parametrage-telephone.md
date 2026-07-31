# Papy Launcher — Paramétrages téléphone (à faire une seule fois)

**Téléphone cible :** Samsung Galaxy A13 4G (Android 13)
**À faire par :** l'administrant (toi), pas par papa

---

## Étape 1 — Activer le mode développeur

1. **Paramètres** → **À propos du téléphone** → **Informations logicielles**
2. Taper **7 fois** rapidement sur **Numéro de version**
3. Message : "Vous êtes maintenant développeur"

## Étape 2 — Activer le débogage USB

1. **Paramètres** → **Options de développement** (apparu en bas)
2. Activer **Débogage USB**

## Étape 3 — Brancher le téléphone au PC

1. Brancher en USB
2. Sur l'écran du téléphone : **Autoriser le débogage USB** → cocher **Toujours** → **OK**

## Étape 4 — Installer Papy Launcher

1. Ouvrir Android Studio sur le PC
2. Sélectionner le téléphone dans le menu device (en haut)
3. Cliquer **Run ▶️** (ou `Shift+F10`)
4. L'appli se compile et s'installe sur le téléphone

## Étape 5 — Définir Papy Launcher comme écran d'accueil

1. Appuyer sur le bouton **Home** du téléphone
2. Android demande quel launcher utiliser → choisir **Papy Launcher**
3. Cocher **Toujours** (ou "Une seule fois" pour tester d'abord)

## Étape 6 — Accorder la permission "Journal d'appels"

> Nécessaire pour les pastilles d'appels manqués sur le bouton Appels.

- Au premier lancement de Papy Launcher, une boîte de dialogue apparaît
- → **Autoriser** l'accès au journal d'appels

(Si manquée : **Paramètres** → **Applications** → **Papy Launcher** → **Autorisations** → **Journal d'appels** → Autoriser)

## Étape 6 — Accorder la permission "Photos et vidéos"

> Nécessaire pour que le bouton Photos affiche les photos du téléphone dans le visionneur intégré.

- Au premier lancement de Papy Launcher, une boîte de dialogue demande l'accès aux photos
- → **Autoriser**

(Si manquée : **Paramètres** → **Applications** → **Papy Launcher** → **Autorisations** → **Photos et vidéos** → Autoriser)

> **Note** : sur Android 13+ la permission s'appelle "Photos et vidéos" (`READ_MEDIA_IMAGES`). Sur Android 12 et avant, c'est "Fichiers et contenu multimédia" (`READ_EXTERNAL_STORAGE`).

## Étape 7 — Activer l'accès aux notifications

> Nécessaire pour les pastilles de notifications sur les boutons SMS, Mail et WhatsApp.

1. **Paramètres** → **Notifications** → **Accès aux notifications**
   (ou : **Paramètres** → **Notifications** → **Notifications d'apps** → **Accès aux notifications**)
2. Trouver **Papy Launcher** dans la liste
3. → **Activer**

## Étape 8 — Autoriser l'affichage au-dessus des autres applis

> Nécessaire pour le bouton "Accueil" flottant qui apparaît quand papa est dans une autre appli.

1. **Paramètres** → **Applications** → **Papy Launcher** → **Afficher au-dessus des autres applications**
   (ou : **Paramètres** → **Applications** → **Accès special** → **Afficher au-dessus des autres applis** → **Papy Launcher**)
2. → **Autoriser**

## Étape 9 — Accorder la permission d'appel (CALL_PHONE)

> Nécessaire pour que le bouton SOS compose le numéro directement.

1. Ouvrir Papy Launcher
2. Long appui (2 secondes) sur **l'heure** en haut → écran admin
3. Saisir le PIN : **0000**
4. **Retour à l'accueil**
5. Appuyer sur le bouton **SOS**
6. Boîte de dialogue → **Autoriser** l'appel téléphonique

(Si manquée : **Paramètres** → **Applications** → **Papy Launcher** → **Autorisations** → **Téléphone** → Autoriser)

## Étape 10 — Activer le mode kiosque (optionnel mais recommandé)

> Empêche papa de quitter le launcher par erreur (swipe, bouton récents, ouverture d'applis non autorisées).

1. Ouvrir le mode admin : long appui sur **l'heure** → PIN **0000**
2. Section **Mode kiosque** → activer l'interrupteur **"Bloquer la sortie du launcher"**
3. Android ouvre **Paramètres** → **Accessibilité**
4. Trouver **Papy Launcher** dans la liste → **Activer**
5. Revenir sur Papy Launcher

> **Note** : le mode kiosque bloque les applis non autorisées et le bouton récents. Le swipe des notifications n'est pas parfaitement bloqué (limitation Android sans root). Pour un blocage total, voir "Device Owner + Lock Task Mode" dans le journal de dev.

## Étape 11 — Configurer le SOS (recommandé)

1. Ouvrir le mode admin : long appui sur **l'heure** → PIN **0000**
2. Section **Bouton SOS** :
   - **Numéro SOS** : mettre ton numéro de téléphone (ou 112 pour les urgences)
   - **Afficher le bouton SOS** : activé (ou désactivé si tu préfères)
3. **Changer le code admin** : saisir un nouveau code à 4 chiffres → **Enregistrer**
4. **Retour à l'écran d'accueil**

## Étape 12 — Choisir les raccourcis affichés (recommandé)

1. Ouvrir le mode admin : long appui sur **l'heure** → PIN
2. Descendre jusqu'à la section **Raccourcis affichés**
3. Activer/désactiver chaque bouton avec son interrupteur :
   - Appels, SMS, WhatsApp, Mail, Photos, Applis
4. **Retour à l'écran d'accueil** → seuls les boutons activés apparaissent

> **Note** : l'écran admin défile verticalement. S'il ne tient pas sur l'écran, fais-le défiler avec le doigt.

---

## Vérification finale

| Test | Action attendue |
|---|---|
| Appuyer sur **Home** | Papy Launcher s'affiche |
| Appuyer sur **Appels** | Le clavier téléphonique s'ouvre |
| Appuyer sur **SMS** | L'appli SMS s'ouvre |
| Appuyer sur **WhatsApp** | WhatsApp s'ouvre |
| Appuyer sur **Mail** | L'appli mail s'ouvre |
| Appuyer sur **Photos** | Le visionneur photo intégré s'ouvre (grille de vignettes) |
| Taper une photo dans le visionneur | La photo s'affiche en plein écran |
| Appuyer sur **Applis** | La liste des applis installées s'affiche |
| Appuyer sur **SOS** | Le numéro configuré est appelé |
| Envoyer un SMS au téléphone | Une pastille rouge apparaît sur le bouton SMS |
| Appeler le téléphone et ne pas répondre | Une pastille rouge apparaît sur le bouton Appels |
| Ouvrir une appli puis regarder en bas | Le bouton **Accueil** est visible |
| Long appui (2s) sur l'heure | L'écran de saisie du PIN apparaît |
| Dans l'admin, désactiver un raccourci | Il disparaît de l'écran d'accueil |

---

## En cas de problème

- **Papy Launcher ne se lance pas** : vérifier le débogage USB (Étape 2) et la connexion (Étape 3)
- **Pas de pastilles** : vérifier l'accès aux notifications (Étape 7) et le journal d'appels (Étape 6)
- **Pas de bouton Accueil** : vérifier l'affichage au-dessus des autres applis (Étape 8)
- **SOS ne marche pas** : vérifier la permission d'appel (Étape 9)
- **Photos : "Aucune photo trouvée"** : vérifier la permission photos (Étape 6)
- **Papa arrive à quitter le launcher** : vérifier le mode kiosque (Étape 10)
- **Écran admin incomplet** : faire défiler l'écran vers le bas avec le doigt