package com.cusca.shopmoney_pg.repositories;

import com.cusca.shopmoney_pg.models.entities.NotificacionEntity;
import com.cusca.shopmoney_pg.models.enums.TipoNotificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotificacionRepository extends JpaRepository<NotificacionEntity, Long> {
    // Notificaciones por usuario
    Page<NotificacionEntity> findByUsuarioId(Long usuarioId, Pageable pageable);

    // Notificaciones por tipo
    Page<NotificacionEntity> findByTipo(TipoNotificacion tipo, Pageable pageable);

    // Notificaciones por usuario y tipo
    Page<NotificacionEntity> findByUsuarioIdAndTipo(Long usuarioId, TipoNotificacion tipo, Pageable pageable);

    // Búsquedas por asunto
    Page<NotificacionEntity> findByAsuntoContainingIgnoreCase(String asunto, Pageable pageable);

    // Notificaciones por rango de fechas
    Page<NotificacionEntity> findByFechaEnvioBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
}
