# iamPaw — Progreso TPO

Resumen vivo de lo que vamos implementando para la entrega.  
Referencia del profe: [2026DA1 — feature/inyeccion-dependencias](https://github.com/ngladkoff/2026DA1/tree/feature/inyeccion-dependencias)

---

## Estado general

| Bloque | Estado | Notas |
|--------|--------|-------|
| Hilt (DI) | ✅ Hecho | Pusheado en `feature/inyeccion-dependencias` |
| Capa `domain/` | ✅ Hecho | Interfaz `IPawRepository` |
| Room (offline-first) | ⬜ **Siguiente** | SSOT para la UI |
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
| 20/06 | `feature/room-offline-first` | Rama creada (base: `feature/inyeccion-dependencias`). Pendiente: implementar Room. |

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
          │         Room (⬜)            Firestore (⬜)
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

## 2. Room — ⬜ SIGUIENTE

**Objetivo:** Room como **única fuente de verdad** para la UI (offline-first).

**Qué vamos a crear:**
- `data/local/PetReportLocal.kt` — `@Entity` (tabla)
- `data/local/IPawDao.kt` — `@Dao` (queries + `Flow<List<>>`)
- `data/local/PawDatabase.kt` — `@Database`
- `data/local/ModelMapping.kt` — `toLocal()` / `toExternal()`

**Flujo objetivo:**
1. UI observa `Flow` desde Room
2. Retrofit trae razas → guarda en Room
3. Feed lee reportes desde Room (ya no solo mock)
4. Estados: Loading / Success / Error

**Rama sugerida:** `feature/room-offline-first` ✅ creada

---

## 3. Firestore — Pendiente

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
2. ⬜ Room (feed offline-first)
3. ⬜ Firestore (reportes en la nube)
4. ⬜ Tests unitarios
5. ⬜ Glide + Splash API + `collectAsStateWithLifecycle`
6. ⬜ IA generativa (Gemini en Match)
7. ⬜ Informe Android Profiler

---

## Arquitectura actual (post-Hilt)

```
ViewModel  →  IPawRepository  →  PawRepository
                                      ├── PawMockDataSource (feed, detalle, match)
                                      └── PawApiDataSource   (razas — The Dog API)
```

**Arquitectura objetivo (post-Room + Firestore):**

```
ViewModel  →  IPawRepository  →  PawRepository
                                      ├── PawDao (Room) ← UI observa esto
                                      ├── PawApiDataSource → escribe en Room
                                      └── FirestoreDataSource → sync con Room
```
