# AppMuseo · Museo de los Museos (EA2 · DSY1105)

MVP de la **aplicación móvil integrada para centralización comercial y Mapa de Museos Virtuales** del caso *Museo de los Museos*.
Versión **sin backend**: el login usa credenciales guardadas en una variable y los catálogos están escritos en el código.
En la Unidad 3 se reemplazan esos repositorios por una API REST (Retrofit) sin tocar las pantallas.

- **Lenguaje:** Kotlin · **UI:** Jetpack Compose + Material 3 · **Arquitectura:** MVVM
- **Persistencia local:** Room (usuarios y solicitudes de cada usuario) + DataStore (sesión, preferencias y foto de perfil)
- **Roles:** Administrador, Investigador y Cliente (se elige al iniciar sesión)
- **Recursos nativos:** cámara, galería, lector QR, ubicación, mapa OpenStreetMap, notificaciones del sistema y vibración
- **Pruebas:** 99 pruebas unitarias con JUnit 4

---

## 1. Cómo abrirla y ejecutarla

1. Descomprime el proyecto.
2. En Android Studio: **File → Open** y elige la carpeta `AppMuseo` (la que contiene `settings.gradle.kts`).
3. Espera a que termine el **Gradle Sync** (la primera vez descarga dependencias, necesita internet).
4. Elige un emulador (Device Manager) y presiona **Run ▶**.

**Usuarios de prueba** (en el login primero se elige el tipo de cuenta y luego aparece su usuario de prueba):

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | `admin@museo.cl` | `museo123` |
| Investigador | `investigador@museo.cl` | `inves2026` |
| Cliente | `estudiante@museo.cl` | `duoc2026` |

Si eliges un tipo de cuenta que no corresponde al usuario, la app no deja entrar y lo avisa.
También puedes crear una cuenta nueva con **"Crear cuenta"** (queda como Cliente): se guarda en Room
y la contraseña se guarda cifrada con SHA-256, nunca en texto plano. Las cuentas de prueba también se copian
a Room la primera vez, por eso se pueden editar desde *Perfil → Mis datos personales*.

> Si ya tenías instalada una versión anterior, la base de datos se reinicia sola (versión 4). Ante cualquier error raro, desinstala la app del emulador y vuelve a ejecutarla.

### Ejecutar las pruebas unitarias
- Clic derecho en `app/src/test/java/com/example/appmuseo` → **Run 'Tests in appmuseo'**, o
- En la terminal de Android Studio: `./gradlew test` (Windows: `gradlew test`).

---

## 2. Estructura de carpetas (MVVM, Guías 7, 9, 10 y 11)

```
app/src/main/java/com/example/appmuseo/
├── MainActivity.kt          → aplica el tema y arranca AppNavigation
├── model/                   → Producto (ProductoFisico / Curso), Museo, Zona, UiStates, ResultadoLogin…
├── data/
│   ├── SesionDataStore.kt   → DataStore Preferences (sesión, notificaciones)
│   └── local/               → Room: SolicitudEntity, UsuarioEntity, sus DAO y AppDatabase
├── repository/              → origen de datos SIMULADO (aquí entra Retrofit en la Unidad 3)
├── utils/                   → lógica pura: Validaciones, CotizadorEnvio, Distancia, Seguridad, Formato…
│                              y recursos nativos: Notificaciones, UbicacionUtils, ImagenUtils
├── navigation/              → Screen (rutas), NavigationEvent, AppNavigation (NavHost)
├── viewmodel/               → un ViewModel por funcionalidad (StateFlow)
└── ui/
    ├── theme/               → colores, tipografía y tema Material 3 (claro/oscuro)
    ├── utils/               → WindowSizeUtils (pantallas Compact / Medium / Expanded)
    ├── components/          → reutilizables: CampoTexto, DialogoConfirmacion, TarjetaProducto, Avatares, MuseoVisual, barras…
    └── screen/              → 20 pantallas
app/src/main/res/drawable-nodpi/ → ilustraciones de los productos (WebP)
app/src/test/…               → pruebas unitarias
```

**Flujo MVVM:** la *pantalla* (View) solo dibuja el estado y avisa eventos → el *ViewModel* aplica la lógica y actualiza un `StateFlow` → el *Repository* entrega los datos (hoy simulados, mañana desde el backend).

---

## 3. Qué cubre del caso

