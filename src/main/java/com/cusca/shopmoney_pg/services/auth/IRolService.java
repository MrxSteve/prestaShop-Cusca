package com.cusca.shopmoney_pg.services.auth;

import com.cusca.shopmoney_pg.models.dto.request.RolRequest;
import com.cusca.shopmoney_pg.models.dto.response.RolResponse;
import com.cusca.shopmoney_pg.models.entities.RolEntity;
import com.cusca.shopmoney_pg.services.base.BaseService;

import java.util.Optional;

public interface IRolService extends BaseService<RolResponse, RolRequest, RolRequest> {
    // Búsquedas específicas basadas en el repository
    Optional<RolResponse> buscarPorNombre(String nombre);

    // Validaciones
    boolean existePorNombre(String nombre);

    // Para uso interno
    RolEntity buscarEntidadPorId(Long id);
    RolEntity buscarEntidadPorNombre(String nombre);
}
