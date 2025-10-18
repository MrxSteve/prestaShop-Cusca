package com.cusca.shopmoney_pg.models.dto.request.update;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class UpdateDetalleVentaRequest {
    private Integer cantidad;
    private BigDecimal precioUnitario;
}
