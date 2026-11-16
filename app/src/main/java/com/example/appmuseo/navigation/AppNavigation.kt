package com.example.appmuseo.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appmuseo.ui.screen.BienvenidaScreen
import com.example.appmuseo.ui.screen.CargaScreen
import com.example.appmuseo.ui.screen.CarritoScreen
import com.example.appmuseo.ui.screen.CatalogoScreen
import com.example.appmuseo.ui.screen.CotizadorScreen
import com.example.appmuseo.ui.screen.DetalleMuseoScreen
import com.example.appmuseo.ui.screen.DetallePedidoScreen
import com.example.appmuseo.ui.screen.EditarPerfilScreen
import com.example.appmuseo.ui.screen.DetalleProductoScreen
import com.example.appmuseo.ui.screen.EscanerScreen
import com.example.appmuseo.ui.screen.FavoritosScreen
import com.example.appmuseo.ui.screen.HomeScreen
import com.example.appmuseo.ui.screen.LoginScreen
import com.example.appmuseo.ui.screen.MapaMuseosScreen
import com.example.appmuseo.ui.screen.MisSolicitudesScreen
import com.example.appmuseo.ui.screen.MuseosScreen
import com.example.appmuseo.ui.screen.NotificacionesScreen
import com.example.appmuseo.ui.screen.PerfilScreen
import com.example.appmuseo.ui.screen.RegistroScreen
import com.example.appmuseo.ui.screen.ResumenCompraScreen
import com.example.appmuseo.ui.screen.SolicitudScreen
import com.example.appmuseo.viewmodel.CarritoViewModel
import com.example.appmuseo.viewmodel.CatalogoViewModel
import com.example.appmuseo.viewmodel.CotizadorViewModel
import com.example.appmuseo.viewmodel.EscanerViewModel
import com.example.appmuseo.viewmodel.FavoritosViewModel
import com.example.appmuseo.viewmodel.LoginViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.MapaViewModel
import com.example.appmuseo.viewmodel.NotificacionesViewModel
import com.example.appmuseo.viewmodel.PerfilViewModel
import com.example.appmuseo.viewmodel.RegistroViewModel
import com.example.appmuseo.viewmodel.SesionViewModel
import com.example.appmuseo.viewmodel.SolicitudViewModel
import com.example.appmuseo.data.local.AppDatabase
import com.example.appmuseo.data.remote.RetrofitInstance
import com.example.appmuseo.repository.AuthRepository
import kotlinx.coroutines.flow.collectLatest

