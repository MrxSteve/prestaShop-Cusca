package com.cusca.shopmoney_pg.utils.mappers;

import com.cusca.shopmoney_pg.models.dto.request.CategoriaRequest;
import com.cusca.shopmoney_pg.models.dto.response.CategoriaResponse;
import com.cusca.shopmoney_pg.models.entities.CategoriaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {
    // De Request a Entity
    CategoriaEntity toEntity(CategoriaRequest request);

    // De Entity a Response
    CategoriaResponse toResponse(CategoriaEntity categoria);

    // Para actualizar una entidad existente
    void updateEntity(@MappingTarget CategoriaEntity categoria, CategoriaRequest request);
}
