# Contacts favoris avec photo — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ajouter un 9e raccourci « Favoris » sur l'écran d'accueil qui ouvre un écran dédié affichant les contacts favoris en tuiles photo, appel direct au tap, configuration admin par sélection depuis le carnet de contacts.

**Architecture:** Nouveau `ShortcutId.FAVORIS` dans le système existant. Nouveau fichier `ContactsHelper.kt` pour les queries `ContactsContract`. Nouvel écran `FavoritesScreen.kt` (grille scrollable). Nouvelle section dans `AdminScreen.kt` (ajout/retrait favoris). Stockage des `lookupKey` en SharedPreferences (JSON). Permission `READ_CONTACTS` runtime.

**Tech Stack:** Kotlin + Jetpack Compose, `ContactsContract` (Android framework), Coil `AsyncImage` (déjà présent), `org.json.JSONArray` (stdlib).

## Global Constraints

- minSdk 23, targetSdk 37 (copiés verbatim de `app/build.gradle.kts`).
- Pas de nouvelle dépendance externe (Coil déjà présent, `org.json` dans la stdlib Android).
- Couleurs du DESIGN.md : `indigo-favoris` = `#3949AB`, `Surface Card` = `#F5F5F5`, `Surface Pale` = `#EEEEEE`, `Texte Principal` = `#333333`, `Texte Aide` = `#666666`, `Rouge SOS Familial` = `#C62828`, `Indigo Mécanique` = `#1A237E`.
- Texte UI en français (dur dans `strings.xml`).
- Pas de commentaires dans le code sauf demande explicite.
- Vérifier le build après chaque tâche : `export ANDROID_HOME=/home/yo/Android/Sdk && ./gradlew :app:compileDebugKotlin 2>&1 | tail -15` (build successful attendu).

---

## File Structure

| Fichier | Action | Responsabilité |
|---|---|---|
| `app/src/main/res/values/strings.xml` | Modifier | Ajouter `btn_favoris` |
| `app/src/main/AndroidManifest.xml` | Modifier | Ajouter `READ_CONTACTS` |
| `app/src/main/java/com/papy/launcher/Shortcuts.kt` | Modifier | Ajouter `ShortcutId.FAVORIS` + entrée dans `Shortcuts.all` |
| `app/src/main/java/com/papy/launcher/Prefs.kt` | Modifier | Ajouter `getFavorites` / `addFavorite` / `removeFavorite` (JSON) |
| `app/src/main/java/com/papy/launcher/ContactsHelper.kt` | Créer | Queries `ContactsContract` : `Contact` data class + `getContactByLookupKey` + `getPhoneNumber` (privée) |
| `app/src/main/java/com/papy/launcher/FavoritesScreen.kt` | Créer | Écran dédié : grille 2 colonnes, `FavoriteTile`, appel direct |
| `app/src/main/java/com/papy/launcher/MainActivity.kt` | Modifier | Navigation `"favorites"` + `onFavorites` callback + action raccourci |
| `app/src/main/java/com/papy/launcher/AdminScreen.kt` | Modifier | Section « Favoris » : bouton ajouter, liste avec bouton retirer, `PermissionButton` contacts |
| `DESIGN.md` | Modifier | Ajouter `indigo-favoris` dans frontmatter + section Tertiary |

---

### Task 1: Raccourci FAVORIS + libellé + manifeste

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/AndroidManifest.xml:5` (après `CALL_PHONE`)
- Modify: `app/src/main/java/com/papy/launcher/Shortcuts.kt`
- Modify: `DESIGN.md`

**Interfaces:**
- Produces: `ShortcutId.FAVORIS` dans l'enum, `Shortcut(ShortcutId.FAVORIS, R.string.btn_favoris, Color(0xFF3949AB))` dans `Shortcuts.all`. `R.string.btn_favoris` = "Favoris". Permission `READ_CONTACTS` dans le manifeste.

- [ ] **Step 1: Ajouter le libellé dans strings.xml**

Modifier `app/src/main/res/values/strings.xml`, après la ligne `<string name="btn_prout">Prout</string>` :

```xml
    <string name="btn_favoris">Favoris</string>
```

- [ ] **Step 2: Ajouter la permission READ_CONTACTS au manifeste**

Modifier `app/src/main/AndroidManifest.xml`, après la ligne `<uses-permission android:name="android.permission.CALL_PHONE" />` :

```xml
    <uses-permission android:name="android.permission.READ_CONTACTS" />
