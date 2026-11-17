package com.foro.moderacion.repository;

import com.foro.moderacion.model.EstadoReporte;
import com.foro.moderacion.model.Reporte;
import com.foro.moderacion.model.TipoContenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA de los reportes. Todas las consultas se derivan del nombre.
 */
@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    List<Reporte> findAllByOrderByFechaCreacionDesc();

    List<Reporte> findByEstadoOrderByFechaCreacionAsc(EstadoReporte estado);

    List<Reporte> findByReportanteIdOrderByFechaCreacionDesc(Long reportanteId);

    /** Reportes que siguen abiertos sobre un mismo contenido. */
    List<Reporte> findByTipoContenidoAndContenidoIdAndEstado(TipoContenido tipo, Long contenidoId,
                                                             EstadoReporte estado);

    /** Un usuario no puede tener dos reportes pendientes sobre el mismo contenido. */
    boolean existsByTipoContenidoAndContenidoIdAndReportanteIdAndEstado(
            TipoContenido tipo, Long contenidoId, Long reportanteId, EstadoReporte estado);

    long countByNormaId(Long normaId);
}
