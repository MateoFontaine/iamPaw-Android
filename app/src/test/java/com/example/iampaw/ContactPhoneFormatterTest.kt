package com.example.iampaw

import com.example.iampaw.data.ContactPhoneFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContactPhoneFormatterTest {

    @Test
    fun `normaliza celular argentino con 11`() {
        assertEquals("5491122541234", ContactPhoneFormatter.normalizeForWhatsApp("11 2254 1234"))
    }

    @Test
    fun `acepta numero ya con 54`() {
        assertEquals("5491122541234", ContactPhoneFormatter.normalizeForWhatsApp("5491122541234"))
    }

    @Test
    fun `rechaza numero muy corto`() {
        assertNull(ContactPhoneFormatter.normalizeForWhatsApp("12345"))
    }
}
