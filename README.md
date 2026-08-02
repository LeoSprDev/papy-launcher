# 📞 Papy Launcher

> Un écran d'accueil Android pensé pour les seniors. Gros boutons, couleurs qui parlent, zéro complication.

Papy Launcher remplace l'écran d'accueil Android par une surface unique, lisible et non-déconcertante. Papa (85 ans) veut passer un appel, lire ses SMS, voir ses photos — pas naviguer dans un tiroir d'applis ou se faire peur avec une notification. Ici, chaque fonction est une tuile colorée. La verte, c'est les appels. La rouge en bas, c'est le secours. La couleur est l'identifiant, le texte n'est que la confirmation.

Le projet est né d'un cas réel (papa, 85 ans, Galaxy A13), pas d'un cahier des charges "senior". Tout est pensé à partir de ses difficultés : gestuelles complexes, mémoire, jargon. Et tout reste local — aucune donnée ne quitte le téléphone.

---

## ✨ Fonctionnalités

**Écran d'accueil — 9 tuiles colorées configurables :**

| Tuile | Couleur | Action |
|---|---|---|
| 📞 Appels | Vert | Ouvre le dialer natif |
| 💬 SMS | Bleu | Appli SMS native |
| 🟢 WhatsApp | Vert WhatsApp | Lance WhatsApp |
| ✉️ Mail | Orange | Appli mail native |
| 🖼️ Photos | Violet | Visionneur photos intégré (Coil) |
| 📱 Applis | Gris | Liste des applis installées |
| 📷 Appareil photo | Teal | Caméra native |
| 💨 Prout | Violet clair | Son synthétisé via AudioTrack (oui, vraiment) |
| ⭐ Favoris | Indigo | Grille de contacts favoris (photo + appel direct) |

**Plus :**
- 🆘 **Bouton SOS** — appel direct au numéro configuré (modifiable, masquable)
- 🕐 **Horloge** en grand (48sp) + date sur l'écran d'accueil
- 🔔 **Pastilles de notifications** — appels manqués, SMS, mail, WhatsApp
- 🏠 **Bouton Accueil flottant** — toujours visible par-dessus les autres applis (overlay)
- 🔒 **Mode kiosque** — empêche papa de quitter le launcher par erreur (AccessibilityService, best-effort)

**Mode admin (PIN 4 chiffres) — pour l'aidant :**
- Trigger discret : **7 clics sur l'horloge** (comme le mode développeur Android)
- Configurer le SOS (numéro + affichage)
- Changer le PIN
- Activer/désactiver les raccourcis (les 9 tuiles)
- **Gérer les favoris** — ajouter/retirer depuis le carnet de contacts, avec photos
- Réglages rapides : Wi-Fi, données, Bluetooth, affichage, son, luminosité
- **Liens directs vers les autorisations système** (notifications, overlay, accessibilité, write settings, launcher par défaut) — avec indicateurs d'état (✅/⭕)
- **Boutons redemander les permissions** (CALL_PHONE, READ_CALL_LOG, photos, READ_CONTACTS)

---

## 📱 Compatibilité

