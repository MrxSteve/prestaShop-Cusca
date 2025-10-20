package com.cusca.shopmoney_pg.controllers;

import com.cusca.shopmoney_pg.models.dto.response.MovimientoCuentaResponse;
import com.cusca.shopmoney_pg.models.enums.TipoMovimiento;
import com.cusca.shopmoney_pg.models.enums.TipoReferencia;
import com.cusca.shopmoney_pg.services.finance.IMovimientoCuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/movimientos-cuenta")
@RequiredArgsConstructor
@Tag(name = "Movimientos de Cuenta", description = "Consulta de movimientos y estados de cuenta - Solo ADMIN")
@PreAuthorize("hasRole('ADMIN')")
public class MovimientoCuentaController {
    private final IMovimientoCuentaService movimientoCuentaService;

    // CONSULTAS BASICAS

    @GetMapping
    @Operation(summary = "Listar todos los movimientos", description = "Obtiene todos los movimientos de cuenta con paginación - Solo ADMIN")
    @ApiResponse(responseCode = "200", description = "Movimientos obtenidos exitosamente")
    public ResponseEntity<Page<MovimientoCuentaResponse>> listarTodos(
            @PageableDefault(size = 10) @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        Page<MovimientoCuentaResponse> movimientos = movimientoCuentaService.listarTodos(pageable);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener movimiento por ID", description = "Obtiene un movimiento específico por su ID - Solo ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimiento encontrado"),
        @ApiResponse(responseCode = "404", description = "Movimiento no encontrado")
    })
    public ResponseEntity<MovimientoCuentaResponse> obtenerPorId(@PathVariable Long id) {
        return movimientoCuentaService.buscarPorId(id)
                .map(movimiento -> ResponseEntity.ok(movimiento))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tipo/{tipoMovimiento}")
    @Operation(summary = "Buscar movimientos por tipo", description = "Obtiene movimientos por tipo (CARGO/ABONO) - Solo ADMIN")
    public ResponseEntity<Page<MovimientoCuentaResponse>> buscarPorTipo(
            @PathVariable TipoMovimiento tipoMovimiento,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MovimientoCuentaResponse> movimientos = movimientoCuentaService.buscarPorTipoMovimiento(tipoMovimiento, pageable);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/fecha")
    @Operation(summary = "Buscar movimientos por rango de fecha", description = "Obtiene movimientos en un rango de fechas - Solo ADMIN")
    public ResponseEntity<Page<MovimientoCuentaResponse>> buscarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MovimientoCuentaResponse> movimientos = movimientoCuentaService.buscarPorFecha(fechaInicio, fechaFin, pageable);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/cuenta-cliente/{cuentaClienteId}")
    @Operation(summary = "Movimientos por cuenta de cliente", description = "Obtiene todos los movimientos de una cuenta de cliente específica - Solo ADMIN")
    public ResponseEntity<Page<MovimientoCuentaResponse>> movimientosPorCuentaCliente(
            @PathVariable Long cuentaClienteId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MovimientoCuentaResponse> movimientos = movimientoCuentaService.buscarPorCuentaCliente(cuentaClienteId, pageable);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/cuenta-cliente/{cuentaClienteId}/tipo/{tipoMovimiento}")
    @Operation(summary = "Movimientos por cuenta y tipo", description = "Obtiene movimientos de una cuenta filtrados por tipo - Solo ADMIN")
    public ResponseEntity<Page<MovimientoCuentaResponse>> movimientosPorCuentaYTipo(
            @PathVariable Long cuentaClienteId,
            @PathVariable TipoMovimiento tipoMovimiento,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MovimientoCuentaResponse> movimientos = movimientoCuentaService.buscarPorClienteYTipo(cuentaClienteId, tipoMovimiento, pageable);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/cuenta-cliente/{cuentaClienteId}/fecha")
    @Operation(summary = "Movimientos por cuenta y fecha", description = "Obtiene movimientos de una cuenta filtrados por rango de fechas - Solo ADMIN")
    public ResponseEntity<Page<MovimientoCuentaResponse>> movimientosPorCuentaYFecha(
            @PathVariable Long cuentaClienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MovimientoCuentaResponse> movimientos = movimientoCuentaService.buscarPorClienteYFecha(cuentaClienteId, fechaInicio, fechaFin, pageable);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Movimientos por usuario", description = "Obtiene todos los movimientos de un usuario específico - Solo ADMIN")
    public ResponseEntity<Page<MovimientoCuentaResponse>> movimientosPorUsuario(
            @PathVariable Long usuarioId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MovimientoCuentaResponse> movimientos = movimientoCuentaService.buscarPorUsuario(usuarioId, pageable);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/referencia/{tipoReferencia}/{referenciaId}")
    @Operation(summary = "Buscar movimientos por referencia", description = "Obtiene movimientos por tipo de referencia y ID - Solo ADMIN")
    public ResponseEntity<Page<MovimientoCuentaResponse>> buscarPorReferencia(
            @PathVariable TipoReferencia tipoReferencia,
            @PathVariable Long referenciaId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MovimientoCuentaResponse> movimientos = movimientoCuentaService.buscarPorReferencia(tipoReferencia, referenciaId, pageable);
        return ResponseEntity.ok(movimientos);
    }

    // ESTADOS DE CUENTA Y SALDOS

    @GetMapping("/cuenta-cliente/{cuentaClienteId}/saldo-actual")
    @Operation(summary = "Obtener saldo actual", description = "Obtiene el saldo actual de una cuenta de cliente - Solo ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Saldo obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cuenta de cliente no encontrada")
    })
    public ResponseEntity<BigDecimal> obtenerSaldoActual(@PathVariable Long cuentaClienteId) {
        BigDecimal saldo = movimientoCuentaService.obtenerSaldoActual(cuentaClienteId);
        return ResponseEntity.ok(saldo);
    }

    @GetMapping("/cuenta-cliente/{cuentaClienteId}/estado-cuenta")
    @Operation(summary = "Obtener estado de cuenta", description = "Obtiene el estado de cuenta de un cliente en un rango de fechas - Solo ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado de cuenta obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cuenta de cliente no encontrada")
    })
    public ResponseEntity<Page<MovimientoCuentaResponse>> obtenerEstadoCuenta(
            @PathVariable Long cuentaClienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MovimientoCuentaResponse> estadoCuenta = movimientoCuentaService.obtenerEstadoCuenta(cuentaClienteId, fechaInicio, fechaFin, pageable);
        return ResponseEntity.ok(estadoCuenta);
    }

    // ESTADÍSTICAS Y REPORTES

    @GetMapping("/estadisticas/cargos-del-dia")
    @Operation(summary = "Total de cargos del día", description = "Obtiene el total de cargos (ventas) del día actual - Solo ADMIN")
    public ResponseEntity<BigDecimal> obtenerTotalCargosDelDia() {
        BigDecimal total = movimientoCuentaService.obtenerTotalCargosDelDia();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/estadisticas/abonos-del-dia")
    @Operation(summary = "Total de abonos del día", description = "Obtiene el total de abonos (pagos) del día actual - Solo ADMIN")
    public ResponseEntity<BigDecimal> obtenerTotalAbonosDelDia() {
        BigDecimal total = movimientoCuentaService.obtenerTotalAbonosDelDia();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/estadisticas/cargos-del-mes")
    @Operation(summary = "Total de cargos del mes", description = "Obtiene el total de cargos (ventas) del mes actual - Solo ADMIN")
    public ResponseEntity<BigDecimal> obtenerTotalCargosDelMes() {
        BigDecimal total = movimientoCuentaService.obtenerTotalCargosDelMes();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/estadisticas/abonos-del-mes")
    @Operation(summary = "Total de abonos del mes", description = "Obtiene el total de abonos (pagos) del mes actual - Solo ADMIN")
    public ResponseEntity<BigDecimal> obtenerTotalAbonosDelMes() {
        BigDecimal total = movimientoCuentaService.obtenerTotalAbonosDelMes();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/estadisticas/resumen-del-dia")
    @Operation(summary = "Resumen financiero del día", description = "Obtiene un resumen de cargos y abonos del día actual - Solo ADMIN")
    public ResponseEntity<ResumenFinanciero> obtenerResumenDelDia() {
        BigDecimal totalCargos = movimientoCuentaService.obtenerTotalCargosDelDia();
        BigDecimal totalAbonos = movimientoCuentaService.obtenerTotalAbonosDelDia();
        BigDecimal diferencia = totalCargos.subtract(totalAbonos);

        ResumenFinanciero resumen = new ResumenFinanciero(totalCargos, totalAbonos, diferencia);
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/estadisticas/resumen-del-mes")
    @Operation(summary = "Resumen financiero del mes", description = "Obtiene un resumen de cargos y abonos del mes actual - Solo ADMIN")
    public ResponseEntity<ResumenFinanciero> obtenerResumenDelMes() {
        BigDecimal totalCargos = movimientoCuentaService.obtenerTotalCargosDelMes();
        BigDecimal totalAbonos = movimientoCuentaService.obtenerTotalAbonosDelMes();
        BigDecimal diferencia = totalCargos.subtract(totalAbonos);

        ResumenFinanciero resumen = new ResumenFinanciero(totalCargos, totalAbonos, diferencia);
        return ResponseEntity.ok(resumen);
    }

    // CLASE AUXILIAR PARA RESÚMENES

    public static class ResumenFinanciero {
        public final BigDecimal totalCargos;
        public final BigDecimal totalAbonos;
        public final BigDecimal diferencia;

        public ResumenFinanciero(BigDecimal totalCargos, BigDecimal totalAbonos, BigDecimal diferencia) {
            this.totalCargos = totalCargos;
            this.totalAbonos = totalAbonos;
            this.diferencia = diferencia;
        }
    }
}
