package com.cusca.shopmoney_pg.services.finance;

import com.cusca.shopmoney_pg.models.dto.request.AbonoRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateAbonoRequest;
import com.cusca.shopmoney_pg.models.dto.response.AbonoResponse;
import com.cusca.shopmoney_pg.models.entities.AbonoEntity;
import com.cusca.shopmoney_pg.models.entities.CuentaClienteEntity;
import com.cusca.shopmoney_pg.models.enums.EstadoAbono;
import com.cusca.shopmoney_pg.models.enums.MetodoPago;
import com.cusca.shopmoney_pg.repositories.AbonoRepository;
import com.cusca.shopmoney_pg.repositories.CuentaClienteRepository;
import com.cusca.shopmoney_pg.services.account.ICuentaClienteService;
import com.cusca.shopmoney_pg.utils.exceptions.InvalidSaleStateException;
import com.cusca.shopmoney_pg.utils.exceptions.ResourceNotFoundException;
import com.cusca.shopmoney_pg.utils.mappers.AbonoMapper;
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
public class AbonoServiceImpl implements IAbonoService {
    private final AbonoRepository abonoRepository;
    private final CuentaClienteRepository cuentaClienteRepository;
    private final ICuentaClienteService cuentaClienteService;
    private final AbonoMapper abonoMapper;

