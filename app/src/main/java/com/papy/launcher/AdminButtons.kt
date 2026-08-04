package com.papy.launcher

import android.app.Activity
import android.content.Context
import android.os.Build
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.papy.launcher.ui.theme.PapyBlue
import com.papy.launcher.ui.theme.PapyBorder
import com.papy.launcher.ui.theme.PapyGreen
import com.papy.launcher.ui.theme.PapyGreenLight
import com.papy.launcher.ui.theme.PapyRedLight
import com.papy.launcher.ui.theme.PapyTextBlueGray
import com.papy.launcher.ui.theme.PapyTextDark

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = PapyTextDark,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    )
}

@Composable
fun AdminButton(
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PapyBlue)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun AdminLinkButton(
    label: String,
    active: Boolean,
    hint: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) PapyBlue else PapyTextBlueGray)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (active) PapyGreen else PapyBorder),
            contentAlignment = Alignment.Center
        ) {
            if (active) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = hint,
                fontSize = 14.sp,
                color = PapyBorder
            )
        }
        Text(
            text = if (active) "Activé" else "À activer",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) PapyGreenLight else PapyRedLight
        )
    }
}

@Composable
fun PermissionButton(
    label: String,
    granted: Boolean,
    permission: String,
    context: Context,
    onResult: (Boolean) -> Unit
) {
    val activity = context as? Activity
    val activityResult = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { g ->
        onResult(g)
        if (g) Toast.makeText(context, "Permission accordée", Toast.LENGTH_SHORT).show()
        else Toast.makeText(context, "Permission refusée — ouvrez les réglages de l'app", Toast.LENGTH_LONG).show()
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PapyBlue)
            .clickable {
                if (granted) {
                    openAppDetailsSettings(context, "désactivez « $label »")
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    openAppDetailsSettings(context, "activez « $label »")
                } else {
                    val canRequest = activity?.let {
                        !it.shouldShowRequestPermissionRationale(permission)
                    } != false
                    if (canRequest) {
                        activityResult.launch(permission)
                    } else {
                        openAppDetailsSettings(context, "activez « $label »")
                    }
                }
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (granted) PapyGreen else PapyBorder),
            contentAlignment = Alignment.Center
        ) {
            if (granted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (granted) "Accordée" else "À accorder",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (granted) PapyGreenLight else PapyRedLight
        )
    }
}