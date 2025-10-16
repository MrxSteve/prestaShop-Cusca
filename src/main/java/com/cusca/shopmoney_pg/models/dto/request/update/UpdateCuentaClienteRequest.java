package com.cusca.shopmoney_pg.models.dto.request.update;

import com.cusca.shopmoney_pg.models.enums.EstadoCuenta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class UpdateCuentaClienteRequest {
    private BigDecimal limiteCredito;
    private LocalDate fechaApertura;
    private EstadoCuenta estado;
}
