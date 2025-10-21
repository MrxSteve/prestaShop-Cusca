package com.cusca.shopmoney_pg.services.auth;

import com.cusca.shopmoney_pg.models.dto.request.UsuarioRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateUsuarioRequest;
import com.cusca.shopmoney_pg.models.dto.response.UsuarioResponse;
import com.cusca.shopmoney_pg.models.entities.RolEntity;
import com.cusca.shopmoney_pg.models.entities.UsuarioEntity;
import com.cusca.shopmoney_pg.models.enums.EstadoUsuario;
import com.cusca.shopmoney_pg.repositories.UsuarioRepository;
import com.cusca.shopmoney_pg.utils.exceptions.*;
import com.cusca.shopmoney_pg.utils.mappers.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UsuarioRepository usuarioRepository;
    private final IRolService rolService;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UsuarioResponse crear(UsuarioRequest request) {
        // Crear usuario
        UsuarioEntity usuario = usuarioMapper.toEntity(request);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));

        // Asignar roles si se proporcionaron
        if (request.getRolesIds() != null && !request.getRolesIds().isEmpty()) {
            List<RolEntity> roles = new ArrayList<>();
            for (Long rolId : request.getRolesIds()) {
                roles.add(rolService.buscarEntidadPorId(rolId));
            }
            usuario.setRoles(roles);
        }

        UsuarioEntity usuarioGuardado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(usuarioGuardado);
    }

    @Override
    public UsuarioResponse actualizar(Long id, UpdateUsuarioRequest request) {
        UsuarioEntity usuario = buscarEntidadPorId(id);

        // Actualizar datos básicos
        usuarioMapper.updateEntityFromRequest(request, usuario);

        // Actualizar roles si se proporcionaron
        if (request.getRolesIds() != null) {
            List<RolEntity> nuevosRoles = new ArrayList<>();
            for (Long rolId : request.getRolesIds()) {
                nuevosRoles.add(rolService.buscarEntidadPorId(rolId));
            }
            usuario.setRoles(nuevosRoles);
        }

        // Actualizar timestamp manualmente
        usuario.getCreateUpdateStamp().setUpdatedAt(LocalDateTime.now());

        UsuarioEntity usuarioActualizado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(usuarioActualizado);
    }

    @Override
    public void eliminar(Long id) {
        UsuarioEntity usuario = buscarEntidadPorId(id);

        // Verificar que no tenga cuenta de cliente con saldo pendiente
        if (usuario.getCuentaCliente() != null &&
                usuario.getCuentaCliente().getSaldoActual().compareTo(java.math.BigDecimal.ZERO) > 0) {
            throw new UserHasPendingBalanceException("No se puede eliminar el usuario porque tiene saldo pendiente en su cuenta");
        }

        usuarioRepository.delete(usuario);
    }

    @Override
    public Optional<UsuarioResponse> buscarPorId(Long id) {
        UsuarioEntity usuario = buscarEntidadPorId(id);
        return Optional.ofNullable(usuarioMapper.toResponse(usuario));
    }

    @Override
    public Page<UsuarioResponse> listarTodos(Pageable pageable) {
        return usuarioRepository.findAll(pageable)
                .map(usuarioMapper::toResponse);
    }

    @Override
    public Page<UsuarioResponse> buscarPorNombreContaining(String nombre, Pageable pageable) {
        return usuarioRepository.findByNombreCompletoContainingIgnoreCase(nombre, pageable)
                .map(usuarioMapper::toResponse);
    }

    @Override
    public Page<UsuarioResponse> buscarPorEstado(EstadoUsuario estado, Pageable pageable) {
        return usuarioRepository.findByEstado(estado, pageable)
                .map(usuarioMapper::toResponse);
    }

    @Override
    public Page<UsuarioResponse> buscarPorRolNombre(String nombreRol, Pageable pageable) {
        return usuarioRepository.findByRolNombre(nombreRol, pageable)
                .map(usuarioMapper::toResponse);
    }

    @Override
    public Page<UsuarioResponse> buscarUsuariosConCuenta(Pageable pageable) {
        return usuarioRepository.findUsuariosConCuenta(pageable)
                .map(usuarioMapper::toResponse);
    }

    @Override
    public Page<UsuarioResponse> buscarUsuariosSinCuenta(Pageable pageable) {
        return usuarioRepository.findUsuariosSinCuenta(pageable)
                .map(usuarioMapper::toResponse);
    }

    @Override
    public Optional<UsuarioResponse> buscarPorEmail(String email) {
        UsuarioEntity usuario = buscarEntidadPorEmail(email);
        return Optional.ofNullable(usuarioMapper.toResponse(usuario));
    }

    @Override
    public Optional<UsuarioResponse> buscarPorDui(String dui) {
        UsuarioEntity usuario = usuarioRepository.findByDui(dui)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con DUI: " + dui));
        return Optional.ofNullable(usuarioMapper.toResponse(usuario));
    }

    @Override
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByDui(String dui) {
        return usuarioRepository.existsByDui(dui);
    }

    // Gestión de roles
    @Override
    public UsuarioResponse asignarRol(Long usuarioId, Long rolId) {
        UsuarioEntity usuario = buscarEntidadPorId(usuarioId);
        RolEntity rol = rolService.buscarEntidadPorId(rolId);

        if (usuario.getRoles().contains(rol)) {
            throw new RoleAlreadyAssignedException("El usuario ya tiene asignado este rol");
        }

        usuario.getRoles().add(rol);
        usuario.getCreateUpdateStamp().setUpdatedAt(LocalDateTime.now());

        UsuarioEntity usuarioActualizado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(usuarioActualizado);
    }

    @Override
    public UsuarioResponse removerRol(Long usuarioId, Long rolId) {
        UsuarioEntity usuario = buscarEntidadPorId(usuarioId);
        RolEntity rol = rolService.buscarEntidadPorId(rolId);

        if (!usuario.getRoles().contains(rol)) {
            throw new RoleNotAssignedException("El usuario no tiene asignado este rol");
        }

        if (usuario.getRoles().size() == 1) {
            throw new LastRoleRemovalException("No se puede remover el último rol del usuario");
        }

        usuario.getRoles().remove(rol);
        usuario.getCreateUpdateStamp().setUpdatedAt(LocalDateTime.now());

        UsuarioEntity usuarioActualizado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(usuarioActualizado);
    }

    // Gestión de estado
    @Override
    public UsuarioResponse cambiarEstado(Long id, EstadoUsuario nuevoEstado) {
        UsuarioEntity usuario = buscarEntidadPorId(id);
        usuario.setEstado(nuevoEstado);
        usuario.getCreateUpdateStamp().setUpdatedAt(LocalDateTime.now());

        UsuarioEntity usuarioActualizado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(usuarioActualizado);
    }

    @Override
    public UsuarioResponse activar(Long id) {
        return cambiarEstado(id, EstadoUsuario.ACTIVO);
    }

    @Override
    public UsuarioResponse desactivar(Long id) {
        return cambiarEstado(id, EstadoUsuario.INACTIVO);
    }

    @Override
    public UsuarioResponse suspender(Long id) {
        return cambiarEstado(id, EstadoUsuario.SUSPENDIDO);
    }

    // Para uso interno
    @Override
    public UsuarioEntity buscarEntidadPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    @Override
    public UsuarioEntity buscarEntidadPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }
}
