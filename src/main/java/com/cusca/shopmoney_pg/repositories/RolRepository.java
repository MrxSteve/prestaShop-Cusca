package com.cusca.shopmoney_pg.repositories;

import com.cusca.shopmoney_pg.models.entities.RolEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<RolEntity, Long> {
    // Búsqueda por nombre ignorando mayúsculas/minúsculas
    Optional<RolEntity> findByNombreIgnoreCase(String nombre);

    // Verificar si existe un rol por nombre
    boolean existsByNombreIgnoreCase(String nombre);

    // Listar todos los roles paginados
    Page<RolEntity> findAll(Pageable pageable);
}
