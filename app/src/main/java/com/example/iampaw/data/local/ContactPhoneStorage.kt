package com.example.iampaw.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactPhoneStorage @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(userId: String): String {
        if (userId.isBlank()) return ""
        return prefs.getString(keyFor(userId), "").orEmpty()
    }

    fun save(userId: String, phone: String) {
        if (userId.isBlank()) return
        prefs.edit().putString(keyFor(userId), phone).apply()
    }

    private fun keyFor(userId: String) = "contact_phone_$userId"

    companion object {
        private const val PREFS_NAME = "iampaw_contact_phone"
    }
}
