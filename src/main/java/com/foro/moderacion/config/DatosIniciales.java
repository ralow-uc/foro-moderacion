package com.foro.moderacion.config;

import com.foro.moderacion.model.EstadoReporte;
import com.foro.moderacion.model.Gravedad;
import com.foro.moderacion.model.Norma;
import com.foro.moderacion.model.Reporte;
import com.foro.moderacion.model.Sancion;
import com.foro.moderacion.model.TipoContenido;
import com.foro.moderacion.repository.NormaRepository;
import com.foro.moderacion.repository.ReporteRepository;
import com.foro.moderacion.repository.SancionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Carga las normas, reportes y sanciones de ejemplo al iniciar con el perfil
 * "local" (H2 en memoria). Son los mismos datos que scripts/02_datos_iniciales.sql
 * siembra en Oracle, de modo que la demostracion sea igual en los dos perfiles.
 *
 * Los ID de contenido y de usuarios corresponden a lo que siembran foro-temas y
 * foro-usuarios en sus propios perfiles locales.
 */
@Component
@Profile("local")
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final NormaRepository normaRepository;
    private final ReporteRepository reporteRepository;
    private final SancionRepository sancionRepository;

    public DatosIniciales(NormaRepository normaRepository, ReporteRepository reporteRepository,
                          SancionRepository sancionRepository) {
        this.normaRepository = normaRepository;
        this.reporteRepository = reporteRepository;
        this.sancionRepository = sancionRepository;
    }

    @Override
    public void run(String... args) {
        if (normaRepository.count() > 0) {
            return;
        }

        Norma respeto = norma("N1", "Respeto entre cuberos",
                "Trata a los demás con respeto. Nada de insultos, burlas por tiempos lentos ni ataques personales.",
                Gravedad.GRAVE);
        Norma spam = norma("N2", "Sin spam ni publicidad",
                "No se permite publicidad de tiendas, enlaces de afiliados ni mensajes repetidos.", Gravedad.GRAVE);
        Norma categoria = norma("N3", "Contenido en la categoría correcta",
                "Publica en la categoría que corresponde y busca antes de repetir una pregunta.", Gravedad.LEVE);
        Norma datos = norma("N4", "Sin información personal",
                "No publiques teléfonos, direcciones ni datos personales tuyos o de otras personas.", Gravedad.GRAVE);
        norma("N5", "Sin contenido ofensivo",
                "Prohibido el contenido discriminatorio, violento o sexual.", Gravedad.CRITICA);
        norma("N6", "Sin piratería ni contenido ilegal",
                "No compartas copias ilegales de software de cronometraje, videos o material con derechos.",
                Gravedad.CRITICA);

        reporteRepository.save(new Reporte(TipoContenido.TEMA, 2L, categoria, 3L, "feliks",
                "Esta pregunta ya se hizo varias veces este mes."));
        reporteRepository.save(new Reporte(TipoContenido.COMENTARIO, 12L, respeto, 3L, "feliks",
                "El comentario desacredita la reseña sin argumentos."));
        descartado(new Reporte(TipoContenido.TEMA, 9L, spam, 5L, "tymon",
                "Parece que promociona una marca de lubricante."));
        descartado(new Reporte(TipoContenido.COMENTARIO, 9L, datos, 5L, "tymon",
                "Pide datos para contactarlo."));

        levantada(new Sancion(TipoContenido.TEMA, 8L, spam, null,
                        "Parecía publicidad encubierta de una tienda.", 2L, "moderadora"),
                "Reseña del GAN 14 con MagLev", 3L, "feliks",
                "Apelación aceptada: es una reseña personal, no publicidad.");
        levantada(new Sancion(TipoContenido.COMENTARIO, 6L, categoria, null,
                        "Comentario fuera del tema de la publicación.", 2L, "moderadora"),
                "Lo de no mezclar simétricos el mismo día es el mejor consejo del post.", 4L, "yiheng",
                "Revisado: el comentario sí aporta al tema.");
        levantada(new Sancion(TipoContenido.COMENTARIO, 13L, spam, null,
                        "Mensaje repetido en varios temas.", 1L, "admin"),
                "Coincido. En las piezas casi no se nota después del primer día.", 3L, "feliks",
                "Era un único mensaje; el duplicado fue un error de la aplicación.");

        log.info("Datos de ejemplo cargados: {} normas, {} reportes, {} sanciones",
                normaRepository.count(), reporteRepository.count(), sancionRepository.count());
    }

    private Norma norma(String codigo, String nombre, String descripcion, Gravedad gravedad) {
        return normaRepository.save(new Norma(codigo, nombre, descripcion, gravedad));
    }

    private void descartado(Reporte reporte) {
        reporte.resolver(EstadoReporte.DESCARTADO, "moderadora");
        reporteRepository.save(reporte);
    }

    private void levantada(Sancion sancion, String titulo, Long autorId, String autorNombre, String motivo) {
        sancion.registrarContenido(titulo, autorId, autorNombre);
        sancion.levantar("admin", motivo);
        sancionRepository.save(sancion);
    }
}
