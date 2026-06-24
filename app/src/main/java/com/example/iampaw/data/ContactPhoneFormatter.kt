package com.example.iampaw.data

/** Normaliza teléfonos argentinos para enlaces de WhatsApp (solo dígitos, con 54). */
object ContactPhoneFormatter {

    fun normalizeForWhatsApp(input: String): String? {
        var digits = input.filter { it.isDigit() }
        if (digits.length < 10) return null
        if (digits.startsWith("0")) digits = digits.dropWhile { it == '0' }
        if (digits.startsWith("54")) {
            return digits.takeIf { it.length >= 12 }
        }
        digits = "549$digits"
        return digits.takeIf { it.length >= 12 }
    }

    fun formatForDisplay(input: String): String {
        val normalized = normalizeForWhatsApp(input) ?: return input.trim()
        return if (normalized.startsWith("54") && normalized.length > 2) {
            "+${normalized.substring(0, 2)} ${normalized.substring(2)}"
        } else {
            normalized
        }
    }
}
