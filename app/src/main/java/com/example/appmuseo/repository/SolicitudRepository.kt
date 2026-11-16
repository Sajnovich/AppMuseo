package com.example.appmuseo.repository

import com.example.appmuseo.data.local.SolicitudDao
import com.example.appmuseo.data.local.SolicitudEntity
import com.example.appmuseo.data.remote.PedidosApi
import com.example.appmuseo.data.remote.Red
import com.example.appmuseo.data.remote.ResultadoApi
import com.example.appmuseo.data.remote.dto.PedidoDto
import com.example.appmuseo.data.remote.dto.PedidoRequestDto
import com.example.appmuseo.model.Boleta
import com.example.appmuseo.model.LineaBoleta
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.ZoneId

// Los pedidos se crean en ms-pedidos (backend) y Room guarda una copia para verlos sin conexión
class SolicitudRepository(
    private val dao: SolicitudDao,
    private val api: PedidosApi? = null
) {

    fun obtenerPorUsuario(correo: String): Flow<List<SolicitudEntity>> = dao.obtenerPorUsuario(correo)

    suspend fun insertar(solicitud: SolicitudEntity): Long = dao.insertar(solicitud)

    suspend fun actualizarEstado(id: Int, nuevoEstado: String) = dao.actualizarEstado(id, nuevoEstado)

    suspend fun cambiarDueno(anterior: String, nuevo: String) = dao.cambiarDueno(anterior, nuevo)

    suspend fun eliminar(solicitud: SolicitudEntity) = dao.eliminar(solicitud)

    // ---------- Backend (ms-pedidos) ----------

    // POST /api/pedidos: el servidor calcula precios, envío, neto e IVA y descuenta el stock
    suspend fun crearEnServidor(pedido: PedidoRequestDto): ResultadoApi<PedidoDto> {
        val servicio = api ?: return ResultadoApi.SinConexion("Sin servidor configurado")
        return Red.llamar { servicio.crear(pedido) }
    }

    // PATCH /api/pedidos/{id}/avanzar
    suspend fun avanzarEnServidor(idRemoto: Long): ResultadoApi<PedidoDto> {
        val servicio = api ?: return ResultadoApi.SinConexion("Sin servidor configurado")
        return Red.llamar { servicio.avanzar(idRemoto) }
    }

    // DELETE /api/pedidos/{id}: solo si aún no se envía; el stock vuelve al catálogo
    suspend fun anularEnServidor(idRemoto: Long): ResultadoApi<Unit> {
        val servicio = api ?: return ResultadoApi.SinConexion("Sin servidor configurado")
        return Red.llamar { servicio.anular(idRemoto) }
    }

    // GET /api/pedidos?correo=: trae al teléfono los pedidos hechos en otro dispositivo
    // y actualiza el estado de los que ya estaban. Devuelve cuántos pedidos tiene el usuario
    suspend fun sincronizar(correo: String): ResultadoApi<Int> {
        val servicio = api ?: return ResultadoApi.SinConexion("Sin servidor configurado")
        return when (val respuesta = Red.llamar { servicio.listar(correo) }) {
            is ResultadoApi.Exito -> {
                respuesta.datos.forEach { pedido ->
                    val guardado = dao.buscarPorIdRemoto(pedido.id)
                    if (guardado == null) {
                        dao.insertar(aEntidad(pedido, correo))
                    } else if (guardado.estado != pedido.estado) {
                        dao.actualizarEstado(guardado.id, pedido.estado)
                    }
                }
                ResultadoApi.Exito(respuesta.datos.size)
            }
            is ResultadoApi.ErrorServidor -> respuesta
            is ResultadoApi.SinConexion -> respuesta
        }
    }

    companion object {

        // convierte el pedido del backend en la fila que guarda Room
        fun aEntidad(pedido: PedidoDto, correoUsuario: String): SolicitudEntity {
            val lineas = pedido.lineas.map {
                LineaBoleta(it.productoId.toInt(), it.cantidad, it.nombre, it.precioUnitario)
            }
            val conDespacho = !pedido.zonaEnvio.isNullOrBlank()
            return SolicitudEntity(
                correoUsuario = correoUsuario,
                tipo = if (conDespacho) "COMPRA" else "INSCRIPCIÓN",
                detalle = lineas.joinToString(", ") { "${it.cantidad}x ${it.nombre}" },
                lineas = Boleta.codificar(lineas),
                cantidadProductos = pedido.cantidadProductos,
                nombreCliente = pedido.nombreCliente,
                correoCliente = pedido.correoCliente,
                zonaEnvio = if (conDespacho) "${pedido.zonaEnvio} · ${etiquetaEnvio(pedido.tipoEnvio)}" else "",
                direccion = pedido.direccion ?: "",
                subtotal = pedido.subtotal,
                costoEnvio = pedido.costoEnvio,
                total = pedido.total,
                estado = pedido.estado,
                fecha = fechaEnMilis(pedido.fecha),
                idRemoto = pedido.id
            )
        }

        fun etiquetaEnvio(tipo: String?): String = if (tipo == "EXPRESS") "Express" else "Estándar"

        // el backend manda la fecha como "2026-11-16T10:30:00.123"
        fun fechaEnMilis(fecha: String?): Long =
            try {
                LocalDateTime.parse(fecha).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
    }
}
