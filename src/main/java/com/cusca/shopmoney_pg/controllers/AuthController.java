package com.cusca.shopmoney_pg.controllers;

import com.cusca.shopmoney_pg.models.dto.request.ChangePasswordRequest;
import com.cusca.shopmoney_pg.models.dto.request.LoginRequest;
import com.cusca.shopmoney_pg.models.dto.response.AuthResponse;
import com.cusca.shopmoney_pg.models.dto.response.UsuarioResponse;
import com.cusca.shopmoney_pg.services.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para autenticación de usuarios")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario y devuelve un token JWT junto con la información del usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content
            )
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Cerrar sesión",
            description = "Cierra la sesión del usuario autenticado. El cliente debe eliminar el token JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout exitoso",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content
            )
    })
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Cambiar contraseña",
            description = "Permite al usuario autenticado cambiar su contraseña"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contraseña cambiada exitosamente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o contraseña actual incorrecta",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content
            )
    })
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/perfil")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Ver perfil",
            description = "Devuelve la información del usuario autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil obtenido exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content
            )
    })
    public ResponseEntity<UsuarioResponse> verPerfil() {
        UsuarioResponse response = authService.getProfile();
        return ResponseEntity.ok(response);
    }
}
