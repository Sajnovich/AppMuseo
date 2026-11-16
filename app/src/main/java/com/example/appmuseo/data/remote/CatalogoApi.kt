package com.example.appmuseo.data.remote

import com.example.appmuseo.data.remote.dto.MuseoDto
import com.example.appmuseo.data.remote.dto.ProductoDto
import retrofit2.Response
import retrofit2.http.GET
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
}
