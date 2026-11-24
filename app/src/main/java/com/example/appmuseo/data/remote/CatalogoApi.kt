package com.example.appmuseo.data.remote

import com.example.appmuseo.data.remote.dto.MuseoDto
import com.example.appmuseo.data.remote.dto.MuseoRequestDto
import com.example.appmuseo.data.remote.dto.ProductoDto
import com.example.appmuseo.data.remote.dto.ProductoRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// ms-catalogo (puerto 8082)
interface CatalogoApi {

    @GET("api/museos")
    suspend fun museos(): Response<List<MuseoDto>>

    @GET("api/museos/{id}")
    suspend fun museo(@Path("id") id: Long): Response<MuseoDto>

    @GET("api/productos")
    suspend fun productos(): Response<List<ProductoDto>>

    @GET("api/productos/{id}")
    suspend fun producto(@Path("id") id: Long): Response<ProductoDto>

    // ---- gestión (necesitan token JWT con el rol correcto) ----

    // Administrador: alta, modificación y desactivación de productos y cursos
    @POST("api/productos")
    suspend fun crearProducto(@Body datos: ProductoRequestDto): Response<ProductoDto>

    @PUT("api/productos/{id}")
    suspend fun actualizarProducto(@Path("id") id: Long, @Body datos: ProductoRequestDto): Response<ProductoDto>

    @DELETE("api/productos/{id}")
    suspend fun eliminarProducto(@Path("id") id: Long): Response<Unit>

    // Investigador y Administrador: fichas de museos virtuales
    @POST("api/museos")
    suspend fun crearMuseo(@Body datos: MuseoRequestDto): Response<MuseoDto>

    @PUT("api/museos/{id}")
    suspend fun actualizarMuseo(@Path("id") id: Long, @Body datos: MuseoRequestDto): Response<MuseoDto>

    @DELETE("api/museos/{id}")
    suspend fun eliminarMuseo(@Path("id") id: Long): Response<Unit>
}
