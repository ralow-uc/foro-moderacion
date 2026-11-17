package com.foro.moderacion.service.impl;

import com.foro.moderacion.cliente.ContenidoModerado;
import com.foro.moderacion.dto.LevantamientoRequest;
import com.foro.moderacion.dto.SancionRequest;
import com.foro.moderacion.dto.SancionResponse;
import com.foro.moderacion.estrategia.EstrategiaDeModeracion;
import com.foro.moderacion.estrategia.FabricaDeEstrategiasDeModeracion;
import com.foro.moderacion.exception.EstadoInvalidoException;
import com.foro.moderacion.exception.RecursoNoEncontradoException;
import com.foro.moderacion.model.Norma;
import com.foro.moderacion.model.Reporte;
import com.foro.moderacion.model.Sancion;
import com.foro.moderacion.model.TipoContenido;
import com.foro.moderacion.repository.NormaRepository;
import com.foro.moderacion.repository.ReporteRepository;
import com.foro.moderacion.repository.SancionRepository;
import com.foro.moderacion.security.UsuarioAutenticado;
import com.foro.moderacion.service.ReporteService;
import com.foro.moderacion.service.SancionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logica de negocio de las sanciones.
 *
 * Orden de una sancion:
 *   1. validar todo lo local (norma activa, reporte coherente, sin sancion vigente);
 *   2. pedir el baneo a foro-temas con la estrategia del tipo de contenido;
 *   3. registrar la sancion y aceptar los reportes pendientes.
 *
 * Se valida antes de llamar a foro-temas para no banear algo que despues no se
 * puede registrar. Si aun asi falla el guardado, se compensa restituyendo el
 * contenido, para que las dos bases no queden en desacuerdo.
 */
@Service
public class SancionServiceImpl implements SancionService {

    private static final Logger log = LoggerFactory.getLogger(SancionServiceImpl.class);
    private static final int LARGO_MAXIMO_MOTIVO_EN_FORO = 500;

    private final SancionRepository sancionRepository;
    private final NormaRepository normaRepository;
    private final ReporteRepository reporteRepository;
    private final ReporteService reporteService;
    private final FabricaDeEstrategiasDeModeracion fabrica;

    public SancionServiceImpl(SancionRepository sancionRepository,
                              NormaRepository normaRepository,
                              ReporteRepository reporteRepository,
                              ReporteService reporteService,
                              FabricaDeEstrategiasDeModeracion fabrica) {
        this.sancionRepository = sancionRepository;
        this.normaRepository = normaRepository;
        this.reporteRepository = reporteRepository;
        this.reporteService = reporteService;
        this.fabrica = fabrica;
    }

    @Override
    @Transactional
    public SancionResponse sancionar(SancionRequest solicitud, UsuarioAutenticado moderador,
                                     String autorizacion) {
        TipoContenido tipo = solicitud.tipoContenido();
        Long contenidoId = solicitud.contenidoId();

        Norma norma = normaRepository.findById(solicitud.normaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("la norma", solicitud.normaId()));
        if (!norma.isActivo()) {
            throw new EstadoInvalidoException("La norma " + norma.getCodigo() + " no esta vigente");
        }
        Reporte reporte = validarReporte(solicitud);
        if (sancionRepository.existsByTipoContenidoAndContenidoIdAndVigenteTrue(tipo, contenidoId)) {
            throw new EstadoInvalidoException("El " + tipo.name().toLowerCase() + " " + contenidoId
                    + " ya tiene una sancion vigente");
        }

        String motivo = solicitud.motivo().trim();
        EstrategiaDeModeracion estrategia = fabrica.para(tipo);
        ContenidoModerado contenido = estrategia.banear(contenidoId, motivoParaElForo(norma, motivo), autorizacion);

        try {
            Sancion sancion = new Sancion(tipo, contenidoId, norma, reporte, motivo,
                    moderador.id(), moderador.nombreUsuario());
            sancion.registrarContenido(contenido.titulo(), contenido.autorId(), contenido.autorNombre());
            Sancion guardada = sancionRepository.save(sancion);
            int aceptados = reporteService.aceptarPendientesSobre(tipo, contenidoId, moderador);

            log.info("Sancion {}: {} {} baneado por {} ({}), {} reporte(s) aceptado(s)", guardada.getId(),
                    tipo, contenidoId, moderador.nombreUsuario(), norma.getCodigo(), aceptados);
            return SancionResponse.desde(guardada, aceptados);
        } catch (RuntimeException e) {
            log.error("No se pudo registrar la sancion de {} {}; se restituye el contenido", tipo, contenidoId);
            estrategia.restituir(contenidoId, autorizacion);
            throw e;
        }
    }

    @Override
    @Transactional
    public SancionResponse levantar(Long id, LevantamientoRequest solicitud,
                                    UsuarioAutenticado administrador, String autorizacion) {
        Sancion sancion = buscarOFallar(id);
        if (!sancion.isVigente()) {
            throw new EstadoInvalidoException("La sancion " + id + " ya fue levantada");
        }

        fabrica.para(sancion.getTipoContenido()).restituir(sancion.getContenidoId(), autorizacion);
        sancion.levantar(administrador.nombreUsuario(), solicitud.motivo().trim());

        log.info("Sancion {} levantada por {}", id, administrador.nombreUsuario());
        return SancionResponse.desde(sancionRepository.save(sancion));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SancionResponse> listar(Boolean vigente, TipoContenido tipo) {
        return sancionRepository.buscar(vigente, tipo).stream().map(SancionResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SancionResponse obtenerPorId(Long id) {
        return SancionResponse.desde(buscarOFallar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SancionResponse> listarPorAutor(Long autorId) {
        return sancionRepository.findByAutorIdOrderByFechaCreacionDesc(autorId).stream()
                .map(SancionResponse::desde).toList();
    }

    /** Si la sancion cita un reporte, tiene que estar pendiente y ser sobre el mismo contenido. */
    private Reporte validarReporte(SancionRequest solicitud) {
        if (solicitud.reporteId() == null) {
            return null;
        }
        Reporte reporte = reporteRepository.findById(solicitud.reporteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("el reporte", solicitud.reporteId()));
        if (reporte.getTipoContenido() != solicitud.tipoContenido()
                || !reporte.getContenidoId().equals(solicitud.contenidoId())) {
            throw new EstadoInvalidoException("El reporte " + reporte.getId()
                    + " no corresponde a ese contenido");
        }
        if (!reporte.estaPendiente()) {
            throw new EstadoInvalidoException("El reporte " + reporte.getId() + " ya fue resuelto");
        }
        return reporte;
    }

    /** foro-temas guarda el motivo junto al contenido: se le antepone la norma citada. */
    private String motivoParaElForo(Norma norma, String motivo) {
        String completo = norma.getCodigo() + " - " + norma.getNombre() + ": " + motivo;
        return completo.length() > LARGO_MAXIMO_MOTIVO_EN_FORO
                ? completo.substring(0, LARGO_MAXIMO_MOTIVO_EN_FORO)
                : completo;
    }

    private Sancion buscarOFallar(Long id) {
        return sancionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("la sancion", id));
    }
}
