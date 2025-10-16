package com.cusca.shopmoney_pg.models.dto.request;

import com.cusca.shopmoney_pg.models.enums.EstadoProducto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ProductoRequest {
    @NotBlank(message = "El nombre del producto es requerido")
    @Size(max = 255, message = "El nombre no debe exceder 255 caracteres")
    private String nombre;

    private String descripcion;

    private String imagenUrl;

    @NotNull(message = "El precio unitario es requerido")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Positive(message = "El precio unitario debe ser un valor positivo")
    private BigDecimal precioUnitario;

    private EstadoProducto estado;

    @NotNull(message = "La categoría es requerida")
    private Long categoriaId;
}