- **minSdk 23** (Android 6.0) — tourne sur les vieux téléphones que possèdent les seniors
- **targetSdk 37**
- Testé sur **Google Pixel 6a** (Android 16)
- Ciblé pour **Samsung Galaxy A13 4G** (Android 13)
- **Portrait uniquement** (le paysage n'a pas de sens pour un senior)
- **Thème clair forcé** — le mode sombre est un non-sens produit, pas un défaut manquant

---

## 🔒 Vie privée

- **Aucun réseau** — aucune donnée ne quitte le téléphone
- **Aucune publicité, aucune télémétrie**
- **Aucune dépendance cloud** — Coil charge uniquement des URI locales
- Les favoris sont stockés par référence (`lookupKey`) — les contacts restent dans le carnet Android, Papy Launcher ne fait que les lire

---

## 🛠️ Installation

### Pré-requis
- Android Studio (Koala ou plus récent)
- Un téléphone Android (API 23+) avec le **débogage USB** activé

### Build & déploiement

```bash
git clone https://github.com/LeoSprDev/papy-launcher.git
cd papy-launcher
./gradlew assembleDebug
```

APK debug → `app/build/outputs/apk/debug/app-debug.apk`

**Via Android Studio :** branche le téléphone → sélectionne-le dans le menu device → `Run ▶️` (`Shift+F10`)

**Via ADB :**
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Paramétrage obligatoire sur le téléphone

Papy Launcher nécessite plusieurs autorisations manuelles (Android les cache derrière des écrans Settings). Tout est détaillé dans [`docs/parametrage-telephone.md`](docs/parametrage-telephone.md) (12 étapes, à faire une fois par l'aidant).

Les liens directs vers chaque autorisation sont accessibles depuis le **mode admin → section « Autorisations système »**, avec un indicateur ✅/⭕ pour voir ce qui reste à activer.

---

## 🏗️ Stack technique

- **Kotlin + Jetpack Compose** — UI déclarative
- **AndroidX + Compose BOM** 2026.02.01
- **Coil** 2.7.0 — chargement d'images (photos + favoris)
- **Material Icons Extended** — icônes des tuiles
- **Dépendance externe unique : Coil.** Tout le reste est AndroidX/Compose.

Pas de Room, pas de Retrofit, pas de Hilt. SharedPreferences pour le stockage (PIN, SOS, kiosque, raccourcis, favoris en JSON). Simple, léger, maintenable.

---

## 📂 Architecture

```
app/src/main/java/com/papy/launcher/
├── MainActivity.kt              — Activity principale, navigation 7 écrans, trigger admin (7 clics)
├── Prefs.kt                     — SharedPreferences (PIN, SOS, kiosque, raccourcis, favoris JSON)
├── Shortcuts.kt                 — Modèle des 9 raccourcis (enum + data class)
├── PinScreen.kt                 — Écran saisie PIN
├── AdminScreen.kt               — Config admin (SOS, PIN, kiosque, raccourcis, autorisations, permissions, réglages)
├── ManageFavoritesScreen.kt    — Gestion des favoris (ajouter/retirer)
├── FavoritesScreen.kt           — Écran favoris (grille photo + appel direct)
├── ContactsHelper.kt            — Queries ContactsContract
├── AppListScreen.kt             — Liste des applis installées
├── PhotosScreen.kt              — Visionneur photos
├── ProutSound.kt                — Synthèse son prout (AudioTrack)
├── PapyNotificationListener.kt  — Pastilles notif (SMS, mail, WhatsApp)
├── MissedCalls.kt               — Appels manqués (CallLog)
├── PapyKioskService.kt          — Mode kiosque (AccessibilityService)
├── HomeButtonService.kt         — Bouton Home flottant (overlay)
└── ui/theme/                    — Thème clair forcé, couleurs, typo
```

---

## 📖 Documentation

Tout est dans `docs/` :
- [`docs/handoff.md`](docs/handoff.md) — état du projet, architecture, reprise
- [`docs/parametrage-telephone.md`](docs/parametrage-telephone.md) — 12 étapes de paramétrage du téléphone
- [`docs/2026-07-31-journal-dev.md`](docs/2026-07-31-journal-dev.md) — journal de dev complet
- [`docs/superpowers/specs/`](docs/superpowers/specs/) — specs des features (Appareil photo/Prout, Favoris)
- [`DESIGN.md`](DESIGN.md) — design system complet (couleurs, typo, composants)
- [`PRODUCT.md`](PRODUCT.md) — document produit (utilisateurs, principes, accessibilité)

---

## 🎯 Principes produit

1. **Un écran, un job.** Pas de hub, pas de tiroirs cachés.
2. **Lisibilité avant esthétique.** 48sp pour l'horloge, 36sp pour le SOS, 26sp sur les tuiles.
3. **Papa ne peut pas se perdre.** Kiosque + bouton home flottant + absence d'entrées discrètes.
4. **Privacy par défaut, hors-ligne par construction.** Aucun réseau, aucune pub.
5. **Compatible avec les vieux Android.** minSdk 23.

---

## 📜 Licence

Apache 2.0 — voir [LICENSE](LICENSE).

---

## 🙋 Contexte

Papy Launcher a été construit pour papa (85 ans), par son fils, avec l'aide d'opencode. Le code est ouvert pour que d'autres aidants puissent l'adapter à leur propre parent. Si tu l'utilises, dis-moi comment ça marche pour le tien. 💜