| Requerimiento del caso | Dónde está |
|---|---|
| Pantalla de carga y bienvenida | `CargaScreen` (logo grande → chico, barra y rebote) y `BienvenidaScreen` ("Crear cuenta" / "Ya tengo cuenta") |
| Inicio de sesión por rol | `LoginScreen` (Administrador, Investigador, Cliente) |
| Panel principal | `HomeScreen`: saludo, accesos rápidos, sección Explora, destacados y barra inferior |
| Catálogo unificado con imágenes (merch, conservación, publicaciones, cursos) | `CatalogoScreen`, `TarjetaProducto` (ilustración de cada producto), `CatalogoRepository` |
| Detalle con cantidad y disponibilidad | `DetalleProductoScreen` |
| Carrito con datos calculados | `CarritoScreen`: subtotal, envío según destino y tipo (Estándar / Express) y total; `ResumenCompra` |
| Resumen de compra antes de confirmar | `SolicitudScreen` (datos) → `ResumenCompraScreen` (productos + envío = total) |
| Historial de compras de cada usuario | Room: `SolicitudEntity.correoUsuario`, `MisSolicitudesScreen`; cada pedido abre `DetallePedidoScreen` con las pestañas **Seguimiento** (Pendiente → Confirmada → Enviada → Entregada) y **Boleta** (detalle con neto, IVA y total; se puede compartir) |
| Editar perfil (correo y contraseña) y foto | `EditarPerfilScreen`, `PerfilScreen`; la foto se guarda y se mantiene al cerrar la app |
| Favoritos | `FavoritosScreen` (museos y recursos) |
| Notificaciones dentro de la app | `NotificacionesScreen` con filtros por tipo |
| Cotizador de envío nacional e internacional | `CotizadorEnvio` (reglas), `CotizadorScreen` |
| Mapa de museos virtuales de Iberoamérica | `MapaMuseosScreen` (mapa real de OpenStreetMap con osmdroid, sin API key, filtros Todos / País / Proyecto, tarjeta del museo y "museo más cercano a mí") |
| Ficha de cada museo y lista completa | `DetalleMuseoScreen` (descripción, etiquetas, calificación, proyecto virtual, favoritos, cómo llegar) y `MuseosScreen` (buscador y filtro) |
| Lectura de códigos con cámara | `EscanerScreen`: al tocar "Escáner QR" se abre directo la cámara (Google Code Scanner). El QR `MUSEO-1` … `MUSEO-14` abre la ficha del museo y `MDM-001` … el producto; pantalla "¡Código identificado!" |
| Persistencia local de estados esenciales | DataStore (sesión) + Room (solicitudes) |
| Notificaciones push simuladas | Notificación local del sistema (`Notificaciones`) + aviso animado en la app (`NotificacionSimulada`); se activan/desactivan en Perfil |
| Registro de usuarios | `RegistroScreen` → `AuthRepository.registrar()` → Room (`UsuarioEntity`) |
| Accesibilidad básica | `contentDescription`, filas clickeables completas, textos en `sp`, aviso con `liveRegion`, tema con buen contraste |
| Datos sintéticos (Leyes 21.719 y 19.628) | Todos los productos, museos y usuarios son ficticios |

## 4. Qué se usó de cada guía

| Guía | Tema | Dónde |
|---|---|---|
| 2–3 | Null safety, `when`, colecciones (`filter`, `map`, `chunked`) | `CotizadorViewModel`, `UiStates`, `Formato`, `HomeScreen` |
| 4 | POO: herencia, clases abstractas, polimorfismo, control de errores (`try/catch`, `require`) | `Producto` → `ProductoFisico` / `Curso`, `Screen.construirRuta()`, `CotizadorEnvio` |
| 5 | Corrutinas, `suspend`, `delay`, `sealed class ResultadoLogin` | `AuthRepository`, `LoginViewModel` |
| 7 | Estructura MVVM | Carpetas del proyecto |
| 8 | Text, Button, Image, Column, Row, Modifier, Scaffold | Todas las pantallas |
| 9 | Window Size Classes | `WindowSizeUtils`, Catálogo (2/3/4 columnas) |
| 10 | `sealed class Screen` (rutas con argumentos), `NavigationEvent`, `MainViewModel`, TopAppBar, NavigationBar | `navigation/`, `Barras.kt` |
| 11 | UiState + Errores, `validarFormulario()`, `isError` / `supportingText`, ViewModel compartido | Login, Registro, Editar perfil, Solicitud, Cotizador y Escáner |
| 12 | `rememberSaveable`, `derivedStateOf`, `animateColorAsState`, `AnimatedVisibility`, `AnimatedContent`, transiciones, DataStore | Carga, Carrito, Perfil, Mis solicitudes, navegación |
| 13 | Cámara y galería, FileProvider, permisos en tiempo de ejecución, `ImagenInteligente` | Perfil, Escáner, Mapa (ubicación) |
| EA2 | Room | `data/local/` |

