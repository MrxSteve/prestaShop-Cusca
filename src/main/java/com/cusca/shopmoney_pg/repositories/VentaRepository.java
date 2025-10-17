package com.cusca.shopmoney_pg.repositories;

import com.cusca.shopmoney_pg.models.entities.VentaEntity;
import com.cusca.shopmoney_pg.models.enums.EstadoVenta;
import com.cusca.shopmoney_pg.models.enums.TipoVenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface VentaRepository extends JpaRepository<VentaEntity, Long> {
    // Ventas por cuenta de cliente
    Page<VentaEntity> findByCuentaClienteId(Long cuentaClienteId, Pageable pageable);
    Page<VentaEntity> findByCuentaClienteUsuarioId(Long usuarioId, Pageable pageable);

    // Ventas por tipo
    Page<VentaEntity> findByTipoVenta(TipoVenta tipoVenta, Pageable pageable);

    // Ventas por estado
    Page<VentaEntity> findByEstado(EstadoVenta estado, Pageable pageable);

    // Ventas por rango de fechas
    Page<VentaEntity> findByFechaVentaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);

    // Ventas por rango de total
    Page<VentaEntity> findByTotalBetween(BigDecimal montoMin, BigDecimal montoMax, Pageable pageable);

    // Ventas de contado (cliente ocasional)
    Page<VentaEntity> findByClienteOcasionalContainingIgnoreCase(String clienteOcasional, Pageable pageable);

    // Ventas por cliente y estado
    Page<VentaEntity> findByCuentaClienteIdAndEstado(Long cuentaClienteId, EstadoVenta estado, Pageable pageable);

    // Ventas por cliente y rango de fechas
    @Query("SELECT v FROM VentaEntity v WHERE v.cuentaCliente.id = :clienteId AND v.fechaVenta BETWEEN :fechaInicio AND :fechaFin")
    Page<VentaEntity> findByClienteAndFechaRange(@Param("clienteId") Long clienteId,
                                           @Param("fechaInicio") LocalDateTime fechaInicio,
                                           @Param("fechaFin") LocalDateTime fechaFin,
                                           Pageable pageable);

    // Ventas por cliente
    @Query("SELECT COUNT(v) FROM VentaEntity v WHERE v.cuentaCliente.id = :clienteId")
    long countVentasByCliente(@Param("clienteId") Long clienteId);

    @Query("SELECT SUM(v.total) FROM VentaEntity v WHERE v.cuentaCliente.id = :clienteId")
    BigDecimal sumTotalByCliente(@Param("clienteId") Long clienteId);
}
