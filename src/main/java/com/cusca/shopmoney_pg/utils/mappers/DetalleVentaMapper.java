package com.cusca.shopmoney_pg.utils.mappers;

import com.cusca.shopmoney_pg.models.dto.request.DetalleVentaRequest;
import com.cusca.shopmoney_pg.models.dto.response.DetalleVentaResponse;
import com.cusca.shopmoney_pg.models.entities.DetalleVentaEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetalleVentaMapper {

    // De Request a Entity
    @Mapping(target = "venta", ignore = true) // Se asignará manualmente en el service
    @Mapping(target = "producto", ignore = true) // Se asignará manualmente en el service
    @Mapping(target = "id", ignore = true)
    DetalleVentaEntity toEntity(DetalleVentaRequest request);

    // De Entity a Response
    @Mapping(target = "nombreProducto", expression = "java(detalleVenta.getProducto() != null ? detalleVenta.getProducto().getNombre() : null)")
    DetalleVentaResponse toResponse(DetalleVentaEntity detalleVenta);

    // Para actualizar una entidad existente
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "venta", ignore = true) // No se debe cambiar la venta
    @Mapping(target = "producto", ignore = true) // No se debe cambiar el producto
    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget DetalleVentaEntity detalleVenta, DetalleVentaRequest request);
}
