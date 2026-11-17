package com.foro.moderacion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Reporte de un usuario sobre un tema o un comentario que cree que incumple
 * una norma del foro.
 *
 * CONTENIDO_ID apunta a un registro de foro-temas y, como en el resto del
 * sistema, no lleva clave foranea: cada microservicio es dueno de sus tablas.
 * REPORTANTE_ID y REPORTANTE_NOMBRE se copian del JWT de quien reporta.
 */
@Entity
@Table(name = "REPORTE")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reporte_seq_gen")
    @SequenceGenerator(name = "reporte_seq_gen", sequenceName = "REPORTE_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_CONTENIDO", nullable = false, length = 20)
    private TipoContenido tipoContenido;

    @Column(name = "CONTENIDO_ID", nullable = false)
    private Long contenidoId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "NORMA_ID", nullable = false)
    private Norma norma;

    @Column(name = "REPORTANTE_ID", nullable = false)
    private Long reportanteId;

    @Column(name = "REPORTANTE_NOMBRE", nullable = false, length = 50)
    private String reportanteNombre;

    @Column(name = "DETALLE", length = 500)
    private String detalle;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoReporte estado = EstadoReporte.PENDIENTE;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_RESOLUCION")
    private LocalDateTime fechaResolucion;

    /** Moderador que acepto o descarto el reporte. */
    @Column(name = "RESUELTO_POR", length = 50)
    private String resueltoPor;

    protected Reporte() {
        // Constructor requerido por JPA.
    }

    public Reporte(TipoContenido tipoContenido, Long contenidoId, Norma norma,
                   Long reportanteId, String reportanteNombre, String detalle) {
        this.tipoContenido = tipoContenido;
        this.contenidoId = contenidoId;
        this.norma = norma;
        this.reportanteId = reportanteId;
        this.reportanteNombre = reportanteNombre;
        this.detalle = detalle;
    }

    @PrePersist
    void alInsertar() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    public boolean estaPendiente() {
        return estado == EstadoReporte.PENDIENTE;
    }

    /** Cierra el reporte con el resultado de la revision. */
    public void resolver(EstadoReporte resultado, String moderador) {
        this.estado = resultado;
        this.resueltoPor = moderador;
        this.fechaResolucion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public TipoContenido getTipoContenido() {
        return tipoContenido;
    }

    public Long getContenidoId() {
        return contenidoId;
    }

    public Norma getNorma() {
        return norma;
    }

    public Long getReportanteId() {
        return reportanteId;
    }

    public String getReportanteNombre() {
        return reportanteNombre;
    }

    public String getDetalle() {
        return detalle;
    }

    public EstadoReporte getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public String getResueltoPor() {
        return resueltoPor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reporte otro)) {
            return false;
        }
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
