# Prompt para agente — Gemini Match + datos reales en iamPaw

Copiá **solo la fase que corres** en un agente nuevo. No implementar todas las fases de una vez.

---

## CÓMO USAR ESTE DOCUMENTO

1. `git checkout develop && git pull origin develop`
2. Crear rama **una sola vez:** `feature/gemini-match`
3. En cada chat nuevo, pegar el bloque de **UNA fase** + esta sección de contexto
4. Al terminar cada fase: commit, el usuario prueba, después sigue la siguiente fase
5. Al terminar Fase 2: PR → `develop`, actualizar `PROGRESO.md`

**Ejemplo de mensaje al agente:**
> Implementá solo la **Fase 0** de `PROMPT_GEMINI_MATCH.md`. No avances a Fase 1.

---

## CONTEXTO DEL PROYECTO

**iamPaw** — app Android Kotlin + Jetpack Compose para mascotas perdidas. TPO DA1.

**Repo:** `https://github.com/MateoFontaine/iamPaw-Android.git`  
**Rama base:** `develop` → crear `feature/gemini-match`

### Ya implementado ✅
- Hilt, MVVM, Repository, `domain/IPawRepository`
- Room offline-first + Firestore sync + **room sync deletes**
- Tests unitarios (MockK): Feed + Login
- `collectAsStateWithLifecycle` en 6 pantallas
- Filtros Feed (búsqueda + Perdidos/Encontrados)
- Flujo Report → Match → publicar (con `ReportDraftStore`)

### Arquitectura actual
```
FeedScreen → FeedViewModel → IPawRepository → PawRepository
                                                  ├── IPawDao (Room) ← UI observa Flow
                                                  ├── FirestoreReportDataSource
                                                  ├── PawMockDataSource ← HARDCODE (a remover)
                                                  └── PawApiDataSource (razas)
```

### Lo que está hardcodeado HOY (problema)
| Lugar | Qué hace |
|-------|----------|
| `refreshFeedIfEmpty()` | Siembra Rocco, Luna, Milo desde mock |
| `getMatchedDogs()` | Devuelve Bobby fake |
| `MatchViewModel` | `delay(3500)` + mock, **sin IA** |
| `getDogDetail(id)` | Ignora `id`, devuelve Bobby del mock |
| `DetailViewModel` | Siempre carga `getDogDetail("1")` |
| Feed/Match → Detail | Navega sin pasar `postId` |

---

## DECISIONES YA TOMADAS (no cambiar sin consultar)

| Tema | Decisión |
|------|----------|
| Ramas | **Una sola:** `feature/gemini-match` (Fase 0 + 1 + 2) |
| Feed inicial | **Vacío** — el usuario carga 3 perros reales manualmente |
| Mock seed | **Quitar** del flujo principal (no más Rocco/Luna/Milo automáticos) |
| Room/Firestore | **Sí** agregar `color`, `size`, `details`, `aiAnalysis` |
| Gemini v1 | **Con foto** (multimodal) + texto del formulario |
| Usuario vs IA | **Usuario = fuente de verdad** en DB; `aiAnalysis` = texto aparte de Gemini |
| Match % | Mostrar en UI; **no** persistir ranking en DB |
| Fallback | Si Gemini falla → mensaje de error; **no** volver al mock de Bobby |

### Modelo de datos: usuario vs IA

```
DATOS DEL USUARIO (guardar en Room + Firestore):
  name, breed, color, size, details, location, imageUrl, status

ANÁLISIS IA (guardar al publicar, campo aparte):
  aiAnalysis  ← lo que Gemini describió en Match (no pisa breed/color del usuario)

MATCH % y ranking:
  Solo en MatchScreen (transitorio)
```

Si usuario dice "Golden" e IA ve "posible mestizo", **mostrar ambos** — nunca sobrescribir la raza del usuario con la de la IA.

---

## FASE 0 — Datos reales (sin Gemini todavía)

**Objetivo:** Feed y Detail usan Room/Firestore real. Sin mock seed. Campos extra persistidos.

### Checklist

