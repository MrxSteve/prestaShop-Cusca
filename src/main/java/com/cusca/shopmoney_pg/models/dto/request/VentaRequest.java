package com.cusca.shopmoney_pg.models.dto.request;

import com.cusca.shopmoney_pg.models.enums.EstadoVenta;
import com.cusca.shopmoney_pg.models.enums.TipoVenta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class VentaRequest {
    private Long cuentaClienteId;

    @Size(max = 255, message = "El nombre del cliente ocasional no debe exceder 255 caracteres")
    private String clienteOcasional;

    @NotNull(message = "El tipo de venta es requerido")
    private TipoVenta tipoVenta;

    private EstadoVenta estado;

    private String observaciones;

    @NotEmpty(message = "Debe incluir al menos un detalle de venta")
    @Valid
    private List<DetalleVentaRequest> detalleVentas;
}
