package com.example.appmuseo.data.remote

import com.example.appmuseo.data.remote.dto.ActualizarPerfilDto
import com.example.appmuseo.data.remote.dto.LoginRequestDto
import com.example.appmuseo.data.remote.dto.RegistroRequestDto
import com.example.appmuseo.data.remote.dto.UsuarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// ms-usuarios (puerto 8081). Retrofit genera el código de cada llamada a partir de las anotaciones
interface UsuariosApi {

    @POST("api/usuarios/login")
    suspend fun login(@Body datos: LoginRequestDto): Response<UsuarioDto>

    @POST("api/usuarios/registro")
    suspend fun registrar(@Body datos: RegistroRequestDto): Response<UsuarioDto>

    @GET("api/usuarios/correo/{correo}")
    suspend fun buscarPorCorreo(@Path("correo") correo: String): Response<UsuarioDto>

    @PUT("api/usuarios/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body datos: ActualizarPerfilDto): Response<UsuarioDto>
}