- [ ] Rama `feature/gemini-match` desde `develop`
- [ ] `PetReportLocal`: agregar `color`, `size`, `details`, `aiAnalysis` (default `""`)
- [ ] `PawDatabase` version **3** (destructive migration ya existe)
- [ ] `ModelMapping.kt` / `DogPost` o DTO: propagar campos nuevos si hace falta
- [ ] `FirestoreReportDataSource`: leer/escribir campos nuevos
- [ ] `saveReport` / `ReportViewModel.publishReport`: persistir color, size, details
- [ ] `refreshFeedIfEmpty()`: **no** insertar mock (vacío o solo log)
- [ ] `getDogDetail(id)`: leer desde `IPawDao.getById(id)` → mapear a `DetailState`
- [ ] `DetailState`: usar `details` del usuario + `aiAnalysis` por separado
- [ ] Navegación: `detail_screen/{postId}` (Navigation Compose argument)
- [ ] `FeedScreen`: pasar `post.id` al navegar a Detail
- [ ] `DetailViewModel`: recibir `postId` (SavedStateHandle o nav argument), cargar por id
- [ ] Sacar `getMatchedDogs()` del flujo hasta Fase 2 (puede devolver `emptyList()` temporalmente)
- [ ] `FakePawRepository` en tests: actualizar si rompe compilación
- [ ] **No borrar** `PawMockDataSource` todavía si otros tests lo usan; sacarlo del Repository

### Archivos probables

```
data/local/PetReportLocal.kt
data/local/PawDatabase.kt
data/local/ModelMapping.kt
data/local/IPawDao.kt
data/remote/FirestoreReportDataSource.kt
data/PawRepository.kt
domain/IPawRepository.kt          (si agregás métodos)
components/feed/FeedState.kt      (DogPost + campos si aplica)
components/detail/DetailState.kt
components/detail/DetailViewModel.kt
components/detail/DetailScreen.kt
components/feed/FeedScreen.kt
components/NavigationStack.kt
components/Screen.kt
components/match/MatchViewModel.kt  (publishReport: pasar color/size/details)
app/src/test/.../FakePawRepository.kt
```

### Cómo verificar Fase 0

1. Sync Gradle → desinstalar app o borrar datos (sacar mock viejos)
2. Feed **vacío** al inicio
3. Crear 2–3 reportes reales con foto, color, tamaño, detalles
4. Aparecen en Feed y Firebase Console
5. Tocar tarjeta → Detail muestra **ese** perro (no Bobby)
6. Campos color/tamaño/detalles persisten tras cerrar app

### Commit sugerido
```
feat: datos reales en feed y detalle, campos extra en Room/Firestore
```

---

## FASE 1 — Infraestructura Gemini (sin UI final)

**Objetivo:** Servicio de IA inyectable con Hilt. API key configurada. Sin cambiar Match UI todavía.

**Depende de:** Fase 0 mergeada o commiteada en la misma rama.

### Checklist

- [ ] `local.properties`: `GEMINI_API_KEY=tu_key` (Google AI Studio)
- [ ] `app/build.gradle.kts`: `buildConfigField("String", "GEMINI_API_KEY", ...)`
- [ ] Gradle: dependencia Google Generative AI SDK para Android
  - Referencia: `com.google.ai.client.generativeai:generativeai` (ver versión estable en documentación)
- [ ] Crear `data/ai/GeminiMatchAnalyzer.kt` (o `data/remote/`)
- [ ] Crear modelos de respuesta:
  ```kotlin
  data class GeminiMatchResult(
      val aiAnalysis: String,
      val matches: List<ScoredMatch>
  )
  data class ScoredMatch(
      val postId: String,
      val matchPercentage: Int,
      val reason: String
  )
  ```
- [ ] Método principal:
  ```kotlin
  suspend fun analyzeReport(
      draft: ReportDraft,
      candidates: List<DogPost>,
      imageBytes: ByteArray?  // o Uri/path resuelto desde draft.imageUrl
  ): Result<GeminiMatchResult>
  ```
- [ ] Prompt: pedir **JSON** estructurado (facilita parseo)
- [ ] Parser con manejo de error si Gemini devuelve markdown/json mal formado
- [ ] Hilt: `@Provides` / `@Inject` del analyzer
- [ ] `IPawRepository`: agregar `suspend fun getMatchCandidates(draft: ReportDraft): List<DogPost>`
  - Candidatos: posts en Room con **status opuesto** al draft (Perdido ↔ Encontrado)
  - Opcional: filtrar por misma ciudad/zona o misma raza si hay datos
  - Excluir el propio reporte si ya existiera
- [ ] Log de debug; no crashear si API key vacía

### Prompt de referencia (adaptar)

```
Sos un asistente de mascotas perdidas en Argentina.
El usuario reportó: [nombre, raza, color, tamaño, ubicación, detalles, estado].
Analizá la imagen adjunta y compará con estos reportes existentes:
[lista id, nombre, raza, ubicación, estado, color, detalles por candidato]

Respondé SOLO JSON:
{
  "aiAnalysis": "descripción visual + observaciones (mencionar si difiere de lo reportado)",
  "matches": [
    { "postId": "...", "matchPercentage": 87, "reason": "..." }
  ]
}
```

