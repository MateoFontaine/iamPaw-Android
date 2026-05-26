# 🐾 iamPaw - Smart Pet Recovery App

<p align="left">
  <img src="https://img.shields.io/badge/Kotlin-1.9.0-purple.svg?style=flat&logo=kotlin" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-UI-blue.svg?style=flat&logo=android" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Firebase-Auth-orange.svg?style=flat&logo=firebase" alt="Firebase" />
  <img src="https://img.shields.io/badge/Architecture-MVVM-success.svg?style=flat" alt="MVVM" />
</p>

iamPaw es una aplicación móvil nativa diseñada para modernizar y agilizar la búsqueda de mascotas perdidas. Utilizando un enfoque centrado en la comunidad, inteligencia artificial y tecnología QR, la app permite a los usuarios reportar mascotas perdidas o encontradas de manera rápida, eficiente y centralizada.

---

## ✨ Features Principales (Vertical Slices)

* **🔐 Autenticación Segura (Zero-Friction):** Inicio de sesión persistente e integrado con Google Sign-In mediante Firebase Auth.
* **🏠 Feed Inmersivo:** Muro principal con tarjetas de mascotas perdidas/encontradas, con navegación fluida tipo "burbuja".
* **📸 Reporte Inteligente:** Flujo de creación de reportes respaldado por **The Dog API** para la identificación precisa de razas y manejo de estados mediante ViewModels.
* **👤 Perfil y Gamificación:** Panel de usuario dinámico que expone métricas de impacto (alertas activas, mascotas ayudadas) para incentivar la participación comunitaria.

---

## 🛠️ Stack Tecnológico

La aplicación está construida siguiendo los estándares modernos de desarrollo Android:

* **Lenguaje:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material Design 3)
* **Arquitectura:** MVVM (Model-View-ViewModel)
* **Navegación:** Navigation Compose (Single-Activity Architecture)
* **Backend & Auth:** Firebase
* **Networking:** Retrofit2 + OkHttp (Integración con APIs REST)
* **Imágenes:** Coil (Carga asíncrona de imágenes)

---

## 🌿 Flujo de Trabajo y Git Flow

Este repositorio sigue una estrategia estricta de branching orientada a CI/CD y revisiones de código eficientes:

* `main`: Entorno de producción. Código 100% estable.
* `develop`: Rama principal de integración. Refleja el estado actual del próximo release.
* `feature/*`: Ramas efímeras creadas por los desarrolladores para nuevas funcionalidades (ej. `feature/pantalla-feed`). Se integran a `develop` mediante Pull Requests.

---

## 🚀 Instalación y Uso Local

Para correr este proyecto en tu entorno local:

1. Clonar el repositorio:
   ```bash
   git clone [https://github.com/MateoFontaine/iamPaw-Android.git](https://github.com/MateoFontaine/iamPaw-Android.git)
