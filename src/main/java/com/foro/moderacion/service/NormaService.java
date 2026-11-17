package com.foro.moderacion.service;

import com.foro.moderacion.dto.NormaRequest;
import com.foro.moderacion.dto.NormaResponse;

import java.util.List;

/**
 * Contrato de la capa de servicio de Norma. El controlador depende de esta
 * interfaz y no de la implementacion (inversion de dependencias).
 */
public interface NormaService {

    List<NormaResponse> listar(boolean soloActivos);

    NormaResponse obtenerPorId(Long id);

    NormaResponse crear(NormaRequest solicitud);

    NormaResponse actualizar(Long id, NormaRequest solicitud);

    void eliminar(Long id);
}
