package com.cusca.shopmoney_pg.models.dto.request.update;

import com.cusca.shopmoney_pg.models.enums.EstadoUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class UpdateUsuarioRequest {
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String direccion;
    private String dui;
    private LocalDate fechaNacimiento;
    private EstadoUsuario estado;
    private List<Long> rolesIds;
}
