package com.cusca.shopmoney_pg.services.finance;

import com.cusca.shopmoney_pg.models.dto.response.MovimientoCuentaResponse;
import com.cusca.shopmoney_pg.models.entities.MovimientoCuentaEntity;
import com.cusca.shopmoney_pg.models.enums.TipoMovimiento;
import com.cusca.shopmoney_pg.models.enums.TipoReferencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface IMovimientoCuentaService {
    // CRUD básico (solo consultas)
    Optional<MovimientoCuentaResponse> buscarPorId(Long id);
    Page<MovimientoCuentaResponse> listarTodos(Pageable pageable);

    // Búsquedas especializadas para administradores
    Page<MovimientoCuentaResponse> buscarPorCuentaCliente(Long cuentaClienteId, Pageable pageable);
    Page<MovimientoCuentaResponse> buscarPorUsuario(Long usuarioId, Pageable pageable);
    Page<MovimientoCuentaResponse> buscarPorTipoMovimiento(TipoMovimiento tipoMovimiento, Pageable pageable);
    Page<MovimientoCuentaResponse> buscarPorClienteYTipo(Long cuentaClienteId, TipoMovimiento tipoMovimiento, Pageable pageable);
    Page<MovimientoCuentaResponse> buscarPorFecha(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);
    Page<MovimientoCuentaResponse> buscarPorClienteYFecha(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);

    // Estados de cuenta y consultas de saldo
    BigDecimal obtenerSaldoActual(Long cuentaClienteId);
    Page<MovimientoCuentaResponse> obtenerEstadoCuenta(Long cuentaClienteId, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);

    // Estadísticas y reportes
    BigDecimal obtenerTotalCargosDelDia();
    BigDecimal obtenerTotalAbonosDelDia();
    BigDecimal obtenerTotalCargosDelMes();
    BigDecimal obtenerTotalAbonosDelMes();

    // Búsquedas por referencia (para auditoría)
    Page<MovimientoCuentaResponse> buscarPorReferencia(TipoReferencia tipoReferencia, Long referenciaId, Pageable pageable);

    // Creación de movimientos (para uso interno del sistema)
    MovimientoCuentaResponse crearMovimiento(Long cuentaClienteId, TipoMovimiento tipoMovimiento,
                                           String concepto, BigDecimal monto,
                                           BigDecimal saldoAnterior, BigDecimal saldoNuevo,
                                           TipoReferencia referenciaTipo, Long referenciaId, Long usuarioId);

    // Para uso interno
    MovimientoCuentaEntity buscarEntidadPorId(Long id);
}
