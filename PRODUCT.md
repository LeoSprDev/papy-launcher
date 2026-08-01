# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Stack

Kotlin + Jetpack Compose; AndroidX + Compose BOM 2026.02.01; minSdk 23 (Android 6.0), targetSdk 37; dépendance externe unique Coil 2.7.0 (chargement d'images locales); build Gradle Kotlin DSL. Aucun réseau, aucune télémétrie.

## Users

**Utilisateur principal :** personne âgée (85 ans, cas fondateur : papa) équipée d'un smartphone Android d'entrée de gamme (Samsung Galaxy A13 4G, Android 13). Job : passer/recevoir un appel, WhatsApp vidéo, lire et envoyer SMS et mails, voir ses photos, lancer quelques applis, déclencher le SOS — sans se perdre dans Android. Difficultés confirmées : gestuelles complexes (slide, swipe, clic long), mémoire (oublie les procédures en quelques jours), jargon technique.

**Utilisateur secondaire :** l'administrant (le fils / aidant familial). Configure le launcher une fois, à distance ou en présentiel, puis intervient rarement. Job : paramétrer SOS, PIN, raccourcis, kiosque, réglages rapides.

**Audience visée à terme :** autres seniors / aidants — launcher partageable et réutilisable (open source, éventuellement Play Store). Les décisions de design doivent rester génériques et non durcir un cas personnel unique.

## Product Purpose

Remplacer l'écran d'accueil Android par une surface unique, lisible et non-déconcertante qui donne accès aux seules fonctions utiles à un senior, tout en laissant un aidant reprendre la main via un mode admin protégé. Succès = papa utilise son téléphone seul, sans appel à l'aidant, sans quitter le launcher par erreur.

## Positioning

Launcher Android minimal, privacy-first et hors-ligne, conçu depuis le cas réel d'un senior de 85 ans — pas un launcher généraliste thémé « senior ». Concurrency : les launchers seniors existants sont soit legacy/lourds (BaldPhone) soit récents mais exigeants (Android 14+). Papy Launcher se positionne sur la compatibilité large (minSdk 23), l'absence totale de réseau/publicité, et un mode kiosque best-effort intégré.

## Operating Context

- **Device cible :** Samsung Galaxy A13 4G (Android 13). **Device test :** Google Pixel 6a (Android 16).
- **Orientation :** portrait uniquement (verrouillé via `android:screenOrientation="portrait"` sur MainActivity pour éviter plantages).
- **Thème :** clair forcé (`LightColorScheme`, `Color.White`), pas de mode sombre — lisibilité senior.
- **Mode kiosque :** AccessibilityService + liste blanche d'applis ; best-effort (le swipe notifications n'est pas parfaitement bloqué sans root / Device Owner).
- **Permissions à activer manuellement une fois :** accès notifications, overlay (bouton home flottant), accessibilité (kiosque), écriture réglages (luminosité). Voir `docs/parametrage-telephone.md`.
- **Installation :** APK debug déployé via Android Studio ou ADB ; pas encore d'APK release signé.

## Capabilities and Constraints

**Fonctionnel confirmé (validé sur device) :**
- Écran d'accueil : grille dynamique de gros boutons colorés (Appels, SMS, WhatsApp, Mail, Photos, Applis) + bouton SOS pleine largeur (affichable/masquable).
- Appels (dialer), SMS (appli native), WhatsApp (lancement + détection), Mail (appli native), Photos (visionneur intégré grille + plein écran via Coil), Liste applis installées (icônes + noms).
- SOS : appel direct (`ACTION_CALL`) sur numéro configurable, permission runtime.
- Mode admin : trigger discret (long appui 2s sur l'heure), PIN 4 chiffres (défaut `0000`), écran admin scrollable.
- Config admin : SOS on/off + numéro, changement PIN, kiosque on/off, raccourcis on/off par bouton, réglages rapides (Wi-Fi, données, Bluetooth, affichage, son), slider luminosité.
- Pastilles : appels manqués (`CallLog`), SMS/Mail/WhatsApp (`NotificationListenerService`).
- Bouton Home flottant (overlay `WindowManager`, service foreground).
- Horloge + date en grand, locale FR.

**Contraintes techniques :**
- Aucune donnée ne quitte le téléphone (privacy-first, hors-ligne).
- SharedPreferences local pour PIN, SOS, kiosque, raccourcis, préférences.
- Navigation 5 écrans : `home`/`pin`/`admin`/`applist`/`photos`.
- Mode kiosque best-effort (AccessibilityService) ; alternative future Device Owner + Lock Task Mode nécessite factory reset / téléphone sans compte Google.

**Indécis / ouverts :**
- Raccourcis réglages simplifiés sur l'accueil pour papa (actuellement admin-only) — choix à confirmer.
- Icône d'appli personnalisée (actuellement défaut Android).
- Internationalisation (FR dur actuellement ; EN à prévoir si distribution large).
- APK release signé et publication Play Store.

## Brand Commitments

- **Nom :** « Papy Launcher » — définitif.
- **Langue UI :** français — définitif (i18n EN possible sans changer la langue par défaut).
- **Ton :** bienveillant, simple, chaleureux, sans jargon — à figer dans les libellés et messages.
- **Icône / identité visuelle :** non encore établie (ouverte, mais doit rester lisible en grande taille pour un senior).

## Evidence on Hand

- `docs/2026-07-31-journal-dev.md` — journal complet : contexte, installation, code, bugs, décisions.
- `docs/handoff.md` — état du projet, architecture, reprise.
- `docs/parametrage-telephone.md` — 11 étapes d'activation sur le téléphone cible.
- Code source Kotlin vérifié dans `app/src/main/java/com/papy/launcher/`.
- APK debug généré (`app/build/outputs/apk/debug/app-debug.apk`, ~12.2 Mo).
- Tests unitaires/instrumentés présents en squelette mais vides — pas encore de couverture de test.
- Aucun témoignage, capture utilisateur final, ou métrique d'usage real-world pour l'instant ; ne pas fabriquer.

## Product Principles

1. **Un écran, un job.** Chaque écran sert une intention claire ; pas de hub, pas de tiroirs cachés. La profondeur de navigation se compte en appuis, pas en niveaux.
2. **Lisibilité avant esthétique.** Gros boutons, gros texte, contraste fort, thème clair forcé. Le design sert la reconnaissance immédiate, pas l'expression.
3. **Papa ne peut pas se perdre.** Le mode kiosque, le bouton home flottant et l'absence d'entrées discrètes rendent la sortie du launcher difficile par erreur. L'aidant reprend la main, pas le senior.
4. **Privacy par défaut, hors-ligne par construction.** Aucun réseau, aucune pub, aucune télémétrie. Les données restent sur le téléphone.
5. **Compatible avec les vieux Android.** minSdk 23 : le launcher tourne sur l'appareil réel du senior, pas seulement sur des flagships récents.

## Accessibility & Inclusion

**Besoins confirmés (cas fondateur + contraintes produit durables) :**
- Cibles tactiles larges, gestures simples uniquement (pas de swipe/slide ; clic long réservé au trigger admin discret).
- Contraste fort et thème clair forcé.
- Charge cognitive minimale : écrans ultra-minimaux, chemins courts, pas de jargon, libellés explicites en français.
- Mémoire : les procédures doivent être redécouvrables à l'écran, sans dépendre de l'apprentissage antérieur.
- Compatibilité ascendante : minSdk 23 pour rester installable sur des appareils anciens que possèdent les seniors.