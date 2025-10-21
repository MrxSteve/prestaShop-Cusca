package com.cusca.shopmoney_pg.models.dto.request;

import com.cusca.shopmoney_pg.models.enums.EstadoUsuario;
import com.cusca.shopmoney_pg.utils.validations.user.ExistsByDUI;
import com.cusca.shopmoney_pg.utils.validations.user.ExistsByEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class UsuarioRequest {
    @NotBlank(message = "El nombre completo es requerido")
    @Size(max = 255, message = "El nombre completo no debe exceder 255 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe tener un formato válido")
    @ExistsByEmail
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @Size(max = 20, message = "El teléfono no debe exceder 20 caracteres")
    private String telefono;

    private String direccion;

    @Size(max = 50, message = "El DUI no debe exceder 50 caracteres")
    @ExistsByDUI
    private String dui;

    @Past(message = "La fecha de nacimiento debe ser anterior a la fecha actual")
    private LocalDate fechaNacimiento;

    private EstadoUsuario estado;

    private List<Long> rolesIds;
}