### Cómo verificar Fase 1

1. Test manual temporal o unit test con mock del SDK
2. Con API key válida: llamar analyzer desde un test/debug con 1 draft + 1 candidato
3. Recibir JSON parseado sin crash
4. **No** hace falta que MatchScreen se vea distinto aún

### Commit sugerido
```
feat: agregar GeminiMatchAnalyzer con Hilt y API key
```

---

## FASE 2 — Match con IA real + publicar aiAnalysis

**Objetivo:** Reemplazar simulación en Match. UI muestra análisis IA y coincidencias reales.

**Depende de:** Fase 0 + Fase 1.

### Checklist

- [ ] `MatchedDog`: agregar `postId: String`, `reason: String` (opcional)
- [ ] `MatchState`: agregar `aiAnalysis: String`, `errorMessage: String?`
- [ ] `MatchViewModel`: reemplazar `startScanningSimulation()` por:
  1. Leer `draftStore.peek()` — si null, error
  2. `repository.getMatchCandidates(draft)`
  3. Cargar imagen del draft como bytes/File para Gemini
  4. `geminiMatchAnalyzer.analyzeReport(...)`
  5. Mapear `ScoredMatch` → `MatchedDog` (resolver datos del candidato desde Room por postId)
  6. Actualizar `uiState` (isScanning=false, aiAnalysis, matches, error)
- [ ] Si `candidates.isEmpty()`: igual llamar Gemini para **solo** descripción visual, matches vacíos + mensaje amigable
- [ ] Si Gemini falla: `errorMessage`, matches vacíos, **sin mock**
- [ ] `publishReport()`: al crear `DogPost`/`PetReportLocal`, incluir `aiAnalysis` del estado actual
- [ ] `MatchScreen`: mostrar bloque "Análisis iamPaw AI" con `state.aiAnalysis`
- [ ] `MatchCard`: navegar a `detail_screen/{postId}` del match
- [ ] `DetailScreen`: mostrar `details` (usuario) y `aiAnalysis` (IA) en secciones separadas
- [ ] (Opcional) `MatchViewModelTest` con mock de `GeminiMatchAnalyzer`

### Flujo completo esperado

```
Report (foto + form) → Match
  → Gemini describe imagen + cruza con candidatos de Room
  → UI: análisis + % coincidencias
  → Publicar → Room + Firestore (con aiAnalysis guardado)
  → Feed muestra reporte real
```

### Cómo verificar Fase 2

1. Tener **al menos 2 reportes** en feed (status opuestos ayuda: uno Perdido, uno Encontrado)
2. Crear nuevo reporte → ir a Match
3. Ver loading → análisis IA con texto real (no genérico hardcode)
4. Ver coincidencias con % (si hay candidatos compatibles)
5. Publicar → en Detail del reporte nuevo aparece `aiAnalysis`
6. Modo avión en Match → mensaje de error, app no crashea
7. Sin API key → error claro en logs/UI

### Commit sugerido
```
feat: integrar Gemini en Match con análisis visual y ranking real
```

---

## DESPUÉS DE LAS 3 FASES

- [ ] Actualizar `PROGRESO.md` sección IA generativa
- [ ] PR `feature/gemini-match` → `develop`
- [ ] Usuario carga 3 perros reales para demo final
- [ ] Siguiente bloque TPO: Glide + Splash API (otro prompt/rama)

---

## REGLAS PARA EL AGENTE

- **Una fase por chat** salvo que el usuario diga lo contrario
- Minimizar scope — no refactorizar código no relacionado
- No reintroducir mock seed en feed
- **Nunca** sobrescribir breed/color del usuario con salida de Gemini
- Commits: `feat:` o `fix:` según estilo del repo
- **NO push** sin aprobación del usuario
- Explicar al usuario qué hizo y cómo probar cada fase

---

## SETUP MANUAL DEL USUARIO (antes de Fase 2)

1. Crear API key en https://aistudio.google.com/
2. Agregar a `local.properties`:
   ```
   GEMINI_API_KEY=tu_clave_aqui
   ```
3. Desinstalar app / borrar datos para limpiar mock viejos
4. Crear 2–3 reportes reales con fotos antes de probar Match

---

## AL TERMINAR CADA FASE, REPORTAR

1. Qué archivos se crearon/modificaron
2. Cómo probarlo en Android Studio
3. Qué queda para la siguiente fase
4. Si algo requirió decisión no prevista en este doc, documentarlo acá
