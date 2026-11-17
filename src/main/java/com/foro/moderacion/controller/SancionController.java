package com.foro.moderacion.controller;

import com.foro.moderacion.dto.LevantamientoRequest;
import com.foro.moderacion.dto.SancionRequest;
import com.foro.moderacion.dto.SancionResponse;
import com.foro.moderacion.model.TipoContenido;
import com.foro.moderacion.security.UsuarioAutenticado;
import com.foro.moderacion.service.SancionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * Controlador REST de las sanciones (banear temas o comentarios).
 *
 *   POST /api/sanciones                    Banea un contenido. MODERADOR o ADMIN.
 *   GET  /api/sanciones?vigente=&tipo=     Lista con filtros. MODERADOR o ADMIN.
 *   GET  /api/sanciones/{id}               Detalle. MODERADOR o ADMIN.
 *   GET  /api/sanciones/autor/{autorId}    Historial de un autor. MODERADOR o ADMIN.
 *   PUT  /api/sanciones/{id}/levantar      Levanta la sancion y restituye el contenido. Solo ADMIN.
 *
 * Que solo el ADMIN pueda levantar sanciones es deliberado: el moderador
 * actua, el administrador revisa las apelaciones.
 */
@RestController
@RequestMapping("/api/sanciones")
@PreAuthorize("hasAnyRole('MODERADOR', 'ADMIN')")
public class SancionController {

    private final SancionService sancionService;

    public SancionController(SancionService sancionService) {
        this.sancionService = sancionService;
    }

    @PostMapping
    public ResponseEntity<SancionResponse> sancionar(@Valid @RequestBody SancionRequest solicitud,
                                                     @AuthenticationPrincipal UsuarioAutenticado moderador,
                                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String autorizacion,
                                                     UriComponentsBuilder uriBuilder) {
        SancionResponse creada = sancionService.sancionar(solicitud, moderador, autorizacion);
        return ResponseEntity
                .created(uriBuilder.path("/api/sanciones/{id}").buildAndExpand(creada.id()).toUri())
                .body(creada);
    }

    @GetMapping
    public ResponseEntity<List<SancionResponse>> listar(
            @RequestParam(name = "vigente", required = false) Boolean vigente,
            @RequestParam(name = "tipo", required = false) TipoContenido tipo) {
        return ResponseEntity.ok(sancionService.listar(vigente, tipo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SancionResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(sancionService.obtenerPorId(id));
    }

    @GetMapping("/autor/{autorId}")
    public ResponseEntity<List<SancionResponse>> listarPorAutor(@PathVariable Long autorId) {
        return ResponseEntity.ok(sancionService.listarPorAutor(autorId));
    }

    @PutMapping("/{id}/levantar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SancionResponse> levantar(@PathVariable Long id,
                                                    @Valid @RequestBody LevantamientoRequest solicitud,
                                                    @AuthenticationPrincipal UsuarioAutenticado administrador,
                                                    @RequestHeader(HttpHeaders.AUTHORIZATION) String autorizacion) {
        return ResponseEntity.ok(sancionService.levantar(id, solicitud, administrador, autorizacion));
    }
}
