package com.cusca.shopmoney_pg.utils.mappers;

import com.cusca.shopmoney_pg.models.dto.request.RolRequest;
import com.cusca.shopmoney_pg.models.dto.response.RolResponse;
import com.cusca.shopmoney_pg.models.entities.RolEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RolMapper {
    // Entity to Response
    RolResponse toResponse(RolEntity rol);

    // Request to Entity
    RolEntity toEntity(RolRequest request);

    // Update entity from request
    void updateEntityFromRequest(RolRequest request, @MappingTarget RolEntity rol);
}
