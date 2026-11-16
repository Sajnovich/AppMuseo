package com.example.appmuseo.data.remote.dto

import com.google.gson.annotations.SerializedName

// respuesta de la API pública de OpenStreetMap (Nominatim)
data class LugarOsmDto(
    @SerializedName("display_name") val nombreCompleto: String?,
    val lat: String?,
    val lon: String?,
    val address: DireccionOsmDto?
)

data class DireccionOsmDto(
    val road: String?,
    @SerializedName("house_number") val numero: String?,
    val suburb: String?,
    val city: String?,
    val town: String?,
    val state: String?,
    val country: String?
)
