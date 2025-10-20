package com.cusca.shopmoney_pg.services.notification;

import com.cusca.shopmoney_pg.models.dto.request.NotificacionRequest;
import com.cusca.shopmoney_pg.models.dto.response.NotificacionResponse;
import com.cusca.shopmoney_pg.models.entities.NotificacionEntity;
import com.cusca.shopmoney_pg.models.entities.UsuarioEntity;
import com.cusca.shopmoney_pg.models.entities.VentaEntity;
import com.cusca.shopmoney_pg.models.entities.DetalleVentaEntity;
import com.cusca.shopmoney_pg.models.enums.TipoNotificacion;
import com.cusca.shopmoney_pg.repositories.NotificacionRepository;
import com.cusca.shopmoney_pg.repositories.UsuarioRepository;
import com.cusca.shopmoney_pg.utils.mappers.NotificacionMapper;
import com.cusca.shopmoney_pg.utils.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificacionServiceImpl implements INotificacionService {
    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    @Async
    public void enviarFacturaVenta(VentaEntity venta) {
        try {
            // Solo enviar si el cliente tiene cuenta (no es ocasional)
            if (venta.getCuentaCliente() == null) {
                log.info("No se envía factura: cliente ocasional sin cuenta");
                return;
            }

            UsuarioEntity usuario = venta.getCuentaCliente().getUsuario();

            // Crear variables para el template de la factura
            Map<String, Object> variables = new HashMap<>();
            variables.put("numeroFactura", venta.getId());
            variables.put("fechaVenta", venta.getFechaVenta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            variables.put("nombreVendedor", "Sistema ShopMoney");
            variables.put("tipoVenta", venta.getTipoVenta().toString());
            variables.put("nombreCliente", usuario.getNombreCompleto());
            variables.put("emailCliente", usuario.getEmail());
            variables.put("numeroCuenta", venta.getCuentaCliente().getId());
            variables.put("total", venta.getTotal().toString());
            variables.put("subtotalFactura", venta.getSubtotal().toString());
            variables.put("descuento", "0.00");

            // Crear lista de productos para la factura
            List<Map<String, Object>> productos = venta.getDetalleVentas().stream()
                    .map(detalle -> {
                        Map<String, Object> producto = new HashMap<>();
                        producto.put("nombre", detalle.getProducto().getNombre());
                        producto.put("cantidad", detalle.getCantidad().toString());
                        producto.put("precio", detalle.getPrecioUnitario().toString());
                        producto.put("subtotal", detalle.getSubtotal().toString());
                        return producto;
                    }).toList();
            variables.put("productos", productos);

            // Determinar asunto según tipo de venta
            String asunto = venta.getTipoVenta().toString().equals("CREDITO") ?
                "Factura de Venta a Crédito - ShopMoney" :
                "Factura de Venta - ShopMoney";

            // Enviar correo con la factura
            emailService.enviarEmail(usuario.getEmail(), asunto, "email-venta", variables);

            // Si es venta a crédito, también guardar notificación de cargo
            if (venta.getTipoVenta().toString().equals("CREDITO")) {
                guardarNotificacionCargo(usuario, venta);
            }

            log.info("Factura enviada por correo para venta ID: {} - Usuario: {}", venta.getId(), usuario.getEmail());

        } catch (Exception e) {
            log.error("Error enviando factura por correo para venta ID {}: {}", venta.getId(), e.getMessage());
        }
    }

    @Async
    public void enviarNotificacionAbono(Long usuarioId, String concepto, String monto) {
        try {
            UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

            // Guardar notificación en base de datos
            String asunto = "Abono recibido - ShopMoney";
            String mensaje = String.format("Se ha registrado un abono de $%s por concepto: %s", monto, concepto);
            guardarNotificacion(usuario, TipoNotificacion.ABONO, asunto, mensaje);

            // Preparar variables para el correo
            Map<String, Object> variables = new HashMap<>();
            variables.put("nombreCliente", usuario.getNombreCompleto());
            variables.put("concepto", concepto);
            variables.put("monto", monto);
            variables.put("fechaAbono", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            // Enviar correo
            emailService.enviarEmail(usuario.getEmail(), asunto, "email-abono", variables);

            log.info("Notificación de abono enviada para usuario: {}", usuario.getEmail());

        } catch (Exception e) {
            log.error("Error enviando notificación de abono: {}", e.getMessage());
        }
    }

    private void guardarNotificacionCargo(UsuarioEntity usuario, VentaEntity venta) {
        try {
            String asunto = "Nuevo cargo en tu cuenta - ShopMoney";
            String mensaje = String.format("Se ha registrado un cargo de $%s por compra - Factura #%d",
                    venta.getTotal().toString(), venta.getId());

            guardarNotificacion(usuario, TipoNotificacion.CARGO, asunto, mensaje);

        } catch (Exception e) {
            log.error("Error guardando notificación de cargo: {}", e.getMessage());
        }
    }

    private void guardarNotificacion(UsuarioEntity usuario, TipoNotificacion tipo, String asunto, String mensaje) {
        try {
            NotificacionEntity notificacion = NotificacionEntity.builder()
                    .usuario(usuario)
                    .tipo(tipo)
                    .asunto(asunto)
                    .mensaje(mensaje)
                    .build();

            notificacionRepository.save(notificacion);

        } catch (Exception e) {
            log.error("Error guardando notificación en BD: {}", e.getMessage());
        }
    }
}
