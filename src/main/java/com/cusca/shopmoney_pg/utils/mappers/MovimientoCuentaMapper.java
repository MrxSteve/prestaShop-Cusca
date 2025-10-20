package com.cusca.shopmoney_pg.utils.mappers;

import com.cusca.shopmoney_pg.models.dto.response.MovimientoCuentaResponse;
import com.cusca.shopmoney_pg.models.entities.MovimientoCuentaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MovimientoCuentaMapper {

    @Mapping(source = "cuentaCliente.id", target = "cuentaClienteId")
    @Mapping(source = "cuentaCliente.usuario.id", target = "usuarioId")
    @Mapping(source = "createUpdateStamp.createdAt", target = "createdAt")
    MovimientoCuentaResponse toResponse(MovimientoCuentaEntity entity);
}
