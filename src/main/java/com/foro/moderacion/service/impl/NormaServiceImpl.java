package com.foro.moderacion.service.impl;

import com.foro.moderacion.dto.NormaRequest;
import com.foro.moderacion.dto.NormaResponse;
import com.foro.moderacion.exception.DatosDuplicadosException;
import com.foro.moderacion.exception.RecursoNoEncontradoException;
import com.foro.moderacion.model.Norma;
import com.foro.moderacion.repository.NormaRepository;
import com.foro.moderacion.service.NormaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logica de negocio de Norma: el nombre es unico sin distinguir mayusculas.
 */
@Service
public class NormaServiceImpl implements NormaService {

    private static final Logger log = LoggerFactory.getLogger(NormaServiceImpl.class);

    private final NormaRepository normaRepository;

    public NormaServiceImpl(NormaRepository normaRepository) {
        this.normaRepository = normaRepository;
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
        if (normaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new DatosDuplicadosException("Ya existe un registro con el nombre '" + nombre + "'");
        }
        Norma nuevo = new Norma(nombre, solicitud.descripcion());
        nuevo.setActivo(solicitud.activo());
        Norma guardado = normaRepository.save(nuevo);
        log.debug("Norma {} creado", guardado.getId());
        return NormaResponse.desde(guardado);
    }

    @Override
    @Transactional
    public NormaResponse actualizar(Long id, NormaRequest solicitud) {
        Norma existente = buscarOFallar(id);
        String nombre = solicitud.nombre().trim();
        if (normaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new DatosDuplicadosException("Ya existe un registro con el nombre '" + nombre + "'");
        }
        existente.setNombre(nombre);
        existente.setDescripcion(solicitud.descripcion());
        existente.setActivo(solicitud.activo());
        return NormaResponse.desde(normaRepository.save(existente));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        normaRepository.delete(buscarOFallar(id));
        log.debug("Norma {} eliminado", id);
    }

    private Norma buscarOFallar(Long id) {
        return normaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Norma", id));
    }
}
