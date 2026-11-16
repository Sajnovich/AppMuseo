package com.example.appmuseo.data.remote

import com.example.appmuseo.data.remote.dto.LugarOsmDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// API externa y pública de OpenStreetMap (https://nominatim.openstreetmap.org)
interface NominatimApi {

    // de coordenadas a dirección (geocodificación inversa): se usa en la ficha del museo
    @GET("reverse")
    suspend fun direccion(
        @Query("lat") latitud: Double,
        @Query("lon") longitud: Double,
        @Query("format") formato: String = "jsonv2",
        @Query("accept-language") idioma: String = "es",
        @Query("zoom") zoom: Int = 16
    ): Response<LugarOsmDto>

}
