package com.example.appmuseo.remoto

import com.example.appmuseo.data.remote.ResultadoApi
import com.example.appmuseo.data.remote.dto.MuseoDto
import com.example.appmuseo.data.remote.dto.ProductoDto
import com.example.appmuseo.model.CambioImagen
import com.example.appmuseo.model.FormProducto
import com.example.appmuseo.repository.GestionRepository
import com.example.appmuseo.utils.AlmacenImagenes
import com.example.appmuseo.utils.ImagenGuardada
import com.example.appmuseo.utils.ImagenesCatalogo
import com.example.appmuseo.utils.claveMuseo
import com.example.appmuseo.utils.claveProducto
import com.example.appmuseo.utils.tieneImagen
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.just
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import com.example.appmuseo.viewmodel.GestionViewModel

// Cambiar, quitar o agregar la imagen de un producto o museo desde los formularios de gestión
@OptIn(ExperimentalCoroutinesApi::class)
class ImagenesGestionSpec : FunSpec({

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    val taza = ProductoDto(1, "Taza", "Taza de cerámica", 8_990, "MERCH", 25, 0.4, "MDM-001", null, null, false)
    val museo = MuseoDto(3, "Museo del Puerto", "Chile", "Valparaíso", "Historia", "Recorrido virtual", "", -33.04, -71.62, 2025, 4.6, 98)
    val productoValido = FormProducto(nombre = "Lápiz", descripcion = "Lápiz con logo", precio = "1990", stock = "50", pesoKg = "0,05")

    fun almacen() = mockk<AlmacenImagenes>().also {
        coEvery { it.guardar(any(), any()) } just Runs
        coEvery { it.quitar(any()) } just Runs
        coEvery { it.olvidar(any()) } just Runs
    }

    test("las claves separan productos de museos con el mismo id") {
        claveProducto(5) shouldBe "producto_5"
        claveMuseo(5L) shouldBe "museo_5"
    }

    test("la carpeta se lee como imagen propia, quitada o se ignora") {
        val mapa = ImagenesCatalogo.leerNombres(
            listOf("producto_5_1764000000.jpg", "producto_12.quitada", "museo_3_99.jpg", "notas.txt"),
            "/datos/catalogo"
        )
        mapa["producto_5"] shouldBe ImagenGuardada.Propia("/datos/catalogo/producto_5_1764000000.jpg")
        mapa["producto_12"] shouldBe ImagenGuardada.Quitada
        mapa["museo_3"] shouldBe ImagenGuardada.Propia("/datos/catalogo/museo_3_99.jpg")
        mapa.size shouldBe 3
    }

    test("tiene imagen si es propia o trae ilustración, y no si se quitó") {
        tieneImagen(ImagenGuardada.Propia("/a.jpg"), traePorDefecto = false) shouldBe true
        tieneImagen(null, traePorDefecto = true) shouldBe true
        tieneImagen(null, traePorDefecto = false) shouldBe false
        tieneImagen(ImagenGuardada.Quitada, traePorDefecto = true) shouldBe false
    }

    test("al editar un producto con foto nueva, se guarda con la clave del producto") {
        val repo = mockk<GestionRepository>()
        val imagenes = almacen()
        coEvery { repo.guardarProducto(1, any()) } returns ResultadoApi.Exito(taza)
        val vm = GestionViewModel(repo, imagenes)
        vm.editarProducto(taza)
        vm.onProductoChange("imagen") { it.copy(imagen = CambioImagen.Nueva("content://galeria/7")) }

        vm.guardarProducto()

        coVerify { imagenes.guardar("producto_1", "content://galeria/7") }
    }

    test("un producto nuevo recibe la imagen con el id que entrega el backend") {
        val repo = mockk<GestionRepository>()
        val imagenes = almacen()
        coEvery { repo.guardarProducto(null, any()) } returns ResultadoApi.Exito(taza.copy(id = 13, nombre = "Lápiz"))
        val vm = GestionViewModel(repo, imagenes)
        vm.nuevoProducto()
        vm.onProductoChange("nombre") { productoValido.copy(imagen = CambioImagen.Nueva("content://camara/1")) }

        vm.guardarProducto()

        coVerify { imagenes.guardar("producto_13", "content://camara/1") }
    }

    test("quitar la imagen de una ficha de museo") {
        val repo = mockk<GestionRepository>()
        val imagenes = almacen()
        coEvery { repo.guardarMuseo(3, any()) } returns ResultadoApi.Exito(museo)
        val vm = GestionViewModel(repo, imagenes)
        vm.editarMuseo(museo)
        vm.onMuseoChange("imagen") { it.copy(imagen = CambioImagen.Quitar) }

        vm.guardarMuseo()

        coVerify { imagenes.quitar("museo_3") }
    }

    test("sin cambios en la imagen no se toca el almacenamiento") {
        val repo = mockk<GestionRepository>()
        val imagenes = almacen()
        coEvery { repo.guardarProducto(1, any()) } returns ResultadoApi.Exito(taza)
        val vm = GestionViewModel(repo, imagenes)
        vm.editarProducto(taza)

        vm.guardarProducto()

        coVerify(exactly = 0) { imagenes.guardar(any(), any()); imagenes.quitar(any()) }
    }

    test("si el backend rechaza el cambio (403) la imagen no se guarda") {
        val repo = mockk<GestionRepository>()
        val imagenes = almacen()
        coEvery { repo.guardarProducto(1, any()) } returns ResultadoApi.ErrorServidor(403, "Sin permiso")
        val vm = GestionViewModel(repo, imagenes)
        vm.editarProducto(taza)
        vm.onProductoChange("imagen") { it.copy(imagen = CambioImagen.Nueva("content://galeria/7")) }

        vm.guardarProducto()

        coVerify(exactly = 0) { imagenes.guardar(any(), any()) }
    }

    test("al eliminar un producto o museo se borra también su imagen") {
        val repo = mockk<GestionRepository>()
        val imagenes = almacen()
        coEvery { repo.eliminarProducto(1) } returns ResultadoApi.Exito(Unit)
        coEvery { repo.eliminarMuseo(3) } returns ResultadoApi.Exito(Unit)
        val vm = GestionViewModel(repo, imagenes)

        vm.eliminarProducto(taza)
        vm.eliminarMuseo(museo)

        coVerify { imagenes.olvidar("producto_1"); imagenes.olvidar("museo_3") }
    }
})