    @Override
    public AbonoResponse crear(AbonoRequest request) {
        // Verificar que la cuenta del cliente existe
        CuentaClienteEntity cuentaCliente = cuentaClienteRepository.findById(request.getCuentaClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta de cliente no encontrada con ID: " + request.getCuentaClienteId()));

        // Crear el abono
        AbonoEntity abono = abonoMapper.toEntity(request);
        abono.setCuentaCliente(cuentaCliente);
        abono.setFechaAbono(LocalDateTime.now());

        // El estado por defecto es APLICADO, pero se puede procesar según la lógica de negocio
        if (abono.getEstado() == null) {
            abono.setEstado(EstadoAbono.APLICADO);
        }

        AbonoEntity abonoGuardado = abonoRepository.save(abono);

        // Si el abono está en estado APLICADO, aplicarlo automáticamente a la cuenta
        if (abonoGuardado.getEstado() == EstadoAbono.APLICADO) {
            cuentaClienteService.abonarSaldo(cuentaCliente.getId(), abono.getMonto(),
                    "Abono #" + abonoGuardado.getId(), cuentaCliente.getUsuario().getId());
        }

        return abonoMapper.toResponse(abonoGuardado);
    }

    @Override
    public AbonoResponse actualizar(Long id, UpdateAbonoRequest request) {
        AbonoEntity abono = buscarEntidadPorId(id);

        // Validar que el abono se puede modificar
        if (!puedeModificar(id)) {
            throw new InvalidSaleStateException("El abono no se puede modificar en su estado actual: " + abono.getEstado());
        }

        abonoMapper.updateEntity(abono, request);
        AbonoEntity abonoActualizado = abonoRepository.save(abono);

        return abonoMapper.toResponse(abonoActualizado);
    }

    @Override
    public void eliminar(Long id) {
        AbonoEntity abono = buscarEntidadPorId(id);

        // Solo se pueden eliminar abonos RECHAZADOS o PENDIENTES
        if (abono.getEstado() == EstadoAbono.APLICADO) {
            throw new InvalidSaleStateException("No se pueden eliminar abonos aplicados");
        }

        abonoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AbonoResponse> buscarPorId(Long id) {
        AbonoEntity abono = buscarEntidadPorId(id);
        return Optional.of(abonoMapper.toResponse(abono));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AbonoResponse> listarTodos(Pageable pageable) {
        return abonoRepository.findAll(pageable)
                .map(abonoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AbonoResponse> buscarPorCuentaCliente(Long cuentaClienteId, Pageable pageable) {
        return abonoRepository.findByCuentaClienteId(cuentaClienteId, pageable)
                .map(abonoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AbonoResponse> buscarPorUsuario(Long usuarioId, Pageable pageable) {
        return abonoRepository.findByCuentaClienteUsuarioId(usuarioId, pageable)
                .map(abonoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AbonoResponse> buscarPorEstado(EstadoAbono estado, Pageable pageable) {
        return abonoRepository.findByEstado(estado, pageable)
                .map(abonoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AbonoResponse> buscarPorMetodoPago(MetodoPago metodoPago, Pageable pageable) {
        return abonoRepository.findByMetodoPago(metodoPago, pageable)
                .map(abonoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AbonoResponse> buscarPorClienteYEstado(Long cuentaClienteId, EstadoAbono estado, Pageable pageable) {
        return abonoRepository.findByCuentaClienteIdAndEstado(cuentaClienteId, estado, pageable)
                .map(abonoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AbonoResponse> buscarPorFecha(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        // Convertir LocalDate a LocalDateTime para la consulta
        LocalDateTime fechaInicioDateTime = fechaInicio.atStartOfDay(); // 00:00:00
        LocalDateTime fechaFinDateTime = fechaFin.atTime(23, 59, 59); // 23:59:59

        return abonoRepository.findByFechaAbonoBetween(fechaInicioDateTime, fechaFinDateTime, pageable)
                .map(abonoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AbonoResponse> buscarPorRangoMonto(BigDecimal montoMin, BigDecimal montoMax, Pageable pageable) {
        return abonoRepository.findByMontoBetween(montoMin, montoMax, pageable)
                .map(abonoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeModificar(Long abonoId) {
        AbonoEntity abono = buscarEntidadPorId(abonoId);
        // Solo se pueden modificar abonos PENDIENTES
        return abono.getEstado() == EstadoAbono.PENDIENTE;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeAplicar(Long abonoId) {
        AbonoEntity abono = buscarEntidadPorId(abonoId);
        // Solo se pueden aplicar abonos PENDIENTES
        return abono.getEstado() == EstadoAbono.PENDIENTE;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeRechazar(Long abonoId) {
        AbonoEntity abono = buscarEntidadPorId(abonoId);
        // Solo se pueden rechazar abonos PENDIENTES
        return abono.getEstado() == EstadoAbono.PENDIENTE;
    }

    @Override
    public AbonoResponse cambiarEstado(Long id, EstadoAbono nuevoEstado) {
        AbonoEntity abono = buscarEntidadPorId(id);
        EstadoAbono estadoAnterior = abono.getEstado();

        // Validar transiciones de estado válidas
        validarTransicionEstado(estadoAnterior, nuevoEstado);

        abono.setEstado(nuevoEstado);
        AbonoEntity abonoActualizado = abonoRepository.save(abono);

        return abonoMapper.toResponse(abonoActualizado);
    }

    @Override
    public AbonoResponse aplicar(Long id) {
        AbonoEntity abono = buscarEntidadPorId(id);

        if (!puedeAplicar(id)) {
            throw new InvalidSaleStateException("El abono no se puede aplicar en su estado actual: " + abono.getEstado());
        }

        // Aplicar el abono a la cuenta del cliente
        cuentaClienteService.abonarSaldo(abono.getCuentaCliente().getId(), abono.getMonto(),
                "Aplicación abono #" + abono.getId(), abono.getCuentaCliente().getUsuario().getId());

        return cambiarEstado(id, EstadoAbono.APLICADO);
    }

    @Override
    public AbonoResponse marcarComoPendiente(Long id) {
        return cambiarEstado(id, EstadoAbono.PENDIENTE);
    }

    @Override
    public AbonoResponse rechazar(Long id, String motivo) {
        AbonoEntity abono = buscarEntidadPorId(id);

        if (!puedeRechazar(id)) {
            throw new InvalidSaleStateException("El abono no se puede rechazar en su estado actual: " + abono.getEstado());
        }

        // Agregar motivo de rechazo a las observaciones
        String observacionesActuales = abono.getObservaciones() != null ? abono.getObservaciones() : "";
        abono.setObservaciones(observacionesActuales + "\nRECHAZADO: " + motivo);
        abonoRepository.save(abono);

        return cambiarEstado(id, EstadoAbono.RECHAZADO);
    }

    // OPERACIONES DE ABONO

    @Override
    public AbonoResponse procesarAbono(AbonoRequest request) {
        return crear(request);
    }

    // METODOS PARA CLIENTES

    @Override
    @Transactional(readOnly = true)
    public Optional<AbonoResponse> buscarMiAbonoPorId(Long abonoId, String emailUsuario) {
        AbonoEntity abono = abonoRepository.findById(abonoId)
                .orElseThrow(() -> new ResourceNotFoundException("Abono no encontrado con ID: " + abonoId));

        // Verificar que el abono pertenece al usuario autenticado
        if (abono.getCuentaCliente() == null) {
            throw new ResourceNotFoundException("No tienes permiso para ver este abono");
        }

        if (!abono.getCuentaCliente().getUsuario().getEmail().equals(emailUsuario)) {
            throw new ResourceNotFoundException("No tienes permiso para ver este abono");
        }

        return Optional.of(abonoMapper.toResponse(abono));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AbonoResponse> buscarMisAbonos(String emailUsuario, Pageable pageable) {
        var cuenta = cuentaClienteService.buscarPorUsuarioEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes una cuenta de crédito creada"));

        return abonoRepository.findByCuentaClienteId(cuenta.getId(), pageable)
                .map(abonoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AbonoResponse> buscarMisAbonosPorEstado(String emailUsuario, EstadoAbono estado, Pageable pageable) {
        var cuenta = cuentaClienteService.buscarPorUsuarioEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes una cuenta de crédito creada"));

        return abonoRepository.findByCuentaClienteIdAndEstado(cuenta.getId(), estado, pageable)
                .map(abonoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AbonoResponse> buscarMisAbonosPorFecha(String emailUsuario, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        var cuenta = cuentaClienteService.buscarPorUsuarioEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes una cuenta de crédito creada"));

        // Convertir LocalDate a LocalDateTime para la consulta
        LocalDateTime fechaInicioDateTime = fechaInicio.atStartOfDay(); // 00:00:00
        LocalDateTime fechaFinDateTime = fechaFin.atTime(23, 59, 59); // 23:59:59

        return abonoRepository.findByClienteAndFechaRange(cuenta.getId(), fechaInicioDateTime, fechaFinDateTime, pageable)
                .map(abonoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AbonoResponse> buscarMisAbonosPendientes(String emailUsuario, Pageable pageable) {
        var cuenta = cuentaClienteService.buscarPorUsuarioEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes una cuenta de crédito creada"));

        return abonoRepository.findByCuentaClienteIdAndEstado(cuenta.getId(), EstadoAbono.PENDIENTE, pageable)
                .map(abonoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AbonoEntity buscarEntidadPorId(Long id) {
        return abonoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abono no encontrado con ID: " + id));
    }

    // MÉTODOS AUXILIARES PRIVADOS

    private void validarTransicionEstado(EstadoAbono estadoActual, EstadoAbono nuevoEstado) {
        // Definir transiciones válidas
        boolean transicionValida = switch (estadoActual) {
            case PENDIENTE -> nuevoEstado == EstadoAbono.APLICADO || nuevoEstado == EstadoAbono.RECHAZADO;
            case APLICADO -> false; // Los abonos aplicados no se pueden cambiar
            case RECHAZADO -> nuevoEstado == EstadoAbono.PENDIENTE; // Se puede reactivar un abono rechazado
        };

        if (!transicionValida) {
            throw new InvalidSaleStateException(String.format("No se puede cambiar el estado de %s a %s",
                    estadoActual, nuevoEstado));
        }
    }
}
