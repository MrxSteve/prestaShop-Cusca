package com.cusca.shopmoney_pg.services.auth;

import com.cusca.shopmoney_pg.models.dto.request.RolRequest;
import com.cusca.shopmoney_pg.models.dto.response.RolResponse;
import com.cusca.shopmoney_pg.models.entities.RolEntity;
import com.cusca.shopmoney_pg.repositories.RolRepository;
import com.cusca.shopmoney_pg.utils.exceptions.IntegrityConstraintException;
import com.cusca.shopmoney_pg.utils.exceptions.ResourceNotFoundException;
import com.cusca.shopmoney_pg.utils.mappers.RolMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRolService {
    private final RolRepository rolRepository;
    private final RolMapper rolMapper;

    @Override
    public RolResponse crear(RolRequest request) {
        RolEntity rol = rolMapper.toEntity(request);
        RolEntity rolGuardado = rolRepository.save(rol);
        return rolMapper.toResponse(rolGuardado);
    }

    @Override
    public RolResponse actualizar(Long id, RolRequest request) {
        RolEntity rol = buscarEntidadPorId(id);

        rolMapper.updateEntityFromRequest(request, rol);
        RolEntity rolActualizado = rolRepository.save(rol);
        return rolMapper.toResponse(rolActualizado);
    }

    @Override
    public void eliminar(Long id) {
        RolEntity rol = buscarEntidadPorId(id);

        // Verificar que no tenga usuarios asignados
        if (!rol.getUsuarios().isEmpty()) {
            throw new IntegrityConstraintException("No se puede eliminar el rol porque tiene usuarios asignados");
        }

        rolRepository.delete(rol);
    }

    @Override
    public Optional<RolResponse> buscarPorId(Long id) {
        RolEntity rol = buscarEntidadPorId(id);
        return Optional.of(rolMapper.toResponse(rol));
    }

    @Override
    public Page<RolResponse> listarTodos(Pageable pageable) {
        return rolRepository.findAll(pageable)
                .map(rolMapper::toResponse);
    }

    @Override
    public Optional<RolResponse> buscarPorNombre(String nombre) {
        RolEntity rol = buscarEntidadPorNombre(nombre);
        return Optional.of(rolMapper.toResponse(rol));

    }

    @Override
    public boolean existePorNombre(String nombre) {
        return rolRepository.existsByNombreIgnoreCase(nombre);
    }

    @Override
    public RolEntity buscarEntidadPorId(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol con ID " + id + " no encontrado"));
    }

    @Override
    public RolEntity buscarEntidadPorNombre(String nombre) {
        return rolRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new ResourceNotFoundException("Rol con nombre " + nombre + " no encontrado"));
    }
}