```

- [ ] **Step 3: Ajouter ShortcutId.FAVORIS et l'entrée Shortcuts.all**

Modifier `app/src/main/java/com/papy/launcher/Shortcuts.kt` :

Enum — ajouter `FAVORIS` à la fin :
```kotlin
enum class ShortcutId {
    APPELS, SMS, WHATSAPP, MAIL, PHOTOS, APPLIS, APPAREIL_PHOTO, PROUT, FAVORIS
}
```

Liste `Shortcuts.all` — ajouter après la ligne PROUT :
```kotlin
        Shortcut(ShortcutId.FAVORIS, R.string.btn_favoris, Color(0xFF3949AB))
```

- [ ] **Step 4: Ajouter la couleur dans DESIGN.md**

Dans le frontmatter `colors:`, après `violet-prout: "#8E63BC"` :
```yaml
  indigo-favoris: "#3949AB"
```

Dans la section `### Tertiary`, après la ligne `**Violet Prout**` :
```markdown
- **Indigo Favoris** (#3949AB) : bouton Favoris. Indigo profond, plus clair que l'Indigo Mécanique (#1A237E) — distinct pour ne pas confondre avec l'admin, mais dans la même famille bleutée.
```

- [ ] **Step 5: Vérifier le build**

Run: `export ANDROID_HOME=/home/yo/Android/Sdk && ./gradlew :app:compileDebugKotlin 2>&1 | tail -15`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/res/values/strings.xml app/src/main/AndroidManifest.xml app/src/main/java/com/papy/launcher/Shortcuts.kt DESIGN.md
git commit -m "Favoris: raccourci FAVORIS + libellé + permission READ_CONTACTS"
```

---

### Task 2: Stockage des favoris dans Prefs (JSON)

**Files:**
- Modify: `app/src/main/java/com/papy/launcher/Prefs.kt`

**Interfaces:**
- Produces: `Prefs.getFavorites(context): List<String>`, `Prefs.addFavorite(context, lookupKey: String)`, `Prefs.removeFavorite(context, lookupKey: String)`. Les `lookupKey` sont stockés ordonnés, dédoublonnés.

- [ ] **Step 1: Ajouter les fonctions de favoris dans Prefs.kt**

Modifier `app/src/main/java/com/papy/launcher/Prefs.kt`, ajouter les imports en haut du fichier (après `import android.content.SharedPreferences`) :

```kotlin
import org.json.JSONArray
```

Ajouter à la fin de l'object `Prefs` (avant le `}` fermant de l'object), après la fonction `getEnabledShortcuts` :

```kotlin
    private const val KEY_FAVORITES = "favorites_list"

    fun getFavorites(context: Context): List<String> {
        val json = prefs(context).getString(KEY_FAVORITES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addFavorite(context: Context, lookupKey: String) {
        val current = getFavorites(context).toMutableList()
        if (lookupKey in current) return
        current.add(lookupKey)
        prefs(context).edit().putString(KEY_FAVORITES, JSONArray(current).toString()).apply()
    }

    fun removeFavorite(context: Context, lookupKey: String) {
        val current = getFavorites(context).toMutableList()
        if (lookupKey !in current) return
        current.remove(lookupKey)
        prefs(context).edit().putString(KEY_FAVORITES, JSONArray(current).toString()).apply()
    }
```

- [ ] **Step 2: Vérifier le build**

Run: `export ANDROID_HOME=/home/yo/Android/Sdk && ./gradlew :app:compileDebugKotlin 2>&1 | tail -15`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/papy/launcher/Prefs.kt
git commit -m "Favoris: stockage JSON des lookupKey dans Prefs"
```

---

### Task 3: ContactsHelper — queries ContactsContract

**Files:**
- Create: `app/src/main/java/com/papy/launcher/ContactsHelper.kt`

**Interfaces:**
- Produces: `data class Contact(lookupKey: String, displayName: String, phoneNumber: String?, photoUri: Uri?)`, `fun getContactByLookupKey(context: Context, lookupKey: String): Contact?`. La fonction `getPhoneNumber` est privée.

- [ ] **Step 1: Créer ContactsHelper.kt**

Créer le fichier `app/src/main/java/com/papy/launcher/ContactsHelper.kt` :

```kotlin
package com.papy.launcher

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

data class Contact(
    val lookupKey: String,
    val displayName: String,
    val phoneNumber: String?,
    val photoUri: Uri?
)

fun getContactByLookupKey(context: Context, lookupKey: String): Contact? {
    val uri = ContactsContract.Contacts.getLookupUri(-1L, lookupKey) ?: return null
    val resolved = try {
        context.contentResolver.query(
            uri,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
            ),
            null, null, null
        )
    } catch (e: Exception) {
        return null
    }
    resolved?.use { cursor ->
        if (!cursor.moveToFirst()) return null
        val contactId = cursor.getLong(0)
        val displayName = cursor.getString(1) ?: return null
        val photoUriStr = cursor.getString(2)
        val photoUri = photoUriStr?.let { Uri.parse(it) }
        val hasPhone = cursor.getInt(3) == 1
        val phoneNumber = if (hasPhone) getPhoneNumber(context, contactId) else null
        return Contact(lookupKey, displayName, phoneNumber, photoUri)
    }
    return null
}

private fun getPhoneNumber(context: Context, contactId: Long): String? {
    val cursor = try {
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE
            ),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )
    } catch (e: Exception) {
        return null
    }
    cursor?.use {
        var mobile: String? = null
        var first: String? = null
        while (it.moveToNext()) {
            val number = it.getString(0) ?: continue
            val type = it.getInt(1)
            if (first == null) first = number
            if (type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                mobile = number
                break
            }
        }
        return mobile ?: first
    }
    return null
}
```

- [ ] **Step 2: Vérifier le build**

Run: `export ANDROID_HOME=/home/yo/Android/Sdk && ./gradlew :app:compileDebugKotlin 2>&1 | tail -15`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/papy/launcher/ContactsHelper.kt
git commit -m "Favoris: ContactsHelper (queries ContactsContract)"
```

---

### Task 4: FavoritesScreen — écran dédié

**Files:**
- Create: `app/src/main/java/com/papy/launcher/FavoritesScreen.kt`

**Interfaces:**
- Consumes: `Prefs.getFavorites(context): List<String>`, `getContactByLookupKey(context, lookupKey): Contact?`, `performCall(context, number)` (de `MainActivity.kt:484`), `Context.checkSelfPermission(Manifest.permission.CALL_PHONE)`.
- Produces: `@Composable fun FavoritesScreen(onBack: () -> Unit)`.

- [ ] **Step 1: Créer FavoritesScreen.kt**

Créer le fichier `app/src/main/java/com/papy/launcher/FavoritesScreen.kt` :

```kotlin
package com.papy.launcher

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage

@Composable
fun FavoritesScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val lookupKeys = Prefs.getFavorites(context)
        val resolved = lookupKeys.mapNotNull { getContactByLookupKey(context, it) }
        contacts = resolved
        loaded = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A237E))
                    .clickable { onBack() }
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Retour",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Favoris",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (loaded && contacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucun favori. Ajoutez-en depuis l'administration.",
                    fontSize = 20.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contacts) { contact ->
                    FavoriteTile(contact = contact, context = context)
                }
            }
        }
    }
}

