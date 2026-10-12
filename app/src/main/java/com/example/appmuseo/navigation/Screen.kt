package com.example.appmuseo.navigation

/**
 * RUTAS DE LA APP (Guía 10): una "sealed class" con una ruta por pantalla.
 * Usar objetos en vez de textos sueltos evita errores de tipeo al navegar.
 */
sealed class Screen(val route: String) {

    // Rutas simples (sin argumentos): "data object"
    data object Carga : Screen("carga")
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object Catalogo : Screen("catalogo")
    data object Carrito : Screen("carrito")
    data object Solicitud : Screen("solicitud")
    data object MisSolicitudes : Screen("mis_solicitudes")
    data object Cotizador : Screen("cotizador")
    data object Mapa : Screen("mapa")
    data object Escaner : Screen("escaner")
    data object Perfil : Screen("perfil")

    // Ruta CON argumento: el detalle necesita saber qué producto mostrar
    data class Detalle(val productoId: Int) : Screen(RUTA_DETALLE) {
        // Polimorfismo (Guía 4): esta pantalla construye su ruta reemplazando el argumento
        override fun construirRuta(): String = route.replace("{$ARG_PRODUCTO_ID}", productoId.toString())
    }

    /** Ruta final que se entrega al NavController. Por defecto es la misma ruta. */
    open fun construirRuta(): String = route

    companion object {
        const val ARG_PRODUCTO_ID = "productoId"
        const val RUTA_DETALLE = "detalle/{$ARG_PRODUCTO_ID}"
    }
}
