package com.papy.launcher

import com.papy.launcher.ui.components.ScreenHeader
import com.papy.launcher.ui.theme.PapyRed
import com.papy.launcher.ui.theme.PapyTextDark
import com.papy.launcher.ui.theme.PapyTextGray
import com.papy.launcher.ui.theme.PapyTextLight
import com.papy.launcher.ui.theme.PapySurfaceLight
import com.papy.launcher.ui.theme.PapySurfaceMuted
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.provider.ContactsContract
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner

@Composable
fun ManageFavoritesScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var favorites by remember { mutableStateOf(Prefs.getFavorites(context)) }
    var contactsGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            favorites = Prefs.getFavorites(context)
            contactsGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose { lifecycleOwner.lifecycle.removeObserver(lifecycleObserver) }
    }

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
                            favorites = Prefs.getFavorites(context)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ScreenHeader(title = "Gérer les favoris", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))

        if (!contactsGranted) {
            Text(
                text = "Autorisez d'abord les contacts dans l'administration (section Permissions).",
                fontSize = 18.sp,
                color = PapyRed,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        AdminButton(
            label = if (contactsGranted) "Ajouter un favori" else "Ajouter un favori (contacts requis)"
        ) {
            if (contactsGranted) {
                pickContactLauncher.launch(null)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (favorites.isEmpty()) {
            Text(
                text = "Aucun favori. Touchez « Ajouter un favori ».",
                fontSize = 18.sp,
                color = PapyTextGray
            )
        } else {
            var resolvedFavorites by remember { mutableStateOf<List<Contact>>(emptyList()) }
            LaunchedEffect(favorites) {
                resolvedFavorites = withContext(Dispatchers.IO) {
                    favorites.mapNotNull { getContactByLookupKey(context, it) }
                }
            }
            for (contact in resolvedFavorites) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PapySurfaceLight)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PapySurfaceMuted),
                        contentAlignment = Alignment.Center
                    ) {
                        if (contact.photoUri != null) {
                            coil.compose.AsyncImage(
                                model = contact.photoUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
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
                                color = PapyTextDark
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contact.displayName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PapyTextDark
                        )
                        if (!contact.phoneNumber.isNullOrEmpty()) {
                            Text(
                                text = contact.phoneNumber,
                                fontSize = 16.sp,
                                color = PapyTextGray
                            )
                        } else {
                            Text(
                                text = "Aucun numéro",
                                fontSize = 16.sp,
                                color = PapyTextLight
                            )
                        }
                    }
                    Text(
                        text = "Retirer",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PapyRed,
                        modifier = Modifier.clickable {
                            Prefs.removeFavorite(context, contact.lookupKey)
                            favorites = Prefs.getFavorites(context)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}