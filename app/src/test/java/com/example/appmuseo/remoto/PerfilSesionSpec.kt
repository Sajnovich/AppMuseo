package com.example.appmuseo.remoto

import android.app.Application
import com.example.appmuseo.data.SesionDataStore
import com.example.appmuseo.data.local.AppDatabase
import com.example.appmuseo.data.local.SolicitudDao
import com.example.appmuseo.data.local.UsuarioDao
import com.example.appmuseo.data.remote.RetrofitInstance
import com.example.appmuseo.data.remote.UsuariosApi
import com.example.appmuseo.data.remote.dto.UsuarioDto
import com.example.appmuseo.model.PerfilUsuario
import com.example.appmuseo.model.Rol
import com.example.appmuseo.navigation.NavigationEvent
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.PerfilViewModel
import com.example.appmuseo.viewmodel.SesionViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class PerfilSesionSpec : FunSpec({

    val reloj = TestCoroutineScheduler()
    val estudiante = PerfilUsuario("Estudiante Demo", "estudiante@museo.cl", "Estudiante", Rol.CLIENTE, 2)

    beforeTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(reloj))
        mockkConstructor(SesionDataStore::class)
        every { anyConstructed<SesionDataStore>().obtenerNotificaciones() } returns flowOf(true)
        coEvery { anyConstructed<SesionDataStore>().guardarNotificaciones(any()) } just runs
        coEvery { anyConstructed<SesionDataStore>().guardarSesion(any()) } just runs
        coEvery { anyConstructed<SesionDataStore>().cerrarSesion() } just runs
        coEvery { anyConstructed<SesionDataStore>().guardarToken(any(), any()) } just runs
        every { anyConstructed<SesionDataStore>().obtenerToken() } returns flowOf("token-guardado" to "2026-11-24T22:30:00")
    }
    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    fun perfilViewModel(api: UsuariosApi): PerfilViewModel {
        val base = mockk<AppDatabase>()
        every { base.usuarioDao() } returns mockk<UsuarioDao>(relaxed = true)
        every { base.solicitudDao() } returns mockk<SolicitudDao>(relaxed = true)
        mockkObject(AppDatabase.Companion)
        every { AppDatabase.obtenerBaseDeDatos(any()) } returns base
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.usuarios } returns api
        return PerfilViewModel(mockk<Application>(relaxed = true))
    }

    test("editar el perfil guarda los cambios en ms-usuarios") {
        val api = mockk<UsuariosApi>()
        coEvery { api.buscarPorCorreo("estudiante@museo.cl") } returns
            Response.success(UsuarioDto(3, "Estudiante Demo", "estudiante@museo.cl", "Estudiante", "CLIENTE", 2))
        coEvery { api.actualizar(3, any()) } returns
            Response.success(UsuarioDto(3, "Matías Demo", "estudiante@museo.cl", "Estudiante", "CLIENTE", 2))
        val viewModel = perfilViewModel(api)
        var perfilNuevo: PerfilUsuario? = null

        viewModel.iniciarEdicion(estudiante)
        viewModel.onNombreChange("Matías Demo")
        viewModel.onClaveActualChange("duoc2026")
        viewModel.guardarCambios("estudiante@museo.cl") { perfilNuevo = it }
        reloj.advanceUntilIdle()

        perfilNuevo?.nombre shouldBe "Matías Demo"
        viewModel.edicion.value.guardado shouldBe true
        viewModel.edicion.value.claveActual shouldBe ""
    }

    test("con la contraseña actual incorrecta se marca ese campo") {
        val api = mockk<UsuariosApi>()
        coEvery { api.buscarPorCorreo(any()) } returns
            Response.success(UsuarioDto(3, "Estudiante Demo", "estudiante@museo.cl", "Estudiante", "CLIENTE", 2))
        coEvery { api.actualizar(any(), any()) } returns Response.error(
            401, """{"estado":401,"mensaje":"La contraseña actual no es correcta"}""".toResponseBody("application/json".toMediaType())
        )
        val viewModel = perfilViewModel(api)

        viewModel.iniciarEdicion(estudiante)
        viewModel.onClaveActualChange("mala")
        viewModel.guardarCambios("estudiante@museo.cl") {}
        reloj.advanceUntilIdle()

        viewModel.edicion.value.errores.claveActual shouldBe "La contraseña actual no es correcta"
        viewModel.edicion.value.guardando shouldBe false
    }

    test("sin la contraseña actual no se llama al servidor") {
        val api = mockk<UsuariosApi>()
        val viewModel = perfilViewModel(api)

        viewModel.iniciarEdicion(estudiante)
        viewModel.guardarCambios("estudiante@museo.cl") {}

        viewModel.edicion.value.errores.claveActual shouldBe "Escribe tu contraseña actual para confirmar"
        coVerify(exactly = 0) { api.actualizar(any(), any()) }
    }

    test("el interruptor de notificaciones guarda la preferencia") {
        val viewModel = perfilViewModel(mockk())
        reloj.advanceUntilIdle()
        viewModel.notificacionesActivas.value shouldBe true

        viewModel.alternarNotificaciones()
        viewModel.mostrarMensaje.value shouldBe true
        reloj.advanceUntilIdle()

        viewModel.notificacionesActivas.value shouldBe false
        viewModel.mostrarMensaje.value shouldBe false
        coVerify { anyConstructed<SesionDataStore>().guardarNotificaciones(false) }
    }

    test("al abrir la app se recupera la sesión guardada") {
        every { anyConstructed<SesionDataStore>().obtenerSesionActiva() } returns flowOf(true)
        every { anyConstructed<SesionDataStore>().obtenerUsuario() } returns flowOf(estudiante)

        val viewModel = SesionViewModel(mockk(relaxed = true))
        reloj.advanceUntilIdle()

        viewModel.sesionActiva.value shouldBe true
        viewModel.usuario.value.correo shouldBe "estudiante@museo.cl"
        // el token JWT guardado vuelve a quedar listo para las peticiones
        com.example.appmuseo.data.remote.TokenSesion.token shouldBe "token-guardado"
    }

    test("una sesión guardada sin token JWT pide iniciar sesión de nuevo") {
        every { anyConstructed<SesionDataStore>().obtenerSesionActiva() } returns flowOf(true)
        every { anyConstructed<SesionDataStore>().obtenerUsuario() } returns flowOf(estudiante)
        every { anyConstructed<SesionDataStore>().obtenerToken() } returns flowOf(null to null)

        val viewModel = SesionViewModel(mockk(relaxed = true))
        reloj.advanceUntilIdle()

        viewModel.sesionActiva.value shouldBe false
    }

    test("iniciar y cerrar sesión cambia el usuario activo") {
        every { anyConstructed<SesionDataStore>().obtenerSesionActiva() } returns flowOf(null)
        val viewModel = SesionViewModel(mockk(relaxed = true))
        reloj.advanceUntilIdle()
        viewModel.sesionActiva.value shouldBe false

        viewModel.iniciarSesion(estudiante)
        viewModel.usuario.value.nombre shouldBe "Estudiante Demo"
        coVerify { anyConstructed<SesionDataStore>().guardarSesion(estudiante) }

        viewModel.cerrarSesion()
        reloj.advanceUntilIdle()
        viewModel.sesionActiva.value shouldBe false
        viewModel.usuario.value.correo shouldBe ""
        com.example.appmuseo.data.remote.TokenSesion.hayToken() shouldBe false
    }

    test("la navegación emite un evento por cada acción") {
        val viewModel = MainViewModel()
        val eventos = mutableListOf<NavigationEvent>()
        val escucha = TestScope(UnconfinedTestDispatcher(reloj))
        val trabajo = escucha.launch { viewModel.navigationEvents.collect { eventos.add(it) } }

        viewModel.navigateTo(Screen.Home)
        viewModel.navegarPestana(Screen.Home)
        viewModel.navigateBack()
        viewModel.navigateUp()
        reloj.advanceUntilIdle()

        eventos.size shouldBe 4
        (eventos[1] as NavigationEvent.NavigateTo).singleTop shouldBe true
        eventos[2] shouldBe NavigationEvent.PopBackStack
        eventos[3] shouldBe NavigationEvent.NavigateUp
        trabajo.cancel()
    }
})
