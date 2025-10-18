package com.cusca.shopmoney_pg.models.dto.response;

import com.cusca.shopmoney_pg.models.enums.EstadoAbono;
import com.cusca.shopmoney_pg.models.enums.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AbonoResponse {
    private Long id;
    private CuentaClienteResponse cuentaCliente;
    private BigDecimal monto;
    private LocalDateTime fechaAbono;
    private MetodoPago metodoPago;
    private String observaciones;
    private EstadoAbono estado;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
