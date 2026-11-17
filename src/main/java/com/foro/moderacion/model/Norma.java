package com.foro.moderacion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.type.NumericBooleanConverter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad JPA de ejemplo generada por el arquetipo. Se mapea a la tabla
 * NORMA (ver scripts/01_crear_tablas.sql).
 *
 * Convenciones que el arquetipo deja fijadas para todas las entidades:
 *   - ID por secuencia de Oracle con allocationSize = 1.
 *   - Booleanos como NUMBER(1) con NumericBooleanConverter, para que el mapeo
 *     valide contra Oracle 23 (que tiene un BOOLEAN nativo distinto).
 *   - La fecha de creacion la fija la aplicacion, no el cliente.
 */
@Entity
@Table(name = "NORMA")
public class Norma {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "norma_seq_gen")
    @SequenceGenerator(name = "norma_seq_gen", sequenceName = "NORMA_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "DESCRIPCION", length = 400)
    private String descripcion;

    @Column(name = "ACTIVO", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean activo = true;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    protected Norma() {
        // Constructor requerido por JPA.
    }

    public Norma(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    @PrePersist
    void alInsertar() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Norma otro)) {
            return false;
        }
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
