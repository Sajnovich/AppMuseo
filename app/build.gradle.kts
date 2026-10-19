plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // KSP = "Kotlin Symbol Processing". Room lo usa para GENERAR automáticamente
    // el código de la base de datos a partir de nuestras anotaciones (@Entity, @Dao...).
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.appmuseo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.appmuseo"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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
    }
}

dependencies {
    // --- Dependencias "extra" vistas en las guías 10, 11 y 12 ---
    implementation("androidx.navigation:navigation-compose:2.8.5")             // Navegación (NavHost / NavController)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")     // viewModel() dentro de Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")       // collectAsState y ciclo de vida
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")   // Corrutinas (Guía 5)
    implementation("androidx.datastore:datastore-preferences:1.1.1")           // DataStore (Guía 12): sesión y preferencias

    // --- Room: base de datos local SQLite (persistencia de las solicitudes) ---
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")                             // soporte de corrutinas y Flow
    ksp("androidx.room:room-compiler:2.6.1")                                   // genera el código de la BD

    // --- Cámara / galería (Guía 13): Coil muestra una imagen a partir de su Uri ---
    implementation("io.coil-kt:coil-compose:2.7.0")

    // --- Íconos adicionales de Material (Map, Museum, School, QrCodeScanner, etc.) ---
    implementation("androidx.compose.material:material-icons-extended")
    // Lector de códigos QR y de barras de Google (ML Kit / Google Play services), igual que el ejemplo 105 del curso
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")

    // --- Dependencias que trae la plantilla "Empty Compose Activity" ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.windowsizeclass)                  // Adaptabilidad (Guía 9)

    // --- Pruebas ---
    testImplementation(libs.junit)                                            // Pruebas unitarias (carpeta test/)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
