package com.cusca.shopmoney_pg.models.dto.response;

import com.cusca.shopmoney_pg.models.enums.EstadoProducto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ProductoResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String imagenUrl;
    private BigDecimal precioUnitario;
    private EstadoProducto estado;

    private CategoriaResponse categoria;
}
