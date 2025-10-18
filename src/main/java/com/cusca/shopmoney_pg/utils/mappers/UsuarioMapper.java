package com.cusca.shopmoney_pg.utils.mappers;

import com.cusca.shopmoney_pg.models.dto.request.UsuarioRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateUsuarioRequest;
import com.cusca.shopmoney_pg.models.dto.response.UsuarioResponse;
import com.cusca.shopmoney_pg.models.entities.UsuarioEntity;
import com.cusca.shopmoney_pg.models.stamp.CreateUpdateStamp;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UsuarioMapper {
    @Mapping(target = "createdAt", expression = "java(usuario.getCreateUpdateStamp() != null ? usuario.getCreateUpdateStamp().getCreatedAt() : null)")
    @Mapping(target = "updatedAt", expression = "java(usuario.getCreateUpdateStamp() != null ? usuario.getCreateUpdateStamp().getUpdatedAt() : null)")
    @Mapping(target = "cuentaCliente", ignore = true) // Ignorar para evitar referencia circular
    UsuarioResponse toResponse (UsuarioEntity usuario);

    // Request to Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "cuentaCliente", ignore = true)
    @Mapping(target = "movimientosRealizados", ignore = true)
    @Mapping(target = "notificaciones", ignore = true)
    @Mapping(target = "createUpdateStamp", expression = "java(createStamp())")
    UsuarioEntity toEntity(UsuarioRequest request);

    // Update entity from request
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "cuentaCliente", ignore = true)
    @Mapping(target = "movimientosRealizados", ignore = true)
    @Mapping(target = "notificaciones", ignore = true)
    @Mapping(target = "createUpdateStamp.updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateEntityFromRequest(UpdateUsuarioRequest request, @MappingTarget UsuarioEntity usuario);

    default CreateUpdateStamp createStamp() {
        return CreateUpdateStamp.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
