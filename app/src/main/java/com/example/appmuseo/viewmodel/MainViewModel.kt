package com.example.appmuseo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuseo.navigation.NavigationEvent
import com.example.appmuseo.navigation.Screen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * ViewModel de NAVEGACIÓN (Guía 10).
 * Emite eventos por un SharedFlow; AppNavigation los escucha y mueve el NavController.
 * Así la lógica de "a dónde ir" queda en el ViewModel y no dentro de cada pantalla.
 */
class MainViewModel : ViewModel() {

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    // Versión de solo lectura para que la UI pueda observar pero no emitir
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    fun navigateTo(
        screen: Screen,
        popUpToRoute: Screen? = null,
        inclusive: Boolean = false,
        singleTop: Boolean = false
    ) {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.NavigateTo(screen, popUpToRoute, inclusive, singleTop))
        }
    }

    /** Navegación de la barra inferior: siempre deja "Inicio" como base de la pila. */
    fun navegarPestana(screen: Screen) {
        navigateTo(screen, popUpToRoute = Screen.Home, singleTop = true)
    }

    fun navigateBack() {
        viewModelScope.launch { _navigationEvents.emit(NavigationEvent.PopBackStack) }
    }

    fun navigateUp() {
        viewModelScope.launch { _navigationEvents.emit(NavigationEvent.NavigateUp) }
    }
}
