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
    val resolved = try {
        context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
            ),
            "${ContactsContract.Contacts.LOOKUP_KEY} = ?",
            arrayOf(lookupKey),
            null
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