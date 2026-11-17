package com.foro.moderacion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import org.hibernate.type.NumericBooleanConverter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Sancion aplicada por un moderador: un tema o un comentario baneado por
 * incumplir una norma.
 *
 * La sancion es el registro historico de la decision. El efecto (ocultar el
 * contenido) lo aplica foro-temas, que es el dueno de TEMA y COMENTARIO. Al
 * levantarla no se borra: queda VIGENTE = 0 con quien y por que la levanto.
 *
 * AUTOR_ID / AUTOR_NOMBRE son del autor del contenido sancionado, tal como los
 * devolvio foro-temas; permiten ver el historial de sanciones de un usuario.
 */
@Entity
@Table(name = "SANCION")
public class Sancion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sancion_seq_gen")
    @SequenceGenerator(name = "sancion_seq_gen", sequenceName = "SANCION_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_CONTENIDO", nullable = false, length = 20)
    private TipoContenido tipoContenido;

    @Column(name = "CONTENIDO_ID", nullable = false)
    private Long contenidoId;

    @Column(name = "TITULO_CONTENIDO", length = 200)
    private String tituloContenido;

    @Column(name = "AUTOR_ID")
    private Long autorId;

    @Column(name = "AUTOR_NOMBRE", length = 50)
    private String autorNombre;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "NORMA_ID", nullable = false)
    private Norma norma;

    /** Reporte que origino la sancion. Nulo si el moderador actuo de oficio. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPORTE_ID")
    private Reporte reporte;

    @Column(name = "MOTIVO", nullable = false, length = 500)
    private String motivo;

    @Column(name = "MODERADOR_ID", nullable = false)
    private Long moderadorId;

    @Column(name = "MODERADOR_NOMBRE", nullable = false, length = 50)
    private String moderadorNombre;

    @Column(name = "VIGENTE", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean vigente = true;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_LEVANTAMIENTO")
    private LocalDateTime fechaLevantamiento;

    @Column(name = "LEVANTADA_POR", length = 50)
    private String levantadaPor;

    @Column(name = "MOTIVO_LEVANTAMIENTO", length = 500)
    private String motivoLevantamiento;

    protected Sancion() {
        // Constructor requerido por JPA.
    }

    public Sancion(TipoContenido tipoContenido, Long contenidoId, Norma norma, Reporte reporte,
                   String motivo, Long moderadorId, String moderadorNombre) {
        this.tipoContenido = tipoContenido;
        this.contenidoId = contenidoId;
        this.norma = norma;
        this.reporte = reporte;
        this.motivo = motivo;
        this.moderadorId = moderadorId;
        this.moderadorNombre = moderadorNombre;
    }

    @PrePersist
    void alInsertar() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    /** Guarda los datos del contenido que devolvio foro-temas al banearlo. */
    public void registrarContenido(String titulo, Long autorId, String autorNombre) {
        this.tituloContenido = titulo;
        this.autorId = autorId;
        this.autorNombre = autorNombre;
    }

    /** Deja la sancion sin efecto conservandola en el historial. */
    public void levantar(String administrador, String motivo) {
        this.vigente = false;
        this.levantadaPor = administrador;
        this.motivoLevantamiento = motivo;
        this.fechaLevantamiento = LocalDateTime.now();
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

    public String getTituloContenido() {
        return tituloContenido;
    }

    public Long getAutorId() {
        return autorId;
    }

    public String getAutorNombre() {
        return autorNombre;
    }

    public Norma getNorma() {
        return norma;
    }

    public Reporte getReporte() {
        return reporte;
    }

    public String getMotivo() {
        return motivo;
    }

    public Long getModeradorId() {
        return moderadorId;
    }

    public String getModeradorNombre() {
        return moderadorNombre;
    }

    public boolean isVigente() {
        return vigente;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaLevantamiento() {
        return fechaLevantamiento;
    }

    public String getLevantadaPor() {
        return levantadaPor;
    }

    public String getMotivoLevantamiento() {
        return motivoLevantamiento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Sancion otra)) {
            return false;
        }
        return id != null && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
