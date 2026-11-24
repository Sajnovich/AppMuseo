package com.example.appmuseo.data.remote

import com.example.appmuseo.data.remote.dto.PedidoDto
import com.example.appmuseo.data.remote.dto.PedidoRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// ms-pedidos (puerto 8083)
interface PedidosApi {

    @POST("api/pedidos")
    suspend fun crear(@Body pedido: PedidoRequestDto): Response<PedidoDto>

    @GET("api/pedidos")
    suspend fun listar(@Query("correo") correo: String): Response<List<PedidoDto>>

    // Administrador: todas las solicitudes recibidas (sin filtro de correo)
    @GET("api/pedidos")
    suspend fun listarTodos(): Response<List<PedidoDto>>

    @PATCH("api/pedidos/{id}/avanzar")
    suspend fun avanzar(@Path("id") id: Long): Response<PedidoDto>

    @DELETE("api/pedidos/{id}")
    suspend fun anular(@Path("id") id: Long): Response<Unit>
}
