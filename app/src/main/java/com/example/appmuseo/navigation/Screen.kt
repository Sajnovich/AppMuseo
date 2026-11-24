package com.example.appmuseo.navigation

sealed class Screen(val route: String) {

    // Rutas simples (sin argumentos): "data object"
    data object Carga : Screen("carga")
    data object Bienvenida : Screen("bienvenida")
    data object Login : Screen("login")
    data object Registro : Screen("registro")
    data object Home : Screen("home")
    data object Catalogo : Screen("catalogo")
    data object Carrito : Screen("carrito")
    data object Solicitud : Screen("solicitud")
    data object MisSolicitudes : Screen("mis_solicitudes")
    data object Cotizador : Screen("cotizador")
    data object Mapa : Screen("mapa")
    data object Escaner : Screen("escaner")
    data object Perfil : Screen("perfil")
    data object Notificaciones : Screen("notificaciones")
    data object Favoritos : Screen("favoritos")
    data object Museos : Screen("museos")
    data object Resumen : Screen("resumen_compra")
    data object EditarPerfil : Screen("editar_perfil")

    // gestión según el rol (Administrador / Investigador)
    data object GestionCatalogo : Screen("gestion_catalogo")
    data object ProductoForm : Screen("gestion_producto")
    data object GestionMuseos : Screen("gestion_museos")
    data object MuseoForm : Screen("gestion_museo")
    data object GestionPedidos : Screen("gestion_pedidos")

    data class Detalle(val productoId: Int) : Screen(RUTA_DETALLE) {
        override fun construirRuta(): String = route.replace("{$ARG_PRODUCTO_ID}", productoId.toString())
    }

    data class DetalleMuseo(val museoId: Int) : Screen(RUTA_DETALLE_MUSEO) {
        override fun construirRuta(): String = route.replace("{$ARG_MUSEO_ID}", museoId.toString())
    }

    data class DetallePedido(val pedidoId: Int) : Screen(RUTA_DETALLE_PEDIDO) {
        override fun construirRuta(): String = route.replace("{$ARG_PEDIDO_ID}", pedidoId.toString())
    }

    open fun construirRuta(): String = route

    companion object {
        const val ARG_PRODUCTO_ID = "productoId"
        const val RUTA_DETALLE = "detalle/{$ARG_PRODUCTO_ID}"
        const val ARG_MUSEO_ID = "museoId"
        const val RUTA_DETALLE_MUSEO = "museo/{$ARG_MUSEO_ID}"
        const val ARG_PEDIDO_ID = "pedidoId"
        const val RUTA_DETALLE_PEDIDO = "pedido/{$ARG_PEDIDO_ID}"
    }
}
