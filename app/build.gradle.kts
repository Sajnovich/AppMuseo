import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    jacoco
}

// IP donde corren los microservicios. 10.0.2.2 es "localhost del computador" visto desde el emulador.
// Para un celular físico se cambia en gradle.properties (api.host=IP del computador en la red wifi)
val apiHost: String = (project.findProperty("api.host") as String?) ?: "10.0.2.2"

// Datos de la firma (KeyStore): ruta del .jks, alias y contraseñas, leídos desde keystore.properties.
// Se entregan en el repositorio porque lo pide la evaluación; en una app real ese archivo no se publica
val archivoFirma = rootProject.file("keystore.properties")
val firma = Properties().apply {
    if (archivoFirma.exists()) archivoFirma.inputStream().use { load(it) }
}

android {
    namespace = "com.example.appmuseo"
    compileSdk = 35

    defaultConfig {
        // identificador único de la app en Android y en Google Play (no puede empezar con "com.example")
        applicationId = "cl.duoc.museodelosmuseos"
        minSdk = 26
        targetSdk = 35
        // versionCode sube en cada publicación; versionName es lo que ve el usuario
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // direcciones de cada microservicio (quedan en BuildConfig)
        buildConfigField("String", "API_USUARIOS", "\"http://$apiHost:8081/\"")
        buildConfigField("String", "API_CATALOGO", "\"http://$apiHost:8082/\"")
        buildConfigField("String", "API_PEDIDOS", "\"http://$apiHost:8083/\"")
    }

    signingConfigs {
        create("release") {
            if (archivoFirma.exists()) {
                storeFile = rootProject.file(firma.getProperty("storeFile"))
                storePassword = firma.getProperty("storePassword")
                keyAlias = firma.getProperty("keyAlias")
                keyPassword = firma.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // mide qué líneas de código recorren las pruebas unitarias (JaCoCo)
            enableUnitTestCoverage = true
        }
        release {
            // si existe keystore.properties, el APK release sale firmado con nuestra clave
            if (archivoFirma.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    // las pruebas unitarias corren sobre JUnit 5 (Kotest); las de JUnit 4 siguen funcionando con "vintage"
    testOptions {
        unitTests.all { it.useJUnitPlatform() }
        unitTests.isReturnDefaultValues = true
    }
}

// Reporte de cobertura de la lógica (ViewModel, Repository, Retrofit, modelos y utilidades).
// Se deja fuera la interfaz Compose y el código que genera Android.
// Uso: ./gradlew reporteCobertura  ->  app/build/reports/jacoco/reporteCobertura/html/index.html
tasks.register<JacocoReport>("reporteCobertura") {
    group = "verification"
    description = "Corre las pruebas unitarias y genera el reporte de cobertura de la lógica"
    dependsOn("testDebugUnitTest")

    reports {
        html.required.set(true)
        xml.required.set(true)
    }

    val fueraDelReporte = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/ui/**", "**/navigation/AppNavigation*", "**/MainActivity*",
        "**/*_Impl*", "**/data/local/AppDatabase*", "**/data/SesionDataStore*",
        "**/utils/ImagenUtils*", "**/utils/Notificaciones*", "**/utils/UbicacionUtils*",
        "**/data/remote/RetrofitInstance*", "**/data/remote/*Api.class", "**/ComposableSingletons*"
    )
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) { exclude(fueraDelReporte) }
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) { include("outputs/unit_test_code_coverage/debugUnitTest/*.exec") }
    )
}

// nombre del archivo generado: AppMuseo-v1.0.0-release.apk
base {
    archivesName.set("AppMuseo-v1.0.0")
}

dependencies {
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("androidx.compose.material:material-icons-extended")
    implementation("org.osmdroid:osmdroid-android:6.1.20")
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")

    // consumo de API REST
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.windowsizeclass)

    testImplementation(libs.junit)
    // pruebas de la Guía 15: Kotest + MockK + corrutinas
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.3")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