---

## 5. Recursos nativos del dispositivo

Todos los permisos "peligrosos" se piden **en el momento en que se necesitan** (no al abrir la app) y,
si el usuario dice que no, la app sigue funcionando y le explica qué pasó.

| Recurso | Para qué se usa | Permiso y manejo seguro | Archivo |
|---|---|---|---|
| Cámara | Foto de perfil (se copia a la carpeta privada de la app y su ruta queda en DataStore, por usuario) y foto del producto | La foto la toma la app de Cámara (`TakePicture`) en un `Uri` de `FileProvider`; **no** se declara `CAMERA` (si se declarara sin concederlo, Android bloquearía la cámara); `<queries>` para encontrar la app | `BotonesCamaraGaleria.kt`, `ImagenUtils.kt` |
| Galería | Elegir foto de perfil | Selector de fotos `PickVisualMedia`: sin permisos de almacenamiento; si cancela, no pasa nada | `BotonesCamaraGaleria.kt` |
| Ubicación | "Museo más cercano a mí" en el mapa | Solo ubicación **aproximada** (`ACCESS_COARSE_LOCATION`); sin permiso o sin señal se usa Santiago como referencia y se avisa | `UbicacionUtils.kt`, `MapaMuseosScreen.kt` |
| Notificaciones | Avisar cuando una solicitud se registra o cambia de estado | `POST_NOTIFICATIONS` (Android 13+) pedido al confirmar una solicitud o al activarlas en Perfil; sin permiso, el aviso se ve dentro de la app | `Notificaciones.kt`, `SolicitudViewModel.kt` |
| Vibración | Confirmar físicamente "agregar al carrito" | Retroalimentación háptica de Compose, no requiere permiso | `TarjetaProducto.kt`, `DetalleProductoScreen.kt` |
| Lector QR / código de barras | Escanear el QR de un museo (`MUSEO-n`) o el código de un producto (QR y CODE-128) | Google Code Scanner: Google Play services abre su propia cámara, la app no pide permiso; si se cancela, se avisa | `EscanerScreen.kt` |
| App de mapas | Abrir un museo en Google Maps u otra | `Intent` implícito con `try/catch` si no hay app instalada | `MapaMuseosScreen.kt` |

---

## 6. Programación orientada a objetos en el proyecto

| Concepto | Dónde se aplica |
|---|---|
| **Herencia + clase base abstracta** | `sealed class Producto` con miembros `abstract`; `ProductoFisico` y `Curso` heredan de ella |
| **Polimorfismo** | `limiteCompra()`, `disponibilidad()` y `etiquetaAccion()` responden distinto según el tipo: el carrito y las tarjetas no preguntan "¿es un curso?", solo llaman al método |
| **`when` exhaustivo sobre subclases** | `DetalleProductoScreen` muestra stock/peso para `ProductoFisico` y cupos/modalidad para `Curso` |
| **Sobrescritura (`open` / `override`)** | `Screen.construirRuta()` es `open`; `Screen.Detalle` la sobrescribe para insertar el id del producto |
| **Jerarquías cerradas para resultados** | `ResultadoLogin`, `ResultadoRegistro`, `NavigationEvent` (`sealed class`) |
| **Interfaces** | `UsuarioDao` y `SolicitudDao`: Room genera la implementación real y las pruebas usan `FakeUsuarioDao`; `AuthRepository` funciona igual con cualquiera de las dos |
| **Herencia de clases del framework** | Cada ViewModel hereda de `ViewModel` / `AndroidViewModel`; `AppDatabase` hereda de `RoomDatabase` |
| **Encapsulamiento** | `MutableStateFlow` privado y `StateFlow` público de solo lectura en todos los ViewModels; regla de negocio en `init { require(...) }` de `ProductoFisico` |
| **Objetos únicos (`object`)** | `Validaciones`, `CotizadorEnvio`, `Distancia`, `Seguridad`, `Notificaciones` |

---

## 7. Cómo se cubre cada criterio de la rúbrica

