package com.cusca.shopmoney_pg.services.auth;

import com.cusca.shopmoney_pg.models.dto.request.UsuarioRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateUsuarioRequest;
import com.cusca.shopmoney_pg.models.dto.response.UsuarioResponse;
import com.cusca.shopmoney_pg.models.entities.UsuarioEntity;
import com.cusca.shopmoney_pg.models.enums.EstadoUsuario;
import com.cusca.shopmoney_pg.services.base.BaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IUserService extends BaseService<UsuarioResponse, UsuarioRequest, UpdateUsuarioRequest> {
    // Validaciones
    boolean existsByEmail(String email);
    boolean existsByDui(String dui);

    // Búsquedas paginadas basadas en el repository
    Page<UsuarioResponse> buscarPorNombreContaining(String nombre, Pageable pageable);
    Page<UsuarioResponse> buscarPorEstado(EstadoUsuario estado, Pageable pageable);
    Page<UsuarioResponse> buscarPorRolNombre(String nombreRol, Pageable pageable);
    Page<UsuarioResponse> buscarUsuariosConCuenta(Pageable pageable);
    Page<UsuarioResponse> buscarUsuariosSinCuenta(Pageable pageable);

    // Búsquedas específicas
    Optional<UsuarioResponse> buscarPorEmail(String email);
    Optional<UsuarioResponse> buscarPorDui(String dui);

    // Gestión de roles
    UsuarioResponse asignarRol(Long usuarioId, Long rolId);
    UsuarioResponse removerRol(Long usuarioId, Long rolId);

    // Gestión de estado
    UsuarioResponse cambiarEstado(Long id, EstadoUsuario nuevoEstado);
    UsuarioResponse activar(Long id);
    UsuarioResponse desactivar(Long id);
    UsuarioResponse suspender(Long id);

    // Para uso interno (Spring Security)
    UsuarioEntity buscarEntidadPorEmail(String email);
    UsuarioEntity buscarEntidadPorId(Long id);
}
