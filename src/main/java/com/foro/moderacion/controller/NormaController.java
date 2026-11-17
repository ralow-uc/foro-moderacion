package com.foro.moderacion.controller;

import com.foro.moderacion.dto.NormaRequest;
import com.foro.moderacion.dto.NormaResponse;
import com.foro.moderacion.service.NormaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * Controlador REST de las normas del foro (generado por el arquetipo).
 * Leerlas es publico porque el frontend las muestra a todos los usuarios;
 * administrarlas es exclusivo del ADMIN.
 *
 *   GET    /api/normas          Lista (publico). ?soloActivos=true filtra.
 *   GET    /api/normas/{id}     Detalle (publico).
 *   POST   /api/normas          Crea. Solo ADMIN.
 *   PUT    /api/normas/{id}     Actualiza. Solo ADMIN.
 *   DELETE /api/normas/{id}     Elimina. Solo ADMIN.
 */
@RestController
@RequestMapping("/api/normas")
public class NormaController {

    private final NormaService normaService;

    public NormaController(NormaService normaService) {
        this.normaService = normaService;
    }

    @GetMapping
    public ResponseEntity<List<NormaResponse>> listar(
            @RequestParam(name = "soloActivos", defaultValue = "false") boolean soloActivos) {
        return ResponseEntity.ok(normaService.listar(soloActivos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NormaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(normaService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NormaResponse> crear(@Valid @RequestBody NormaRequest solicitud,
                                                    UriComponentsBuilder uriBuilder) {
        NormaResponse creado = normaService.crear(solicitud);
        return ResponseEntity
                .created(uriBuilder.path("/api/normas/{id}").buildAndExpand(creado.id()).toUri())
                .body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NormaResponse> actualizar(@PathVariable Long id,
                                                         @Valid @RequestBody NormaRequest solicitud) {
        return ResponseEntity.ok(normaService.actualizar(id, solicitud));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        normaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
