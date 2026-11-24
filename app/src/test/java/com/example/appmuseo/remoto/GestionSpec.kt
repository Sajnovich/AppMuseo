package com.example.appmuseo.remoto

import com.example.appmuseo.data.remote.CatalogoApi
import com.example.appmuseo.data.remote.PedidosApi
import com.example.appmuseo.data.remote.ResultadoApi
import com.example.appmuseo.data.remote.TokenSesion
import com.example.appmuseo.data.remote.UsuariosApi
import com.example.appmuseo.data.remote.dto.MuseoDto
import com.example.appmuseo.data.remote.dto.PedidoDto
import com.example.appmuseo.data.remote.dto.ProductoDto
import com.example.appmuseo.data.remote.dto.UsuarioDto
import com.example.appmuseo.model.Categoria
import com.example.appmuseo.model.FormMuseo
import com.example.appmuseo.model.FormProducto
import com.example.appmuseo.model.ResultadoLogin
import com.example.appmuseo.repository.AuthRepository
import com.example.appmuseo.repository.GestionRepository
import com.example.appmuseo.utils.ValidacionesGestion
import com.example.appmuseo.viewmodel.GestionViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

// Funciones del Administrador e Investigador y manejo del token JWT
@OptIn(ExperimentalCoroutinesApi::class)
class GestionSpec : FunSpec({

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest {
        Dispatchers.resetMain()
        TokenSesion.limpiar()
    }

    val json = "application/json".toMediaType()
    val taza = ProductoDto(1, "Taza", "Taza de cerámica", 8_990, "MERCH", 25, 0.4, "MDM-001", null, null, false)
    val museo = MuseoDto(3, "Museo del Puerto", "Chile", "Valparaíso", "Historia", "Recorrido virtual", "", -33.04, -71.62, 2025, 4.6, 98)
    fun pedido(estado: String) = PedidoDto(
        7, "2026-00007", "estudiante@museo.cl", "Estudiante", "estudiante@museo.cl", null, null, null,
        89_990, 0, 89_990, 75_622, 14_368, 1, estado, null, emptyList()
    )
    val productoValido = FormProducto(nombre = "Lápiz", descripcion = "Lápiz con logo", precio = "1990", stock = "50", pesoKg = "0,05", codigoBarra = "mdm-099")

    // ---------- token JWT ----------

    test("el login guarda el token JWT que entrega ms-usuarios") {
        val api = mockk<UsuariosApi>()
        coEvery { api.login(any()) } returns Response.success(
            UsuarioDto(1, "Admin", "admin@museo.cl", "Profesional", "ADMINISTRADOR", 3,
                token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.firmaDelServidor", expiraEn = "2026-11-24T22:30:00")
        )

        AuthRepository(demoraMs = 0, api = api).login("admin@museo.cl", "museo123").shouldBeInstanceOf<ResultadoLogin.Exito>()

        TokenSesion.hayToken() shouldBe true
        TokenSesion.horaExpiracion() shouldBe "22:30"
        TokenSesion.abreviado()!! shouldContain "…"
    }

    test("sin conexión no queda un token viejo guardado") {
        TokenSesion.guardar("token-anterior", null)
        val api = mockk<UsuariosApi>()
        coEvery { api.login(any()) } throws java.io.IOException("sin red")

        AuthRepository(demoraMs = 0, api = api).login("estudiante@museo.cl", "duoc2026")

        TokenSesion.hayToken() shouldBe false
        TokenSesion.horaExpiracion() shouldBe null
    }

    // ---------- validaciones de los formularios ----------

    test("un producto vacío marca nombre, descripción, precio, stock y peso") {
        val errores = ValidacionesGestion.validarProducto(FormProducto())
        listOf("nombre", "descripcion", "precio", "stock", "pesoKg").forEach { errores shouldContainKey it }
    }

    test("un curso pide semanas y no pide peso ni código") {
        val errores = ValidacionesGestion.validarProducto(
            FormProducto(nombre = "Curso", descripcion = "x", precio = "100", stock = "10", categoria = Categoria.CURSO, codigoBarra = "MAL")
        )
        errores shouldContainKey "semanas"
        errores shouldNotContainKey "pesoKg"
        errores shouldNotContainKey "codigoBarra"
    }

    test("el código de barra debe tener el formato MDM-000") {
        ValidacionesGestion.validarProducto(productoValido.copy(codigoBarra = "ABC")) shouldContainKey "codigoBarra"
        ValidacionesGestion.validarProducto(productoValido).shouldBeEmpty()
    }

    test("al enviar un curso no se manda peso ni código, y la coma decimal se acepta") {
        val curso = FormProducto(nombre = " Curso ", descripcion = "d", precio = "1000", stock = "20",
            categoria = Categoria.CURSO, semanas = "4", codigoBarra = "MDM-001", pesoKg = "2").aRequest()
        curso.pesoKg shouldBe 0.0
        curso.codigoBarra shouldBe null
        curso.semanas shouldBe 4
        curso.nombre shouldBe "Curso"
        productoValido.aRequest().pesoKg shouldBe 0.05
        productoValido.aRequest().codigoBarra shouldBe "MDM-099"
    }

    test("una ficha de museo revisa coordenadas, año y calificación") {
        val errores = ValidacionesGestion.validarMuseo(FormMuseo(nombre = "M", pais = "Chile", ciudad = "Santiago",
            descripcion = "d", latitud = "-95", longitud = "200", anio = "1990", calificacion = "6", resenas = "-1"))
        listOf("latitud", "longitud", "anio", "calificacion", "resenas").forEach { errores shouldContainKey it }
        ValidacionesGestion.validarMuseo(FormMuseo.desde(museo)).shouldBeEmpty()
    }

    test("editar copia los datos del producto y del museo al formulario") {
        FormProducto.desde(taza).aRequest().precio shouldBe 8_990
        FormMuseo.desde(museo).aRequest().latitud shouldBe -33.04
    }

    // ---------- Administrador: productos ----------

    test("crear un producto lo envía sin id y lo agrega a la lista") {
        val repo = mockk<GestionRepository>()
        coEvery { repo.guardarProducto(null, any()) } returns ResultadoApi.Exito(taza.copy(id = 13, nombre = "Lápiz"))
        val vm = GestionViewModel(repo)
        vm.nuevoProducto()
        vm.onProductoChange("nombre") { productoValido }
        var volvio = false

        vm.guardarProducto { volvio = true }

        volvio shouldBe true
        vm.estado.value.productos shouldHaveSize 1
        vm.estado.value.mensaje!! shouldContain "se agregó"
        coVerify { repo.guardarProducto(null, match { it.codigoBarra == "MDM-099" }) }
    }

    test("con errores en el formulario no se llama al servidor") {
        val repo = mockk<GestionRepository>()
        val vm = GestionViewModel(repo)
        vm.guardarProducto()
        vm.estado.value.errores shouldContainKey "nombre"
        coVerify(exactly = 0) { repo.guardarProducto(any(), any()) }
    }

    test("editar un producto lo reemplaza en la lista") {
        val repo = mockk<GestionRepository>()
        coEvery { repo.productos() } returns ResultadoApi.Exito(listOf(taza))
        coEvery { repo.guardarProducto(1, any()) } returns ResultadoApi.Exito(taza.copy(precio = 9_990))
        val vm = GestionViewModel(repo)
        vm.cargarProductos()
        vm.editarProducto(taza)

        vm.guardarProducto()

        vm.estado.value.productos.single().precio shouldBe 9_990
        vm.estado.value.mensaje!! shouldContain "se actualizó"
    }

    test("si el rol no tiene permiso se muestra el 403 del backend") {
        val repo = mockk<GestionRepository>()
        coEvery { repo.eliminarProducto(1) } returns ResultadoApi.ErrorServidor(403, "Tu rol no tiene permiso para esta acción")
        val vm = GestionViewModel(repo)

        vm.eliminarProducto(taza)

        vm.estado.value.mensaje shouldBe "Tu rol no tiene permiso para esta acción"
    }

    test("sin token el backend responde 401 y se pide volver a iniciar sesión") {
        val repo = mockk<GestionRepository>()
        coEvery { repo.productos() } returns ResultadoApi.ErrorServidor(401, "Falta el token JWT")
        val vm = GestionViewModel(repo)

        vm.cargarProductos()

        vm.estado.value.mensaje!! shouldContain "Cierra sesión"
        vm.estado.value.cargando shouldBe false
    }

    test("los errores de campo del backend (400) quedan en el formulario") {
        val repo = mockk<GestionRepository>()
        coEvery { repo.guardarProducto(any(), any()) } returns
            ResultadoApi.ErrorServidor(400, "Hay datos inválidos", mapOf("codigoBarra" to "El código debe tener el formato MDM-000"))
        val vm = GestionViewModel(repo)
        vm.onProductoChange("nombre") { productoValido }

        vm.guardarProducto()

        vm.estado.value.errores["codigoBarra"] shouldBe "El código debe tener el formato MDM-000"
    }

    test("eliminar un producto lo quita de la lista") {
        val repo = mockk<GestionRepository>()
        coEvery { repo.productos() } returns ResultadoApi.Exito(listOf(taza))
        coEvery { repo.eliminarProducto(1) } returns ResultadoApi.Exito(Unit)
        val vm = GestionViewModel(repo)
        vm.cargarProductos()

        vm.eliminarProducto(taza)

        vm.estado.value.productos shouldHaveSize 0
    }

    // ---------- Investigador: fichas de museos ----------

    test("registrar y corregir una ficha de museo") {
        val repo = mockk<GestionRepository>()
        coEvery { repo.museos() } returns ResultadoApi.Exito(listOf(museo))
        coEvery { repo.guardarMuseo(3, any()) } returns ResultadoApi.Exito(museo.copy(ciudad = "Viña del Mar"))
        coEvery { repo.guardarMuseo(null, any()) } returns ResultadoApi.Exito(museo.copy(id = 15, nombre = "Museo Nuevo"))
        val vm = GestionViewModel(repo)
        vm.cargarMuseos()

        vm.editarMuseo(museo)
        vm.onMuseoChange("ciudad") { it.copy(ciudad = "Viña del Mar") }
        vm.guardarMuseo()
        vm.estado.value.museos.single().ciudad shouldBe "Viña del Mar"

        vm.nuevoMuseo()
        vm.onMuseoChange("nombre") { FormMuseo.desde(museo).copy(id = null, nombre = "Museo Nuevo") }
        vm.guardarMuseo()
        vm.estado.value.museos shouldHaveSize 2
        vm.estado.value.mensaje!! shouldContain "registrada"
    }

    test("una ficha inválida no se envía y eliminar la quita") {
        val repo = mockk<GestionRepository>()
        coEvery { repo.museos() } returns ResultadoApi.Exito(listOf(museo))
        coEvery { repo.eliminarMuseo(3) } returns ResultadoApi.Exito(Unit)
        val vm = GestionViewModel(repo)
        vm.guardarMuseo()
        vm.estado.value.errores shouldContainKey "nombre"

        vm.cargarMuseos()
        vm.eliminarMuseo(museo)
        vm.estado.value.museos shouldHaveSize 0
    }

    // ---------- Administrador: solicitudes ----------

    test("el administrador ve todas las solicitudes, filtra y avanza el estado") {
        val repo = mockk<GestionRepository>()
        coEvery { repo.pedidos() } returns ResultadoApi.Exito(listOf(pedido("PENDIENTE"), pedido("ENTREGADA").copy(id = 8)))
        coEvery { repo.avanzarPedido(7) } returns ResultadoApi.Exito(pedido("CONFIRMADA"))
        val vm = GestionViewModel(repo)

        vm.cargarPedidos()
        vm.estado.value.pendientes shouldBe 1
        vm.filtrarPedidos("PENDIENTE")
        vm.estado.value.pedidosFiltrados shouldHaveSize 1

        vm.avanzarPedido(7)
        vm.estado.value.pedidos.first().estado shouldBe "CONFIRMADA"
        vm.estado.value.mensaje!! shouldContain "confirmada"
        vm.limpiarMensaje()
        vm.estado.value.mensaje shouldBe null
    }

    test("sin conexión avisa que no se guardaron los cambios") {
        val repo = mockk<GestionRepository>()
        coEvery { repo.avanzarPedido(any()) } returns ResultadoApi.SinConexion()
        val vm = GestionViewModel(repo)
        vm.avanzarPedido(7)
        vm.estado.value.mensaje!! shouldContain "Sin conexión"
    }

    test("el repositorio usa POST para crear y PUT para editar") {
        val catalogo = mockk<CatalogoApi>()
        val pedidos = mockk<PedidosApi>()
        coEvery { catalogo.crearProducto(any()) } returns Response.success(taza)
        coEvery { catalogo.actualizarProducto(1, any()) } returns Response.success(taza)
        coEvery { catalogo.crearMuseo(any()) } returns Response.success(museo)
        coEvery { catalogo.actualizarMuseo(3, any()) } returns Response.success(museo)
        coEvery { catalogo.productos() } returns Response.success(listOf(taza))
        coEvery { catalogo.museos() } returns Response.success(listOf(museo))
        coEvery { catalogo.eliminarProducto(1) } returns Response.success(Unit)
        coEvery { catalogo.eliminarMuseo(3) } returns Response.success(Unit)
        coEvery { pedidos.listarTodos() } returns Response.error(403, """{"estado":403,"mensaje":"Sin permiso"}""".toResponseBody(json))
        coEvery { pedidos.avanzar(7) } returns Response.success(pedido("CONFIRMADA"))
        val repo = GestionRepository(catalogo, pedidos)
        val datos = productoValido.aRequest()
        val ficha = FormMuseo.desde(museo).aRequest()

        repo.guardarProducto(null, datos).shouldBeInstanceOf<ResultadoApi.Exito<ProductoDto>>()
        repo.guardarProducto(1, datos).shouldBeInstanceOf<ResultadoApi.Exito<ProductoDto>>()
        repo.guardarMuseo(null, ficha).shouldBeInstanceOf<ResultadoApi.Exito<MuseoDto>>()
        repo.guardarMuseo(3, ficha).shouldBeInstanceOf<ResultadoApi.Exito<MuseoDto>>()
        repo.productos().shouldBeInstanceOf<ResultadoApi.Exito<List<ProductoDto>>>()
        repo.museos().shouldBeInstanceOf<ResultadoApi.Exito<List<MuseoDto>>>()
        repo.eliminarProducto(1).shouldBeInstanceOf<ResultadoApi.Exito<Unit>>()
        repo.eliminarMuseo(3).shouldBeInstanceOf<ResultadoApi.Exito<Unit>>()
        (repo.pedidos() as ResultadoApi.ErrorServidor).codigo shouldBe 403
        repo.avanzarPedido(7).shouldBeInstanceOf<ResultadoApi.Exito<PedidoDto>>()
        coVerify { catalogo.crearProducto(any()); catalogo.actualizarProducto(1, any()); catalogo.crearMuseo(any()); catalogo.actualizarMuseo(3, any()) }
    }
})
