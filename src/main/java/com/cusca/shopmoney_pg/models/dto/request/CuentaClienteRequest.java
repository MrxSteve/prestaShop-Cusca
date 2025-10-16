package com.cusca.shopmoney_pg.models.dto.request;

import com.cusca.shopmoney_pg.models.enums.EstadoCuenta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CuentaClienteRequest {
    @NotNull(message = "El usuario es requerido")
    private Long usuarioId;

    @DecimalMin(value = "0.00", message = "El límite de crédito no puede ser negativo")
    @Positive(message = "El limite de credito debe ser un valor positivo")
    private BigDecimal limiteCredito;

    @DecimalMin(value = "0.00", message = "El saldo actual no puede ser negativo")
    private BigDecimal saldoActual;

    @NotNull(message = "La fecha de apertura es requerida")
    private LocalDate fechaApertura;

    private EstadoCuenta estado;
}
