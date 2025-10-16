package com.cusca.shopmoney_pg.models.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DetalleVentaRequest {
    @NotNull(message = "El producto es requerido")
    private Long productoId;

    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    @Positive(message = "La cantidad debe ser un valor positivo")
    private Integer cantidad;

    @NotNull(message = "El precio unitario es requerido")
    @DecimalMin(value = "0.01", message = "El precio unitario debe ser mayor a 0")
    @Positive(message = "El precio unitario debe ser un valor positivo")
    private BigDecimal precioUnitario;

    @NotNull(message = "El subtotal es requerido")
    @DecimalMin(value = "0.01", message = "El subtotal debe ser mayor a 0")
    @Positive(message = "El subtotal debe ser un valor positivo")
    private BigDecimal subtotal;
}
