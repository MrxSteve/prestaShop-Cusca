package com.cusca.shopmoney_pg.repositories;

import com.cusca.shopmoney_pg.models.entities.CategoriaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Long> {
    // Búsqueda por nombre ignorando mayúsculas/minúsculas
    Optional<CategoriaEntity> findByNombreIgnoreCase(String nombre);

    // Búsqueda por nombre conteniendo texto (paginada)
    Page<CategoriaEntity> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    // Verificar si existe una categoría por nombre
    boolean existsByNombreIgnoreCase(String nombre);

    // Listar todas las categorías paginadas
    Page<CategoriaEntity> findAll(Pageable pageable);
}
