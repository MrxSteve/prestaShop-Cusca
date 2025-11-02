package com.cusca.shopmoney_pg.services.catalog;

import com.cusca.shopmoney_pg.models.dto.request.ProductoImagenRequest;
import com.cusca.shopmoney_pg.models.dto.request.ProductoRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateProductoRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateProductoImagen;
import com.cusca.shopmoney_pg.models.dto.response.ProductoResponse;
import com.cusca.shopmoney_pg.models.enums.EstadoProducto;
import com.cusca.shopmoney_pg.services.base.BaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public interface IProductoService extends BaseService<ProductoResponse, ProductoRequest, UpdateProductoRequest> {
    Page<ProductoResponse> buscarPorNombreContaining(String nombre, Pageable pageable);
    Page<ProductoResponse> buscarPorEstado(EstadoProducto estado, Pageable pageable);
    Page<ProductoResponse> buscarPorCategoria(Long categoriaId, Pageable pageable);
    Page<ProductoResponse> buscarPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax, Pageable pageable);
    Page<ProductoResponse> obtenerProductosMasVendidos(Pageable pageable);
    boolean existePorNombre(String nombre);

    // Nuevos metodos para producto con imagen
    ProductoResponse crearConImagen(ProductoImagenRequest request, MultipartFile imagen);
    ProductoResponse actualizarConImagen(Long id, UpdateProductoImagen request, MultipartFile imagen);
}
