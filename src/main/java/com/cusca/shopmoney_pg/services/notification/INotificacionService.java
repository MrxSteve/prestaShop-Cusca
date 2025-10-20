package com.cusca.shopmoney_pg.services.notification;

import com.cusca.shopmoney_pg.models.entities.VentaEntity;

/**
 * Servicio simplificado para notificaciones automáticas por correo
 * Se enfoca únicamente en envío automático de facturas y notificaciones de abono
 */
public interface INotificacionService {

    /**
     * Envía automáticamente la factura por correo cuando se realiza una venta
     * Funciona tanto para CREDITO como CONTADO, solo si el cliente tiene cuenta
     */
    void enviarFacturaVenta(VentaEntity venta);

    /**
     * Envía automáticamente notificación por correo cuando se registra un abono
     */
    void enviarNotificacionAbono(Long usuarioId, String concepto, String monto);
}
