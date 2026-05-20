# 🐾 iamPaw - Bitácora de Desarrollo y Roadmap

Este archivo sirve como único punto de verdad para el desarrollo de la aplicación **iamPaw** (Android). Detalla las decisiones de arquitectura tomadas en base a la cátedra del **profe Gladkoff (UADE)**, el estado actual del repositorio y los próximos pasos técnicos.

---

## 🛠️ Stack Tecnológico & Arquitectura Base
* **Lenguaje:** Kotlin
* **UI Framework:** Jetpack Compose (Modern Inmersion via `enableEdgeToEdge`)
* **Gestión de Dependencias:** Version Catalog (`libs.versions.toml`) + Gradle Kotlin DSL (`.gradle.kts`)
* **Estructura de Paquetes ("Modo UADE"):**
    * `com.example.iampaw.components`: Motores del sistema (Navegación, Rutas estáticas).
    * `com.example.iampaw.screens`: Vistas/Pantallas de la aplicación.
    * `com.example.iampaw.ui.theme`: Configuración de estilos, colores y tipografías.
* **Control de Versiones:** Git con estándares profesionales (*Conventional Commits*).
* **Estrategia de Datos:** Datos reales desde el inicio (alimentados manualmente en la base de datos). **No se utilizará data mockeada.**

---

## 📅 Registro de Tareas Realizadas (Done)

### Fase 1: Configuración de la Fábrica e Infraestructura
* [x] **Sincronización de Gradle:** Solución de dependencias en rojo mediante catálogo centralizado. Descarga exitosa de Room, Retrofit, Firebase BoM y Glide Compose.
* [x] **Inicialización del Repositorio Local:** Activación de Git en el proyecto.
* [x] **Identidad de Git Configurada:** Configuración global de `user.name` y `user.email` en la Mac de Mateo para enlazar correctamente los commits con la cuenta oficial de GitHub.
* [x] **Primer Commit Oficial:** Guardado con nomenclatura estándar: `chore: configuracion inicial de dependencias y arquitectura base`.
* [x] **Conexión Remota y Autenticación:** Repositorio enlazado a GitHub mediante *Personal Access Token (PAT)* clásico. Subida exitosa a la rama `main`.
* [x] **Estructura de Directorios:** Creación de los paquetes `components` y `screens` dentro de `com.example.iampaw`.
* [x] **Limpieza de `MainActivity.kt`:** Remoción del código genérico de plantilla. El archivo quedó como un contenedor minimalista listo para la inyección de la navegación y lógica de ciclo de vida / Firebase.

---

## 📋 Pendientes y Próximos Pasos (Backlog)

### Fase 2: Control de Versiones Avanzado (GitFlow)
* [ ] **Crear la rama `develop`:** Generar el paso intermedio para integrar el código antes de mandar a `main`.
* [ ] **Crear la feature branch para Navegación:** `feature/navegacion-base`.

### Fase 3: Arquitectura de Navegación (Modo UADE)
* [ ] **Crear `Screen.kt`:** Definir el mapa de rutas estáticas de la aplicación dentro del paquete `components`.
* [ ] **Crear `NavigationStack.kt`:** Programar el componente composable que actuará como el GPS de la app usando `NavHost` y `composable()`.
* [ ] **Inyectar Navegación in `MainActivity.kt`:** Conectar el `navController` al flujo principal.

### Fase 4: Desarrollo de Pantallas (Screens) con Datos Reales
* [ ] **Feature Branch:** `feature/pantalla-login` (Integración de Firebase Auth compartida en MainActivity).
* [ ] **Feature Branch:** `feature/pantalla-feed` (Lista de mascotas conectada a base de datos real).
* [ ] **Feature Branch:** `feature/pantalla-scanner` (Cámara y lectura QR).
* [ ] **Feature Branch:** `feature/pantalla-detalle` (Información de la chapita escaneada).
