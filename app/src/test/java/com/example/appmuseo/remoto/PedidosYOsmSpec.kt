package com.example.appmuseo.remoto

import com.example.appmuseo.data.remote.dto.DireccionOsmDto
import com.example.appmuseo.data.remote.dto.LineaPedidoDto
import com.example.appmuseo.data.remote.dto.LugarOsmDto
import com.example.appmuseo.data.remote.dto.PedidoDto
import com.example.appmuseo.model.Boleta
import com.example.appmuseo.repository.OsmRepository
import com.example.appmuseo.repository.SolicitudRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PedidosYOsmSpec : StringSpec({

    val pedido = PedidoDto(
        id = 12, codigo = "#2026-00012", correoUsuario = "estudiante@museo.cl",
        nombreCliente = "Estudiante Demo", correoCliente = "estudiante@museo.cl",
        zonaEnvio = "Región Metropolitana", tipoEnvio = "ESTANDAR", direccion = "Av. Siempre Viva 742",
        subtotal = 56_950, costoEnvio = 0, total = 56_950, neto = 47_857, iva = 9_093,
        cantidadProductos = 5, estado = "CONFIRMADA", fecha = "2026-11-16T10:30:00.123",
        lineas = listOf(
            LineaPedidoDto(1, "Taza Museo de los Museos", 3, 8_990, 26_970),
            LineaPedidoDto(2, "Polera algodón orgánico", 2, 14_990, 29_980)
        )
    )

    "un pedido del backend se guarda en Room con sus montos y su boleta" {
        val fila = SolicitudRepository.aEntidad(pedido, "estudiante@museo.cl")

        fila.idRemoto shouldBe 12L
        fila.total shouldBe 56_950
        fila.estado shouldBe "CONFIRMADA"
        fila.zonaEnvio shouldBe "Región Metropolitana · Estándar"
        fila.detalle shouldBe "3x Taza Museo de los Museos, 2x Polera algodón orgánico"
        Boleta.decodificar(fila.lineas).sumOf { it.total } shouldBe 56_950
    }

    "una fecha inválida no rompe la app" {
        (SolicitudRepository.fechaEnMilis("no es fecha") > 0) shouldBe true
    }

    "la dirección de OpenStreetMap se arma de lo más preciso a lo más general" {
        val lugar = LugarOsmDto(
            nombreCompleto = "texto largo",
            lat = "-12.05", lon = "-77.04",
            address = DireccionOsmDto("Jirón de la Unión", "300", "Cercado de Lima", "Lima", null, "Lima", "Perú")
        )
        OsmRepository.formatear(lugar) shouldBe "Jirón de la Unión 300, Cercado de Lima, Lima, Perú"
    }

    "si OpenStreetMap no trae detalle se usa el nombre completo" {
        OsmRepository.formatear(LugarOsmDto("Lima, Perú", null, null, null)) shouldBe "Lima, Perú"
    }
})
