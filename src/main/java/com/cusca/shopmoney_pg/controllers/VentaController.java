package com.cusca.shopmoney_pg.controllers;

import com.cusca.shopmoney_pg.models.dto.request.VentaRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateVentaRequest;
import com.cusca.shopmoney_pg.models.dto.response.VentaResponse;
import com.cusca.shopmoney_pg.models.enums.EstadoVenta;
import com.cusca.shopmoney_pg.models.enums.TipoVenta;
import com.cusca.shopmoney_pg.services.sales.IVentaService;
import com.cusca.shopmoney_pg.services.account.ICuentaClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "Gestión de ventas y facturación")
public class VentaController {
    private final IVentaService ventaService;

    // ENDPOINTS PARA ADMINISTRADORES

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear nueva venta", description = "Permite crear una nueva venta (crédito o contado) - Solo ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Venta creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o crédito insuficiente"),
        @ApiResponse(responseCode = "404", description = "Cuenta de cliente o producto no encontrado")
    })
    public ResponseEntity<VentaResponse> crear(@Valid @RequestBody VentaRequest request) {
        VentaResponse response = ventaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas las ventas", description = "Obtiene todas las ventas con paginación - Solo ADMIN")
    @ApiResponse(responseCode = "200", description = "Ventas obtenidas exitosamente")
    public ResponseEntity<Page<VentaResponse>> listarTodas(
            @PageableDefault(size = 10) @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        Page<VentaResponse> ventas = ventaService.listarTodos(pageable);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener venta por ID", description = "Obtiene una venta específica por su ID - Solo ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Venta encontrada"),
        @ApiResponse(responseCode = "404", description = "Venta no encontrada")
    })
    public ResponseEntity<VentaResponse> obtenerPorId(@PathVariable Long id) {
        return ventaService.buscarPorId(id)
                .map(venta -> ResponseEntity.ok(venta))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar venta", description = "Actualiza una venta existente (solo PENDIENTES) - Solo ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Venta actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "No se puede modificar la venta en su estado actual"),
        @ApiResponse(responseCode = "404", description = "Venta no encontrada")
    })
    public ResponseEntity<VentaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVentaRequest request) {
        VentaResponse response = ventaService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar venta", description = "Elimina una venta (solo CANCELADAS) - Solo ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Venta eliminada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solo se pueden eliminar ventas canceladas"),
        @ApiResponse(responseCode = "404", description = "Venta no encontrada")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ventaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tipo/{tipoVenta}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar ventas por tipo", description = "Obtiene ventas por tipo (CREDITO/CONTADO) - Solo ADMIN")
    public ResponseEntity<Page<VentaResponse>> buscarPorTipo(
            @PathVariable TipoVenta tipoVenta,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<VentaResponse> ventas = ventaService.buscarPorTipoVenta(tipoVenta, pageable);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar ventas por estado", description = "Obtiene ventas por estado - Solo ADMIN")
    public ResponseEntity<Page<VentaResponse>> buscarPorEstado(
            @PathVariable EstadoVenta estado,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<VentaResponse> ventas = ventaService.buscarPorEstado(estado, pageable);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/fecha")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar ventas por rango de fecha", description = "Obtiene ventas en un rango de fechas - Solo ADMIN")
    public ResponseEntity<Page<VentaResponse>> buscarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<VentaResponse> ventas = ventaService.buscarPorFecha(fechaInicio, fechaFin, pageable);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/monto")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar ventas por rango de monto", description = "Obtiene ventas por rango de total - Solo ADMIN")
    public ResponseEntity<Page<VentaResponse>> buscarPorRangoMonto(
            @RequestParam BigDecimal montoMin,
            @RequestParam BigDecimal montoMax,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<VentaResponse> ventas = ventaService.buscarPorRangoTotal(montoMin, montoMax, pageable);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/cliente-ocasional")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar ventas por cliente ocasional", description = "Obtiene ventas de clientes ocasionales - Solo ADMIN")
    public ResponseEntity<Page<VentaResponse>> buscarPorClienteOcasional(
            @RequestParam String nombre,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<VentaResponse> ventas = ventaService.buscarPorClienteOcasional(nombre, pageable);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ventas por cliente", description = "Obtiene todas las ventas de un cliente específico - Solo ADMIN")
    public ResponseEntity<Page<VentaResponse>> ventasPorCliente(
            @PathVariable Long clienteId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<VentaResponse> ventas = ventaService.buscarPorUsuario(clienteId, pageable);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/cuenta-cliente/{cuentaClienteId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ventas por cuenta de cliente", description = "Obtiene todas las ventas de una cuenta de cliente específica - Solo ADMIN")
    public ResponseEntity<Page<VentaResponse>> ventasPorCuentaCliente(
            @PathVariable Long cuentaClienteId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<VentaResponse> ventas = ventaService.buscarPorCuentaCliente(cuentaClienteId, pageable);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/cuenta-cliente/{cuentaClienteId}/estado/{estado}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ventas por cuenta de cliente y estado", description = "Obtiene ventas de una cuenta de cliente filtradas por estado - Solo ADMIN")
    public ResponseEntity<Page<VentaResponse>> ventasPorCuentaClienteYEstado(
            @PathVariable Long cuentaClienteId,
            @PathVariable EstadoVenta estado,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<VentaResponse> ventas = ventaService.buscarPorClienteYEstado(cuentaClienteId, estado, pageable);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/cuenta-cliente/{cuentaClienteId}/fecha")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ventas por cuenta de cliente y fecha", description = "Obtiene ventas de una cuenta de cliente filtradas por rango de fechas - Solo ADMIN")
    public ResponseEntity<Page<VentaResponse>> ventasPorCuentaClienteYFecha(
            @PathVariable Long cuentaClienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<VentaResponse> ventas = ventaService.buscarPorClienteYFecha(cuentaClienteId, fechaInicio, fechaFin, pageable);
        return ResponseEntity.ok(ventas);
    }

    @PutMapping("/{id}/marcar-pagada")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Marcar venta como pagada", description = "Marca una venta como completamente pagada - Solo ADMIN")
    public ResponseEntity<VentaResponse> marcarComoPagada(@PathVariable Long id) {
        VentaResponse response = ventaService.marcarComoPagada(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/marcar-parcial")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Marcar venta como pago parcial", description = "Marca una venta como pago parcial - Solo ADMIN")
    public ResponseEntity<VentaResponse> marcarComoParcial(@PathVariable Long id) {
        VentaResponse response = ventaService.marcarComoParcial(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancelar venta", description = "Cancela una venta y revierte cargos si es necesario - Solo ADMIN")
    public ResponseEntity<VentaResponse> cancelar(@PathVariable Long id) {
        VentaResponse response = ventaService.cancelar(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/recalcular")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Recalcular totales", description = "Recalcula los totales de una venta basado en sus detalles - Solo ADMIN")
    public ResponseEntity<VentaResponse> recalcularTotales(@PathVariable Long id) {
        VentaResponse response = ventaService.recalcularTotales(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/puede-modificar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verificar si venta puede modificarse", description = "Verifica si una venta puede ser modificada - Solo ADMIN")
    public ResponseEntity<Boolean> puedeModificar(@PathVariable Long id) {
        boolean puedeModificar = ventaService.puedeModificar(id);
        return ResponseEntity.ok(puedeModificar);
    }

    @GetMapping("/{id}/puede-anular")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verificar si venta puede anularse", description = "Verifica si una venta puede ser anulada - Solo ADMIN")
    public ResponseEntity<Boolean> puedeAnular(@PathVariable Long id) {
        boolean puedeAnular = ventaService.puedeAnular(id);
        return ResponseEntity.ok(puedeAnular);
    }

    // ENDPOINTS PARA CLIENTES

    @GetMapping("/mis-compras")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Ver mi historial de compras", description = "Permite al cliente ver su historial completo de compras")
    @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente")
    public ResponseEntity<Page<VentaResponse>> verMisCompras(
            Authentication authentication,
            @PageableDefault(size = 10) @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        String email = authentication.getName();
        Page<VentaResponse> misCompras = ventaService.buscarMisCompras(email, pageable);
        return ResponseEntity.ok(misCompras);
    }

    @GetMapping("/mis-compras/estado/{estado}")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Ver mis compras por estado", description = "Permite al cliente filtrar sus compras por estado")
    public ResponseEntity<Page<VentaResponse>> verMisComprasPorEstado(
            @PathVariable EstadoVenta estado,
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        String email = authentication.getName();
        Page<VentaResponse> misCompras = ventaService.buscarMisComprasPorEstado(email, estado, pageable);
        return ResponseEntity.ok(misCompras);
    }

    @GetMapping("/mis-compras/fecha")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Ver mis compras por fecha", description = "Permite al cliente filtrar sus compras por rango de fechas")
    public ResponseEntity<Page<VentaResponse>> verMisComprasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        String email = authentication.getName();
        Page<VentaResponse> misCompras = ventaService.buscarMisComprasPorFecha(email, fechaInicio, fechaFin, pageable);
        return ResponseEntity.ok(misCompras);
    }

    @GetMapping("/mis-compras/pendientes")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Ver mis compras pendientes de pago", description = "Permite al cliente ver solo sus compras pendientes")
    public ResponseEntity<Page<VentaResponse>> verMisComprasPendientes(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        String email = authentication.getName();
        Page<VentaResponse> comprasPendientes = ventaService.buscarMisComprasPendientes(email, pageable);
        return ResponseEntity.ok(comprasPendientes);
    }

    @GetMapping("/mi-compra/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Ver detalle de mi compra", description = "Permite al cliente ver el detalle de una compra específica suya")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Compra encontrada"),
        @ApiResponse(responseCode = "404", description = "Compra no encontrada o no pertenece al cliente"),
        @ApiResponse(responseCode = "403", description = "No tienes permiso para ver esta compra")
    })
    public ResponseEntity<VentaResponse> verMiCompra(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();

        return ventaService.buscarMiVentaPorId(id, email)
                .map(venta -> ResponseEntity.ok(venta))
                .orElse(ResponseEntity.notFound().build());
    }
}
