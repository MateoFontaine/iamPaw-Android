package com.example.iampaw

import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.feed.FeedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `al iniciar carga posts del repositorio`() = runTest {
        val fakePosts = listOf(
            DogPost("1", "Rocco", "Golden Retriever", "Palermo", "Hace 2h", "", "Perdido"),
            DogPost("2", "Luna", "Border Collie", "Córdoba", "Hace 5h", "", "Encontrado")
        )
        val vm = FeedViewModel(FakePawRepository(fakePosts))

        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.posts.size)
        assertEquals("Rocco", vm.uiState.value.posts[0].name)
        assertEquals("Luna", vm.uiState.value.posts[1].name)
        assertEquals(false, vm.uiState.value.isLoading)
    }

    @Test
    fun `si sync falla muestra error`() = runTest {
        val vm = FeedViewModel(FakePawRepository(shouldFailSync = true))

        advanceUntilIdle()

        assertEquals(false, vm.uiState.value.isLoading)
        assertNotNull(vm.uiState.value.errorMessage)
        assertEquals("Error simulado", vm.uiState.value.errorMessage)
    }
}
