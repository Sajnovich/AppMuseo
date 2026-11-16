package com.example.appmuseo.repository

import com.example.appmuseo.data.remote.CatalogoApi
import com.example.appmuseo.data.remote.Red
import com.example.appmuseo.data.remote.ResultadoApi
import com.example.appmuseo.data.remote.aModelo
import com.example.appmuseo.model.Museo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Los museos vienen de ms-catalogo (Retrofit). Mientras no responde, se usa la copia local,
// así la app funciona igual sin conexión
class MuseoRepository(private val api: CatalogoApi? = null) {

    companion object {
        // datos del caso guardados en la app (respaldo sin conexión)
        val MUSEOS_LOCALES = listOf(
            Museo(1, "Museo Virtual de Arte Precolombino", "Chile", "Santiago", "Arte",
                "Colección digital de textiles y cerámica andina en 3D.", -33.44, -70.65, 2026, 4.8, 132,
                "Recorre salas virtuales con piezas de las culturas Diaguita, Mapuche e Inca. Cada objeto se puede girar en 3D y trae una ficha con su origen, material y época."),
            Museo(2, "Museo Virtual del Puerto", "Chile", "Valparaíso", "Historia",
                "Recorrido inmersivo por la historia portuaria y sus ascensores.", -33.05, -71.62, 2025, 4.6, 98,
                "Fotografías restauradas, planos antiguos y relatos de trabajadores del puerto. Incluye un recorrido 360° por los ascensores patrimoniales."),
            Museo(3, "Museo Virtual de la Pampa", "Argentina", "Buenos Aires", "Historia",
                "Archivo fotográfico digitalizado de la vida rural.", -34.60, -58.38, 2025, 4.4, 76,
                "Más de dos mil fotografías de estancias, oficios y fiestas rurales, ordenadas por década y con buscador por lugar."),
            Museo(4, "Museo Virtual del Río de la Plata", "Uruguay", "Montevideo", "Arte",
                "Galería de arte contemporáneo con visitas guiadas en línea.", -34.90, -56.16, 2026, 4.5, 64,
                "Exposiciones de artistas rioplatenses con visitas guiadas en vivo cada semana y material educativo descargable."),
            Museo(5, "Museo Virtual Andino", "Perú", "Lima", "Arqueología",
                "Modelos 3D de sitios arqueológicos de la costa y la sierra.", -12.05, -77.04, 2025, 4.9, 210,
                "Sitios como Caral y Chan Chan reconstruidos en 3D, con capas que muestran cómo eran en su época de mayor esplendor."),
            Museo(6, "Museo Virtual del Altiplano", "Bolivia", "La Paz", "Etnografía",
                "Saberes y fiestas del altiplano en formato de archivo sonoro.", -16.50, -68.15, 2026, 4.3, 41,
                "Grabaciones de música, cantos y testimonios aymaras y quechuas, acompañadas de fotografías y transcripciones."),
            Museo(7, "Museo Virtual del Café", "Colombia", "Bogotá", "Etnografía",
                "La cultura cafetera contada por sus comunidades.", 4.71, -74.07, 2025, 4.7, 120,
                "El proceso del café desde la semilla hasta la taza, contado por familias caficultoras. Incluye mapas de las regiones productoras."),
            Museo(8, "Museo Virtual de la Amazonía", "Brasil", "Manaos", "Ciencias naturales",
                "Biodiversidad amazónica con colecciones científicas digitalizadas.", -3.12, -60.02, 2026, 4.8, 155,
                "Ejemplares de flora y fauna digitalizados en alta resolución, con fichas científicas y sonidos de la selva."),
            Museo(9, "Museo Virtual de Arte Moderno", "Brasil", "São Paulo", "Arte",
                "Exposiciones temporales de arte moderno latinoamericano.", -23.55, -46.63, 2025, 4.5, 88,
                "Obras del modernismo latinoamericano con audioguías en español y portugués."),
            Museo(10, "Museo Virtual Mesoamericano", "México", "Ciudad de México", "Arqueología",
                "Reconstrucciones digitales de ciudades mesoamericanas.", 19.43, -99.13, 2026, 4.9, 240,
                "Tenochtitlan y Teotihuacan reconstruidas digitalmente, con recorridos guiados por sus templos y mercados."),
            Museo(11, "Museo Virtual del Caribe", "Cuba", "La Habana", "Historia",
                "Historia marítima y cultural del Caribe.", 23.11, -82.37, 2025, 4.2, 37,
                "Rutas comerciales, fortificaciones y música del Caribe en una línea de tiempo interactiva."),
            Museo(12, "Museo Virtual de Ciencias del Pacífico", "Costa Rica", "San José", "Ciencia",
                "Laboratorios virtuales sobre volcanes y océanos.", 9.93, -84.09, 2026, 4.6, 59,
                "Simuladores de erupciones y corrientes oceánicas pensados para colegios, con guías para profesores."),
            Museo(13, "Museo Virtual de Historia Ibérica", "España", "Madrid", "Historia",
                "Vínculos históricos entre la península y América.", 40.42, -3.70, 2025, 4.4, 71,
                "Documentos, mapas y objetos que muestran el intercambio entre la península ibérica y América."),
            Museo(14, "Museo Virtual de la Navegación", "Portugal", "Lisboa", "Historia",
                "Cartografía histórica y rutas de navegación atlánticas.", 38.72, -9.14, 2026, 4.5, 66,
                "Cartas náuticas digitalizadas e instrumentos de navegación que se pueden explorar en detalle.")
        )

        // copia compartida por todas las pantallas: al sincronizar una, se actualizan todas
        private val cache = MutableStateFlow(MUSEOS_LOCALES)

        @Volatile
        var desdeServidor: Boolean = false
            private set

        // vuelve a la copia local (se usa en las pruebas para que no se afecten entre sí)
        fun restaurarDatosLocales() {
            cache.value = MUSEOS_LOCALES
            desdeServidor = false
        }
    }

    val museosFlow: StateFlow<List<Museo>> get() = cache

    fun obtenerMuseos(): List<Museo> = cache.value

    fun buscarPorId(id: Int): Museo? = cache.value.find { it.id == id }

    // GET /api/museos en ms-catalogo
    suspend fun sincronizar(): ResultadoApi<List<Museo>> {
        val servicio = api ?: return ResultadoApi.SinConexion("Sin servidor configurado")
        return when (val respuesta = Red.llamar { servicio.museos() }) {
            is ResultadoApi.Exito -> {
                val lista = respuesta.datos.map { it.aModelo() }
                if (lista.isNotEmpty()) {
                    cache.value = lista
                    desdeServidor = true
                }
                ResultadoApi.Exito(lista)
            }
            is ResultadoApi.ErrorServidor -> respuesta
            is ResultadoApi.SinConexion -> respuesta
        }
    }

    // "MUSEO-5" -> museo 5
    fun buscarPorCodigo(codigo: String): Museo? {
        val id = codigo.trim().uppercase().removePrefix("MUSEO-").toIntOrNull() ?: return null
        return buscarPorId(id)
    }
}
