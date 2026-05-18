package com.intentos.app.core.utils

import android.content.Context
import android.provider.ContactsContract

object ContactResolver {

    fun findPhoneNumber(context: Context, spokenName: String): String? {
        val target = spokenName.lowercase().trim()

        val contacts = mutableListOf<Pair<String, String>>()

        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(0)?.lowercase()?.trim() ?: continue
                val number = it.getString(1)?.replace(Regex("[^0-9]"), "") ?: continue
                contacts.add(name to number)
            }
        }

        // EXACT match first
        contacts.firstOrNull { it.first == target }?.let {
            return it.second
        }

        // startsWith fallback
        contacts.firstOrNull { it.first.startsWith(target) }?.let {
            return it.second
        }

        return null
    }
}