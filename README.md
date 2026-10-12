# AppMuseo · Museo de los Museos (EA2 · DSY1105)

MVP de la **aplicación móvil integrada para centralización comercial y Mapa de Museos Virtuales** del caso *Museo de los Museos*.
Versión **sin backend**: el login usa credenciales guardadas en una variable y los catálogos están escritos en el código.
En la Unidad 3 se reemplazan esos repositorios por una API REST (Retrofit) sin tocar las pantallas.

- **Lenguaje:** Kotlin · **UI:** Jetpack Compose + Material 3 · **Arquitectura:** MVVM
- **Persistencia local:** Room (solicitudes) + DataStore (sesión y preferencias)
- **Recurso nativo:** cámara y galería (ActivityResult + FileProvider)
- **Pruebas:** 43 pruebas unitarias con JUnit 4

---

## 1. Cómo abrirla y ejecutarla

1. Descomprime el proyecto.
2. En Android Studio: **File → Open** y elige la carpeta `AppMuseo` (la que contiene `settings.gradle.kts`).
3. Espera a que termine el **Gradle Sync** (la primera vez descarga dependencias, necesita internet).
4. Elige un emulador (Device Manager) y presiona **Run ▶**.

**Usuarios de prueba** (también aparecen en la pantalla de login):

| Correo | Contraseña | Perfil |
|---|---|---|
| `admin@museo.cl` | `museo123` | Profesional |
| `estudiante@museo.cl` | `duoc2026` | Estudiante |

### Ejecutar las pruebas unitarias
- Clic derecho en `app/src/test/java/com/example/appmuseo` → **Run 'Tests in appmuseo'**, o
- En la terminal de Android Studio: `./gradlew test` (Windows: `gradlew test`).

---

## 2. Estructura de carpetas (MVVM, Guías 7, 9, 10 y 11)

```
app/src/main/java/com/example/appmuseo/
├── MainActivity.kt          → aplica el tema y arranca AppNavigation
├── model/                   → data classes: Producto, Museo, Zona, UiStates, ResultadoLogin…
├── data/
│   ├── SesionDataStore.kt   → DataStore Preferences (sesión, notificaciones)
│   └── local/               → Room: SolicitudEntity, SolicitudDao, AppDatabase
├── repository/              → origen de datos SIMULADO (aquí entra Retrofit en la Unidad 3)
├── utils/                   → lógica pura: Validaciones, CotizadorEnvio, ProyeccionMapa, Formato
├── navigation/              → Screen (rutas), NavigationEvent, AppNavigation (NavHost)
├── viewmodel/               → un ViewModel por funcionalidad (StateFlow)
└── ui/
    ├── theme/               → colores, tipografía y tema Material 3 (claro/oscuro)
    ├── utils/               → WindowSizeUtils (pantallas Compact / Medium / Expanded)
    ├── components/          → componentes reutilizables (barras, tarjeta, ImagenInteligente…)
    └── screen/              → 12 pantallas
app/src/test/…               → pruebas unitarias
```

**Flujo MVVM:** la *pantalla* (View) solo dibuja el estado y avisa eventos → el *ViewModel* aplica la lógica y actualiza un `StateFlow` → el *Repository* entrega los datos (hoy simulados, mañana desde el backend).

---

## 3. Qué cubre del caso

| Requerimiento del caso | Dónde está |
|---|---|
| Catálogo unificado (merch, conservación, publicaciones, cursos) | `CatalogoScreen`, `CatalogoRepository` |
| Solicitudes de compra o inscripción | `CarritoScreen` → `SolicitudScreen` (formulario validado) |
| Registros con estado pendiente / completado y fecha | Room: `SolicitudEntity`, `MisSolicitudesScreen` |
| Cotizador de envío nacional e internacional | `CotizadorEnvio` (reglas), `CotizadorScreen` |
| Mapa de museos virtuales de Iberoamérica | `MapaMuseosScreen` (mapa simulado + Intent a la app de mapas) |
| Simulación de cámara para lectura de códigos | `EscanerScreen` (foto real, lectura simulada) |
| Persistencia local de estados esenciales | DataStore (sesión) + Room (solicitudes) |
| Notificaciones push simuladas | `NotificacionSimulada` (se activan/desactivan en Perfil) |
| Accesibilidad básica | `contentDescription`, filas clickeables completas, textos en `sp`, aviso con `liveRegion`, tema con buen contraste |
| Datos sintéticos (Leyes 21.719 y 19.628) | Todos los productos, museos y usuarios son ficticios |

## 4. Qué se usó de cada guía

| Guía | Tema | Dónde |
|---|---|---|
| 2–3 | Null safety, `when`, colecciones (`filter`, `map`, `chunked`) | `CotizadorViewModel`, `UiStates`, `Formato`, `HomeScreen` |
| 4 | POO, polimorfismo, control de errores (`try/catch`, `require`) | `Screen.construirRuta()`, `CotizadorEnvio`, `MapaMuseosScreen` |
| 5 | Corrutinas, `suspend`, `delay`, `sealed class ResultadoLogin` | `AuthRepository`, `LoginViewModel` |
| 7 | Estructura MVVM | Carpetas del proyecto |
| 8 | Text, Button, Image, Column, Row, Modifier, Scaffold | Todas las pantallas |
| 9 | Window Size Classes | `WindowSizeUtils`, Catálogo (1/2/3 columnas), Inicio |
| 10 | `sealed class Screen`, `NavigationEvent`, `MainViewModel`, TopAppBar, Drawer, NavigationBar | `navigation/`, `Barras.kt`, `MenuLateral.kt` |
| 11 | UiState + Errores, `validarFormulario()`, `isError` / `supportingText`, ViewModel compartido | Login y Solicitud |
| 12 | `rememberSaveable`, `derivedStateOf`, `animateColorAsState`, `AnimatedVisibility`, DataStore, `CircularProgressIndicator` | Carga, Perfil, Mis solicitudes |
| 13 | Cámara y galería, FileProvider, `ImagenInteligente` | Perfil y Escáner |
| EA2 | Room | `data/local/` |

---

## 5. Qué cambia en la Unidad 3 (backend)

| Hoy (simulado) | Unidad 3 |
|---|---|
| `AuthRepository.USUARIOS_PRUEBA` (variable) | `POST /login` con Retrofit + token |
| `CatalogoRepository` (lista en código) | `GET /productos` y `GET /cursos` del sitio institucional |
| `MuseoRepository` (coordenadas sintéticas) | API de geolocalización; el mapa puede pasar a Google Maps |
| Solicitudes solo en Room | Room como caché + `POST /solicitudes` |
| "Marcar como completada" (botón) | El estado lo cambia el backend |

Como las pantallas y los ViewModels hablan con los repositorios, el cambio se concentra en la carpeta `repository/`.

---

## 6. Problemas comunes

- **"Unsupported class file major version" al sincronizar:** en *Settings → Build, Execution, Deployment → Build Tools → Gradle*, elige como *Gradle JDK* la versión 17 o 21.
- **Android Studio ofrece actualizar AGP:** puedes aceptar o ignorar; el proyecto funciona con la versión incluida.
- **Cámara en el emulador:** la cámara virtual funciona, pero en el Escáner también hay un botón para *simular lectura* sin cámara y un campo para escribir el código (ej. `MDM-001`).
