package com.example.appmuseo.navigation

/**
 * EVENTOS DE NAVEGACIÓN (Guía 10). Las pantallas no navegan directamente:
 * le avisan al MainViewModel, este emite un evento y AppNavigation lo ejecuta.
 */
sealed class NavigationEvent {

    /**
     * Ir a una pantalla.
     * @param popUpToRoute si no es null, se quitan pantallas de la pila hasta llegar a esta.
     * @param inclusive si es true, también se quita [popUpToRoute] (ej: no volver al Login con "atrás").
     * @param singleTop evita abrir dos veces la misma pantalla (útil en la barra inferior).
     */
    data class NavigateTo(
        val route: Screen,
        val popUpToRoute: Screen? = null,
        val inclusive: Boolean = false,
        val singleTop: Boolean = false
    ) : NavigationEvent()

    /** Volver a la pantalla anterior. */
    object PopBackStack : NavigationEvent()

    /** Navegar "hacia arriba" en la jerarquía. */
    object NavigateUp : NavigationEvent()
}