| Indicador | Evidencia en la app |
|---|---|
| **IE 2.1.1** Interfaz estructurada y navegación fluida | Jerarquía clara: Bienvenida → login por rol → Inicio con barra inferior y accesos rápidos; compra en 3 pasos (Carrito → Datos → Resumen); `TopAppBar` con volver; estados vacíos con acción ("Explorar catálogo"); confirmación antes de acciones que no se deshacen (`DialogoConfirmacion`); diseño adaptable (Window Size Classes) |
| **IE 2.1.2** Formularios con validación visual por campo | Login, Registro (con lista de campos obligatorios), Editar perfil, Solicitud, Cotizador y búsqueda del Escáner usan `CampoTexto`: ícono por campo, error en rojo con ícono y mensaje debajo, check verde cuando el dato es válido, textos de ayuda y teclado adecuado |
| **IE 2.2.1** Validaciones desacopladas de la UI | Todas las reglas están en `utils/Validaciones.kt` (Kotlin puro, probado con JUnit); los ViewModels guardan los errores en el `UiState` y la pantalla solo los dibuja |
| **IE 2.2.2** Animaciones con propósito | Logo de carga que se achica y rebota, tarjetas que entran una tras otra, línea del escáner, check con rebote, corazón que late, línea de tiempo del pedido, transiciones entre pantallas, `animateItem` al filtrar o eliminar, cantidad y total del carrito con `AnimatedContent`, avisos con `AnimatedVisibility`, colores de estado con `animateColorAsState`, marcador del mapa con `animateDpAsState`, logo de carga |
| **IE 2.3.1** Clases, herencia y polimorfismo | Ver sección 6 |
| **IE 2.3.2** GitHub y Trello | Ver sección 8 |
| **IE 2.4.1** Recursos nativos seguros y coherentes | Ver sección 5 |

---

## 8. Flujo de trabajo (GitHub + Trello)

- **Trello:** tablero con las columnas *Por hacer → En progreso → En revisión → Listo*. Cada tarjeta corresponde a un *issue* de GitHub.
- **Issues:** cada funcionalidad o corrección tiene su issue con la descripción y los criterios para darla por terminada.
- **Ramas:** se trabaja en ramas `feature/...` (funcionalidades), `fix/...` (correcciones) y `docs/...` (documentación); `main` queda siempre estable.
- **Integración:** cada rama se integra a `main` con un commit de merge (`--no-ff`), así el historial muestra cada parte por separado.
- **Correcciones del profesor por partes:** `feature/bienvenida-login-roles`, `feature/dashboard`, `feature/museos-y-qr`, `feature/compra-y-envio` y `feature/perfil-e-historial`.
- **Commits:** pequeños y descriptivos, en español.

---

## 9. Qué cambia en la Unidad 3 (backend)

| Hoy (simulado) | Unidad 3 |
|---|---|
| `AuthRepository.USUARIOS_PRUEBA` (variable) + usuarios en Room | `POST /login` y `POST /usuarios` con Retrofit + token |
| `CatalogoRepository` (lista en código) | `GET /productos` y `GET /cursos` del sitio institucional |
| `MuseoRepository` (coordenadas sintéticas) | API de geolocalización; el mapa puede pasar a Google Maps |
| Solicitudes solo en Room | Room como caché + `POST /solicitudes` |
| Notificaciones locales | Push reales con Firebase Cloud Messaging |
| "Avanzar estado (simulado)" (botón) | El estado lo cambia el backend |

Como las pantallas y los ViewModels hablan con los repositorios, el cambio se concentra en la carpeta `repository/`.

---

## 10. Problemas comunes

- **"Unsupported class file major version" al sincronizar:** en *Settings → Build, Execution, Deployment → Build Tools → Gradle*, elige como *Gradle JDK* la versión 17 o 21.
- **Android Studio ofrece actualizar AGP:** puedes aceptar o ignorar; el proyecto funciona con la versión incluida.
- **Cámara en el emulador:** la cámara virtual funciona, pero en el Escáner también están los botones *Simular QR de museo* y *Simular producto*, y un campo para escribir el código (ej. `MUSEO-5` o `MDM-001`).
- **"Museo más cercano" siempre dice "desde Santiago" en el emulador:** el emulador no tiene ubicación guardada.
  Abre los *Extended controls* (botón ⋯) → *Location*, elige un punto y presiona *Set location*; luego abre Google Maps una vez y vuelve a la app.
- **No llegan notificaciones:** revisa que estén activadas en *Mi perfil* y que hayas aceptado el permiso (Android 13+). También se pueden activar en *Ajustes → Apps → AppMuseo → Notificaciones*.
