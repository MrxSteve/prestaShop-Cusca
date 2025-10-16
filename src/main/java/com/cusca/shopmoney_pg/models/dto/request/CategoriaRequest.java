package com.cusca.shopmoney_pg.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CategoriaRequest {
    @NotBlank(message = "El nombre de la categoría es requerido")
    private String nombre;
}
