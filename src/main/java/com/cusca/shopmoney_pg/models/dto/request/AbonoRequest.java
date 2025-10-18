package com.cusca.shopmoney_pg.models.dto.request;

import com.cusca.shopmoney_pg.models.enums.EstadoAbono;
import com.cusca.shopmoney_pg.models.enums.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AbonoRequest {
    @NotNull(message = "La cuenta del cliente es requerida")
    private Long cuentaClienteId;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Positive(message = "El monto debe ser un valor positivo")
    private BigDecimal monto;

    private MetodoPago metodoPago;

    private String observaciones;

    private EstadoAbono estado;
}
