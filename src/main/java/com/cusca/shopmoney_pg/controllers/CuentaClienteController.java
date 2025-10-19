package com.cusca.shopmoney_pg.controllers;

import com.cusca.shopmoney_pg.models.dto.request.CuentaClienteRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateCuentaClienteRequest;
import com.cusca.shopmoney_pg.models.dto.response.CuentaClienteResponse;
import com.cusca.shopmoney_pg.models.enums.EstadoCuenta;
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

@RestController
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
@Tag(name = "Cuentas de Cliente", description = "Gestión de cuentas de crédito de clientes")
public class CuentaClienteController {
    private final ICuentaClienteService cuentaClienteService;

    // ENDPOINTS PARA ADMINISTRADORES

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear nueva cuenta", description = "Permite crear una nueva cuenta de cliente (Solo ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cuenta creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe una cuenta para este usuario")
    })
    public ResponseEntity<CuentaClienteResponse> crear(@Valid @RequestBody CuentaClienteRequest request) {
        CuentaClienteResponse response = cuentaClienteService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas las cuentas", description = "Obtiene todas las cuentas con paginación (Solo ADMIN)")
    @ApiResponse(responseCode = "200", description = "Cuentas obtenidas exitosamente")
    public ResponseEntity<Page<CuentaClienteResponse>> listarTodas(
            @PageableDefault(size = 10) @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        Page<CuentaClienteResponse> cuentas = cuentaClienteService.listarTodos(pageable);
        return ResponseEntity.ok(cuentas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener cuenta por ID", description = "Obtiene una cuenta específica por su ID (Solo ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    public ResponseEntity<CuentaClienteResponse> obtenerPorId(@PathVariable Long id) {
        return cuentaClienteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar cuenta", description = "Actualiza una cuenta existente (Solo ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    public ResponseEntity<CuentaClienteResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCuentaClienteRequest request) {
        CuentaClienteResponse response = cuentaClienteService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar cuenta", description = "Elimina una cuenta existente (Solo ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cuenta eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar la cuenta por restricciones de negocio")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cuentaClienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar cuentas por estado", description = "Obtiene cuentas filtradas por estado (Solo ADMIN)")
    public ResponseEntity<Page<CuentaClienteResponse>> buscarPorEstado(
            @PathVariable EstadoCuenta estado,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<CuentaClienteResponse> cuentas = cuentaClienteService.buscarPorEstado(estado, pageable);
        return ResponseEntity.ok(cuentas);
    }

    @GetMapping("/limite-credito")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar cuentas por rango de límite de crédito", description = "Obtiene cuentas por rango de límite (Solo ADMIN)")
    public ResponseEntity<Page<CuentaClienteResponse>> buscarPorRangoLimiteCredito(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<CuentaClienteResponse> cuentas = cuentaClienteService.buscarPorRangoLimiteCredito(min, max, pageable);
        return ResponseEntity.ok(cuentas);
    }

    @GetMapping("/saldo-alto")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar cuentas con saldo alto", description = "Obtiene cuentas con saldo mayor al especificado (Solo ADMIN)")
    public ResponseEntity<Page<CuentaClienteResponse>> buscarPorSaldoAlto(
            @RequestParam BigDecimal saldo,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<CuentaClienteResponse> cuentas = cuentaClienteService.buscarPorSaldoMayorQue(saldo, pageable);
        return ResponseEntity.ok(cuentas);
    }

    @GetMapping("/fecha-apertura")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar cuentas por fecha de apertura", description = "Obtiene cuentas por rango de fechas de apertura (Solo ADMIN)")
    public ResponseEntity<Page<CuentaClienteResponse>> buscarPorFechaApertura(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<CuentaClienteResponse> cuentas = cuentaClienteService.buscarPorFechaApertura(fechaInicio, fechaFin, pageable);
        return ResponseEntity.ok(cuentas);
    }

    @PutMapping("/{id}/limite-credito")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar límite de crédito", description = "Actualiza el límite de crédito de una cuenta (Solo ADMIN)")
    public ResponseEntity<CuentaClienteResponse> actualizarLimiteCredito(
            @PathVariable Long id,
            @RequestParam BigDecimal nuevoLimite) {
        CuentaClienteResponse response = cuentaClienteService.actualizarLimiteCredito(id, nuevoLimite);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar cuenta", description = "Activa una cuenta suspendida (Solo ADMIN)")
    public ResponseEntity<CuentaClienteResponse> activar(@PathVariable Long id) {
        CuentaClienteResponse response = cuentaClienteService.activar(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/suspender")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Suspender cuenta", description = "Suspende una cuenta activa (Solo ADMIN)")
    public ResponseEntity<CuentaClienteResponse> suspender(@PathVariable Long id) {
        CuentaClienteResponse response = cuentaClienteService.suspender(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cerrar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cerrar cuenta", description = "Cierra una cuenta (Solo ADMIN)")
    public ResponseEntity<CuentaClienteResponse> cerrar(@PathVariable Long id) {
        CuentaClienteResponse response = cuentaClienteService.cerrar(id);
        return ResponseEntity.ok(response);
    }

    // ENDPOINTS PARA CLIENTES

    @GetMapping("/mi-cuenta")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Ver mi cuenta", description = "Permite al cliente ver su propia cuenta de crédito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "El cliente no tiene cuenta creada")
    })
    public ResponseEntity<CuentaClienteResponse> verMiCuenta(Authentication authentication) {
        String email = authentication.getName();
        return cuentaClienteService.buscarPorUsuarioEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/mi-saldo-disponible")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Consultar saldo disponible", description = "Permite al cliente consultar su saldo disponible")
    public ResponseEntity<BigDecimal> consultarMiSaldoDisponible(Authentication authentication) {
        String email = authentication.getName();
        return cuentaClienteService.buscarPorUsuarioEmail(email)
                .map(c -> ResponseEntity.ok(c.getSaldoDisponible()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/puedo-comprar")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Verificar disponibilidad para compra", description = "Verifica si el cliente puede realizar una compra por el monto especificado")
    public ResponseEntity<Boolean> puedeRealizarCompra(
            @RequestParam BigDecimal monto,
            Authentication authentication) {
        String email = authentication.getName();
        return cuentaClienteService.buscarPorUsuarioEmail(email)
                .map(c -> ResponseEntity.ok(cuentaClienteService.puedeRealizarCompra(c.getId(), monto)))
                .orElse(ResponseEntity.notFound().build());
    }
}
