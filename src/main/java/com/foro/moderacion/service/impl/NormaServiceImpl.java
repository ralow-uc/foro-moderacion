package com.foro.moderacion.service.impl;

import com.foro.moderacion.dto.NormaRequest;
import com.foro.moderacion.dto.NormaResponse;
import com.foro.moderacion.exception.DatosDuplicadosException;
import com.foro.moderacion.exception.EstadoInvalidoException;
import com.foro.moderacion.exception.RecursoNoEncontradoException;
import com.foro.moderacion.model.Norma;
import com.foro.moderacion.repository.NormaRepository;
import com.foro.moderacion.repository.ReporteRepository;
import com.foro.moderacion.repository.SancionRepository;
import com.foro.moderacion.service.NormaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logica de negocio de las normas: el codigo y el nombre son unicos sin
 * distinguir mayusculas. El codigo se guarda siempre en mayusculas (N1, N2...).
 */
@Service
public class NormaServiceImpl implements NormaService {

    private static final Logger log = LoggerFactory.getLogger(NormaServiceImpl.class);

    private final NormaRepository normaRepository;
    private final ReporteRepository reporteRepository;
    private final SancionRepository sancionRepository;

    public NormaServiceImpl(NormaRepository normaRepository, ReporteRepository reporteRepository,
                            SancionRepository sancionRepository) {
        this.normaRepository = normaRepository;
        this.reporteRepository = reporteRepository;
        this.sancionRepository = sancionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NormaResponse> listar(boolean soloActivos) {
        List<Norma> resultado = soloActivos
                ? normaRepository.findByActivoTrueOrderByNombreAsc()
                : normaRepository.findAllByOrderByNombreAsc();
        return resultado.stream().map(NormaResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NormaResponse obtenerPorId(Long id) {
        return NormaResponse.desde(buscarOFallar(id));
    }

    @Override
    @Transactional
    public NormaResponse crear(NormaRequest solicitud) {
        String nombre = solicitud.nombre().trim();
        String codigo = solicitud.codigo().trim().toUpperCase();
        if (normaRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new DatosDuplicadosException("Ya existe una norma con el codigo '" + codigo + "'");
        }
        if (normaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new DatosDuplicadosException("Ya existe una norma con el nombre '" + nombre + "'");
        }
        Norma nuevo = new Norma(codigo, nombre, solicitud.descripcion().trim(), solicitud.gravedad());
        nuevo.setActivo(solicitud.activo());
        Norma guardado = normaRepository.save(nuevo);
        log.debug("Norma {} creada", guardado.getId());
        return NormaResponse.desde(guardado);
    }

    @Override
    @Transactional
    public NormaResponse actualizar(Long id, NormaRequest solicitud) {
        Norma existente = buscarOFallar(id);
        String nombre = solicitud.nombre().trim();
        String codigo = solicitud.codigo().trim().toUpperCase();
        if (normaRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new DatosDuplicadosException("Ya existe una norma con el codigo '" + codigo + "'");
        }
        if (normaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new DatosDuplicadosException("Ya existe una norma con el nombre '" + nombre + "'");
        }
        existente.setCodigo(codigo);
        existente.setNombre(nombre);
        existente.setDescripcion(solicitud.descripcion().trim());
        existente.setGravedad(solicitud.gravedad());
        existente.setActivo(solicitud.activo());
        return NormaResponse.desde(normaRepository.save(existente));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Norma norma = buscarOFallar(id);
        // Una norma citada por reportes o sanciones es parte del historial de
        // moderacion: se desactiva (activo=false), pero no se borra.
        if (reporteRepository.countByNormaId(id) > 0 || sancionRepository.countByNormaId(id) > 0) {
            throw new EstadoInvalidoException("La norma " + norma.getCodigo()
                    + " esta citada en reportes o sanciones; desactivala en lugar de eliminarla");
        }
        normaRepository.delete(norma);
        log.debug("Norma {} eliminada", id);
    }

    private Norma buscarOFallar(Long id) {
        return normaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("la norma", id));
    }
}
