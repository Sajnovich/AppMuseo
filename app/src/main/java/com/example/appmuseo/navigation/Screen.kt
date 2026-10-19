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

    data class Detalle(val productoId: Int) : Screen(RUTA_DETALLE) {
        override fun construirRuta(): String = route.replace("{$ARG_PRODUCTO_ID}", productoId.toString())
    }

    open fun construirRuta(): String = route

    companion object {
        const val ARG_PRODUCTO_ID = "productoId"
        const val RUTA_DETALLE = "detalle/{$ARG_PRODUCTO_ID}"
    }
}
