package com.papy.launcher

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.papy.launcher.ui.theme.PapyBlue
import com.papy.launcher.ui.theme.PapyBorder
import com.papy.launcher.ui.theme.PapyRed
import com.papy.launcher.ui.theme.PapySurfaceMuted
import com.papy.launcher.ui.theme.PapyTextGrayAlt

@Composable
fun PinScreen(
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val enteredDigits = remember { mutableStateListOf<Int>() }
    val showError = remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val maxDigits = 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Mode Admin",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = PapyBlue
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Entrez le code",
            fontSize = 22.sp,
            color = PapyTextGrayAlt
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Indicateurs de chiffres saisis (cercles)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            for (i in 0 until maxDigits) {
                val filled = i < enteredDigits.size
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (filled) PapyBlue else PapyBorder)
                )
            }
        }

        if (showError.value) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Code incorrect",
                fontSize = 20.sp,
                color = PapyRed,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Pavé numérique 1-9, puis 0 + effacer
        for (row in listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9))) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (digit in row) {
                    PinKey(digit.toString()) {
                        if (enteredDigits.size < maxDigits) {
                            enteredDigits.add(digit)
                            showError.value = false
                            if (enteredDigits.size == maxDigits) {
                                val pin = enteredDigits.joinToString("")
                                if (pin == Prefs.getPin(context)) {
                                    onSuccess()
                                } else {
                                    showError.value = true
                                    enteredDigits.clear()
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Dernière rangée : Effacer, 0, Annuler
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PinKey("C") {
                if (enteredDigits.isNotEmpty()) {
                    enteredDigits.removeAt(enteredDigits.size - 1)
                    showError.value = false
                }
            }
            PinKey("0") {
                if (enteredDigits.size < maxDigits) {
                    enteredDigits.add(0)
                    showError.value = false
                    if (enteredDigits.size == maxDigits) {
                        val pin = enteredDigits.joinToString("")
                        if (pin == Prefs.getPin(context)) {
                            onSuccess()
                        } else {
                            showError.value = true
                            enteredDigits.clear()
                        }
                    }
                }
            }
            PinKey("X") {
                onCancel()
            }
        }
    }
}

@Composable
fun PinKey(
    label: String,
    onClick: () -> Unit
) {
    val isAction = label == "C" || label == "X"
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(if (isAction) PapySurfaceMuted else PapyBlue)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = if (isAction) PapyRed else Color.White
        )
    }
}