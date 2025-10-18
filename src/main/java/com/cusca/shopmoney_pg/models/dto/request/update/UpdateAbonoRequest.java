package com.cusca.shopmoney_pg.models.dto.request.update;

import com.cusca.shopmoney_pg.models.enums.EstadoAbono;
import com.cusca.shopmoney_pg.models.enums.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class UpdateAbonoRequest {
    private MetodoPago metodoPago;
    private String observaciones;
    private EstadoAbono estado;
}
