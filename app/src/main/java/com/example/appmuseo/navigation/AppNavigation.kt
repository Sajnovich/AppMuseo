package com.example.appmuseo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appmuseo.ui.screen.CargaScreen
import com.example.appmuseo.ui.screen.CarritoScreen
import com.example.appmuseo.ui.screen.CatalogoScreen
import com.example.appmuseo.ui.screen.CotizadorScreen
import com.example.appmuseo.ui.screen.DetalleProductoScreen
import com.example.appmuseo.ui.screen.EscanerScreen
import com.example.appmuseo.ui.screen.HomeScreen
import com.example.appmuseo.ui.screen.LoginScreen
import com.example.appmuseo.ui.screen.MapaMuseosScreen
import com.example.appmuseo.ui.screen.MisSolicitudesScreen
import com.example.appmuseo.ui.screen.PerfilScreen
import com.example.appmuseo.ui.screen.RegistroScreen
import com.example.appmuseo.ui.screen.SolicitudScreen
import com.example.appmuseo.viewmodel.CarritoViewModel
import com.example.appmuseo.viewmodel.CatalogoViewModel
import com.example.appmuseo.viewmodel.CotizadorViewModel
import com.example.appmuseo.viewmodel.EscanerViewModel
import com.example.appmuseo.viewmodel.LoginViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.MapaViewModel
import com.example.appmuseo.viewmodel.PerfilViewModel
import com.example.appmuseo.viewmodel.RegistroViewModel
import com.example.appmuseo.viewmodel.SesionViewModel
import com.example.appmuseo.viewmodel.SolicitudViewModel
import com.example.appmuseo.data.local.AppDatabase
import com.example.appmuseo.repository.AuthRepository
import kotlinx.coroutines.flow.collectLatest

/**
 * NAVEGACIÓN DE LA APP (Guías 10 y 11).
 *  1. Crea el NavController.
 *  2. Crea los ViewModels UNA sola vez y los comparte entre pantallas.
 *  3. Escucha los eventos del MainViewModel y mueve el NavController.
 *  4. Declara en el NavHost qué pantalla corresponde a cada ruta.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Repositorio de autenticación con acceso a Room (cuentas registradas). remember = se crea una vez
    val authRepository = remember {
        AuthRepository(usuarioDao = AppDatabase.obtenerBaseDeDatos(context).usuarioDao())
    }

    // ViewModels compartidos (viven mientras viva la Activity)
    val mainViewModel: MainViewModel = viewModel()
    val sesionViewModel: SesionViewModel = viewModel()
    // viewModel { ... } permite entregarle parámetros al ViewModel al crearlo
    val loginViewModel: LoginViewModel = viewModel { LoginViewModel(authRepository) }
    val registroViewModel: RegistroViewModel = viewModel { RegistroViewModel(authRepository) }
    val catalogoViewModel: CatalogoViewModel = viewModel()
    val carritoViewModel: CarritoViewModel = viewModel()
    val solicitudViewModel: SolicitudViewModel = viewModel()
    val cotizadorViewModel: CotizadorViewModel = viewModel()
    val mapaViewModel: MapaViewModel = viewModel()
    val escanerViewModel: EscanerViewModel = viewModel()
    val perfilViewModel: PerfilViewModel = viewModel()

    // Escucha los eventos de navegación emitidos por el MainViewModel
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
        startDestination = Screen.Carga.route
    ) {
        composable(route = Screen.Carga.route) {
            CargaScreen(sesionViewModel, mainViewModel)
        }
        composable(route = Screen.Login.route) {
            LoginScreen(loginViewModel, sesionViewModel, mainViewModel)
        }
        composable(route = Screen.Registro.route) {
            RegistroScreen(registroViewModel, loginViewModel, mainViewModel)
        }
        composable(route = Screen.Home.route) {
            HomeScreen(mainViewModel, sesionViewModel, catalogoViewModel, carritoViewModel)
        }
        composable(route = Screen.Catalogo.route) {
            CatalogoScreen(catalogoViewModel, carritoViewModel, mainViewModel)
        }
        // Ruta con argumento: "detalle/{productoId}" (el id llega como Int)
        composable(
            route = Screen.RUTA_DETALLE,
            arguments = listOf(navArgument(Screen.ARG_PRODUCTO_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val productoId = backStackEntry.arguments?.getInt(Screen.ARG_PRODUCTO_ID) ?: 0
            DetalleProductoScreen(productoId, catalogoViewModel, carritoViewModel, mainViewModel)
        }
        composable(route = Screen.Carrito.route) {
            CarritoScreen(carritoViewModel, mainViewModel)
        }
        composable(route = Screen.Solicitud.route) {
            SolicitudScreen(solicitudViewModel, carritoViewModel, sesionViewModel, mainViewModel)
        }
        composable(route = Screen.MisSolicitudes.route) {
            MisSolicitudesScreen(solicitudViewModel, mainViewModel)
        }
        composable(route = Screen.Cotizador.route) {
            CotizadorScreen(cotizadorViewModel, mainViewModel)
        }
        composable(route = Screen.Mapa.route) {
            MapaMuseosScreen(mapaViewModel, mainViewModel)
        }
        composable(route = Screen.Escaner.route) {
            EscanerScreen(escanerViewModel, carritoViewModel, mainViewModel)
        }
        composable(route = Screen.Perfil.route) {
            PerfilScreen(perfilViewModel, sesionViewModel, carritoViewModel, mainViewModel)
        }
    }
}
