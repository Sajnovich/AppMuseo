package com.example.appmuseo.data.remote

import com.example.appmuseo.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Un solo lugar donde se crean los clientes HTTP.
// "by lazy" hace que cada uno se cree recién la primera vez que se usa
object RetrofitInstance {

    // configuración común: tiempos de espera y log
    private val base: OkHttpClient by lazy {
        val log = HttpLoggingInterceptor().apply {
            // en el Logcat se ven las peticiones y respuestas (solo en debug)
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(log)
            .build()
    }

    // cliente de los 3 microservicios: agrega el token JWT de la sesión en cada petición.
    // El log va antes, así el token no queda escrito en el Logcat
    private val cliente: OkHttpClient by lazy {
        base.newBuilder()
            .addInterceptor { cadena ->
                val token = TokenSesion.token
                val peticion = if (token.isNullOrBlank()) cadena.request()
                else cadena.request().newBuilder().header("Authorization", "Bearer $token").build()
                cadena.proceed(peticion)
            }
            .build()
    }

    // Nominatim exige identificar la app con un User-Agent propio (y no recibe nuestro token)
    private val clienteOsm: OkHttpClient by lazy {
        base.newBuilder()
            .addInterceptor { cadena ->
                cadena.proceed(
                    cadena.request().newBuilder()
                        .header("User-Agent", "AppMuseo/1.0 (proyecto academico Duoc UC)")
                        .build()
                )
            }
            .build()
    }

    private fun crear(urlBase: String, http: OkHttpClient = cliente): Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val usuarios: UsuariosApi by lazy { crear(BuildConfig.API_USUARIOS).create(UsuariosApi::class.java) }
    val catalogo: CatalogoApi by lazy { crear(BuildConfig.API_CATALOGO).create(CatalogoApi::class.java) }
    val pedidos: PedidosApi by lazy { crear(BuildConfig.API_PEDIDOS).create(PedidosApi::class.java) }
    val osm: NominatimApi by lazy { crear("https://nominatim.openstreetmap.org/", clienteOsm).create(NominatimApi::class.java) }
}
