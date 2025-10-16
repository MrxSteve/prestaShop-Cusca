package com.cusca.shopmoney_pg.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RolRequest {
    @NotBlank(message = "El nombre del rol es requerido")
    private String nombre;
}
