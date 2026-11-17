package com.foro.moderacion.controller;

import com.foro.moderacion.dto.ReporteRequest;
import com.foro.moderacion.dto.ReporteResponse;
import com.foro.moderacion.model.EstadoReporte;
import com.foro.moderacion.security.UsuarioAutenticado;
import com.foro.moderacion.service.ReporteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * Controlador REST de los reportes de contenido.
 *
 *   POST /api/reportes                  Reporta un tema o comentario. Cualquier usuario con token.
 *   GET  /api/reportes/mios             Reportes del propio usuario. Cualquier usuario con token.
 *   GET  /api/reportes?estado=PENDIENTE Bandeja de moderacion. MODERADOR o ADMIN.
 *   GET  /api/reportes/{id}             Detalle. MODERADOR o ADMIN.
 *   PUT  /api/reportes/{id}/descartar   Cierra el reporte sin sancion. MODERADOR o ADMIN.
 *
 * Aceptar un reporte no tiene endpoint propio: se acepta al crear la sancion
 * (POST /api/sanciones), que es la decision real del moderador.
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @PostMapping
    public ResponseEntity<ReporteResponse> reportar(@Valid @RequestBody ReporteRequest solicitud,
                                                    @AuthenticationPrincipal UsuarioAutenticado reportante,
                                                    UriComponentsBuilder uriBuilder) {
        ReporteResponse creado = reporteService.reportar(solicitud, reportante);
        return ResponseEntity
                .created(uriBuilder.path("/api/reportes/{id}").buildAndExpand(creado.id()).toUri())
                .body(creado);
    }

    @GetMapping("/mios")
    public ResponseEntity<List<ReporteResponse>> listarPropios(
            @AuthenticationPrincipal UsuarioAutenticado reportante) {
        return ResponseEntity.ok(reporteService.listarPropios(reportante));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MODERADOR', 'ADMIN')")
    public ResponseEntity<List<ReporteResponse>> listar(
            @RequestParam(name = "estado", required = false) EstadoReporte estado) {
        return ResponseEntity.ok(reporteService.listar(estado));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MODERADOR', 'ADMIN')")
    public ResponseEntity<ReporteResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(reporteService.obtenerPorId(id));
    }

    @PutMapping("/{id}/descartar")
    @PreAuthorize("hasAnyRole('MODERADOR', 'ADMIN')")
    public ResponseEntity<ReporteResponse> descartar(@PathVariable Long id,
                                                     @AuthenticationPrincipal UsuarioAutenticado moderador) {
        return ResponseEntity.ok(reporteService.descartar(id, moderador));
    }
}
