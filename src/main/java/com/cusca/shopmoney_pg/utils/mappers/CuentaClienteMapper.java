package com.cusca.shopmoney_pg.utils.mappers;

import com.cusca.shopmoney_pg.models.dto.request.CuentaClienteRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateCuentaClienteRequest;
import com.cusca.shopmoney_pg.models.dto.response.CuentaClienteResponse;
import com.cusca.shopmoney_pg.models.entities.CuentaClienteEntity;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = {UsuarioMapper.class})
public interface CuentaClienteMapper {
    // De Request a Entity
    @Mapping(target = "usuario", ignore = true) // Se asignará manualmente en el service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ventas", ignore = true)
    @Mapping(target = "abonos", ignore = true)
    @Mapping(target = "movimientos", ignore = true)
    CuentaClienteEntity toEntity(CuentaClienteRequest request);

    // De Entity a Response
    @Mapping(target = "saldoDisponible", source = ".", qualifiedByName = "calcularSaldoDisponible")
    CuentaClienteResponse toResponse(CuentaClienteEntity cuentaCliente);

    // Para actualizar una entidad existente
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "usuario", ignore = true) // No se debe cambiar el usuario
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ventas", ignore = true)
    @Mapping(target = "abonos", ignore = true)
    @Mapping(target = "movimientos", ignore = true)
    void updateEntity(UpdateCuentaClienteRequest request, @MappingTarget CuentaClienteEntity cuentaCliente);

    // Metodo personalizado para calcular saldo disponible
    @Named("calcularSaldoDisponible")
    default BigDecimal calcularSaldoDisponible(CuentaClienteEntity cuentaCliente) {
        return cuentaCliente.getSaldoDisponible();
    }
}
