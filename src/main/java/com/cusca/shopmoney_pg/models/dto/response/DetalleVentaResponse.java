package com.cusca.shopmoney_pg.models.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DetalleVentaResponse {
    private Long id;
    private String nombreProducto; // Solo el nombre en lugar del objeto completo
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
