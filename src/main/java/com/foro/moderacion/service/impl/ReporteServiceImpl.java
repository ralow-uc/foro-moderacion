package com.foro.moderacion.service.impl;

import com.foro.moderacion.dto.ReporteRequest;
import com.foro.moderacion.dto.ReporteResponse;
import com.foro.moderacion.exception.DatosDuplicadosException;
import com.foro.moderacion.exception.EstadoInvalidoException;
import com.foro.moderacion.exception.RecursoNoEncontradoException;
import com.foro.moderacion.model.EstadoReporte;
import com.foro.moderacion.model.Norma;
import com.foro.moderacion.model.Reporte;
import com.foro.moderacion.model.TipoContenido;
import com.foro.moderacion.repository.NormaRepository;
import com.foro.moderacion.repository.ReporteRepository;
import com.foro.moderacion.security.UsuarioAutenticado;
import com.foro.moderacion.service.ReporteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reglas de negocio de los reportes:
 *   - solo se puede citar una norma activa;
 *   - un usuario no repite un reporte pendiente sobre el mismo contenido;
 *   - un reporte ya resuelto no se vuelve a resolver.
 */
@Service
public class ReporteServiceImpl implements ReporteService {

    private static final Logger log = LoggerFactory.getLogger(ReporteServiceImpl.class);

    private final ReporteRepository reporteRepository;
    private final NormaRepository normaRepository;

    public ReporteServiceImpl(ReporteRepository reporteRepository, NormaRepository normaRepository) {
        this.reporteRepository = reporteRepository;
        this.normaRepository = normaRepository;
    }

    @Override
    @Transactional
    public ReporteResponse reportar(ReporteRequest solicitud, UsuarioAutenticado reportante) {
        Norma norma = normaRepository.findById(solicitud.normaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("la norma", solicitud.normaId()));
        if (!norma.isActivo()) {
            throw new EstadoInvalidoException("La norma " + norma.getCodigo() + " no esta vigente");
        }
        if (reporteRepository.existsByTipoContenidoAndContenidoIdAndReportanteIdAndEstado(
                solicitud.tipoContenido(), solicitud.contenidoId(), reportante.id(), EstadoReporte.PENDIENTE)) {
            throw new DatosDuplicadosException("Ya tienes un reporte pendiente sobre este contenido");
        }

        String detalle = solicitud.detalle() == null || solicitud.detalle().isBlank()
                ? null : solicitud.detalle().trim();
        Reporte guardado = reporteRepository.save(new Reporte(
                solicitud.tipoContenido(), solicitud.contenidoId(), norma,
                reportante.id(), reportante.nombreUsuario(), detalle));

        log.debug("Reporte {} de {} sobre {} {}", guardado.getId(), reportante.nombreUsuario(),
                solicitud.tipoContenido(), solicitud.contenidoId());
        return ReporteResponse.desde(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponse> listar(EstadoReporte estado) {
        List<Reporte> reportes = estado == null
                ? reporteRepository.findAllByOrderByFechaCreacionDesc()
                : reporteRepository.findByEstadoOrderByFechaCreacionAsc(estado);
        return reportes.stream().map(ReporteResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponse> listarPropios(UsuarioAutenticado reportante) {
        return reporteRepository.findByReportanteIdOrderByFechaCreacionDesc(reportante.id()).stream()
                .map(ReporteResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteResponse obtenerPorId(Long id) {
        return ReporteResponse.desde(buscarOFallar(id));
    }

    @Override
    @Transactional
    public ReporteResponse descartar(Long id, UsuarioAutenticado moderador) {
        Reporte reporte = buscarOFallar(id);
        if (!reporte.estaPendiente()) {
            throw new EstadoInvalidoException("El reporte " + id + " ya fue resuelto como " + reporte.getEstado());
        }
        reporte.resolver(EstadoReporte.DESCARTADO, moderador.nombreUsuario());
        log.info("Reporte {} descartado por {}", id, moderador.nombreUsuario());
        return ReporteResponse.desde(reporteRepository.save(reporte));
    }

    @Override
    @Transactional
    public int aceptarPendientesSobre(TipoContenido tipo, Long contenidoId, UsuarioAutenticado moderador) {
        List<Reporte> pendientes = reporteRepository
                .findByTipoContenidoAndContenidoIdAndEstado(tipo, contenidoId, EstadoReporte.PENDIENTE);
        pendientes.forEach(r -> r.resolver(EstadoReporte.ACEPTADO, moderador.nombreUsuario()));
        reporteRepository.saveAll(pendientes);
        return pendientes.size();
    }

    private Reporte buscarOFallar(Long id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el reporte", id));
    }
}
