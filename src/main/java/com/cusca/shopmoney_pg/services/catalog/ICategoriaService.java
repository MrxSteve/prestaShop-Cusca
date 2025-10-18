package com.cusca.shopmoney_pg.services.catalog;

import com.cusca.shopmoney_pg.models.dto.request.CategoriaRequest;
import com.cusca.shopmoney_pg.models.dto.response.CategoriaResponse;
import com.cusca.shopmoney_pg.services.base.BaseService;

import java.util.Optional;

public interface ICategoriaService extends BaseService<CategoriaResponse, CategoriaRequest, CategoriaRequest> {
    Optional<CategoriaResponse> buscarPorNombre(String nombre);
    boolean existePorNombre(String nombre);
}
