package com.cusca.shopmoney_pg.utils.mappers;

import com.cusca.shopmoney_pg.models.dto.request.AbonoRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateAbonoRequest;
import com.cusca.shopmoney_pg.models.dto.response.AbonoResponse;
import com.cusca.shopmoney_pg.models.entities.AbonoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AbonoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaAbono", ignore = true)
    @Mapping(target = "cuentaCliente", ignore = true)
    @Mapping(target = "createUpdateStamp", ignore = true)
    AbonoEntity toEntity(AbonoRequest request);

    @Mapping(source = "cuentaCliente.id", target = "cuentaClienteId")
    @Mapping(source = "createUpdateStamp.createdAt", target = "createdAt")
    @Mapping(source = "createUpdateStamp.updatedAt", target = "updatedAt")
    AbonoResponse toResponse(AbonoEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaAbono", ignore = true)
    @Mapping(target = "cuentaCliente", ignore = true)
    @Mapping(target = "createUpdateStamp", ignore = true)
    @Mapping(target = "monto", ignore = true)
    void updateEntity(@MappingTarget AbonoEntity entity, UpdateAbonoRequest request);
}
