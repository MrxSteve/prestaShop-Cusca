package com.cusca.shopmoney_pg.controllers;

import com.cusca.shopmoney_pg.models.dto.request.RolRequest;
import com.cusca.shopmoney_pg.models.dto.response.RolResponse;
import com.cusca.shopmoney_pg.services.auth.IRolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@Tag(name = "Gestión de Roles", description = "Endpoints para administrar roles del sistema (Solo Administradores)")
public class RolController {
    private final IRolService iRolService;

    @PostMapping
    @Operation(
            summary = "Crear rol",
            description = "Crea un nuevo rol en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rol creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o rol ya existe"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<RolResponse> crear(@Valid @RequestBody RolRequest request) {
        RolResponse response = iRolService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Listar roles",
            description = "Obtiene una lista paginada de todos los roles"
    )
    public ResponseEntity<Page<RolResponse>> listarTodos(
            @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        Page<RolResponse> roles = iRolService.listarTodos(pageable);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar rol por ID",
            description = "Obtiene un rol específico por su ID"
    )
    public ResponseEntity<RolResponse> buscarPorId(
            @Parameter(description = "ID del rol") @PathVariable Long id) {
        return iRolService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nombre/{nombre}")
    @Operation(
            summary = "Buscar rol por nombre",
            description = "Obtiene un rol específico por su nombre"
    )
    public ResponseEntity<RolResponse> buscarPorNombre(
            @Parameter(description = "Nombre del rol") @PathVariable String nombre) {
        return iRolService.buscarPorNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar rol",
            description = "Actualiza los datos de un rol existente"
    )
    public ResponseEntity<RolResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody RolRequest request) {
        RolResponse response = iRolService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar rol",
            description = "Elimina un rol del sistema"
    )
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        iRolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
