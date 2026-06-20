# iamPaw — Progreso TPO

Resumen vivo de lo que vamos implementando para la entrega.  
Referencia del profe: [2026DA1 — feature/inyeccion-dependencias](https://github.com/ngladkoff/2026DA1/tree/feature/inyeccion-dependencias)

---

## Estado general

| Bloque | Estado | Notas |
|--------|--------|-------|
| Hilt (DI) | ✅ Hecho | Base para testear y conectar Room/Firebase |
| Capa `domain/` | ✅ Hecho | Interfaz `IPawRepository` |
| Room (offline-first) | ⬜ Pendiente | SSOT para la UI |
| Firestore | ⬜ Pendiente | Sync remota de reportes |
| Tests (MockK) | ⬜ Pendiente | ViewModels + Repository |
| Glide + Splash API | ⬜ Pendiente | Requisitos TPO |
| IA generativa | ⬜ Pendiente | Match / Report |
| Android Profiler | ⬜ Pendiente | Informe técnico |

---

## 1. Hilt — Inyección de Dependencias

**Objetivo:** desacoplar ViewModels del `PawRepository()` manual y preparar tests.

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

### Archivos tocados

```
build.gradle.kts                          → plugin Hilt
gradle/libs.versions.toml                 → versiones Hilt/KSP
app/build.gradle.kts                      → deps + plugins
app/src/main/AndroidManifest.xml          → android:name=".IamPawApplication"
app/.../IamPawApplication.kt              → NUEVO
app/.../domain/IPawRepository.kt          → NUEVO
app/.../data/DI/dataModules.kt            → NUEVO
app/.../data/PawRepository.kt             → refactor
app/.../data/PawApiDataSource.kt          → refactor
app/.../data/PawMockDataSource.kt         → @Inject constructor
app/.../components/**/**ViewModel.kt       → @HiltViewModel
app/.../components/**/**Screen.kt         → hiltViewModel()
app/.../MainActivity.kt                   → @AndroidEntryPoint
```

### Cómo verificar en Android Studio

1. Sync Gradle (elefante 🐘)
2. Build → Make Project
3. Correr la app: login, feed, reporte (razas API) deben funcionar igual que antes

> **Fix AGP 9 + KSP:** si aparece error de `kotlin.sourceSets`, en `gradle.properties` debe estar  
> `android.disallowKotlinSourceSets=false` (igual que el repo del profe).

---

## 2. Room — Pendiente

*(Se completa en el próximo bloque)*

---

## 3. Firestore — Pendiente

*(Se completa después de Room)*

---

## 4. Tests — Pendiente

Patrón del demo:
- `FakePawRepository` implementa `IPawRepository`
- `FeedViewModelTest` con `StandardTestDispatcher` + MockK
- `LoginViewModelTest` con `mockk<FirebaseAuth>()`

---

## Notas de arquitectura

```
ViewModel  →  IPawRepository  →  PawRepository
                                      ├── PawMockDataSource (feed, detalle, match)
                                      └── PawApiDataSource   (razas — The Dog API)
```

Próximo paso: Room entra como SSOT; Retrofit y Firestore escriben en Room, la UI observa Room.
