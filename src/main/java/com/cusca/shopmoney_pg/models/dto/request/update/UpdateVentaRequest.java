package com.cusca.shopmoney_pg.models.dto.request.update;

import com.cusca.shopmoney_pg.models.enums.EstadoVenta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class UpdateVentaRequest {
    private String clienteOcasional;
    private EstadoVenta estado;
    private String observaciones;
}
