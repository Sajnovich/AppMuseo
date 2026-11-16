# EA3 · Integración con backend, API externa, pruebas y APK firmada

## 1. Arquitectura

```
App Android (Kotlin + Compose, MVVM)
 ├── ui/screen          pantallas
 ├── viewmodel          estado de cada pantalla (StateFlow)
 ├── repository         decide de dónde salen los datos
 ├── data/remote        Retrofit: ApiService de cada microservicio + DTO
 └── data/local         Room: copia local para funcionar sin conexión
        │
        ├── ms-usuarios :8081  (Spring Boot + MySQL)
        ├── ms-catalogo :8082  (Spring Boot + MySQL)
        ├── ms-pedidos  :8083  (Spring Boot + MySQL)
        └── OpenStreetMap Nominatim (API externa pública, HTTPS)
```

El backend está en el repositorio **AppMuseo-Backend**.

## 2. Consumo de API REST con Retrofit

| Archivo | Para qué sirve |
|---|---|
| `data/remote/UsuariosApi.kt` | login, registro y edición de perfil |
| `data/remote/CatalogoApi.kt` | museos y productos |
| `data/remote/PedidosApi.kt` | crear, listar, avanzar y anular pedidos |
| `data/remote/NominatimApi.kt` | API externa de OpenStreetMap |
| `data/remote/RetrofitInstance.kt` | crea los clientes Retrofit y OkHttp (un solo lugar) |
| `data/remote/ResultadoApi.kt` | convierte cada respuesta en `Exito`, `ErrorServidor` o `SinConexion` |
| `data/remote/Mapeos.kt` | convierte los DTO del backend en los modelos de la app |

Flujo de cada pantalla:

- **Login y registro:** se validan en ms-usuarios. Si no hay conexión, el login usa la copia guardada en Room.
- **Mapa y catálogo:** al abrir piden los datos a ms-catalogo. Si el servidor no responde, aparece un aviso con "Reintentar" y se muestran los datos guardados.
- **Compra:** la app manda solo los productos y las cantidades. ms-pedidos calcula precios, envío, neto e IVA y descuenta el stock. Si no hay stock, se muestra el motivo que entrega el servidor.
- **Mis compras:** se sincronizan con ms-pedidos. Avanzar el estado y anular también se hacen en el servidor.
- **Ficha del museo:** muestra la dirección real que entrega OpenStreetMap a partir de la latitud y la longitud.

### Dirección del backend

- **Emulador:** usa `10.0.2.2`, que es el computador visto desde el emulador. No hay que cambiar nada.
- **Celular físico:** agrega `api.host=IP_DEL_COMPUTADOR` en `gradle.properties`. Agrega también esa IP en `res/xml/network_security_config.xml`.

## 3. Pruebas (Guía 15)

| Tipo | Herramientas | Dónde |
|---|---|---|
| Lógica (repositorios y ViewModel) | Kotest + MockK + kotlinx-coroutines-test | `src/test/java/.../remoto/` |
| Lógica existente (104 pruebas) | JUnit 4, corriendo sobre JUnit 5 con vintage | `src/test/java/...` |
| Interfaz | Compose UI Test (JUnit) | `src/androidTest/java/.../AvisoRedTest.kt` |

Cómo correrlas:

- **Unitarias:** clic derecho en `app/src/test/java` → **Run 'Tests in java'**, o en la terminal `./gradlew test`.
- **De interfaz:** con el emulador encendido, clic derecho en `app/src/androidTest/java` → **Run**, o `./gradlew connectedAndroidTest`.
- **Cobertura:** `./gradlew reporteCobertura` corre las pruebas y genera el reporte de **JaCoCo** en `app/build/reports/jacoco/reporteCobertura/html/index.html`. Mide la lógica de la app: ViewModel, Repository, capa Retrofit, modelos y utilidades. Deja fuera las pantallas Compose (que se prueban con Compose UI Test) y el código que genera Android.

## 4. APK firmada

### Identidad de la app

| Dato | Valor | Para qué sirve |
|---|---|---|
| Nombre visible | Museo de los Museos (`strings.xml` → `app_name`) | Lo que se ve bajo el ícono |
| `applicationId` | `cl.duoc.museodelosmuseos` | Identificador único en Android y Google Play. Google Play rechaza los que empiezan con `com.example` |
| `versionCode` | 1 | Número interno: debe subir en cada publicación |
| `versionName` | 1.0.0 | Versión que ve el usuario |
| Archivo generado | `AppMuseo-v1.0.0-release.apk` | Nombre del APK (`base.archivesName`) |
| Alias de la llave | `appmuseo` | Nombre de la llave dentro del KeyStore |

### Llave y configuración (ya incluidas)

- **Llave:** `keystore/appmuseo-release.jks`, en formato PKCS12. Alias `appmuseo`, RSA 2048, válida 25 años. El certificado dice: Eduardo Sajnovich, Duoc UC, Santiago, CL.
- **Configuración:** `keystore.properties` guarda la ruta, el alias y las contraseñas. `app/build.gradle.kts` los lee en `signingConfigs.release`.
- **Generar la APK firmada:** `./gradlew assembleRelease`, o en Android Studio **Build → Generate Signed App Bundle / APK → APK**, eligiendo el `.jks` del proyecto.
- **Resultado:** `app/build/outputs/apk/release/AppMuseo-v1.0.0-release.apk`. Se copia a `apk/` para entregarla.

> La evaluación pide entregar la llave y su configuración en el repositorio. En una app real **nunca** se publican el `.jks` ni sus contraseñas.

### Instalar y verificar

- **Instalar:** arrastra el `.apk` al emulador, o usa `adb install apk/AppMuseo-v1.0.0-release.apk`.
- **Verificar la firma:** `apksigner verify --print-certs apk/AppMuseo-v1.0.0-release.apk` (está en `Android/sdk/build-tools/<versión>/`).

### Seguridad

- En una app real, el `.jks` y `keystore.properties` no se suben a GitHub y se guarda una copia de respaldo aparte. Aquí se incluyen solo porque la evaluación lo pide.
- Si se pierde el KeyStore, no se pueden publicar actualizaciones de la misma app. Los usuarios tendrían que desinstalar e instalar de nuevo.
- La clave debug es temporal y es igual para todos los desarrolladores. La clave release es única y personal.
