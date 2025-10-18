package com.cusca.shopmoney_pg.controllers;

import com.cusca.shopmoney_pg.models.dto.request.UsuarioRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateUsuarioRequest;
import com.cusca.shopmoney_pg.models.dto.response.UsuarioResponse;
import com.cusca.shopmoney_pg.models.enums.EstadoUsuario;
import com.cusca.shopmoney_pg.services.auth.IUserService;
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
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
@Tag(name = "Gestión de Usuarios", description = "Endpoints para administrar usuarios del sistema")
public class UsuarioController {
    private final IUserService usuarioService;

    @PostMapping
    @Operation(
            summary = "Crear usuario",
            description = "Crea un nuevo usuario en el sistema (Solo Administradores)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o usuario ya existe"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = usuarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Listar usuarios",
            description = "Obtiene una lista paginada de todos los usuarios"
    )
    public ResponseEntity<Page<UsuarioResponse>> listarTodos(Pageable pageable) {
        Page<UsuarioResponse> usuarios = usuarioService.listarTodos(pageable);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar usuario por ID",
            description = "Obtiene los detalles de un usuario específico por su ID"
    )
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    @Operation(
            summary = "Buscar usuario por email",
            description = "Obtiene los detalles de un usuario específico por su email"
    )
    public ResponseEntity<UsuarioResponse> buscarPorEmail(@PathVariable String email) {
        return usuarioService.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/dui/{dui}")
    @Operation(
            summary = "Buscar usuario por DUI",
            description = "Obtiene los detalles de un usuario específico por su DUI"
    )
    public ResponseEntity<UsuarioResponse> buscarPorDUI(@PathVariable String dui) {
        return usuarioService.buscarPorDui(dui)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nombre")
    @Operation(
            summary = "Buscar usuarios por nombre",
            description = "Obtiene una lista de usuarios cuyo nombre contiene el término especificado"
    )
    public ResponseEntity<Page<UsuarioResponse>> buscarPorNombre(
            @RequestParam String nombre,
            Pageable pageable) {
        Page<UsuarioResponse> usuarios = usuarioService.buscarPorNombreContaining(nombre, pageable);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/estado/{estado}")
    @Operation(
            summary = "Buscar usuarios por estado",
            description = "Obtiene una lista de usuarios filtrados por su estado (activo, inactivo, suspendido)"
    )
    public ResponseEntity<Page<UsuarioResponse>> buscarPorEstado(
            @PathVariable EstadoUsuario estado,
            Pageable pageable) {
        Page<UsuarioResponse> usuarios = usuarioService.buscarPorEstado(estado, pageable);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/rol/{nombreRol}")
    @Operation(
            summary = "Buscar usuarios por rol",
            description = "Obtiene una lista de usuarios que tienen un rol específico"
    )
    public ResponseEntity<Page<UsuarioResponse>> buscarPorRol(
            @PathVariable String nombreRol,
            Pageable pageable) {
        Page<UsuarioResponse> usuarios = usuarioService.buscarPorRolNombre(nombreRol, pageable);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/con-cuenta")
    @Operation(
            summary = "Buscar usuarios con cuenta",
            description = "Obtiene una lista de usuarios que tienen una cuenta asociada"
    )
    public ResponseEntity<Page<UsuarioResponse>> buscarUsuariosConCuenta(Pageable pageable) {
        Page<UsuarioResponse> usuarios = usuarioService.buscarUsuariosConCuenta(pageable);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/sin-cuenta")
    @Operation(
            summary = "Buscar usuarios sin cuenta",
            description = "Obtiene una lista de usuarios que no tienen una cuenta asociada"
    )
    public ResponseEntity<Page<UsuarioResponse>> buscarUsuariosSinCuenta(Pageable pageable) {
        Page<UsuarioResponse> usuarios = usuarioService.buscarUsuariosSinCuenta(pageable);
        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar usuario",
            description = "Actualiza la información de un usuario existente"
    )
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUsuarioRequest request) {
        UsuarioResponse response = usuarioService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar usuario",
            description = "Elimina un usuario del sistema"
    )
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Gestión de roles
    @PostMapping("/{usuarioId}/roles/{rolId}")
    @Operation(
            summary = "Asignar rol a usuario",
            description = "Asigna un rol específico a un usuario"
    )
    public ResponseEntity<UsuarioResponse> asignarRol(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId,
            @Parameter(description = "ID del rol") @PathVariable Long rolId) {
        UsuarioResponse response = usuarioService.asignarRol(usuarioId, rolId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{usuarioId}/roles/{rolId}")
    @Operation(
            summary = "Remover rol de usuario",
            description = "Remueve un rol específico de un usuario"
    )
    public ResponseEntity<UsuarioResponse> removerRol(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId,
            @Parameter(description = "ID del rol") @PathVariable Long rolId) {
        UsuarioResponse response = usuarioService.removerRol(usuarioId, rolId);
        return ResponseEntity.ok(response);
    }

    // Gestión de estado
    @PutMapping("/{id}/estado")
    @Operation(
            summary = "Cambiar estado de usuario",
            description = "Cambia el estado (activo, inactivo, suspendido) de un usuario"
    )
    public ResponseEntity<UsuarioResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoUsuario estado) {
        UsuarioResponse response = usuarioService.cambiarEstado(id, estado);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/activar")
    @Operation(
            summary = "Activar usuario",
            description = "Activa un usuario que estaba inactivo o suspendido"
    )
    public ResponseEntity<UsuarioResponse> activar(@PathVariable Long id) {
        UsuarioResponse response = usuarioService.activar(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/desactivar")
    @Operation(
            summary = "Desactivar usuario",
            description = "Desactiva un usuario, impidiendo su acceso al sistema"
    )
    public ResponseEntity<UsuarioResponse> desactivar(@PathVariable Long id) {
        UsuarioResponse response = usuarioService.desactivar(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/suspender")
    @Operation(
            summary = "Suspender usuario",
            description = "Suspende temporalmente a un usuario, manteniendo su información en el sistema"
    )
    public ResponseEntity<UsuarioResponse> suspender(@PathVariable Long id) {
        UsuarioResponse response = usuarioService.suspender(id);
        return ResponseEntity.ok(response);
    }
}
