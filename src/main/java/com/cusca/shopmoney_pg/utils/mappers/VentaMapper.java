package com.cusca.shopmoney_pg.utils.mappers;

import com.cusca.shopmoney_pg.models.dto.request.VentaRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateVentaRequest;
import com.cusca.shopmoney_pg.models.dto.response.VentaResponse;
import com.cusca.shopmoney_pg.models.entities.VentaEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {DetalleVentaMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VentaMapper {

    // De Request a Entity
    @Mapping(target = "cuentaCliente", ignore = true) // Se asignará manualmente en el service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaVenta", ignore = true) // Se asigna automáticamente
    @Mapping(target = "detalleVentas", ignore = true) // Se maneja por separado
    VentaEntity toEntity(VentaRequest request);

    // De Entity a Response
    @Mapping(target = "cuentaClienteId", expression = "java(venta.getCuentaCliente() != null ? venta.getCuentaCliente().getId() : null)")
    VentaResponse toResponse(VentaEntity venta);

    // Para actualizar una entidad existente
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "cuentaCliente", ignore = true) // No se debe cambiar la cuenta
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaVenta", ignore = true) // No se cambia la fecha original
    @Mapping(target = "detalleVentas", ignore = true) // Se maneja por separado
    void updateEntity(@MappingTarget VentaEntity venta, UpdateVentaRequest request);
}
