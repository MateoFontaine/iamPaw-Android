package com.example.iampaw

import com.example.iampaw.components.login.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class LoginViewModelTest {

    private val auth = mockk<FirebaseAuth>(relaxed = true)

    @Test
    fun `setLoading enciende la ruedita`() {
        val vm = LoginViewModel(auth)

        vm.setLoading(true)

        assertEquals(true, vm.uiState.value.isLoading)
    }

    @Test
    fun `setLoading apaga la ruedita`() {
        val vm = LoginViewModel(auth)
        vm.setLoading(true)

        vm.setLoading(false)

        assertEquals(false, vm.uiState.value.isLoading)
    }

    @Test
    fun `al iniciar no esta cargando ni logueado`() {
        val vm = LoginViewModel(auth)

        assertFalse(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.isSuccess)
        assertNull(vm.uiState.value.errorMessage)
    }
}
