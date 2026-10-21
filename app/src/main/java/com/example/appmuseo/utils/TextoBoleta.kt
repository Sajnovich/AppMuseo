package com.example.appmuseo.utils

import com.example.appmuseo.data.local.SolicitudEntity
import com.example.appmuseo.data.local.codigoSolicitud
import com.example.appmuseo.model.Boleta

// la boleta como texto plano, para el boton "Compartir"
fun textoBoleta(solicitud: SolicitudEntity): String = buildString {
    appendLine("MUSEO DE LOS MUSEOS")
    appendLine("Boleta electrónica (simulada)")
    appendLine("N° ${codigoSolicitud(solicitud.id).removePrefix("#")} · ${formatearFecha(solicitud.fecha)}")
    appendLine("Cliente: ${solicitud.nombreCliente}")
    appendLine("------------------------------")
    Boleta.decodificar(solicitud.lineas).forEach {
        appendLine("${it.cantidad} x ${it.nombre}  ${formatearPrecio(it.total)}")
    }
    appendLine("------------------------------")
    appendLine("Subtotal: ${formatearPrecio(solicitud.subtotal)}")
    appendLine("Envío: ${formatearPrecio(solicitud.costoEnvio)}")
    appendLine("Neto: ${formatearPrecio(Boleta.neto(solicitud.total))}")
    appendLine("IVA 19%: ${formatearPrecio(Boleta.iva(solicitud.total))}")
    append("TOTAL: ${formatearPrecio(solicitud.total)}")
}