@Composable
fun FavoriteTile(contact: Contact, context: Context) {
    val hasNumber = !contact.phoneNumber.isNullOrEmpty()
    val modifier = if (hasNumber) {
        Modifier.clickable {
            val number = contact.phoneNumber!!
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                performCall(context, number)
            } else {
                android.widget.Toast.makeText(
                    context,
                    "Autorisez les appels téléphoniques dans l'administration",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    } else {
        Modifier.alpha(0.5f).clickable {
            android.widget.Toast.makeText(
                context,
                "Aucun numéro pour ce contact",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F5F5))
            .then(modifier)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFFEEEEEE)),
            contentAlignment = Alignment.Center
        ) {
            if (contact.photoUri != null) {
                AsyncImage(
                    model = contact.photoUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                )
            } else {
                val initials = contact.displayName.split(" ")
                    .mapNotNull { it.firstOrNull()?.uppercase() }
                    .take(2)
                    .joinToString("")
                Text(
                    text = initials,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = contact.displayName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
```

- [ ] **Step 2: Vérifier le build**

Run: `export ANDROID_HOME=/home/yo/Android/Sdk && ./gradlew :app:compileDebugKotlin 2>&1 | tail -15`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/papy/launcher/FavoritesScreen.kt
git commit -m "Favoris: écran FavoritesScreen (grille photo + appel direct)"
```

---

### Task 5: Navigation MainActivity

**Files:**
- Modify: `app/src/main/java/com/papy/launcher/MainActivity.kt:177` (AppNavigation when block)
- Modify: `app/src/main/java/com/papy/launcher/MainActivity.kt:213` (HomeScreen signature + onFavorites)
- Modify: `app/src/main/java/com/papy/launcher/MainActivity.kt:264` (when sc.id actions)

**Interfaces:**
- Consumes: `FavoritesScreen(onBack: () -> Unit)` de Task 4.

- [ ] **Step 1: Ajouter la navigation favorites dans AppNavigation**

Dans `MainActivity.kt`, dans la fonction `AppNavigation`, après le bloc `"photos" -> PhotosScreen(...)` :

```kotlin
            "favorites" -> FavoritesScreen(
                onBack = { screen = "home" }
            )
```

- [ ] **Step 2: Ajouter onFavorites à HomeScreen**

Signature de `HomeScreen` — ajouter le paramètre `onFavorites: () -> Unit` :

```kotlin
@Composable
fun HomeScreen(
    onAdminTrigger: () -> Unit,
    onApplis: () -> Unit,
    onPhotos: () -> Unit,
    onFavorites: () -> Unit
) {
```

Dans l'appel `HomeScreen(...)` de `AppNavigation`, ajouter `onFavorites = { screen = "favorites" }` :

```kotlin
            "home" -> HomeScreen(
                onAdminTrigger = { screen = "pin" },
                onApplis = { screen = "applist" },
                onPhotos = { screen = "photos" },
                onFavorites = { screen = "favorites" }
            )
```

- [ ] **Step 3: Ajouter l'action raccourci FAVORIS dans le when sc.id**

Dans le `when (sc.id)` des actions (vers la ligne 264), ajouter après `ShortcutId.PROUT -> { ctx -> playFartSound(ctx) }` :

```kotlin
                        ShortcutId.FAVORIS -> { ctx -> onFavorites() }
```

- [ ] **Step 4: Vérifier le build**

Run: `export ANDROID_HOME=/home/yo/Android/Sdk && ./gradlew :app:compileDebugKotlin 2>&1 | tail -15`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/papy/launcher/MainActivity.kt
git commit -m "Favoris: navigation favorites dans MainActivity"
```

---

### Task 6: Section Favoris dans AdminScreen

**Files:**
- Modify: `app/src/main/java/com/papy/launcher/AdminScreen.kt`

**Interfaces:**
- Consumes: `Prefs.getFavorites/addFavorite/removeFavorite` (Task 2), `getContactByLookupKey` (Task 3), `PermissionButton` (existant dans AdminScreen), `ContactsContract.Contacts.CONTENT_URI`, `ActivityForResult` pick contact.
- Produces: section « Favoris » dans l'admin avec ajout/retrait.

- [ ] **Step 1: Ajouter les imports nécessaires**

Dans `AdminScreen.kt`, ajouter aux imports existants (après `import android.provider.Settings`) :

```kotlin
import android.provider.ContactsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
```

- [ ] **Step 2: Ajouter la section Favoris dans AdminScreen**

Dans le corps de la fonction `AdminScreen`, **après** la section « Raccourcis affichés » (la boucle `for (sc in Shortcuts.all)`) et **avant** la ligne `Spacer(modifier = Modifier.height(32.dp))` qui précède « Réglages rapides », insérer :

```kotlin
        Spacer(modifier = Modifier.height(32.dp))

        // Section Favoris
        SectionTitle("Favoris")

        val favorites = remember { mutableStateOf(Prefs.getFavorites(context)) }
        val pickContactLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.PickContact()
        ) { uri ->
            if (uri != null) {
                val cursor = try {
                    context.contentResolver.query(
                        uri,
                        arrayOf(ContactsContract.Contacts.LOOKUP_KEY),
                        null, null, null
                    )
                } catch (e: Exception) {
                    null
                }
                cursor?.use {
                    if (it.moveToFirst()) {
                        val lookupKey = it.getString(0)
                        if (lookupKey != null) {
                            if (Prefs.getFavorites(context).contains(lookupKey)) {
                                Toast.makeText(context, "Déjà dans les favoris", Toast.LENGTH_SHORT).show()
                            } else {
                                Prefs.addFavorite(context, lookupKey)
                                favorites.value = Prefs.getFavorites(context)
                            }
                        }
                    }
                }
            }
        }

        val contactsGranted = isGranted(context, Manifest.permission.READ_CONTACTS)
        AdminButton(
            label = if (contactsGranted) "Ajouter un favori" else "Autoriser les contacts d'abord"
        ) {
            if (contactsGranted) {
                pickContactLauncher.launch(null)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (favorites.value.isEmpty()) {
            Text(
                text = "Aucun favori. Touchez « Ajouter un favori ».",
                fontSize = 16.sp,
                color = Color(0xFF666666)
            )
        } else {
            for (lookupKey in favorites.value) {
                val contact = remember(lookupKey) { getContactByLookupKey(context, lookupKey) }
                if (contact == null) continue
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEEEEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (contact.photoUri != null) {
                            coil.compose.AsyncImage(
                                model = contact.photoUri,
                                contentDescription = null,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            val initials = contact.displayName.split(" ")
                                .mapNotNull { it.firstOrNull()?.uppercase() }
                                .take(2)
                                .joinToString("")
                            Text(
                                text = initials,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contact.displayName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        if (!contact.phoneNumber.isNullOrEmpty()) {
                            Text(
                                text = contact.phoneNumber,
                                fontSize = 16.sp,
                                color = Color(0xFF666666)
                            )
                        } else {
                            Text(
                                text = "Aucun numéro",
                                fontSize = 16.sp,
                                color = Color(0xFF888888)
                            )
                        }
                    }
                    Text(
                        text = "Retirer",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828),
                        modifier = Modifier.clickable {
                            Prefs.removeFavorite(context, lookupKey)
                            favorites.value = Prefs.getFavorites(context)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
```

- [ ] **Step 3: Ajouter le PermissionButton READ_CONTACTS dans la section Permissions**

Dans la section « Permissions de l'application » (après le `PermissionButton` photos), ajouter :

```kotlin
        Spacer(modifier = Modifier.height(8.dp))

        PermissionButton(
            label = "Contacts (favoris)",
            granted = isGranted(context, Manifest.permission.READ_CONTACTS),
            permission = Manifest.permission.READ_CONTACTS,
            context = context,
            onResult = { }
        )
```

- [ ] **Step 4: Ajouter l'import Coil AsyncImage**

Vérifier en haut de `AdminScreen.kt` que l'import existe, sinon ajouter :

```kotlin
import coil.compose.AsyncImage
```

Note : l'utilisation se fait via `coil.compose.AsyncImage` qualifié pleinement dans le code ci-dessus, donc l'import n'est pas strictement requis. Le build confirmera.

- [ ] **Step 5: Vérifier le build**

Run: `export ANDROID_HOME=/home/yo/Android/Sdk && ./gradlew :app:compileDebugKotlin 2>&1 | tail -15`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/papy/launcher/AdminScreen.kt
git commit -m "Favoris: section admin (ajout/retrait + permission READ_CONTACTS)"
```

---

### Task 7: Build APK final + push

**Files:**
- Aucune modification de code.

- [ ] **Step 1: Build APK debug complet**

Run: `export ANDROID_HOME=/home/yo/Android/Sdk && ./gradlew :app:assembleDebug 2>&1 | tail -20`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Pousser sur le remote**

```bash
git push
```
Expected: push OK, branche main à jour.

---

## Self-Review

**Spec coverage :**
- Section 1 (Shortcuts + couleur + strings + manifeste) → Task 1 ✅
- Section 3 (Prefs stockage JSON) → Task 2 ✅
- Section 4 (ContactsHelper) → Task 3 ✅
- Section 5 (FavoritesScreen + tuile + cas) → Task 4 ✅
- Section 6 (Navigation) → Task 5 ✅
- Section 7 (AdminScreen section + ajout + retrait + permission) → Task 6 ✅
- Section 8 (Manifeste READ_CONTACTS) → Task 1 ✅
- Section 9 (DESIGN.md couleur) → Task 1 ✅
- Tests manuels (section Tests de la spec) → non automatisés, à faire sur device après Task 7

**Placeholder scan :** aucun TODO/TBD. Tous les steps contiennent le code réel.

**Type consistency :** `Contact(lookupKey, displayName, phoneNumber, photoUri)` cohérent entre Task 3 (définition), Task 4 (consommation), Task 6 (consommation). `Prefs.getFavorites/addFavorite/removeFavorite` cohérents entre Task 2 (définition), Task 4 (consommation), Task 6 (consommation). `FavoritesScreen(onBack)` cohérent entre Task 4 (définition) et Task 5 (navigation).