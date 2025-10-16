package com.cusca.shopmoney_pg.repositories;

import com.cusca.shopmoney_pg.models.entities.UsuarioEntity;
import com.cusca.shopmoney_pg.models.enums.EstadoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    // Búsquedas básicas
    Optional<UsuarioEntity> findByEmail(String email);
    Optional<UsuarioEntity> findByDui(String dui);
    boolean existsByEmail(String email);
    boolean existsByDui(String dui);

    // Búsqueda por nombre
    Page<UsuarioEntity> findByNombreCompletoContainingIgnoreCase(String nombreCompleto, Pageable pageable);

    // Búsqueda por estado
    Page<UsuarioEntity> findByEstado(EstadoUsuario estado, Pageable pageable);

    // Búsquedas por rol
    @Query("SELECT u FROM UsuarioEntity u JOIN u.roles r WHERE r.nombre = :nombreRol")
    Page<UsuarioEntity> findByRolNombre(@Param("nombreRol") String nombreRol, Pageable pageable);

    // Usuarios con cuenta de cliente
    @Query("SELECT u FROM UsuarioEntity u WHERE u.cuentaCliente IS NOT NULL")
    Page<UsuarioEntity> findUsuariosConCuenta(Pageable pageable);

    // Usuarios sin cuenta de cliente
    @Query("SELECT u FROM UsuarioEntity u WHERE u.cuentaCliente IS NULL")
    Page<UsuarioEntity> findUsuariosSinCuenta(Pageable pageable);

    // Búsqueda múltiple (nombre, email, teléfono)
    @Query("SELECT u FROM UsuarioEntity u WHERE " +
            "LOWER(u.nombreCompleto) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(u.telefono) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<UsuarioEntity> findByBusquedaGeneral(@Param("busqueda") String busqueda, Pageable pageable);
}
