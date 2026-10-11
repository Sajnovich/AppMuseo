package com.example.appmuseo.repository

import com.example.appmuseo.model.Museo

/**
 * "API simulada de geolocalización": entrega la matriz de coordenadas de los
 * museos virtuales. Nombres y descripciones son SINTÉTICOS; las coordenadas
 * son las de la ciudad, para que el mapa los ubique correctamente.
 */
class MuseoRepository {

    private val museos = listOf(
        Museo(1, "Museo Virtual de Arte Precolombino", "Chile", "Santiago", "Arte",
            "Colección digital de textiles y cerámica andina en 3D.", -33.44, -70.65, 2026),
        Museo(2, "Museo Virtual del Puerto", "Chile", "Valparaíso", "Historia",
            "Recorrido inmersivo por la historia portuaria y sus ascensores.", -33.05, -71.62, 2025),
        Museo(3, "Museo Virtual de la Pampa", "Argentina", "Buenos Aires", "Historia",
            "Archivo fotográfico digitalizado de la vida rural.", -34.60, -58.38, 2025),
        Museo(4, "Museo Virtual del Río de la Plata", "Uruguay", "Montevideo", "Arte",
            "Galería de arte contemporáneo con visitas guiadas en línea.", -34.90, -56.16, 2026),
        Museo(5, "Museo Virtual Andino", "Perú", "Lima", "Arqueología",
            "Modelos 3D de sitios arqueológicos de la costa y la sierra.", -12.05, -77.04, 2025),
        Museo(6, "Museo Virtual del Altiplano", "Bolivia", "La Paz", "Etnografía",
            "Saberes y fiestas del altiplano en formato de archivo sonoro.", -16.50, -68.15, 2026),
        Museo(7, "Museo Virtual del Café", "Colombia", "Bogotá", "Etnografía",
            "La cultura cafetera contada por sus comunidades.", 4.71, -74.07, 2025),
        Museo(8, "Museo Virtual de la Amazonía", "Brasil", "Manaos", "Ciencias naturales",
            "Biodiversidad amazónica con colecciones científicas digitalizadas.", -3.12, -60.02, 2026),
        Museo(9, "Museo Virtual de Arte Moderno", "Brasil", "São Paulo", "Arte",
            "Exposiciones temporales de arte moderno latinoamericano.", -23.55, -46.63, 2025),
        Museo(10, "Museo Virtual Mesoamericano", "México", "Ciudad de México", "Arqueología",
            "Reconstrucciones digitales de ciudades mesoamericanas.", 19.43, -99.13, 2026),
        Museo(11, "Museo Virtual del Caribe", "Cuba", "La Habana", "Historia",
            "Historia marítima y cultural del Caribe.", 23.11, -82.37, 2025),
        Museo(12, "Museo Virtual de Ciencias del Pacífico", "Costa Rica", "San José", "Ciencia",
            "Laboratorios virtuales sobre volcanes y océanos.", 9.93, -84.09, 2026),
        Museo(13, "Museo Virtual de Historia Ibérica", "España", "Madrid", "Historia",
            "Vínculos históricos entre la península y América.", 40.42, -3.70, 2025),
        Museo(14, "Museo Virtual de la Navegación", "Portugal", "Lisboa", "Historia",
            "Cartografía histórica y rutas de navegación atlánticas.", 38.72, -9.14, 2026)
    )

    fun obtenerMuseos(): List<Museo> = museos
}
