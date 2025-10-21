package com.cusca.shopmoney_pg.repositories;

import com.cusca.shopmoney_pg.models.entities.CuentaClienteEntity;
import com.cusca.shopmoney_pg.models.enums.EstadoCuenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface CuentaClienteRepository extends JpaRepository<CuentaClienteEntity, Long> {
    // Búsqueda por usuario
    Optional<CuentaClienteEntity> findByUsuarioId(Long usuarioId);
    Optional<CuentaClienteEntity> findByUsuarioEmail(String email);
    boolean existsByUsuarioId(Long usuarioId);

    // Búsquedas por estado
    Page<CuentaClienteEntity> findByEstado(EstadoCuenta estado, Pageable pageable);

    // Búsquedas por rangos de límite de crédito
    Page<CuentaClienteEntity> findByLimiteCreditoBetween(BigDecimal min, BigDecimal max, Pageable pageable);
    Page<CuentaClienteEntity> findByLimiteCreditoGreaterThanEqual(BigDecimal limite, Pageable pageable);

    // Búsquedas por saldo actual
    Page<CuentaClienteEntity> findBySaldoActualGreaterThan(BigDecimal saldo, Pageable pageable);
    Page<CuentaClienteEntity> findBySaldoActualLessThanEqual(BigDecimal saldo, Pageable pageable);

    // Búsquedas por fecha de apertura
    Page<CuentaClienteEntity> findByFechaAperturaBetween(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);
}
