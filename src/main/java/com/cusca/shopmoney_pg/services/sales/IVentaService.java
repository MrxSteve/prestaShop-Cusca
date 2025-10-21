package com.cusca.shopmoney_pg.services.sales;

import com.cusca.shopmoney_pg.models.dto.request.VentaRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateVentaRequest;
import com.cusca.shopmoney_pg.models.dto.response.VentaResponse;
import com.cusca.shopmoney_pg.models.entities.VentaEntity;
import com.cusca.shopmoney_pg.models.enums.EstadoVenta;
import com.cusca.shopmoney_pg.models.enums.TipoVenta;
import com.cusca.shopmoney_pg.services.base.BaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface IVentaService extends BaseService<VentaResponse, VentaRequest, UpdateVentaRequest> {
    // Búsquedas paginadas
    Page<VentaResponse> buscarPorCuentaCliente(Long cuentaClienteId, Pageable pageable);
    Page<VentaResponse> buscarPorUsuario(Long usuarioId, Pageable pageable);
    Page<VentaResponse> buscarPorTipoVenta(TipoVenta tipoVenta, Pageable pageable);
    Page<VentaResponse> buscarPorEstado(EstadoVenta estado, Pageable pageable);
    Page<VentaResponse> buscarPorFecha(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);
    Page<VentaResponse> buscarPorRangoTotal(BigDecimal montoMin, BigDecimal montoMax, Pageable pageable);
    Page<VentaResponse> buscarPorClienteOcasional(String clienteOcasional, Pageable pageable);
    Page<VentaResponse> buscarPorClienteYEstado(Long cuentaClienteId, EstadoVenta estado, Pageable pageable);
    Page<VentaResponse> buscarPorClienteYFecha(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);

    // Validaciones
    boolean puedeModificar(Long ventaId);
    boolean puedeAnular(Long ventaId);

    // Gestión de estado
    VentaResponse cambiarEstado(Long id, EstadoVenta nuevoEstado);
    VentaResponse marcarComoPagada(Long id);
    VentaResponse marcarComoParcial(Long id);
    VentaResponse cancelar(Long id);

    // Operaciones de venta
    VentaResponse recalcularTotales(Long id);

    // Metodo específico para clientes - verificar y obtener su propia venta
    Optional<VentaResponse> buscarMiVentaPorId(Long ventaId, String emailUsuario);

    // Métodos específicos para clientes que manejan validaciones internas
    Page<VentaResponse> buscarMisCompras(String emailUsuario, Pageable pageable);
    Page<VentaResponse> buscarMisComprasPorEstado(String emailUsuario, EstadoVenta estado, Pageable pageable);
    Page<VentaResponse> buscarMisComprasPorFecha(String emailUsuario, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);
    Page<VentaResponse> buscarMisComprasPendientes(String emailUsuario, Pageable pageable);

    // Para uso interno
    VentaEntity buscarEntidadPorId(Long id);
}
