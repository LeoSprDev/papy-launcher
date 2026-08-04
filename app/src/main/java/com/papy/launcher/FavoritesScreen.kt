package com.papy.launcher

import android.Manifest

import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.papy.launcher.ui.components.ScreenHeader
import com.papy.launcher.ui.theme.PapyTextGray
import com.papy.launcher.ui.theme.PapyTextDark
import com.papy.launcher.ui.theme.PapySurfaceLight
import com.papy.launcher.ui.theme.PapySurfaceMuted

@Composable
fun FavoritesScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }
    var contactsGranted by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        contactsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        val lookupKeys = Prefs.getFavorites(context)
        val resolved = if (contactsGranted) lookupKeys.mapNotNull { getContactByLookupKey(context, it) } else emptyList()
        contacts = resolved
        loaded = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScreenHeader(title = "Favoris", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))

        if (loaded && contacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (!contactsGranted && Prefs.getFavorites(context).isNotEmpty())
                        "Autorisez l'accès aux contacts dans l'administration."
                    else
                        "Aucun favori. Ajoutez-en depuis l'administration.",
                    fontSize = 20.sp,
                    color = PapyTextGray,
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
                    FavoriteTile(contact = contact) {
                        val number = contact.phoneNumber
                        if (number != null && ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
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
                }
            }
        }
    }
}

@Composable
fun FavoriteTile(contact: Contact, onClick: () -> Unit) {
    val hasNumber = !contact.phoneNumber.isNullOrEmpty()
    val localContext = LocalContext.current
    val modifier = if (hasNumber) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier.alpha(0.5f).clickable {
            android.widget.Toast.makeText(
                localContext,
                "Aucun numéro pour ce contact",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(PapySurfaceLight)
            .then(modifier)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(PapySurfaceMuted),
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
                    color = PapyTextDark
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = contact.displayName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = PapyTextDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}