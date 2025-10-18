package com.cusca.shopmoney_pg.models.dto.response;

import com.cusca.shopmoney_pg.models.enums.EstadoVenta;
import com.cusca.shopmoney_pg.models.enums.TipoVenta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class VentaResponse {
    private Long id;
    private CuentaClienteResponse cuentaCliente;
    private String clienteOcasional;
    private LocalDateTime fechaVenta;
    private BigDecimal subtotal;
    private BigDecimal total;
    private TipoVenta tipoVenta;
    private EstadoVenta estado;
    private String observaciones;

    private List<DetalleVentaResponse> detalleVentas;
}
