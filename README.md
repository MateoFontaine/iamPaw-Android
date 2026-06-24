# iamPaw

Plataforma Android para búsqueda y rescate de mascotas perdidas en Pinamar. Centraliza reportes con foto, geolocalización y **matching automático con IA** (Google Gemini), con arquitectura **offline-first**.

**Materia:** Desarrollo de Aplicaciones I — UADE  
**Alumno:** Mateo Fontaine — Legajo 1170708  
**Repositorio:** [github.com/MateoFontaine/iamPaw-Android](https://github.com/MateoFontaine/iamPaw-Android)

---

## Propuesta de valor

- Reportes de mascotas **perdidas** y **encontradas** en un solo feed comunitario.
- **Match inteligente:** compara el reporte nuevo con candidatos existentes usando Gemini (análisis visual + datos del formulario).
- **Offline-first:** Room como fuente de verdad local; Firestore sincroniza cuando hay red.
- Contacto directo vía **WhatsApp** con teléfono del reportante.

---

## Casos de uso implementados

| CU | Descripción |
|----|-------------|
| **CU-01** | Splash Screen API + verificación de sesión Firebase |
| **CU-02** | Login con Google (Firebase Authentication) |
| **CU-03** | Feed principal con búsqueda y filtros locales |
| **CU-04** | Reporte con foto, GPS → texto, formulario y Match con IA |
| **CU-05** | Detalle del reporte, mapa (Intent), WhatsApp, marcar resuelto |
| **CU-06** | Perfil de usuario + teléfono de contacto |

---

## Stack tecnológico

| Área | Tecnología |
|------|------------|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | MVVM + Repository Pattern |
| DI | Hilt |
| Base local | Room (SQLite) |
| Backend | Firebase Auth + Firestore |
| Imágenes | Glide |
| Red | Retrofit + OkHttp |
| IA | Gemini REST (`generativelanguage.googleapis.com`) |
| Razas | [The Dog API](https://thedogapi.com/) |
| Tests | JUnit 4, MockK, kotlinx-coroutines-test |

---

## Arquitectura

```
┌─────────────────────────────────────────────────────────┐
│  UI (Compose) — Screen + State                          │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│  ViewModel (@HiltViewModel) — lógica de pantalla          │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│  domain/IPawRepository — contrato                         │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│  data/PawRepository — orquestación                        │
└───────┬─────────────┬─────────────┬─────────────────────┘
        ▼             ▼             ▼
     Room         Firestore    The Dog API
   (SSOT local)   (sync)       (razas)
```

### Flujo de datos (feed)

1. Al abrir el feed → `syncReportsFromFirestore()` baja reportes de Firestore → Room.
2. La UI observa `observeFeed()` → `Flow` desde Room.
3. Al publicar → Room primero → Firestore en best-effort.

### Flujo de Match (IA)

1. Usuario completa reporte con foto → navega a Match.
2. Con red → **Gemini REST** analiza la imagen + candidatos (texto) → JSON con `matchPercentage`.
3. Sin red o si Gemini falla → **MatchLocalFallback** (scoring por raza, color, zona, nombre).
4. Al publicar → se guarda `aiAnalysis` en Room y Firestore.

---

## Estructura del proyecto

```
app/src/main/java/com/example/iampaw/
├── MainActivity.kt              # Entry point + Splash API
├── IamPawApplication.kt         # @HiltAndroidApp
├── components/                  # UI por pantalla (Screen, ViewModel, State)
│   ├── feed/                    # CU-03
│   ├── login/                   # CU-02
│   ├── report/                  # CU-04 (formulario)
│   ├── match/                   # CU-04 (IA + publicar)
│   ├── detail/                  # CU-05
│   ├── profile/                 # CU-06
│   ├── splash/                  # CU-01
│   ├── commons/                 # ReportGlideImage, etc.
│   └── NavigationStack.kt
├── domain/
│   └── IPawRepository.kt        # Contrato de datos
└── data/
    ├── PawRepository.kt         # Implementación
    ├── DI/dataModules.kt        # Módulos Hilt
    ├── local/                   # Room, imágenes, teléfono local
    ├── remote/                  # Firestore (reports, users)
    ├── ai/                      # Gemini REST, parser, fallback
    ├── network/                 # Conectividad
    └── glide/                   # Configuración Glide
```

---

## Requisitos previos

- Android Studio (Ladybug o superior recomendado)
- JDK 17+ (Android Studio incluye JBR)
- Cuenta Firebase con:
  - Authentication (Google Sign-In)
  - Firestore Database
- API keys:
  - **Gemini** → [Google AI Studio](https://aistudio.google.com/apikey)
  - **The Dog API** → [thedogapi.com](https://thedogapi.com/)

---

## Configuración local

### 1. Clonar el repositorio

```bash
git clone https://github.com/MateoFontaine/iamPaw-Android.git
cd iamPaw-Android
git checkout main
```

### 2. Firebase

- Colocar `google-services.json` en `app/` (descargado desde Firebase Console).
- Publicar reglas de Firestore desde `firestore.rules` en Firebase Console.

### 3. API keys en `local.properties`

Crear o editar `local.properties` en la raíz del proyecto:

```properties
sdk.dir=/ruta/a/Android/sdk
GEMINI_API_KEY=tu_api_key_de_gemini
DOG_API_KEY=tu_api_key_de_thedogapi
```

> `local.properties` no se commitea (está en `.gitignore`).

### 4. Sync y build

```bash
./gradlew :app:assembleDebug
```

Abrir en Android Studio → **Sync Project with Gradle Files** → Run en dispositivo o emulador (minSdk 28).

---

## Tests unitarios

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"  # macOS
./gradlew testDebugUnitTest
```

Reporte HTML: `app/build/reports/tests/testDebugUnitTest/index.html`

| Clase de test | Qué cubre |
|---------------|-----------|
| `FeedViewModelTest` | Carga del feed, error de sync |
| `LoginViewModelTest` | Estado de login |
| `GeminiResponseParserTest` | Parseo JSON de Gemini |
| `GeminiErrorMapperTest` | Errores de API |
| `MatchLocalFallbackTest` | Scoring offline |
| `MatchViewModelMappingTest` | Mapper de candidatos |
| `ContactPhoneFormatterTest` | Formato teléfono AR |

Se usa `FakePawRepository` para testear ViewModels sin Firebase real.

---

## Firebase — colecciones

| Colección | Uso |
|-----------|-----|
| `reports` | Reportes de mascotas (espejo de Room) |
| `users` | Teléfono de contacto (`contactPhone`) por UID |

---

## Limitaciones conocidas (MVP)

| Tema | Estado actual |
|------|----------------|
| **Firebase Storage** | Fotos en almacenamiento local (`filesDir`). Mejora futura para multi-dispositivo. |
| **Sync offline al publicar** | Room guarda siempre; subida a Firestore es best-effort (sin cola de reintentos). |
| **Gemini en formulario** | La IA corre en pantalla Match, no al precargar el formulario. |
| **GPS** | Coordenadas se convierten a texto; no se persisten lat/long en Room. |

---

## Git flow

```
feature/*  →  develop  →  main
```

- **`main`:** versión de entrega (esta rama).
- **`develop`:** integración de features.
- **`PROGRESO.md`:** bitácora interna de desarrollo.

---

## Mejoras futuras

- [ ] Firebase Storage para fotos compartidas entre dispositivos
- [ ] Cola de uploads pendientes (WorkManager) al recuperar conectividad
- [ ] Reglas Firestore con ownership estricto (`userId`)
- [ ] Paginación en sync de Firestore

---

## Licencia

Proyecto académico — TPO Desarrollo de Aplicaciones I, UADE 2026.
