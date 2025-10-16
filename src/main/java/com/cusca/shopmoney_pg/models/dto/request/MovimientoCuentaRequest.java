package com.cusca.shopmoney_pg.models.dto.request;

import com.cusca.shopmoney_pg.models.enums.TipoMovimiento;
import com.cusca.shopmoney_pg.models.enums.TipoReferencia;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class MovimientoCuentaRequest {
    @NotNull(message = "La cuenta del cliente es requerida")
    private Long cuentaClienteId;

    @NotNull(message = "El tipo de movimiento es requerido")
    private TipoMovimiento tipoMovimiento;

    @NotBlank(message = "El concepto es requerido")
    private String concepto;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Positive(message = "El monto debe ser un valor positivo")
    private BigDecimal monto;

    @NotNull(message = "El saldo anterior es requerido")
    private BigDecimal saldoAnterior;

    @NotNull(message = "El saldo nuevo es requerido")
    private BigDecimal saldoNuevo;

    private TipoReferencia referenciaTipo;

    @NotNull(message = "El usuario que realiza el movimiento es requerido")
    private Long usuarioId;
}
