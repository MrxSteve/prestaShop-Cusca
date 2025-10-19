package com.cusca.shopmoney_pg.models.dto.response;

import com.cusca.shopmoney_pg.models.enums.EstadoCuenta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CuentaClienteResponse {
    private Long id;
    private Long usuarioId; // Solo el ID en lugar del objeto completo
    private BigDecimal limiteCredito;
    private BigDecimal saldoActual;
    private BigDecimal saldoDisponible;
    private LocalDate fechaApertura;
    private EstadoCuenta estado;
}
