package com.cusca.shopmoney_pg.services.finance;

import com.cusca.shopmoney_pg.models.dto.request.MovimientoCuentaRequest;
import com.cusca.shopmoney_pg.models.dto.response.MovimientoCuentaResponse;
import com.cusca.shopmoney_pg.models.entities.MovimientoCuentaEntity;
import com.cusca.shopmoney_pg.models.enums.TipoMovimiento;
import com.cusca.shopmoney_pg.models.enums.TipoReferencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface IMovimientoCuentaService {
    // CRUD básico
    MovimientoCuentaResponse crear(MovimientoCuentaRequest request);
    Optional<MovimientoCuentaResponse> buscarPorId(Long id);
    Page<MovimientoCuentaResponse> listarTodos(Pageable pageable);

    // Búsquedas paginadas
    Page<MovimientoCuentaResponse> buscarPorCuentaCliente(Long cuentaClienteId, Pageable pageable);
    Page<MovimientoCuentaResponse> buscarPorUsuario(Long usuarioId, Pageable pageable);
    Page<MovimientoCuentaResponse> buscarPorTipoMovimiento(TipoMovimiento tipoMovimiento, Pageable pageable);
    Page<MovimientoCuentaResponse> buscarPorClienteYTipo(Long cuentaClienteId, TipoMovimiento tipoMovimiento, Pageable pageable);
    Page<MovimientoCuentaResponse> buscarPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    Page<MovimientoCuentaResponse> buscarPorClienteYFecha(Long clienteId, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    Page<MovimientoCuentaResponse> buscarMovimientosDelDia(Pageable pageable);
    Page<MovimientoCuentaResponse> buscarMovimientosDelMes(Pageable pageable);

    // Operaciones de movimiento
    MovimientoCuentaResponse registrarCargo(Long cuentaClienteId, BigDecimal monto, String concepto,
                                            TipoReferencia tipoReferencia, Long usuarioId);
    MovimientoCuentaResponse registrarAbono(Long cuentaClienteId, BigDecimal monto, String concepto,
                                            TipoReferencia tipoReferencia, Long usuarioId);
    MovimientoCuentaResponse registrarAjuste(Long cuentaClienteId, BigDecimal monto, String concepto,
                                             Long usuarioId);

    // Validaciones
    boolean puedeRealizarMovimiento(Long cuentaClienteId, TipoMovimiento tipo, BigDecimal monto);

    // Consultas de saldo
    BigDecimal obtenerSaldoActual(Long cuentaClienteId);
    Page<MovimientoCuentaResponse> obtenerEstadoCuenta(Long cuentaClienteId,
                                                       LocalDateTime fechaInicio,
                                                       LocalDateTime fechaFin,
                                                       Pageable pageable);

    // Para uso interno
    MovimientoCuentaEntity buscarEntidadPorId(Long id);
}
