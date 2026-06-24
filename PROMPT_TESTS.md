# Prompt para agente — Tests unitarios en iamPaw

Copiá todo el bloque de abajo y pegalo en un agente nuevo.

---

## CONTEXTO DEL PROYECTO

**iamPaw** — app Android Kotlin + Jetpack Compose para mascotas perdidas. TPO Desarrollo de Aplicaciones I.

**Repo:** `https://github.com/MateoFontaine/iamPaw-Android.git`  
**Rama base:** `feature/firestore-sync` → crear `feature/tests-unitarios`  
**Referencia del profe:** `https://github.com/ngladkoff/2026DA1` → carpeta `AnimeApp - Room`, tests en `app/src/test/`

### Ya implementado ✅
- Hilt (DI), MVVM, Repository Pattern, capa `domain/`
- Room offline-first (Feed observa `Flow` desde `IPawDao`)
- Firestore sync (`FirestoreReportDataSource`, `saveReport`, `syncReportsFromFirestore`)
- Firebase Auth + Google Sign-In
- Feed con búsqueda local, Report → Match → publicar
- Room **2.8.4**, KSP, `android.disallowKotlinSourceSets=false`

### Arquitectura actual
```
ViewModel → IPawRepository → PawRepository
                                ├── IPawDao (Room)
                                ├── FirestoreReportDataSource
                                ├── PawMockDataSource
                                └── PawApiDataSource
```

---

## TAREA: Implementar tests unitarios (MockK)

### Requisito del TPO (obligatorio)
- Tests unitarios de **ViewModels** (estados UI, lógica)
- Tests de **Repository** con mocking (FakeRepository o MockK)
- Usar **MockK** y/o **kotlinx-coroutines-test** (como el demo del profe)

### NO es esto
- ❌ Tests de UI (Espresso / Compose UI tests) — no son el foco
- ❌ Simular toques en pantalla — eso es manual o UI test
- ❌ Usar Firebase/Room real en tests unitarios

### SÍ es esto
- ✅ Probar ViewModel con `FakePawRepository` (datos inventados)
- ✅ Probar `LoginViewModel` con `mockk<FirebaseAuth>()`
- ✅ Correr en JVM desde Android Studio (▶️ verde, sin emulador)

---

## INTERFAZ A IMPLEMENTAR EN FAKE

`domain/IPawRepository.kt` (actual):

```kotlin
interface IPawRepository {
    fun observeFeed(): Flow<List<DogPost>>
    suspend fun refreshFeedIfEmpty()
    suspend fun saveReport(report: DogPost): Result<Unit>
    suspend fun syncReportsFromFirestore()
    fun getDogDetail(id: String): DetailState
    fun getMatchedDogs(): List<MatchedDog>
    suspend fun getBreeds(): List<DogBreed>
}
```

---

## ARCHIVOS A CREAR

```
app/src/test/java/com/example/iampaw/
├── FakePawRepository.kt       # implementa IPawRepository con datos fake
├── FeedViewModelTest.kt       # carga posts, loading, error
└── LoginViewModelTest.kt      # setLoading, login (mock FirebaseAuth)
```

Opcional (bonus TPO):
- `PawRepositoryTest.kt` con mocks de Dao + FirestoreDataSource

---

## DEPENDENCIAS GRADLE (agregar si faltan)

En `gradle/libs.versions.toml`:
```toml
mockk = "1.13.12"
coroutinesTest = "1.9.0"
```

En `app/build.gradle.kts` → testImplementation:
```kotlin
testImplementation(libs.mockk)
testImplementation(libs.kotlinx.coroutines.test)
```

Referencia: demo del profe en `AnimeApp - Room/app/build.gradle.kts`

---

## PATRÓN DEL PROFESOR (adaptar a iamPaw)

### FakePawRepository (basado en FakeAnimeRepository)

