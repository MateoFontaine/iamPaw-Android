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
| Firestore | ⬜ Pendiente | Sync remota de reportes |
| Tests (MockK) | ⬜ Pendiente | ViewModels + Repository (Hilt lo facilita) |
| Glide + Splash API | ⬜ Pendiente | Requisitos TPO |
| IA generativa | ⬜ Pendiente | Match / Report |
| Android Profiler | ⬜ Pendiente | Informe técnico |

---

## Bitácora (para no perderse)

| Fecha | Rama | Qué hicimos |
|-------|------|-------------|
| 20/06 | `feature/inyeccion-dependencias` | Hilt completo + fix AGP 9/KSP. Compila ✅. Push a origin. |
| 20/06 | `feature/room-offline-first` | Room: Entity, Dao, Database, Feed offline-first. Pendiente: probar en device. |

**Rama actual:** `feature/room-offline-first`

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
          │         Room (✅)            Firestore (⬜)
          │         SSOT local           sync remota
          │              ▲                     ▲
          │              └──────────┬──────────┘
          │                         │
          └──────── observa ────────┘
                    Flow desde Room

    Tests (⬜): FakePawRepository → ViewModel sin Android real
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

## 3. Firestore — ⬜ SIGUIENTE

**Objetivo:** reportes de usuarios persisten en la nube y sincronizan con Room.

**Flujo:** Usuario crea reporte → Room (inmediato) → Firestore (background)

---

## 4. Tests — Pendiente

**Depende de:** Hilt ✅ + Room ⬜

Patrón del demo (clase 13):
- `FakePawRepository` implementa `IPawRepository`
- `FeedViewModelTest` con `StandardTestDispatcher` + `advanceUntilIdle()`
- `LoginViewModelTest` con `mockk<FirebaseAuth>()`

---

## Orden recomendado hasta la entrega

1. ✅ Hilt
2. ✅ Room (feed offline-first)
3. ⬜ Firestore (reportes en la nube)
4. ⬜ Tests unitarios
5. ⬜ Glide + Splash API + `collectAsStateWithLifecycle`
6. ⬜ IA generativa (Gemini en Match)
7. ⬜ Informe Android Profiler

---

## Arquitectura actual (post-Room)

```
FeedViewModel  →  IPawRepository  →  PawRepository
                                         ├── IPawDao (Room) ← Feed observa Flow
                                         ├── PawMockDataSource (seed + detalle/match)
                                         └── PawApiDataSource (razas — The Dog API)
```

**Arquitectura objetivo (post-Firestore):**

```
FeedViewModel  →  IPawRepository  →  PawRepository
                                         ├── IPawDao (Room) ← UI observa esto
                                         ├── FirestoreDataSource → sync con Room
                                         └── PawApiDataSource → razas en Room
```
