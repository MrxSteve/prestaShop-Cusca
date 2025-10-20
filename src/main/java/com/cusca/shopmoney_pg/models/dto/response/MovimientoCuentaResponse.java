package com.cusca.shopmoney_pg.models.dto.response;

import com.cusca.shopmoney_pg.models.enums.TipoMovimiento;
import com.cusca.shopmoney_pg.models.enums.TipoReferencia;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class MovimientoCuentaResponse {
    private Long id;
    private Long cuentaClienteId;
    private TipoMovimiento tipoMovimiento;
    private String concepto;
    private BigDecimal monto;
    private BigDecimal saldoAnterior;
    private BigDecimal saldoNuevo;
    private TipoReferencia referenciaTipo;
    private Long referenciaId;
    private LocalDateTime fechaMovimiento;
    private Long usuarioId;

    private LocalDateTime createdAt;
}
