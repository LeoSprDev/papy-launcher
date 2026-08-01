package com.papy.launcher

import androidx.compose.ui.graphics.Color

enum class ShortcutId {
    APPELS, SMS, WHATSAPP, MAIL, PHOTOS, APPLIS, APPAREIL_PHOTO, PROUT, FAVORIS
}

data class Shortcut(
    val id: ShortcutId,
    val labelRes: Int,
    val color: Color,
    val defaultEnabled: Boolean = true
)

object Shortcuts {
    val all = listOf(
        Shortcut(ShortcutId.APPELS, R.string.btn_appels, Color(0xFF2E7D32)),
        Shortcut(ShortcutId.SMS, R.string.btn_sms, Color(0xFF1565C0)),
        Shortcut(ShortcutId.WHATSAPP, R.string.btn_whatsapp, Color(0xFF075E54)),
        Shortcut(ShortcutId.MAIL, R.string.btn_mail, Color(0xFFEF6C00)),
        Shortcut(ShortcutId.PHOTOS, R.string.btn_photos, Color(0xFF6A1B9A)),
        Shortcut(ShortcutId.APPLIS, R.string.btn_applis, Color(0xFF455A64)),
        Shortcut(ShortcutId.APPAREIL_PHOTO, R.string.btn_appareil_photo, Color(0xFF00695C)),
        Shortcut(ShortcutId.PROUT, R.string.btn_prout, Color(0xFF8E63BC)),
        Shortcut(ShortcutId.FAVORIS, R.string.btn_favoris, Color(0xFF3949AB))
    )

    fun byId(id: ShortcutId): Shortcut =
        all.first { it.id == id }
}