package com.cusca.shopmoney_pg.services.account;

import com.cusca.shopmoney_pg.models.dto.request.CuentaClienteRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateCuentaClienteRequest;
import com.cusca.shopmoney_pg.models.dto.response.CuentaClienteResponse;
import com.cusca.shopmoney_pg.models.entities.CuentaClienteEntity;
import com.cusca.shopmoney_pg.models.enums.EstadoCuenta;
import com.cusca.shopmoney_pg.models.enums.TipoReferencia;
import com.cusca.shopmoney_pg.services.base.BaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface ICuentaClienteService extends BaseService<CuentaClienteResponse, CuentaClienteRequest, UpdateCuentaClienteRequest> {
    // Búsquedas paginadas
    Page<CuentaClienteResponse> buscarPorEstado(EstadoCuenta estado, Pageable pageable);
    Page<CuentaClienteResponse> buscarPorRangoLimiteCredito(BigDecimal min, BigDecimal max, Pageable pageable);
    Page<CuentaClienteResponse> buscarPorLimiteCreditoMinimo(BigDecimal limite, Pageable pageable);
    Page<CuentaClienteResponse> buscarPorSaldoMayorQue(BigDecimal saldo, Pageable pageable);
    Page<CuentaClienteResponse> buscarPorSaldoMenorIgual(BigDecimal saldo, Pageable pageable);
    Page<CuentaClienteResponse> buscarPorFechaApertura(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);

    // Búsquedas específicas
    Optional<CuentaClienteResponse> buscarPorUsuario(Long usuarioId);
    Optional<CuentaClienteResponse> buscarPorUsuarioEmail(String email);

    // Validaciones
    boolean existePorUsuario(Long usuarioId);
    boolean puedeRealizarCompra(Long cuentaId, BigDecimal montoCompra);

    // Gestión de límites y saldos - Métodos originales
    CuentaClienteResponse actualizarLimiteCredito(Long id, BigDecimal nuevoLimite);
    CuentaClienteResponse cargarSaldo(Long id, BigDecimal monto, String concepto, Long usuarioId);
    CuentaClienteResponse abonarSaldo(Long id, BigDecimal monto, String concepto, Long usuarioId);

    // Gestión de límites y saldos - Métodos mejorados con trazabilidad
    CuentaClienteResponse cargarSaldoConReferencia(Long id, BigDecimal monto, String concepto, Long usuarioId,
                                                  TipoReferencia tipoReferencia, Long referenciaId);
    CuentaClienteResponse abonarSaldoConReferencia(Long id, BigDecimal monto, String concepto, Long usuarioId,
                                                  TipoReferencia tipoReferencia, Long referenciaId);

    // Gestión de estado
    CuentaClienteResponse cambiarEstado(Long id, EstadoCuenta nuevoEstado);
    CuentaClienteResponse activar(Long id);
    CuentaClienteResponse suspender(Long id);
    CuentaClienteResponse cerrar(Long id);

    // Utilidades financieras
    BigDecimal calcularSaldoDisponible(Long cuentaId);

    // Para uso interno
    CuentaClienteEntity buscarEntidadPorId(Long id);
    CuentaClienteEntity buscarEntidadPorUsuario(Long usuarioId);
}
