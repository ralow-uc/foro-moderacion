package com.foro.moderacion.repository;

import com.foro.moderacion.model.Norma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA de Norma. Al extender JpaRepository se obtiene el CRUD
 * contra Oracle sin escribir SQL; las consultas propias se derivan del nombre.
 */
@Repository
public interface NormaRepository extends JpaRepository<Norma, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<Norma> findByActivoTrueOrderByNombreAsc();

    List<Norma> findAllByOrderByNombreAsc();
}