private const val DURACION_TRANSICION = 300

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Repositorio de autenticación: backend (ms-usuarios) + copia en Room
    val authRepository = remember {
        AuthRepository(
            usuarioDao = AppDatabase.obtenerBaseDeDatos(context).usuarioDao(),
            api = RetrofitInstance.usuarios
        )
    }

    val mainViewModel: MainViewModel = viewModel()
    val sesionViewModel: SesionViewModel = viewModel()
    val loginViewModel: LoginViewModel = viewModel { LoginViewModel(authRepository) }
    val registroViewModel: RegistroViewModel = viewModel { RegistroViewModel(authRepository) }
    val catalogoViewModel: CatalogoViewModel = viewModel()
    val carritoViewModel: CarritoViewModel = viewModel()
    val solicitudViewModel: SolicitudViewModel = viewModel()
    val cotizadorViewModel: CotizadorViewModel = viewModel()
    val mapaViewModel: MapaViewModel = viewModel()
    val escanerViewModel: EscanerViewModel = viewModel()
    val perfilViewModel: PerfilViewModel = viewModel()
    val notificacionesViewModel: NotificacionesViewModel = viewModel()
    val favoritosViewModel: FavoritosViewModel = viewModel()

    // cantidad sin leer para el numerito de la barra inferior
    val notificaciones by notificacionesViewModel.notificaciones.collectAsState()
    val noLeidas = notificaciones.count { !it.leida }

    // cada vez que cambia la cuenta se carga SU historial y SU foto
    val usuario by sesionViewModel.usuario.collectAsState()
    val fotoPerfil by perfilViewModel.imagenUri.collectAsState()
    LaunchedEffect(usuario.correo) {
        solicitudViewModel.cargarHistorial(usuario.correo)
        perfilViewModel.cargarFoto(usuario.correo)
    }

    LaunchedEffect(key1 = Unit) {
        mainViewModel.navigationEvents.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateTo -> {
                    navController.navigate(event.route.construirRuta()) {
                        event.popUpToRoute?.let { destino ->
                            popUpTo(destino.route) { inclusive = event.inclusive }
                        }
                        launchSingleTop = event.singleTop
                    }
                }
                is NavigationEvent.PopBackStack -> navController.popBackStack()
                is NavigationEvent.NavigateUp -> navController.navigateUp()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Carga.route,
        // transiciones entre pantallas (guía 12): la nueva entra
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(DURACION_TRANSICION)) +
                    fadeIn(tween(DURACION_TRANSICION))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(DURACION_TRANSICION)) +
                    fadeOut(tween(DURACION_TRANSICION))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(DURACION_TRANSICION)) +
                    fadeIn(tween(DURACION_TRANSICION))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(DURACION_TRANSICION)) +
                    fadeOut(tween(DURACION_TRANSICION))
        }
    ) {
        composable(route = Screen.Carga.route) {
            CargaScreen(sesionViewModel, mainViewModel)
        }
        composable(route = Screen.Bienvenida.route) {
            BienvenidaScreen(mainViewModel)
        }
        composable(route = Screen.Login.route) {
            LoginScreen(loginViewModel, sesionViewModel, mainViewModel)
        }
        composable(route = Screen.Registro.route) {
            RegistroScreen(registroViewModel, loginViewModel, mainViewModel)
        }
        composable(route = Screen.Home.route) {
            HomeScreen(mainViewModel, sesionViewModel, catalogoViewModel, carritoViewModel, notificacionesViewModel, fotoPerfil)
        }
        composable(route = Screen.Catalogo.route) {
            CatalogoScreen(catalogoViewModel, carritoViewModel, mainViewModel, noLeidas)
        }
        composable(
            route = Screen.RUTA_DETALLE,
            arguments = listOf(navArgument(Screen.ARG_PRODUCTO_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val productoId = backStackEntry.arguments?.getInt(Screen.ARG_PRODUCTO_ID) ?: 0
            DetalleProductoScreen(productoId, catalogoViewModel, carritoViewModel, favoritosViewModel, mainViewModel)
        }
        composable(route = Screen.Carrito.route) {
            CarritoScreen(carritoViewModel, mainViewModel)
        }
        composable(route = Screen.Solicitud.route) {
            SolicitudScreen(solicitudViewModel, carritoViewModel, sesionViewModel, mainViewModel)
        }
        composable(route = Screen.Resumen.route) {
            ResumenCompraScreen(solicitudViewModel, carritoViewModel, notificacionesViewModel, mainViewModel)
        }
        composable(
            route = Screen.RUTA_DETALLE_PEDIDO,
            arguments = listOf(navArgument(Screen.ARG_PEDIDO_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val pedidoId = backStackEntry.arguments?.getInt(Screen.ARG_PEDIDO_ID) ?: 0
            DetallePedidoScreen(pedidoId, solicitudViewModel, notificacionesViewModel, catalogoViewModel, carritoViewModel, mainViewModel)
        }
        composable(route = Screen.MisSolicitudes.route) {
            MisSolicitudesScreen(solicitudViewModel, notificacionesViewModel, mainViewModel)
        }
        composable(route = Screen.Cotizador.route) {
            CotizadorScreen(cotizadorViewModel, mainViewModel)
        }
        composable(route = Screen.Mapa.route) {
            MapaMuseosScreen(mapaViewModel, mainViewModel, noLeidas)
        }
        composable(route = Screen.Escaner.route) {
            EscanerScreen(escanerViewModel, carritoViewModel, mainViewModel)
        }
        composable(route = Screen.Perfil.route) {
            PerfilScreen(perfilViewModel, sesionViewModel, carritoViewModel, solicitudViewModel, favoritosViewModel, mainViewModel, noLeidas)
        }
        composable(route = Screen.EditarPerfil.route) {
            EditarPerfilScreen(perfilViewModel, sesionViewModel, mainViewModel)
        }
        composable(route = Screen.Notificaciones.route) {
            NotificacionesScreen(notificacionesViewModel, mainViewModel)
        }
        composable(route = Screen.Museos.route) {
            MuseosScreen(mapaViewModel, favoritosViewModel, mainViewModel)
        }
        composable(
            route = Screen.RUTA_DETALLE_MUSEO,
            arguments = listOf(navArgument(Screen.ARG_MUSEO_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val museoId = backStackEntry.arguments?.getInt(Screen.ARG_MUSEO_ID) ?: 0
            DetalleMuseoScreen(museoId, mapaViewModel, favoritosViewModel, mainViewModel)
        }
        composable(route = Screen.Favoritos.route) {
            FavoritosScreen(favoritosViewModel, catalogoViewModel, mainViewModel)
        }
    }
}
