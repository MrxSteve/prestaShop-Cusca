package com.cusca.shopmoney_pg.services.finance;

import com.cusca.shopmoney_pg.models.dto.response.MovimientoCuentaResponse;
import com.cusca.shopmoney_pg.models.entities.MovimientoCuentaEntity;
import com.cusca.shopmoney_pg.models.enums.TipoMovimiento;
import com.cusca.shopmoney_pg.models.enums.TipoReferencia;
import com.cusca.shopmoney_pg.repositories.MovimientoCuentaRepository;
import com.cusca.shopmoney_pg.repositories.CuentaClienteRepository;
import com.cusca.shopmoney_pg.repositories.UsuarioRepository;
import com.cusca.shopmoney_pg.utils.exceptions.ResourceNotFoundException;
import com.cusca.shopmoney_pg.utils.mappers.MovimientoCuentaMapper;
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
@Transactional(readOnly = true)
public class MovimientoCuentaServiceImpl implements IMovimientoCuentaService {
    private final MovimientoCuentaRepository movimientoCuentaRepository;
    private final CuentaClienteRepository cuentaClienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoCuentaMapper movimientoCuentaMapper;

    @Override
    public Optional<MovimientoCuentaResponse> buscarPorId(Long id) {
        MovimientoCuentaEntity movimiento = buscarEntidadPorId(id);
        return Optional.of(movimientoCuentaMapper.toResponse(movimiento));
    }

    @Override
    public Page<MovimientoCuentaResponse> listarTodos(Pageable pageable) {
        return movimientoCuentaRepository.findAll(pageable)
                .map(movimientoCuentaMapper::toResponse);
    }

    @Override
    public Page<MovimientoCuentaResponse> buscarPorCuentaCliente(Long cuentaClienteId, Pageable pageable) {
        return movimientoCuentaRepository.findByCuentaClienteId(cuentaClienteId, pageable)
                .map(movimientoCuentaMapper::toResponse);
    }

    @Override
    public Page<MovimientoCuentaResponse> buscarPorUsuario(Long usuarioId, Pageable pageable) {
        return movimientoCuentaRepository.findByCuentaClienteUsuarioId(usuarioId, pageable)
                .map(movimientoCuentaMapper::toResponse);
    }

    @Override
    public Page<MovimientoCuentaResponse> buscarPorTipoMovimiento(TipoMovimiento tipoMovimiento, Pageable pageable) {
        return movimientoCuentaRepository.findByTipoMovimiento(tipoMovimiento, pageable)
                .map(movimientoCuentaMapper::toResponse);
    }

    @Override
    public Page<MovimientoCuentaResponse> buscarPorClienteYTipo(Long cuentaClienteId, TipoMovimiento tipoMovimiento, Pageable pageable) {
        return movimientoCuentaRepository.findByCuentaClienteIdAndTipoMovimiento(cuentaClienteId, tipoMovimiento, pageable)
                .map(movimientoCuentaMapper::toResponse);
    }

    @Override
    public Page<MovimientoCuentaResponse> buscarPorFecha(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        // Convertir LocalDate a LocalDateTime para la consulta
        LocalDateTime fechaInicioDateTime = fechaInicio.atStartOfDay(); // 00:00:00
        LocalDateTime fechaFinDateTime = fechaFin.atTime(23, 59, 59); // 23:59:59

        return movimientoCuentaRepository.findByFechaMovimientoBetween(fechaInicioDateTime, fechaFinDateTime, pageable)
                .map(movimientoCuentaMapper::toResponse);
    }

    @Override
    public Page<MovimientoCuentaResponse> buscarPorClienteYFecha(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        // Convertir LocalDate a LocalDateTime para la consulta
        LocalDateTime fechaInicioDateTime = fechaInicio.atStartOfDay(); // 00:00:00
        LocalDateTime fechaFinDateTime = fechaFin.atTime(23, 59, 59); // 23:59:59

        return movimientoCuentaRepository.findByClienteAndFechaRange(clienteId, fechaInicioDateTime, fechaFinDateTime, pageable)
                .map(movimientoCuentaMapper::toResponse);
    }

    // ESTADOS DE CUENTA Y SALDOS

