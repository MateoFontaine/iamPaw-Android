# iamPaw — Progreso TPO

Resumen vivo de lo que vamos implementando para la entrega.  
Referencia del profe: [2026DA1 — feature/inyeccion-dependencias](https://github.com/ngladkoff/2026DA1/tree/feature/inyeccion-dependencias)

---

## Estado general

| Bloque | Estado | Notas |
|--------|--------|-------|
| Hilt (DI) | ✅ Hecho | Pusheado en `feature/inyeccion-dependencias` |
| Capa `domain/` | ✅ Hecho | Interfaz `IPawRepository` |
| Room (offline-first) | ✅ Hecho | Feed observa Room con Flow |
| Firestore | ✅ Hecho | Sync remota de reportes con Room |
| Room sync deletes | ✅ Hecho | Borra local si ya no está en Firestore |
| Tests (MockK) | ✅ Hecho | FeedViewModel + LoginViewModel (5 tests JVM) |
| collectAsStateWithLifecycle | ✅ Hecho | 6 pantallas Compose (PR `feature/lifecycle-state`) |
| Glide + Splash API | ⬜ Pendiente | Requisitos TPO |
| IA generativa | ✅ Hecho | Gemini REST en Match (foto + candidatos), `aiAnalysis` al publicar |
| Android Profiler | ⬜ Pendiente | Informe técnico |

---

## Bitácora (para no perderse)

| Fecha | Rama | Qué hicimos |
|-------|------|-------------|
| 20/06 | `feature/inyeccion-dependencias` | Hilt completo + fix AGP 9/KSP. Compila ✅. Push a origin. |
| 20/06 | `feature/room-offline-first` | Room: Entity, Dao, Database, Feed offline-first. Pendiente: probar en device. |
| 23/06 | `feature/firestore-sync` | Firestore: saveReport + syncReportsFromFirestore. ReportScreen publica al Feed. |
| 23/06 | `feature/tests-unitarios` | Tests unitarios: FakePawRepository, FeedViewModelTest, LoginViewModelTest. |
| 23/06 | `feature/lifecycle-state` | `collectAsStateWithLifecycle` en Feed, Login, Detail, Match, Profile, Report. |
| 23/06 | `feature/room-sync-deletes` | Sync borra en Room reportes eliminados de Firestore (mock seed intacto). |
| 24/06 | `feature/gemini-match` | Fases 0–2: datos reales, Gemini REST (multimodal), Match con % real, fallback offline. |

**Rama actual:** `feature/gemini-match`

**PR Hilt pendiente:** [Abrir PR → develop](https://github.com/MateoFontaine/iamPaw-Android/pull/new/feature/inyeccion-dependencias)

> Room se branchó desde `feature/inyeccion-dependencias` porque Hilt aún no está mergeado en `develop`. Cuando mergees el PR de Hilt, al mergear Room a `develop` todo queda en orden.

---

## Mapa mental — cómo encaja todo

```
                    ┌─────────────────────────────────┐
                    │           HILT (✅)              │
                    │  Conecta todas las piezas       │
                    └─────────────────────────────────┘
                                    │
          ┌─────────────────────────┼─────────────────────────┐
          ▼                         ▼                         ▼
    ViewModels               PawRepository              FirebaseAuth
    (piden interfaz)         (implementación)           (login/perfil)
          │                         │
          │              ┌──────────┴──────────┐
          │              ▼                     ▼
          │         Room (✅)            Firestore (✅)
          │         SSOT local           sync remota
          │              ▲                     ▲
          │              └──────────┬──────────┘
          │                         │
          └──────── observa ────────┘
                    Flow desde Room

    Tests (✅): FakePawRepository → ViewModel sin Android real
```

### ¿Para qué nos sirve Hilt? (resumen)

| Sin Hilt | Con Hilt |
|----------|----------|
| `PawRepository()` hardcodeado en cada ViewModel | ViewModel recibe `IPawRepository` inyectado |
| Cambiar mock → Room → Firestore = tocar todos los VMs | Cambiás solo `dataModules.kt` |
| Tests difíciles (Firebase/Retrofit reales) | Tests con `FakePawRepository` + MockK |

**Hilt no reemplaza Room ni Firebase** — los conecta para que el ViewModel no se entere de cuál usa.

---

## 1. Hilt — Inyección de Dependencias ✅

**Commit:** `1c69f41` — `feat: implementar inyeccion de dependencias con Hilt`  
**Rama:** `feature/inyeccion-dependencias` (pusheada)

**Concepto clave:**
- El ViewModel pide `IPawRepository` (interfaz), no sabe si hay mock, Room o Firestore atrás.
- Hilt conecta las piezas en `data/DI/dataModules.kt`.

### Checklist

- [x] Dependencias Gradle (Hilt + KSP + hilt-navigation-compose)
- [x] `IamPawApplication` con `@HiltAndroidApp`
- [x] `AndroidManifest.xml` registra la Application
- [x] `domain/IPawRepository.kt`
- [x] `data/DI/dataModules.kt` (Retrofit, API, FirebaseAuth, Repository)
- [x] `PawRepository` implementa `IPawRepository` + `@Inject`
- [x] `PawApiDataSource` recibe `IDogAPI` por constructor
- [x] ViewModels con `@HiltViewModel` + `@Inject constructor`
- [x] Screens usan `hiltViewModel()` en lugar de `viewModel()`
- [x] `MainActivity` con `@AndroidEntryPoint`
- [x] Fix `gradle.properties` → `android.disallowKotlinSourceSets=false`
- [x] Compila y corre en Android Studio ✅

### Archivos principales

```
app/.../IamPawApplication.kt       → arranca Hilt
app/.../domain/IPawRepository.kt → contrato del repo
app/.../data/DI/dataModules.kt   → "depósito" de dependencias
app/.../data/PawRepository.kt    → implementación actual (mock + API)
```

---

## 2. Room — Offline-first ✅

**Objetivo:** Room como **única fuente de verdad** para el Feed (offline-first).

### Checklist

- [x] `PetReportLocal.kt` — `@Entity`
- [x] `IPawDao.kt` — `observeAll()`, `search()`, `insertAll()`
- [x] `PawDatabase.kt` — `@Database` + singleton
- [x] `ModelMapping.kt` — `PetReportLocal` ↔ `DogPost`
- [x] Hilt provee `PawDatabase` + `IPawDao` en `dataModules.kt`
- [x] `PawRepository` observa Room + seed inicial desde mock si DB vacía
- [x] `FeedViewModel` observa `Flow` + Loading/Success/Error
- [x] `FeedScreen` — loading, error, búsqueda reactiva
- [ ] Probar offline en dispositivo

### Archivos principales

```
data/local/PetReportLocal.kt   → tabla SQLite
data/local/IPawDao.kt          → queries + Flow
data/local/PawDatabase.kt      → instancia Room
data/local/ModelMapping.kt     → conversión Local ↔ UI
data/PawRepository.kt          → observeFeed() + refreshFeedIfEmpty()
components/feed/FeedViewModel.kt → collect Flow
```

### Cómo testear vos

1. **Sync Gradle** → Run app → Feed muestra Rocco, Luna, Milo
2. **Búsqueda:** escribí "Rocco" o "Golden" → filtra la lista
3. **Offline:** cerrá app → activá modo avión → abrí app → feed sigue con datos
4. **Persistencia:** desinstalá y reinstalá → seed vuelve a cargar mock en primera apertura

### Flujo de datos (Feed)

```
FeedScreen → FeedViewModel → IPawRepository → IPawDao → Room (SQLite)
                                    ↑
                         refreshFeedIfEmpty() si DB vacía
                         (seed desde PawMockDataSource, una sola vez)
```

---

## 3. Firestore — ✅ Hecho

**Objetivo:** reportes de usuarios persisten en la nube y sincronizan con Room.

**Flujo:** Usuario crea reporte → Room (inmediato) → Firestore (background). Al abrir Feed → sync Firestore → Room.

### Checklist

- [x] Dependencia `firebase-firestore` + `kotlinx-coroutines-play-services`
- [x] `provideFirestore()` en `dataModules.kt`
- [x] `PetReportLocal` con `userId` + `createdAt` (DB v2, destructive migration)
- [x] `FirestoreReportDataSource.kt` — colección `reports`
- [x] `IPawRepository.saveReport()` + `syncReportsFromFirestore()`
- [x] `PawRepository` — Room primero, Firestore en background (errores no crashean)
- [x] `ReportViewModel.submitReport()` + botón en `ReportScreen`
- [x] `FeedViewModel` llama sync al iniciar
- [ ] Probar sync entre dos dispositivos con misma cuenta

### Archivos principales

```
data/remote/FirestoreReportDataSource.kt  → CRUD colección reports
data/PawRepository.kt                     → saveReport + sync
domain/IPawRepository.kt                  → contrato extendido
components/report/ReportViewModel.kt      → submitReport()
components/feed/FeedViewModel.kt          → sync al abrir feed
```

### Cómo testear vos

1. **Sync Gradle** → Run app → login con Google
2. **Crear reporte:** ReportScreen → completar raza + ubicación → Publicar → aparece en Feed
3. **Firestore Console:** ver documento en colección `reports`
4. **Segundo dispositivo/emulador:** misma cuenta → abrir Feed → sync trae reportes
5. **Offline:** modo avión → Feed sigue mostrando Room → al volver internet, sync actualiza

### Flujo de datos (post-Firestore)

```
ReportScreen → ReportViewModel → saveReport()
                                    ├── IPawDao.insert (inmediato)
                                    └── FirestoreReportDataSource.save (background)

FeedViewModel → syncReportsFromFirestore()
                    └── Firestore → IPawDao.insertAll → UI observa Flow
```

---

## 4. Tests unitarios — ✅ Hecho

**Rama:** `feature/tests-unitarios` (desde `feature/firestore-sync`)  
**Depende de:** Hilt ✅ + Room ✅ + Firestore ✅

### Checklist

- [x] Dependencias Gradle: MockK + kotlinx-coroutines-test
- [x] `FakePawRepository` — implementa `IPawRepository` con datos inventados
- [x] `FeedViewModelTest` — carga posts + error si sync falla
- [x] `LoginViewModelTest` — `setLoading` + estado inicial
- [x] Todos los tests pasan en JVM (sin emulador)

### Archivos principales

```
app/src/test/java/com/example/iampaw/
├── FakePawRepository.kt    → repo fake para tests
├── FeedViewModelTest.kt      → 2 tests (feed + sync error)
└── LoginViewModelTest.kt     → 3 tests (loading + estado inicial)
```

### Cómo correr

1. Android Studio → abrir cualquier `*Test.kt` → ▶️ junto a la clase
2. Terminal: `./gradlew testDebugUnitTest`
3. Reporte HTML: `app/build/reports/tests/testDebugUnitTest/index.html`

### Qué valida cada test

| Test | Qué prueba |
|------|------------|
| `al iniciar carga posts del repositorio` | FeedViewModel carga posts del fake y apaga loading |
| `si sync falla muestra error` | Si sync lanza excepción, `errorMessage` se setea |
| `setLoading enciende la ruedita` | LoginViewModel pone `isLoading = true` |
| `setLoading apaga la ruedita` | LoginViewModel pone `isLoading = false` |
| `al iniciar no esta cargando ni logueado` | Estado inicial del login es correcto |

> `signInWithFirebase` no se testea en JVM (callbacks Firebase + APIs Android). Se prueba manual en emulador.

---

## 5. collectAsStateWithLifecycle — ✅ Hecho

**Rama:** `feature/lifecycle-state` (desde `develop`)

### Checklist

- [x] Dependencia `lifecycle-runtime-compose`
- [x] `FeedScreen`, `LoginScreen`, `DetailScreen`, `MatchScreen`, `ProfileScreen`, `ReportScreen`
- [x] `BreedAutocompleteList` en Report (lista de razas)

### Qué cambia

`collectAsState()` → `collectAsStateWithLifecycle()`: la UI deja de observar el ViewModel cuando la pantalla no está visible (ahorro de recursos en background).

### Cómo correr / verificar

Sync Gradle → Run app → navegar pantallas; comportamiento visual igual al anterior.

---

## 6. Room sync deletes — ✅ Hecho

**Rama:** `feature/room-sync-deletes` (desde `develop`)

### Problema

Borrar documentos en Firebase Console no los sacaba del feed: `syncReportsFromFirestore()` solo hacía `insertAll`, nunca `delete`.

### Solución

Al sincronizar:
1. Traer todos los reportes de Firestore
2. Insertar/actualizar en Room (`REPLACE`)
3. Borrar de Room los que tienen `userId` (reportes de usuario) y ya no están en Firestore
4. **No tocar** mock seed (Rocco, Luna, Milo → `userId` vacío)

### Archivos

```
data/local/IPawDao.kt           → getSyncedReportIds(), deleteByIds()
data/PawRepository.kt           → sync con purge
data/local/ReportImageStorage.kt → deleteReportImage() al borrar
```

### Cómo testear

1. Crear reporte → aparece en Feed y Firebase
2. Borrar documento en Firebase Console
3. Cerrar y abrir Feed (o reabrir app) → desaparece del celular
4. Mock (Rocco, Luna, Milo) siguen si no están en Firebase

---

## Orden recomendado hasta la entrega

1. ✅ Hilt
2. ✅ Room (feed offline-first)
3. ✅ Firestore (reportes en la nube)
4. ✅ Tests unitarios
5. ✅ collectAsStateWithLifecycle
6. ✅ Room sync deletes (borrar local si no está en Firebase)
7. ✅ IA generativa (Gemini en Match — REST, modelos 2.5/3.x)
8. ⬜ Glide + Splash API
9. ⬜ Informe Android Profiler

---

## Arquitectura actual (post-Firestore)

```
FeedViewModel  →  IPawRepository  →  PawRepository
                                         ├── IPawDao (Room) ← UI observa Flow
                                         ├── FirestoreReportDataSource → sync
                                         ├── GeminiMatchAnalyzer (REST) → Match
                                         └── PawApiDataSource (razas — The Dog API)
```
