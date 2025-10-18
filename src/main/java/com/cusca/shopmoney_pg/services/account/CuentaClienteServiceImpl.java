package com.cusca.shopmoney_pg.services.account;

import com.cusca.shopmoney_pg.models.dto.request.CuentaClienteRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateCuentaClienteRequest;
import com.cusca.shopmoney_pg.models.dto.response.CuentaClienteResponse;
import com.cusca.shopmoney_pg.models.entities.CuentaClienteEntity;
import com.cusca.shopmoney_pg.models.entities.MovimientoCuentaEntity;
import com.cusca.shopmoney_pg.models.entities.UsuarioEntity;
import com.cusca.shopmoney_pg.models.enums.EstadoCuenta;
import com.cusca.shopmoney_pg.models.enums.TipoMovimiento;
import com.cusca.shopmoney_pg.repositories.CuentaClienteRepository;
import com.cusca.shopmoney_pg.repositories.MovimientoCuentaRepository;
import com.cusca.shopmoney_pg.repositories.UsuarioRepository;
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
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CuentaClienteServiceImpl implements ICuentaClienteService {
    private final CuentaClienteRepository cuentaClienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoCuentaRepository movimientoCuentaRepository;
    private final CuentaClienteMapper cuentaClienteMapper;

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
            crearMovimiento(cuentaGuardada, TipoMovimiento.CARGO, "Saldo inicial",
                    cuentaGuardada.getSaldoActual(), BigDecimal.ZERO, cuentaGuardada.getSaldoActual(),
                    null, usuario.getId());
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

        // Verificar que tenga crédito disponible
        BigDecimal saldoDisponible = cuenta.getSaldoDisponible();
        return saldoDisponible.compareTo(montoCompra) >= 0;
    }

    @Override
    public CuentaClienteResponse actualizarLimiteCredito(Long id, BigDecimal nuevoLimite) {
        CuentaClienteEntity cuenta = buscarEntidadPorId(id);
        BigDecimal limiteAnterior = cuenta.getLimiteCredito();

        cuenta.setLimiteCredito(nuevoLimite);
        CuentaClienteEntity cuentaActualizada = cuentaClienteRepository.save(cuenta);

        // Crear movimiento de ajuste de límite
        crearMovimiento(cuenta, TipoMovimiento.AJUSTE,
                String.format("Ajuste de límite de crédito de $%.2f a $%.2f", limiteAnterior, nuevoLimite),
                BigDecimal.ZERO, cuenta.getSaldoActual(), cuenta.getSaldoActual(),
                null, null); // Usuario ID se debería pasar desde el controller

        return cuentaClienteMapper.toResponse(cuentaActualizada);
    }

    @Override
    public CuentaClienteResponse cargarSaldo(Long id, BigDecimal monto, String concepto, Long usuarioId) {
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("El monto a cargar debe ser mayor que cero");
        }

        CuentaClienteEntity cuenta = buscarEntidadPorId(id);
        BigDecimal saldoAnterior = cuenta.getSaldoActual();
        BigDecimal saldoNuevo = saldoAnterior.add(monto);

        cuenta.setSaldoActual(saldoNuevo);
        CuentaClienteEntity cuentaActualizada = cuentaClienteRepository.save(cuenta);

        // Crear movimiento de cargo
        crearMovimiento(cuenta, TipoMovimiento.CARGO, concepto, monto,
                saldoAnterior, saldoNuevo, null, usuarioId);

        return cuentaClienteMapper.toResponse(cuentaActualizada);
    }

    @Override
    public CuentaClienteResponse abonarSaldo(Long id, BigDecimal monto, String concepto, Long usuarioId) {
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

        // Crear movimiento de abono
        crearMovimiento(cuenta, TipoMovimiento.ABONO, concepto, monto,
                saldoAnterior, saldoNuevo, null, usuarioId);

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


    // Metodo auxiliar para crear movimientos de cuenta
    private void crearMovimiento(CuentaClienteEntity cuenta, TipoMovimiento tipo, String concepto,
                                 BigDecimal monto, BigDecimal saldoAnterior, BigDecimal saldoNuevo,
                                 String referenciaTipo, Long usuarioId) {
        MovimientoCuentaEntity movimiento = MovimientoCuentaEntity.builder()
                .cuentaCliente(cuenta)
                .tipoMovimiento(tipo)
                .concepto(concepto)
                .monto(monto)
                .saldoAnterior(saldoAnterior)
                .saldoNuevo(saldoNuevo)
                .fechaMovimiento(LocalDateTime.now())
                .build();

        if (usuarioId != null) {
            UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));
            movimiento.setUsuario(usuario);
        }

        movimientoCuentaRepository.save(movimiento);
    }
}
