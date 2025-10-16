package com.cusca.shopmoney_pg.models.dto.response;

import com.cusca.shopmoney_pg.models.enums.EstadoUsuario;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UsuarioResponse {
    private Long id;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String direccion;
    private String dui;
    private LocalDate fechaNacimiento;
    private EstadoUsuario estado;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<RolResponse> roles;
    private CuentaClienteResponse cuentaCliente;
}
