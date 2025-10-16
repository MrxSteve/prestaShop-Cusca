package com.cusca.shopmoney_pg.repositories;

import com.cusca.shopmoney_pg.models.entities.AbonoEntity;
import com.cusca.shopmoney_pg.models.enums.EstadoAbono;
import com.cusca.shopmoney_pg.models.enums.MetodoPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AbonoRepository extends JpaRepository<AbonoEntity, Long> {
    // Abonos por cliente
    Page<AbonoEntity > findByCuentaClienteId(Long cuentaClienteId, Pageable pageable);
    Page<AbonoEntity> findByCuentaClienteUsuarioId(Long usuarioId, Pageable pageable);

    // Abonos por estado
    Page<AbonoEntity > findByEstado(EstadoAbono estado, Pageable pageable);
    List<AbonoEntity > findByEstado(EstadoAbono estado);

    // Abonos por metodo de pago
    Page<AbonoEntity > findByMetodoPago(MetodoPago metodoPago, Pageable pageable);

    // Abonos por cliente y estado
    Page<AbonoEntity > findByCuentaClienteIdAndEstado(Long cuentaClienteId, EstadoAbono estado, Pageable pageable);

    // Abonos por rango de fechas
    Page<AbonoEntity > findByFechaAbonoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);

    // Abonos por cliente y rango de fechas
    @Query("SELECT a FROM AbonoEntity a WHERE a.cuentaCliente.id = :clienteId AND a.fechaAbono BETWEEN :fechaInicio AND :fechaFin")
    Page<AbonoEntity > findByClienteAndFechaRange(@Param("clienteId") Long clienteId,
                                           @Param("fechaInicio") LocalDateTime fechaInicio,
                                           @Param("fechaFin") LocalDateTime fechaFin,
                                           Pageable pageable);

    // Abonos del día
    @Query("SELECT a FROM AbonoEntity a WHERE DATE(a.fechaAbono) = CURRENT_DATE")
    Page<AbonoEntity > findAbonosDelDia(Pageable pageable);

    // Abonos del mes actual
    @Query("SELECT a FROM AbonoEntity a WHERE YEAR(a.fechaAbono) = YEAR(CURRENT_DATE) AND MONTH(a.fechaAbono) = MONTH(CURRENT_DATE)")
    Page<AbonoEntity > findAbonosDelMes(Pageable pageable);
}
