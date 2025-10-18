package com.cusca.shopmoney_pg.repositories;

import com.cusca.shopmoney_pg.models.entities.MovimientoCuentaEntity;
import com.cusca.shopmoney_pg.models.enums.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface MovimientoCuentaRepository extends JpaRepository<MovimientoCuentaEntity, Long> {
    // Movimientos por cliente
    Page<MovimientoCuentaEntity> findByCuentaClienteId(Long cuentaClienteId, Pageable pageable);
    Page<MovimientoCuentaEntity> findByCuentaClienteUsuarioId(Long usuarioId, Pageable pageable);

    // Movimientos por tipo
    Page<MovimientoCuentaEntity> findByTipoMovimiento(TipoMovimiento tipoMovimiento, Pageable pageable);

    // Movimientos por cliente y tipo
    Page<MovimientoCuentaEntity> findByCuentaClienteIdAndTipoMovimiento(Long cuentaClienteId, TipoMovimiento tipoMovimiento, Pageable pageable);

    // Movimientos por rango de fechas
    Page<MovimientoCuentaEntity> findByFechaMovimientoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);

    // Movimientos por cliente y rango de fechas
    @Query("SELECT m FROM MovimientoCuentaEntity m WHERE m.cuentaCliente.id = :clienteId AND m.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin ORDER BY m.fechaMovimiento DESC")
    Page<MovimientoCuentaEntity> findByClienteAndFechaRange(@Param("clienteId") Long clienteId,
                                                      @Param("fechaInicio") LocalDateTime fechaInicio,
                                                      @Param("fechaFin") LocalDateTime fechaFin,
                                                      Pageable pageable);
}
