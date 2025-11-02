package com.cusca.shopmoney_pg.utils.mappers;

import com.cusca.shopmoney_pg.models.dto.request.ProductoImagenRequest;
import com.cusca.shopmoney_pg.models.dto.request.ProductoRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateProductoImagen;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateProductoRequest;
import com.cusca.shopmoney_pg.models.dto.response.ProductoResponse;
import com.cusca.shopmoney_pg.models.entities.ProductoEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {CategoriaMapper.class})
public interface ProductoMapper {
    // De Request a Entity
    @Mapping(target = "categoria", ignore = true) // Se asignará manualmente en el service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "detalleVentas", ignore = true)
    ProductoEntity toEntity(ProductoRequest request);

    // De Entity a Response
    ProductoResponse toResponse(ProductoEntity producto);

    // Para actualizar una entidad existente
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "categoria", ignore = true) // Se asignará manualmente en el service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "detalleVentas", ignore = true)
    void updateEntity(UpdateProductoRequest request, @MappingTarget ProductoEntity producto);

    // Nuevos metodos para producto con imagen
    @Mapping(target = "categoria", ignore = true) // Se asignará manualmente en el service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "detalleVentas", ignore = true)
    ProductoEntity toEntityWhithImage(ProductoImagenRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "categoria", ignore = true) // Se asignará manualmente en el service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "detalleVentas", ignore = true)
    void updateEntityWithImage(UpdateProductoImagen request, @MappingTarget ProductoEntity producto);
}