    @Override
    public BigDecimal obtenerSaldoActual(Long cuentaClienteId) {
        var cuenta = cuentaClienteRepository.findById(cuentaClienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta de cliente no encontrada con ID: " + cuentaClienteId));

        return cuenta.getSaldoActual();
    }

    @Override
    public Page<MovimientoCuentaResponse> obtenerEstadoCuenta(Long cuentaClienteId, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        // Verificar que la cuenta existe
        if (!cuentaClienteRepository.existsById(cuentaClienteId)) {
            throw new ResourceNotFoundException("Cuenta de cliente no encontrada con ID: " + cuentaClienteId);
        }

        return buscarPorClienteYFecha(cuentaClienteId, fechaInicio, fechaFin, pageable);
    }

    // ESTADÍSTICAS Y REPORTES

    @Override
    public BigDecimal obtenerTotalCargosDelDia() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDelDia = hoy.atStartOfDay();
        LocalDateTime finDelDia = hoy.atTime(23, 59, 59);

        return movimientoCuentaRepository.findByFechaMovimientoBetween(inicioDelDia, finDelDia, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(m -> m.getTipoMovimiento() == TipoMovimiento.CARGO)
                .map(MovimientoCuentaEntity::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal obtenerTotalAbonosDelDia() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDelDia = hoy.atStartOfDay();
        LocalDateTime finDelDia = hoy.atTime(23, 59, 59);

        return movimientoCuentaRepository.findByFechaMovimientoBetween(inicioDelDia, finDelDia, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(m -> m.getTipoMovimiento() == TipoMovimiento.ABONO)
                .map(MovimientoCuentaEntity::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal obtenerTotalCargosDelMes() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioDelMes = hoy.withDayOfMonth(1);
        LocalDate finDelMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

        LocalDateTime inicioDelMesDateTime = inicioDelMes.atStartOfDay();
        LocalDateTime finDelMesDateTime = finDelMes.atTime(23, 59, 59);

        return movimientoCuentaRepository.findByFechaMovimientoBetween(inicioDelMesDateTime, finDelMesDateTime, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(m -> m.getTipoMovimiento() == TipoMovimiento.CARGO)
                .map(MovimientoCuentaEntity::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal obtenerTotalAbonosDelMes() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioDelMes = hoy.withDayOfMonth(1);
        LocalDate finDelMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

        LocalDateTime inicioDelMesDateTime = inicioDelMes.atStartOfDay();
        LocalDateTime finDelMesDateTime = finDelMes.atTime(23, 59, 59);

        return movimientoCuentaRepository.findByFechaMovimientoBetween(inicioDelMesDateTime, finDelMesDateTime, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(m -> m.getTipoMovimiento() == TipoMovimiento.ABONO)
                .map(MovimientoCuentaEntity::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    @Override
    public Page<MovimientoCuentaResponse> buscarPorReferencia(TipoReferencia tipoReferencia, Long referenciaId, Pageable pageable) {
        return movimientoCuentaRepository.findByReferenciaTipoAndReferenciaId(tipoReferencia, referenciaId, pageable)
                .map(movimientoCuentaMapper::toResponse);
    }

    // CREACIÓN DE MOVIMIENTOS (PARA USO INTERNO)

    @Override
    @Transactional
    public MovimientoCuentaResponse crearMovimiento(Long cuentaClienteId, TipoMovimiento tipoMovimiento,
                                                  String concepto, BigDecimal monto,
                                                  BigDecimal saldoAnterior, BigDecimal saldoNuevo,
                                                  TipoReferencia referenciaTipo, Long referenciaId, Long usuarioId) {

        // Verificar que la cuenta existe
        var cuentaCliente = cuentaClienteRepository.findById(cuentaClienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta de cliente no encontrada con ID: " + cuentaClienteId));

        // Verificar que el usuario existe
        var usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        // Crear el movimiento
        MovimientoCuentaEntity movimiento = MovimientoCuentaEntity.builder()
                .cuentaCliente(cuentaCliente)
                .tipoMovimiento(tipoMovimiento)
                .concepto(concepto)
                .monto(monto)
                .saldoAnterior(saldoAnterior)
                .saldoNuevo(saldoNuevo)
                .referenciaTipo(referenciaTipo)
                .referenciaId(referenciaId)
                .usuario(usuario)
                .fechaMovimiento(LocalDateTime.now())
                .build();

        MovimientoCuentaEntity movimientoGuardado = movimientoCuentaRepository.save(movimiento);
        return movimientoCuentaMapper.toResponse(movimientoGuardado);
    }

    @Override
    public MovimientoCuentaEntity buscarEntidadPorId(Long id) {
        return movimientoCuentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento de cuenta no encontrado con ID: " + id));
    }
}
