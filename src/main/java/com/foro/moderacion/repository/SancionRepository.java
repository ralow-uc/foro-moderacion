package com.foro.moderacion.repository;

import com.foro.moderacion.model.Sancion;
import com.foro.moderacion.model.TipoContenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA de las sanciones.
 */
@Repository
public interface SancionRepository extends JpaRepository<Sancion, Long> {

    /**
     * Sanciones con filtros opcionales de vigencia y tipo. Cada condicion se
     * desactiva sola cuando su parametro llega nulo. Los dos parametros se
     * comparan contra una columna, asi que Hibernate les infiere el tipo aunque
     * lleguen nulos (a diferencia del texto libre de foro-temas, que necesita CAST).
     */
    @Query("""
            SELECT s FROM Sancion s
            WHERE (:vigente IS NULL OR s.vigente = :vigente)
              AND (:tipo IS NULL OR s.tipoContenido = :tipo)
            ORDER BY s.fechaCreacion DESC, s.id DESC
            """)
    List<Sancion> buscar(@Param("vigente") Boolean vigente, @Param("tipo") TipoContenido tipo);

    /** Un contenido no puede tener dos sanciones vigentes a la vez. */
    boolean existsByTipoContenidoAndContenidoIdAndVigenteTrue(TipoContenido tipo, Long contenidoId);

    /** Historial de sanciones de un autor, para ver reincidencias. */
    List<Sancion> findByAutorIdOrderByFechaCreacionDesc(Long autorId);

    long countByNormaId(Long normaId);
}
