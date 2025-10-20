package com.cusca.shopmoney_pg.utils.mappers;

import com.cusca.shopmoney_pg.models.dto.request.NotificacionRequest;
import com.cusca.shopmoney_pg.models.dto.response.NotificacionResponse;
import com.cusca.shopmoney_pg.models.entities.NotificacionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UsuarioMapper.class})
public interface NotificacionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaEnvio", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    NotificacionEntity toEntity(NotificacionRequest request);

    NotificacionResponse toResponse(NotificacionEntity entity);
}
