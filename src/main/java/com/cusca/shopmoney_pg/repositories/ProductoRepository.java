package com.cusca.shopmoney_pg.repositories;

import com.cusca.shopmoney_pg.models.entities.ProductoEntity;
import com.cusca.shopmoney_pg.models.enums.EstadoProducto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {
    // Búsquedas básicas por nombre
    Page<ProductoEntity> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
    boolean existsByNombre(String nombre);
    Optional<ProductoEntity> findByNombreIgnoreCase(String nombre);

    // Búsquedas por estado
    Page<ProductoEntity> findByEstado(EstadoProducto estado, Pageable pageable);

    // Búsquedas por categoría
    Page<ProductoEntity> findByCategoriaId(Long categoriaId, Pageable pageable);
    Page<ProductoEntity> findByCategoriaNombreContainingIgnoreCase(String categoriaNombre, Pageable pageable);

    // Búsqueda por rango de precio
    Page<ProductoEntity> findByPrecioUnitarioBetween(BigDecimal precioMin, BigDecimal precioMax, Pageable pageable);

    // Productos más vendidos
    @Query("SELECT p FROM ProductoEntity p JOIN p.detalleVentas dv " +
            "GROUP BY p ORDER BY SUM(dv.cantidad) DESC")
    Page<ProductoEntity> findProductosMasVendidos(Pageable pageable);
}
