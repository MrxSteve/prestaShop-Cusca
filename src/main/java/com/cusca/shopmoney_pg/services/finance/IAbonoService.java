package com.cusca.shopmoney_pg.services.finance;

import com.cusca.shopmoney_pg.models.dto.request.AbonoRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateAbonoRequest;
import com.cusca.shopmoney_pg.models.dto.response.AbonoResponse;
import com.cusca.shopmoney_pg.models.entities.AbonoEntity;
import com.cusca.shopmoney_pg.models.enums.EstadoAbono;
import com.cusca.shopmoney_pg.models.enums.MetodoPago;
import com.cusca.shopmoney_pg.services.base.BaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface IAbonoService extends BaseService<AbonoResponse, AbonoRequest, UpdateAbonoRequest> {
    // Búsquedas paginadas para administradores
    Page<AbonoResponse> buscarPorCuentaCliente(Long cuentaClienteId, Pageable pageable);
    Page<AbonoResponse> buscarPorUsuario(Long usuarioId, Pageable pageable);
    Page<AbonoResponse> buscarPorEstado(EstadoAbono estado, Pageable pageable);
    Page<AbonoResponse> buscarPorMetodoPago(MetodoPago metodoPago, Pageable pageable);
    Page<AbonoResponse> buscarPorClienteYEstado(Long cuentaClienteId, EstadoAbono estado, Pageable pageable);
    Page<AbonoResponse> buscarPorFecha(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);
    Page<AbonoResponse> buscarPorRangoMonto(BigDecimal montoMin, BigDecimal montoMax, Pageable pageable);

    // Validaciones
    boolean puedeModificar(Long abonoId);
    boolean puedeAplicar(Long abonoId);
    boolean puedeRechazar(Long abonoId);

    // Gestión de estado
    AbonoResponse cambiarEstado(Long id, EstadoAbono nuevoEstado);
    AbonoResponse aplicar(Long id);
    AbonoResponse marcarComoPendiente(Long id);
    AbonoResponse rechazar(Long id, String motivo);

    // Operaciones de abono
    AbonoResponse procesarAbono(AbonoRequest request);

    // Métodos específicos para clientes
    Optional<AbonoResponse> buscarMiAbonoPorId(Long abonoId, String emailUsuario);
    Page<AbonoResponse> buscarMisAbonos(String emailUsuario, Pageable pageable);
    Page<AbonoResponse> buscarMisAbonosPorEstado(String emailUsuario, EstadoAbono estado, Pageable pageable);
    Page<AbonoResponse> buscarMisAbonosPorFecha(String emailUsuario, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);
    Page<AbonoResponse> buscarMisAbonosPendientes(String emailUsuario, Pageable pageable);

    // Para uso interno
    AbonoEntity buscarEntidadPorId(Long id);
}
