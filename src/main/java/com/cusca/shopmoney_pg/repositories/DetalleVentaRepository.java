package com.cusca.shopmoney_pg.repositories;

import com.cusca.shopmoney_pg.models.entities.DetalleVentaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleVentaRepository extends JpaRepository<DetalleVentaEntity, Long> {
    // Detalles por venta
    Page<DetalleVentaEntity> findByVentaId(Long ventaId, Pageable pageable);
}
