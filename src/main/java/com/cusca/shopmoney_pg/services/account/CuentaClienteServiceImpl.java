package com.cusca.shopmoney_pg.services.account;

import com.cusca.shopmoney_pg.models.dto.request.CuentaClienteRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateCuentaClienteRequest;
import com.cusca.shopmoney_pg.models.dto.response.CuentaClienteResponse;
import com.cusca.shopmoney_pg.models.entities.CuentaClienteEntity;
import com.cusca.shopmoney_pg.models.entities.UsuarioEntity;
import com.cusca.shopmoney_pg.models.enums.EstadoCuenta;
import com.cusca.shopmoney_pg.models.enums.TipoMovimiento;
import com.cusca.shopmoney_pg.models.enums.TipoReferencia;
import com.cusca.shopmoney_pg.repositories.CuentaClienteRepository;
import com.cusca.shopmoney_pg.repositories.UsuarioRepository;
import com.cusca.shopmoney_pg.services.finance.IMovimientoCuentaService;
import com.cusca.shopmoney_pg.utils.exceptions.InvalidAccountStateException;
import com.cusca.shopmoney_pg.utils.exceptions.InvalidAmountException;
import com.cusca.shopmoney_pg.utils.exceptions.InsufficientBalanceException;
import com.cusca.shopmoney_pg.utils.exceptions.ResourceAlreadyExistsException;
import com.cusca.shopmoney_pg.utils.exceptions.ResourceNotFoundException;
import com.cusca.shopmoney_pg.utils.exceptions.UserHasPendingBalanceException;
import com.cusca.shopmoney_pg.utils.mappers.CuentaClienteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CuentaClienteServiceImpl implements ICuentaClienteService {
    private final CuentaClienteRepository cuentaClienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CuentaClienteMapper cuentaClienteMapper;
    private final IMovimientoCuentaService movimientoCuentaService;

    @Override
    public CuentaClienteResponse crear(CuentaClienteRequest request) {
        // Verificar que el usuario existe
        UsuarioEntity usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + request.getUsuarioId()));

        // Verificar que no exista una cuenta para este usuario
        if (cuentaClienteRepository.existsByUsuarioId(request.getUsuarioId())) {
            throw new ResourceAlreadyExistsException("Ya existe una cuenta para el usuario con ID: " + request.getUsuarioId());
        }

        CuentaClienteEntity cuentaCliente = cuentaClienteMapper.toEntity(request);
        cuentaCliente.setUsuario(usuario);

        // Establecer valores por defecto si no se proporcionan
        if (cuentaCliente.getLimiteCredito() == null) {
            cuentaCliente.setLimiteCredito(BigDecimal.ZERO);
        }
        if (cuentaCliente.getSaldoActual() == null) {
            cuentaCliente.setSaldoActual(BigDecimal.ZERO);
        }
        if (cuentaCliente.getEstado() == null) {
            cuentaCliente.setEstado(EstadoCuenta.ACTIVA);
        }
        if (cuentaCliente.getFechaApertura() == null) {
            cuentaCliente.setFechaApertura(LocalDate.now());
        }

        CuentaClienteEntity cuentaGuardada = cuentaClienteRepository.save(cuentaCliente);

        // Crear movimiento inicial si hay saldo inicial
        if (cuentaGuardada.getSaldoActual().compareTo(BigDecimal.ZERO) > 0) {
            movimientoCuentaService.crearMovimiento(cuentaGuardada.getId(), TipoMovimiento.CARGO, "Saldo inicial",
                    cuentaGuardada.getSaldoActual(), BigDecimal.ZERO, cuentaGuardada.getSaldoActual(),
                    TipoReferencia.AJUSTE, null, usuario.getId());
        }

        return cuentaClienteMapper.toResponse(cuentaGuardada);
    }

    @Override
    public CuentaClienteResponse actualizar(Long id, UpdateCuentaClienteRequest request) {
        CuentaClienteEntity cuentaCliente = buscarEntidadPorId(id);

        cuentaClienteMapper.updateEntity(request, cuentaCliente);
        CuentaClienteEntity cuentaActualizada = cuentaClienteRepository.save(cuentaCliente);

        return cuentaClienteMapper.toResponse(cuentaActualizada);
    }

    @Override
    public void eliminar(Long id) {
        CuentaClienteEntity cuenta = buscarEntidadPorId(id);

        // Verificar que la cuenta esté en estado CERRADA antes de eliminar
        if (cuenta.getEstado() != EstadoCuenta.CERRADA) {
            throw new InvalidAccountStateException("Solo se pueden eliminar cuentas en estado CERRADA");
        }

        // Verificar que no tenga saldo pendiente
        if (cuenta.getSaldoActual().compareTo(BigDecimal.ZERO) != 0) {
            throw new UserHasPendingBalanceException("No se puede eliminar una cuenta con saldo pendiente");
        }

        cuentaClienteRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CuentaClienteResponse> buscarPorId(Long id) {
        CuentaClienteEntity cuentaCliente = buscarEntidadPorId(id);
        return Optional.of(cuentaClienteMapper.toResponse(cuentaCliente));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CuentaClienteResponse> listarTodos(Pageable pageable) {
        return cuentaClienteRepository.findAll(pageable)
                .map(cuentaClienteMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CuentaClienteResponse> buscarPorEstado(EstadoCuenta estado, Pageable pageable) {
        return cuentaClienteRepository.findByEstado(estado, pageable)
                .map(cuentaClienteMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CuentaClienteResponse> buscarPorRangoLimiteCredito(BigDecimal min, BigDecimal max, Pageable pageable) {
        return cuentaClienteRepository.findByLimiteCreditoBetween(min, max, pageable)
                .map(cuentaClienteMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CuentaClienteResponse> buscarPorLimiteCreditoMinimo(BigDecimal limite, Pageable pageable) {
        return cuentaClienteRepository.findByLimiteCreditoGreaterThanEqual(limite, pageable)
                .map(cuentaClienteMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CuentaClienteResponse> buscarPorSaldoMayorQue(BigDecimal saldo, Pageable pageable) {
        return cuentaClienteRepository.findBySaldoActualGreaterThan(saldo, pageable)
                .map(cuentaClienteMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CuentaClienteResponse> buscarPorSaldoMenorIgual(BigDecimal saldo, Pageable pageable) {
        return cuentaClienteRepository.findBySaldoActualLessThanEqual(saldo, pageable)
                .map(cuentaClienteMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CuentaClienteResponse> buscarPorFechaApertura(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        return cuentaClienteRepository.findByFechaAperturaBetween(fechaInicio, fechaFin, pageable)
                .map(cuentaClienteMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CuentaClienteResponse> buscarPorUsuario(Long usuarioId) {
        CuentaClienteEntity cuentaCliente = buscarEntidadPorUsuario(usuarioId);
        return Optional.of(cuentaClienteMapper.toResponse(cuentaCliente));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CuentaClienteResponse> buscarPorUsuarioEmail(String email) {
        CuentaClienteEntity cuenta = cuentaClienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada para el email: " + email));
        return Optional.of(cuentaClienteMapper.toResponse(cuenta));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorUsuario(Long usuarioId) {
        return cuentaClienteRepository.existsByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeRealizarCompra(Long cuentaId, BigDecimal montoCompra) {
        CuentaClienteEntity cuenta = buscarEntidadPorId(cuentaId);

        // Verificar que la cuenta esté activa
        if (cuenta.getEstado() != EstadoCuenta.ACTIVA) {
            return false;
        }

        // Calcular saldo disponible manualmente: límite de crédito - saldo actual
        BigDecimal saldoDisponible = cuenta.getLimiteCredito().subtract(cuenta.getSaldoActual());

        // Verificar que el saldo disponible sea suficiente
        return saldoDisponible.compareTo(montoCompra) >= 0;
    }

    @Override
    public CuentaClienteResponse actualizarLimiteCredito(Long id, BigDecimal nuevoLimite) {
        CuentaClienteEntity cuenta = buscarEntidadPorId(id);
        BigDecimal limiteAnterior = cuenta.getLimiteCredito();

        cuenta.setLimiteCredito(nuevoLimite);
        CuentaClienteEntity cuentaActualizada = cuentaClienteRepository.save(cuenta);

        // Crear movimiento de ajuste de límite
        movimientoCuentaService.crearMovimiento(cuenta.getId(), TipoMovimiento.AJUSTE,
                String.format("Ajuste de límite de crédito de $%.2f a $%.2f", limiteAnterior, nuevoLimite),
                BigDecimal.ZERO, cuenta.getSaldoActual(), cuenta.getSaldoActual(),
                TipoReferencia.AJUSTE, null, null); // Usuario ID se debería pasar desde el controller

        return cuentaClienteMapper.toResponse(cuentaActualizada);
    }

    @Override
    public CuentaClienteResponse cargarSaldo(Long id, BigDecimal monto, String concepto, Long usuarioId) {
        return cargarSaldoConReferencia(id, monto, concepto, usuarioId, TipoReferencia.AJUSTE, null);
    }

    @Override
    public CuentaClienteResponse cargarSaldoConReferencia(Long id, BigDecimal monto, String concepto, Long usuarioId,
                                                         TipoReferencia tipoReferencia, Long referenciaId) {
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("El monto a cargar debe ser mayor que cero");
        }

        CuentaClienteEntity cuenta = buscarEntidadPorId(id);
        BigDecimal saldoAnterior = cuenta.getSaldoActual();
        BigDecimal saldoNuevo = saldoAnterior.add(monto);

        cuenta.setSaldoActual(saldoNuevo);
        CuentaClienteEntity cuentaActualizada = cuentaClienteRepository.save(cuenta);

        // Crear movimiento de cargo CON referencia
        movimientoCuentaService.crearMovimiento(cuenta.getId(), TipoMovimiento.CARGO, concepto, monto,
                saldoAnterior, saldoNuevo, tipoReferencia, referenciaId, usuarioId);

        return cuentaClienteMapper.toResponse(cuentaActualizada);
    }

    @Override
    public CuentaClienteResponse abonarSaldo(Long id, BigDecimal monto, String concepto, Long usuarioId) {
        return abonarSaldoConReferencia(id, monto, concepto, usuarioId, TipoReferencia.AJUSTE, null);
    }

    @Override
    public CuentaClienteResponse abonarSaldoConReferencia(Long id, BigDecimal monto, String concepto, Long usuarioId,
                                                         TipoReferencia tipoReferencia, Long referenciaId) {
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("El monto a abonar debe ser mayor que cero");
        }

        CuentaClienteEntity cuenta = buscarEntidadPorId(id);
        BigDecimal saldoAnterior = cuenta.getSaldoActual();
        BigDecimal saldoNuevo = saldoAnterior.subtract(monto);

        // Verificar que no quede saldo negativo
        if (saldoNuevo.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException("El abono no puede ser mayor que el saldo actual");
        }

        cuenta.setSaldoActual(saldoNuevo);
        CuentaClienteEntity cuentaActualizada = cuentaClienteRepository.save(cuenta);

        // Crear movimiento de abono CON referencia
        movimientoCuentaService.crearMovimiento(cuenta.getId(), TipoMovimiento.ABONO, concepto, monto,
                saldoAnterior, saldoNuevo, tipoReferencia, referenciaId, usuarioId);

        return cuentaClienteMapper.toResponse(cuentaActualizada);
    }

    @Override
    public CuentaClienteResponse cambiarEstado(Long id, EstadoCuenta nuevoEstado) {
        CuentaClienteEntity cuenta = buscarEntidadPorId(id);
        cuenta.setEstado(nuevoEstado);
        CuentaClienteEntity cuentaActualizada = cuentaClienteRepository.save(cuenta);

        return cuentaClienteMapper.toResponse(cuentaActualizada);
    }

    @Override
    public CuentaClienteResponse activar(Long id) {
        return cambiarEstado(id, EstadoCuenta.ACTIVA);
    }

    @Override
    public CuentaClienteResponse suspender(Long id) {
        return cambiarEstado(id, EstadoCuenta.SUSPENDIDA);
    }

    @Override
    public CuentaClienteResponse cerrar(Long id) {
        CuentaClienteEntity cuenta = buscarEntidadPorId(id);

        // Verificar que no tenga saldo pendiente antes de cerrar
        if (cuenta.getSaldoActual().compareTo(BigDecimal.ZERO) != 0) {
            throw new UserHasPendingBalanceException("No se puede cerrar una cuenta con saldo pendiente");
        }

        return cambiarEstado(id, EstadoCuenta.CERRADA);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoDisponible(Long cuentaId) {
        CuentaClienteEntity cuenta = buscarEntidadPorId(cuentaId);
        return cuenta.getSaldoDisponible();
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaClienteEntity buscarEntidadPorId(Long id) {
        return cuentaClienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaClienteEntity buscarEntidadPorUsuario(Long usuarioId) {
        return cuentaClienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada para usuario ID: " + usuarioId));
    }
}
