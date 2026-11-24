package com.example.appmuseo.repository

import com.example.appmuseo.data.remote.CatalogoApi
import com.example.appmuseo.data.remote.PedidosApi
import com.example.appmuseo.data.remote.Red
import com.example.appmuseo.data.remote.ResultadoApi
import com.example.appmuseo.data.remote.RetrofitInstance
import com.example.appmuseo.data.remote.dto.MuseoDto
import com.example.appmuseo.data.remote.dto.MuseoRequestDto
import com.example.appmuseo.data.remote.dto.PedidoDto
import com.example.appmuseo.data.remote.dto.ProductoDto
import com.example.appmuseo.data.remote.dto.ProductoRequestDto

// Funciones de gestión del Administrador y del Investigador.
// Todo se hace directo contra el backend (con el token JWT): así los cambios quedan en MySQL
// y se ven en la app de cualquier usuario al sincronizar.
class GestionRepository(
    private val catalogo: CatalogoApi = RetrofitInstance.catalogo,
    private val pedidos: PedidosApi = RetrofitInstance.pedidos
) {

    // ---- productos y cursos (Administrador) ----
    suspend fun productos(): ResultadoApi<List<ProductoDto>> = Red.llamar { catalogo.productos() }

    suspend fun guardarProducto(id: Long?, datos: ProductoRequestDto): ResultadoApi<ProductoDto> =
        if (id == null) Red.llamar { catalogo.crearProducto(datos) }
        else Red.llamar { catalogo.actualizarProducto(id, datos) }

    suspend fun eliminarProducto(id: Long): ResultadoApi<Unit> = Red.llamar { catalogo.eliminarProducto(id) }

    // ---- fichas de museos (Investigador y Administrador) ----
    suspend fun museos(): ResultadoApi<List<MuseoDto>> = Red.llamar { catalogo.museos() }

    suspend fun guardarMuseo(id: Long?, datos: MuseoRequestDto): ResultadoApi<MuseoDto> =
        if (id == null) Red.llamar { catalogo.crearMuseo(datos) }
        else Red.llamar { catalogo.actualizarMuseo(id, datos) }

    suspend fun eliminarMuseo(id: Long): ResultadoApi<Unit> = Red.llamar { catalogo.eliminarMuseo(id) }

    // ---- solicitudes recibidas (Administrador) ----
    suspend fun pedidos(): ResultadoApi<List<PedidoDto>> = Red.llamar { pedidos.listarTodos() }

    suspend fun avanzarPedido(id: Long): ResultadoApi<PedidoDto> = Red.llamar { pedidos.avanzar(id) }
}
