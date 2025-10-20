package com.cusca.shopmoney_pg.controllers;

import com.cusca.shopmoney_pg.models.dto.request.AbonoRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateAbonoRequest;
import com.cusca.shopmoney_pg.models.dto.response.AbonoResponse;
import com.cusca.shopmoney_pg.models.enums.EstadoAbono;
import com.cusca.shopmoney_pg.models.enums.MetodoPago;
import com.cusca.shopmoney_pg.services.finance.IAbonoService;
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
@RequestMapping("/api/abonos")
@RequiredArgsConstructor
@Tag(name = "Abonos", description = "Gestión de abonos y pagos de clientes")
public class AbonoController {
    private final IAbonoService abonoService;

    // ENDPOINTS PARA ADMINISTRADORES

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear nuevo abono", description = "Permite crear un nuevo abono para una cuenta de cliente - Solo ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Abono creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Cuenta de cliente no encontrada")
    })
    public ResponseEntity<AbonoResponse> crear(@Valid @RequestBody AbonoRequest request) {
        AbonoResponse response = abonoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los abonos", description = "Obtiene todos los abonos con paginación - Solo ADMIN")
    @ApiResponse(responseCode = "200", description = "Abonos obtenidos exitosamente")
    public ResponseEntity<Page<AbonoResponse>> listarTodos(
            @PageableDefault(size = 10) @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        Page<AbonoResponse> abonos = abonoService.listarTodos(pageable);
        return ResponseEntity.ok(abonos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener abono por ID", description = "Obtiene un abono específico por su ID - Solo ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Abono encontrado"),
            @ApiResponse(responseCode = "404", description = "Abono no encontrado")
    })
    public ResponseEntity<AbonoResponse> obtenerPorId(@PathVariable Long id) {
        return abonoService.buscarPorId(id)
                .map(abono -> ResponseEntity.ok(abono))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar abono", description = "Actualiza un abono existente (solo PENDIENTES) - Solo ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Abono actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "No se puede modificar el abono en su estado actual"),
            @ApiResponse(responseCode = "404", description = "Abono no encontrado")
    })
    public ResponseEntity<AbonoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAbonoRequest request) {
        AbonoResponse response = abonoService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar abono", description = "Elimina un abono (solo RECHAZADOS o PENDIENTES) - Solo ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Abono eliminado exitosamente"),
            @ApiResponse(responseCode = "400", description = "No se pueden eliminar abonos aplicados"),
            @ApiResponse(responseCode = "404", description = "Abono no encontrado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        abonoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar abonos por estado", description = "Obtiene abonos por estado - Solo ADMIN")
    public ResponseEntity<Page<AbonoResponse>> buscarPorEstado(
            @PathVariable EstadoAbono estado,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<AbonoResponse> abonos = abonoService.buscarPorEstado(estado, pageable);
        return ResponseEntity.ok(abonos);
    }

    @GetMapping("/metodo-pago/{metodoPago}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar abonos por método de pago", description = "Obtiene abonos por método de pago - Solo ADMIN")
    public ResponseEntity<Page<AbonoResponse>> buscarPorMetodoPago(
            @PathVariable MetodoPago metodoPago,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<AbonoResponse> abonos = abonoService.buscarPorMetodoPago(metodoPago, pageable);
        return ResponseEntity.ok(abonos);
    }

    @GetMapping("/fecha")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar abonos por rango de fecha", description = "Obtiene abonos en un rango de fechas - Solo ADMIN")
    public ResponseEntity<Page<AbonoResponse>> buscarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<AbonoResponse> abonos = abonoService.buscarPorFecha(fechaInicio, fechaFin, pageable);
        return ResponseEntity.ok(abonos);
    }

    @GetMapping("/monto")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar abonos por rango de monto", description = "Obtiene abonos por rango de monto - Solo ADMIN")
    public ResponseEntity<Page<AbonoResponse>> buscarPorRangoMonto(
            @RequestParam BigDecimal montoMin,
            @RequestParam BigDecimal montoMax,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<AbonoResponse> abonos = abonoService.buscarPorRangoMonto(montoMin, montoMax, pageable);
        return ResponseEntity.ok(abonos);
    }

    @GetMapping("/cuenta-cliente/{cuentaClienteId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Abonos por cuenta de cliente", description = "Obtiene todos los abonos de una cuenta de cliente específica - Solo ADMIN")
    public ResponseEntity<Page<AbonoResponse>> abonosPorCuentaCliente(
            @PathVariable Long cuentaClienteId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<AbonoResponse> abonos = abonoService.buscarPorCuentaCliente(cuentaClienteId, pageable);
        return ResponseEntity.ok(abonos);
    }

    @GetMapping("/cuenta-cliente/{cuentaClienteId}/estado/{estado}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Abonos por cuenta de cliente y estado", description = "Obtiene abonos de una cuenta de cliente filtrados por estado - Solo ADMIN")
    public ResponseEntity<Page<AbonoResponse>> abonosPorCuentaClienteYEstado(
            @PathVariable Long cuentaClienteId,
            @PathVariable EstadoAbono estado,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<AbonoResponse> abonos = abonoService.buscarPorClienteYEstado(cuentaClienteId, estado, pageable);
        return ResponseEntity.ok(abonos);
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Abonos por usuario", description = "Obtiene todos los abonos de un usuario específico - Solo ADMIN")
    public ResponseEntity<Page<AbonoResponse>> abonosPorUsuario(
            @PathVariable Long usuarioId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<AbonoResponse> abonos = abonoService.buscarPorUsuario(usuarioId, pageable);
        return ResponseEntity.ok(abonos);
    }

    @PutMapping("/{id}/aplicar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aplicar abono", description = "Aplica un abono pendiente a la cuenta del cliente - Solo ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Abono aplicado exitosamente"),
            @ApiResponse(responseCode = "400", description = "El abono no se puede aplicar en su estado actual"),
            @ApiResponse(responseCode = "404", description = "Abono no encontrado")
    })
    public ResponseEntity<AbonoResponse> aplicar(@PathVariable Long id) {
        AbonoResponse response = abonoService.aplicar(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/marcar-pendiente")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Marcar abono como pendiente", description = "Marca un abono como pendiente - Solo ADMIN")
    public ResponseEntity<AbonoResponse> marcarComoPendiente(@PathVariable Long id) {
        AbonoResponse response = abonoService.marcarComoPendiente(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rechazar abono", description = "Rechaza un abono pendiente con motivo - Solo ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Abono rechazado exitosamente"),
            @ApiResponse(responseCode = "400", description = "El abono no se puede rechazar en su estado actual"),
            @ApiResponse(responseCode = "404", description = "Abono no encontrado")
    })
    public ResponseEntity<AbonoResponse> rechazar(
            @PathVariable Long id,
            @RequestParam String motivo) {
        AbonoResponse response = abonoService.rechazar(id, motivo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/puede-modificar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verificar si abono puede modificarse", description = "Verifica si un abono puede ser modificado - Solo ADMIN")
    public ResponseEntity<Boolean> puedeModificar(@PathVariable Long id) {
        boolean puedeModificar = abonoService.puedeModificar(id);
        return ResponseEntity.ok(puedeModificar);
    }

    @GetMapping("/{id}/puede-aplicar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verificar si abono puede aplicarse", description = "Verifica si un abono puede ser aplicado - Solo ADMIN")
    public ResponseEntity<Boolean> puedeAplicar(@PathVariable Long id) {
        boolean puedeAplicar = abonoService.puedeAplicar(id);
        return ResponseEntity.ok(puedeAplicar);
    }

    @GetMapping("/{id}/puede-rechazar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verificar si abono puede rechazarse", description = "Verifica si un abono puede ser rechazado - Solo ADMIN")
    public ResponseEntity<Boolean> puedeRechazar(@PathVariable Long id) {
        boolean puedeRechazar = abonoService.puedeRechazar(id);
        return ResponseEntity.ok(puedeRechazar);
    }

    // ENDPOINTS PARA CLIENTES

    @GetMapping("/mis-abonos")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Ver mi historial de abonos", description = "Permite al cliente ver su historial completo de abonos")
    @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente")
    public ResponseEntity<Page<AbonoResponse>> verMisAbonos(
            Authentication authentication,
            @PageableDefault(size = 10) @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        String email = authentication.getName();
        Page<AbonoResponse> misAbonos = abonoService.buscarMisAbonos(email, pageable);
        return ResponseEntity.ok(misAbonos);
    }

    @GetMapping("/mis-abonos/estado/{estado}")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Ver mis abonos por estado", description = "Permite al cliente filtrar sus abonos por estado")
    public ResponseEntity<Page<AbonoResponse>> verMisAbonosPorEstado(
            @PathVariable EstadoAbono estado,
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        String email = authentication.getName();
        Page<AbonoResponse> misAbonos = abonoService.buscarMisAbonosPorEstado(email, estado, pageable);
        return ResponseEntity.ok(misAbonos);
    }

    @GetMapping("/mis-abonos/fecha")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Ver mis abonos por fecha", description = "Permite al cliente filtrar sus abonos por rango de fechas")
    public ResponseEntity<Page<AbonoResponse>> verMisAbonosPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        String email = authentication.getName();
        Page<AbonoResponse> misAbonos = abonoService.buscarMisAbonosPorFecha(email, fechaInicio, fechaFin, pageable);
        return ResponseEntity.ok(misAbonos);
    }

    @GetMapping("/mis-abonos/pendientes")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Ver mis abonos pendientes", description = "Permite al cliente ver solo sus abonos pendientes")
    public ResponseEntity<Page<AbonoResponse>> verMisAbonosPendientes(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        String email = authentication.getName();
        Page<AbonoResponse> abonosPendientes = abonoService.buscarMisAbonosPendientes(email, pageable);
        return ResponseEntity.ok(abonosPendientes);
    }

    @GetMapping("/mi-abono/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Ver detalle de mi abono", description = "Permite al cliente ver el detalle de un abono específico suyo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Abono encontrado"),
            @ApiResponse(responseCode = "404", description = "Abono no encontrado o no pertenece al cliente"),
            @ApiResponse(responseCode = "403", description = "No tienes permiso para ver este abono")
    })
    public ResponseEntity<AbonoResponse> verMiAbono(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();

        return abonoService.buscarMiAbonoPorId(id, email)
                .map(abono -> ResponseEntity.ok(abono))
                .orElse(ResponseEntity.notFound().build());
    }
}