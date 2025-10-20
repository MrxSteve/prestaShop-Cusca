package com.cusca.shopmoney_pg.services.sales;

import com.cusca.shopmoney_pg.models.dto.request.DetalleVentaRequest;
import com.cusca.shopmoney_pg.models.dto.request.VentaRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateVentaRequest;
import com.cusca.shopmoney_pg.models.dto.response.VentaResponse;
import com.cusca.shopmoney_pg.models.entities.CuentaClienteEntity;
import com.cusca.shopmoney_pg.models.entities.DetalleVentaEntity;
import com.cusca.shopmoney_pg.models.entities.ProductoEntity;
import com.cusca.shopmoney_pg.models.entities.VentaEntity;
import com.cusca.shopmoney_pg.models.enums.EstadoVenta;
import com.cusca.shopmoney_pg.models.enums.TipoReferencia;
import com.cusca.shopmoney_pg.models.enums.TipoVenta;
import com.cusca.shopmoney_pg.repositories.*;
import com.cusca.shopmoney_pg.services.account.ICuentaClienteService;
import com.cusca.shopmoney_pg.utils.exceptions.InsufficientBalanceException;
import com.cusca.shopmoney_pg.utils.exceptions.InvalidSaleStateException;
import com.cusca.shopmoney_pg.utils.exceptions.InvalidSaleTypeException;
import com.cusca.shopmoney_pg.utils.exceptions.ResourceNotFoundException;
import com.cusca.shopmoney_pg.utils.mappers.DetalleVentaMapper;
import com.cusca.shopmoney_pg.utils.mappers.VentaMapper;
import com.cusca.shopmoney_pg.services.notification.NotificacionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class VentaServiceImpl implements IVentaService{
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final CuentaClienteRepository cuentaClienteRepository;
    private final ICuentaClienteService cuentaClienteService;
    private final VentaMapper ventaMapper;
    private final DetalleVentaMapper detalleVentaMapper;
    private final NotificacionServiceImpl notificacionService;

    @Override
    public VentaResponse crear(VentaRequest request) {
        // Primero calcular totales basado en los productos y cantidades
        BigDecimal totalCalculado = calcularTotalVenta(request.getDetalleVentas());

        // Determinar el tipo de venta y procesar según corresponda
        if (request.getTipoVenta() == TipoVenta.CREDITO) {
            return procesarVentaCredito(request, totalCalculado);
        } else {
            return procesarVentaContado(request, totalCalculado);
        }
    }

    private VentaResponse procesarVentaCredito(VentaRequest request, BigDecimal totalCalculado) {
        // Validar que se especifique una cuenta de cliente
        if (request.getCuentaClienteId() == null) {
            throw new InvalidSaleTypeException("Para ventas a crédito se debe especificar una cuenta de cliente");
        }

        // Verificar que la cuenta existe y puede realizar la compra
        CuentaClienteEntity cuenta = cuentaClienteRepository.findById(request.getCuentaClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta de cliente no encontrada"));

        // Verificar que la cuenta puede realizar la compra
        if (!cuentaClienteService.puedeRealizarCompra(cuenta.getId(), totalCalculado)) {
            throw new InsufficientBalanceException("La cuenta no tiene crédito suficiente para realizar esta compra. " +
                    "Saldo disponible: $" + cuenta.getSaldoDisponible());
        }

        // Crear la venta
        VentaEntity venta = crearVentaBase(request, cuenta, null, totalCalculado);
        venta.setEstado(EstadoVenta.PENDIENTE); // Las ventas a crédito inician como PENDIENTE

        VentaEntity ventaGuardada = ventaRepository.save(venta);

        // Procesar detalles de venta
        procesarDetallesVenta(ventaGuardada, request.getDetalleVentas());

        // Cargar el monto a la cuenta del cliente usando el ID de la venta como referencia
        cuentaClienteService.cargarSaldoConReferencia(cuenta.getId(), totalCalculado,
                "Venta #" + ventaGuardada.getId(), cuenta.getUsuario().getId(),
                TipoReferencia.VENTA, ventaGuardada.getId());

        // FACTURA POR CORREO (VENTA A CRÉDITO)
        notificacionService.enviarFacturaVenta(ventaGuardada);

        return ventaMapper.toResponse(ventaGuardada);
    }

    private VentaResponse procesarVentaContado(VentaRequest request, BigDecimal totalCalculado) {
        CuentaClienteEntity cuenta = null;
        String clienteOcasional = null;

        // Puede ser cliente con cuenta o cliente ocasional
        if (request.getCuentaClienteId() != null) {
            cuenta = cuentaClienteRepository.findById(request.getCuentaClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cuenta de cliente no encontrada"));
        } else {
            // Validar que se especifique el nombre del cliente ocasional
            if (request.getClienteOcasional() == null || request.getClienteOcasional().trim().isEmpty()) {
                throw new InvalidSaleTypeException("Para ventas al contado sin cuenta se debe especificar el nombre del cliente");
            }
            clienteOcasional = request.getClienteOcasional();
        }

        // Crear la venta
        VentaEntity venta = crearVentaBase(request, cuenta, clienteOcasional, totalCalculado);
        venta.setEstado(EstadoVenta.PAGADA); // Las ventas al contado se marcan como PAGADA inmediatamente

        VentaEntity ventaGuardada = ventaRepository.save(venta);

        // Procesar detalles de venta
        procesarDetallesVenta(ventaGuardada, request.getDetalleVentas());

        // FACTURA POR CORREO (VENTA DE CONTADO - SOLO SI TIENE CUENTA)
        if (cuenta != null) { // Solo enviar correo si el cliente tiene cuenta registrada
            notificacionService.enviarFacturaVenta(ventaGuardada);
        }

        return ventaMapper.toResponse(ventaGuardada);
    }

    @Override
    public VentaResponse actualizar(Long id, UpdateVentaRequest request) {
        VentaEntity venta = buscarEntidadPorId(id);

        // Validar que la venta se puede modificar
        if (!puedeModificar(id)) {
            throw new InvalidSaleStateException("La venta no se puede modificar en su estado actual: " + venta.getEstado());
        }

        ventaMapper.updateEntity(venta, request);
        VentaEntity ventaActualizada = ventaRepository.save(venta);

        return ventaMapper.toResponse(ventaActualizada);
    }

    @Override
    public void eliminar(Long id) {
        VentaEntity venta = buscarEntidadPorId(id);

        // Solo se pueden eliminar ventas CANCELADAS
        if (venta.getEstado() != EstadoVenta.CANCELADA) {
            throw new InvalidSaleStateException("Solo se pueden eliminar ventas canceladas");
        }

        ventaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VentaResponse> buscarPorId(Long id) {
        VentaEntity venta = buscarEntidadPorId(id);
        return Optional.of(ventaMapper.toResponse(venta));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> listarTodos(Pageable pageable) {
        return ventaRepository.findAll(pageable)
                .map(ventaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> buscarPorCuentaCliente(Long cuentaClienteId, Pageable pageable) {
        return ventaRepository.findByCuentaClienteId(cuentaClienteId, pageable)
                .map(ventaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> buscarPorUsuario(Long usuarioId, Pageable pageable) {
        return ventaRepository.findByCuentaClienteUsuarioId(usuarioId, pageable)
                .map(ventaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> buscarPorTipoVenta(TipoVenta tipoVenta, Pageable pageable) {
        return ventaRepository.findByTipoVenta(tipoVenta, pageable)
                .map(ventaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> buscarPorEstado(EstadoVenta estado, Pageable pageable) {
        return ventaRepository.findByEstado(estado, pageable)
                .map(ventaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> buscarPorFecha(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        // Convertir LocalDate a LocalDateTime para la consulta
        LocalDateTime fechaInicioDateTime = fechaInicio.atStartOfDay(); // 00:00:00
        LocalDateTime fechaFinDateTime = fechaFin.atTime(23, 59, 59); // 23:59:59

        return ventaRepository.findByFechaVentaBetween(fechaInicioDateTime, fechaFinDateTime, pageable)
                .map(ventaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> buscarPorRangoTotal(BigDecimal montoMin, BigDecimal montoMax, Pageable pageable) {
        return ventaRepository.findByTotalBetween(montoMin, montoMax, pageable)
                .map(ventaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> buscarPorClienteOcasional(String clienteOcasional, Pageable pageable) {
        return ventaRepository.findByClienteOcasionalContainingIgnoreCase(clienteOcasional, pageable)
                .map(ventaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> buscarPorClienteYEstado(Long cuentaClienteId, EstadoVenta estado, Pageable pageable) {
        return ventaRepository.findByCuentaClienteIdAndEstado(cuentaClienteId, estado, pageable)
                .map(ventaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> buscarPorClienteYFecha(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        // Convertir LocalDate a LocalDateTime para la consulta
        LocalDateTime fechaInicioDateTime = fechaInicio.atStartOfDay(); // 00:00:00
        LocalDateTime fechaFinDateTime = fechaFin.atTime(23, 59, 59); // 23:59:59

        return ventaRepository.findByClienteAndFechaRange(clienteId, fechaInicioDateTime, fechaFinDateTime, pageable)
                .map(ventaMapper::toResponse);
    }

    // VALIDACIONES

    @Override
    @Transactional(readOnly = true)
    public boolean puedeModificar(Long ventaId) {
        VentaEntity venta = buscarEntidadPorId(ventaId);

        // Solo se pueden modificar ventas PENDIENTES
        return venta.getEstado() == EstadoVenta.PENDIENTE;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeAnular(Long ventaId) {
        VentaEntity venta = buscarEntidadPorId(ventaId);

        // Se pueden anular ventas PENDIENTES y PARCIALES
        return venta.getEstado() == EstadoVenta.PENDIENTE || venta.getEstado() == EstadoVenta.PARCIAL;
    }

    // GESTIÓN DE ESTADO

    @Override
    public VentaResponse cambiarEstado(Long id, EstadoVenta nuevoEstado) {
        VentaEntity venta = buscarEntidadPorId(id);
        EstadoVenta estadoAnterior = venta.getEstado();

        // Validar transiciones de estado válidas
        validarTransicionEstado(estadoAnterior, nuevoEstado);

        venta.setEstado(nuevoEstado);
        VentaEntity ventaActualizada = ventaRepository.save(venta);

        return ventaMapper.toResponse(ventaActualizada);
    }

    @Override
    public VentaResponse marcarComoPagada(Long id) {
        VentaEntity venta = buscarEntidadPorId(id);

        // Si es venta a crédito, crear abono automático con referencia a la venta
        if (venta.getTipoVenta() == TipoVenta.CREDITO && venta.getCuentaCliente() != null) {
            cuentaClienteService.abonarSaldoConReferencia(venta.getCuentaCliente().getId(),
                    venta.getTotal(), "Pago venta #" + venta.getId(),
                    venta.getCuentaCliente().getUsuario().getId(),
                    TipoReferencia.VENTA, venta.getId());
        }

        return cambiarEstado(id, EstadoVenta.PAGADA);
    }

    @Override
    public VentaResponse marcarComoParcial(Long id) {
        return cambiarEstado(id, EstadoVenta.PARCIAL);
    }

    @Override
    public VentaResponse cancelar(Long id) {
        VentaEntity venta = buscarEntidadPorId(id);

        // Si es venta a crédito pendiente, revertir el cargo con referencia a la venta
        if (venta.getTipoVenta() == TipoVenta.CREDITO &&
                venta.getCuentaCliente() != null &&
                venta.getEstado() == EstadoVenta.PENDIENTE) {

            cuentaClienteService.abonarSaldoConReferencia(venta.getCuentaCliente().getId(),
                    venta.getTotal(), "Cancelación venta #" + venta.getId(),
                    venta.getCuentaCliente().getUsuario().getId(),
                    TipoReferencia.VENTA, venta.getId());
        }

        return cambiarEstado(id, EstadoVenta.CANCELADA);
    }

    @Override
    public VentaResponse recalcularTotales(Long id) {
        VentaEntity venta = buscarEntidadPorId(id);

        // Solo se pueden recalcular ventas PENDIENTES
        if (!puedeModificar(id)) {
            throw new InvalidSaleStateException("Solo se pueden recalcular totales de ventas PENDIENTES");
        }

        // Recalcular subtotal y total basado en los detalles
        BigDecimal nuevoSubtotal = venta.getDetalleVentas().stream()
                .map(DetalleVentaEntity::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        venta.setSubtotal(nuevoSubtotal);
        venta.setTotal(nuevoSubtotal); // Por ahora sin impuestos adicionales

        VentaEntity ventaActualizada = ventaRepository.save(venta);

        return ventaMapper.toResponse(ventaActualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaEntity buscarEntidadPorId(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VentaResponse> buscarMiVentaPorId(Long ventaId, String emailUsuario) {
        // Buscar la venta
        VentaEntity venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con ID: " + ventaId));

        // Verificar que la venta pertenece al usuario autenticado
        if (venta.getCuentaCliente() == null) {
            throw new ResourceNotFoundException("No tienes permiso para ver esta compra");
        }

        if (!venta.getCuentaCliente().getUsuario().getEmail().equals(emailUsuario)) {
            throw new ResourceNotFoundException("No tienes permiso para ver esta compra");
        }

        return Optional.of(ventaMapper.toResponse(venta));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> buscarMisCompras(String emailUsuario, Pageable pageable) {
        // Buscar la cuenta del cliente por email
        var cuenta = cuentaClienteService.buscarPorUsuarioEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes una cuenta de crédito creada"));

        return ventaRepository.findByCuentaClienteId(cuenta.getId(), pageable)
                .map(ventaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> buscarMisComprasPorEstado(String emailUsuario, EstadoVenta estado, Pageable pageable) {
        // Buscar la cuenta del cliente por email
        var cuenta = cuentaClienteService.buscarPorUsuarioEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes una cuenta de crédito creada"));

        return ventaRepository.findByCuentaClienteIdAndEstado(cuenta.getId(), estado, pageable)
                .map(ventaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> buscarMisComprasPorFecha(String emailUsuario, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        // Buscar la cuenta del cliente por email
        var cuenta = cuentaClienteService.buscarPorUsuarioEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes una cuenta de crédito creada"));

        // Convertir LocalDate a LocalDateTime para la consulta
        LocalDateTime fechaInicioDateTime = fechaInicio.atStartOfDay(); // 00:00:00
        LocalDateTime fechaFinDateTime = fechaFin.atTime(23, 59, 59); // 23:59:59

        return ventaRepository.findByClienteAndFechaRange(cuenta.getId(), fechaInicioDateTime, fechaFinDateTime, pageable)
                .map(ventaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> buscarMisComprasPendientes(String emailUsuario, Pageable pageable) {
        // Buscar la cuenta del cliente por email
        var cuenta = cuentaClienteService.buscarPorUsuarioEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes una cuenta de crédito creada"));

        return ventaRepository.findByCuentaClienteIdAndEstado(cuenta.getId(), EstadoVenta.PENDIENTE, pageable)
                .map(ventaMapper::toResponse);
    }

    // MÉTODOS AUXILIARES

    private VentaEntity crearVentaBase(VentaRequest request, CuentaClienteEntity cuenta, String clienteOcasional, BigDecimal totalCalculado) {
        VentaEntity venta = ventaMapper.toEntity(request);
        venta.setCuentaCliente(cuenta);
        venta.setClienteOcasional(clienteOcasional);
        venta.setFechaVenta(LocalDateTime.now());
        venta.setTotal(totalCalculado);
        venta.setSubtotal(totalCalculado); // Establecer el subtotal igual al total por ahora

        // Establecer estado por defecto si no se proporciona
        if (venta.getEstado() == null) {
            venta.setEstado(EstadoVenta.PENDIENTE);
        }

        return venta;
    }

    private void procesarDetallesVenta(VentaEntity venta, List<DetalleVentaRequest> detallesRequest) {
        List<DetalleVentaEntity> detalles = new ArrayList<>();
        BigDecimal subtotalVenta = BigDecimal.ZERO;

        for (DetalleVentaRequest detalleRequest : detallesRequest) {
            // Verificar que el producto existe
            ProductoEntity producto = productoRepository.findById(detalleRequest.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + detalleRequest.getProductoId()));

            DetalleVentaEntity detalle = detalleVentaMapper.toEntity(detalleRequest);
            detalle.setVenta(venta);
            detalle.setProducto(producto);

            // Establecer el precio unitario del producto automáticamente
            detalle.setPrecioUnitario(producto.getPrecioUnitario());
            detalle.setCantidad(detalleRequest.getCantidad());

            // Calcular subtotal automáticamente
            BigDecimal subtotalDetalle = producto.getPrecioUnitario().multiply(BigDecimal.valueOf(detalleRequest.getCantidad()));
            detalle.setSubtotal(subtotalDetalle);

            subtotalVenta = subtotalVenta.add(subtotalDetalle);
            detalles.add(detalle);
        }

        venta.setDetalleVentas(detalles);
        venta.setSubtotal(subtotalVenta);
    }

    private void validarTransicionEstado(EstadoVenta estadoActual, EstadoVenta nuevoEstado) {
        // Definir transiciones válidas
        boolean transicionValida = switch (estadoActual) {
            case PENDIENTE -> nuevoEstado == EstadoVenta.PAGADA ||
                    nuevoEstado == EstadoVenta.PARCIAL ||
                    nuevoEstado == EstadoVenta.CANCELADA;
            case PARCIAL -> nuevoEstado == EstadoVenta.PAGADA ||
                    nuevoEstado == EstadoVenta.CANCELADA;
            case PAGADA -> false; // Las ventas pagadas no se pueden cambiar
            case CANCELADA -> false; // Las ventas canceladas no se pueden cambiar
        };

        if (!transicionValida) {
            throw new InvalidSaleStateException(String.format("No se puede cambiar el estado de %s a %s",
                    estadoActual, nuevoEstado));
        }
    }

    private BigDecimal calcularTotalVenta(List<DetalleVentaRequest> detallesRequest) {
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleVentaRequest detalleRequest : detallesRequest) {
            // Obtener el producto para extraer su precio
            ProductoEntity producto = productoRepository.findById(detalleRequest.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + detalleRequest.getProductoId()));

            // Calcular subtotal usando el precio del producto
            BigDecimal subtotal = producto.getPrecioUnitario().multiply(BigDecimal.valueOf(detalleRequest.getCantidad()));
            total = total.add(subtotal);
        }

        return total;
    }
}
