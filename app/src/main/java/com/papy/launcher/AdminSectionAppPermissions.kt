package com.papy.launcher

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun AdminSectionAppPermissions(
    callPhoneGranted: Boolean,
    callLogGranted: Boolean,
    photosGranted: Boolean,
    contactsGranted: Boolean,
    context: Context,
    onCallPhoneResult: (Boolean) -> Unit,
    onCallLogResult: (Boolean) -> Unit,
    onPhotosResult: (Boolean) -> Unit,
    onContactsResult: (Boolean) -> Unit
) {
    SectionTitle("Permissions de l'application")

    PermissionButton(
        label = "Appels téléphoniques (SOS)",
        granted = callPhoneGranted,
        permission = Manifest.permission.CALL_PHONE,
        context = context,
        onResult = onCallPhoneResult
    )
    Spacer(modifier = Modifier.height(8.dp))

    PermissionButton(
        label = "Journal d'appels (appels manqués)",
        granted = callLogGranted,
        permission = Manifest.permission.READ_CALL_LOG,
        context = context,
        onResult = onCallLogResult
    )
    Spacer(modifier = Modifier.height(8.dp))

    PermissionButton(
        label = "Photos (visionneur)",
        granted = photosGranted,
        permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE,
        context = context,
        onResult = onPhotosResult
    )
    Spacer(modifier = Modifier.height(8.dp))

    PermissionButton(
        label = "Contacts (favoris)",
        granted = contactsGranted,
        permission = Manifest.permission.READ_CONTACTS,
        context = context,
        onResult = onContactsResult
    )
}