```kotlin
class FakePawRepository(
    private val posts: List<DogPost> = emptyList(),
    private val shouldFailSync: Boolean = false
) : IPawRepository {

    override fun observeFeed(): Flow<List<DogPost>> = flowOf(posts)

    override suspend fun refreshFeedIfEmpty() { /* no-op */ }

    override suspend fun syncReportsFromFirestore() {
        if (shouldFailSync) throw IOException("Error simulado")
    }

    override suspend fun saveReport(report: DogPost) = Result.success(Unit)

    override fun getDogDetail(id: String) = DetailState()

    override fun getMatchedDogs() = emptyList<MatchedDog>()

    override suspend fun getBreeds() = emptyList<DogBreed>()
}
```

### FeedViewModelTest

`FeedViewModel` actual (`components/feed/FeedViewModel.kt`):
- En `init` llama `observeFeed()` → `refreshFeedIfEmpty()` + `syncReportsFromFirestore()` + `collect` de `observeFeed()`
- Estado: `FeedState(posts, isLoading, errorMessage)`

Tests mínimos:
1. `al iniciar carga posts del repositorio` → 2 posts fake, `isLoading = false`
2. `si sync falla muestra error` → `shouldFailSync = true`, verificar `errorMessage`

Setup obligatorio:
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `al iniciar carga posts del repositorio`() = runTest {
        val fakePosts = listOf(
            DogPost("1", "Rocco", "Golden Retriever", "Palermo", "Hace 2h", "", "Perdido"),
            DogPost("2", "Luna", "Border Collie", "Córdoba", "Hace 5h", "", "Encontrado")
        )
        val vm = FeedViewModel(FakePawRepository(fakePosts))
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.posts.size)
        assertEquals(false, vm.uiState.value.isLoading)
    }
}
```

**Importante:** instanciar `FeedViewModel(repo)` directamente en test — NO usar Hilt en unit tests.

### LoginViewModelTest

`LoginViewModel` actual usa `uiState` (NO `uiEvent` como el demo del profe):
```kotlin
data class LoginState(isLoading, isSuccess, errorMessage)
fun setLoading(isLoading: Boolean)
fun signInWithFirebase(idToken: String)
```

Tests mínimos:
1. `setLoading true actualiza uiState` → simple, sin mock
2. `setLoading false actualiza uiState`
3. Opcional: mock `FirebaseAuth` + `Task` para `signInWithFirebase` (más complejo; MockK relaxed puede alcanzar)

Ejemplo simple:
```kotlin
@Test
fun `setLoading enciende la ruedita`() {
    val auth = mockk<FirebaseAuth>(relaxed = true)
    val vm = LoginViewModel(auth)
    vm.setLoading(true)
    assertEquals(true, vm.uiState.value.isLoading)
}
```

---

## REGLAS

- Rama: `feature/tests-unitarios` desde `feature/firestore-sync`
- Commits: `test: ...` o `feat: ...` según estilo del repo
- **NO push** sin aprobación del usuario
- Explicar cada archivo al usuario (él defiende el TPO)
- Actualizar `PROGRESO.md` sección Tests
- No refactorizar código de producción salvo que impida testear (ej. hacer constructor testeable)
- Minimizar scope

---

## CÓMO VERIFICAR

1. Sync Gradle
2. Android Studio → abrir `FeedViewModelTest.kt` → Run tests (▶️)
3. Todos verdes ✅ sin emulador
4. Terminal: `./gradlew testDebugUnitTest` (si hay Java en PATH)

---

## DESPUÉS DE TESTS (no implementar ahora)

Queda del TPO:
- Glide + placeholders (reemplazar Coil en feed/detalle)
- Splash Screen API oficial
- `collectAsStateWithLifecycle` en pantallas
- IA generativa (Gemini en Match)
- Informe Android Profiler
- Merge PRs a `develop`

---

## AL TERMINAR, REPORTAR

1. Qué tests se crearon y qué valida cada uno
2. Cómo correrlos en Android Studio
3. Rama y commits
4. Si algún test requirió ajuste mínimo en ViewModel, explicar por qué
