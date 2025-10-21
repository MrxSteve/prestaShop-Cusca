package com.cusca.shopmoney_pg.services.catalog;

import com.cusca.shopmoney_pg.models.dto.request.CategoriaRequest;
import com.cusca.shopmoney_pg.models.dto.response.CategoriaResponse;
import com.cusca.shopmoney_pg.models.entities.CategoriaEntity;
import com.cusca.shopmoney_pg.repositories.CategoriaRepository;
import com.cusca.shopmoney_pg.utils.exceptions.ResourceAlreadyExistsException;
import com.cusca.shopmoney_pg.utils.exceptions.ResourceNotFoundException;
import com.cusca.shopmoney_pg.utils.mappers.CategoriaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements ICategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    @Override
    public CategoriaResponse crear(CategoriaRequest request) {
        // Verificar que no exista una categoría con el mismo nombre
        if (categoriaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new ResourceAlreadyExistsException("Ya existe una categoría con el nombre: " + request.getNombre());
        }

        CategoriaEntity categoria = categoriaMapper.toEntity(request);
        CategoriaEntity categoriaGuardada = categoriaRepository.save(categoria);

        return categoriaMapper.toResponse(categoriaGuardada);
    }

    @Override
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        CategoriaEntity categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        // Verificar que no exista otra categoría con el mismo nombre (excluyendo la actual)
        Optional<CategoriaEntity> categoriaExistente = categoriaRepository.findByNombreIgnoreCase(request.getNombre());
        if (categoriaExistente.isPresent() && !categoriaExistente.get().getId().equals(id)) {
            throw new ResourceAlreadyExistsException("Ya existe otra categoría con el nombre: " + request.getNombre());
        }

        categoriaMapper.updateEntity(categoria, request);
        CategoriaEntity categoriaActualizada = categoriaRepository.save(categoria);

        return categoriaMapper.toResponse(categoriaActualizada);
    }

    @Override
    public void eliminar(Long id) {
        buscarPorId(id);
        categoriaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoriaResponse> buscarPorId(Long id) {
        CategoriaEntity categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        return Optional.of(categoriaMapper.toResponse(categoria));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoriaResponse> listarTodos(Pageable pageable) {
        return categoriaRepository.findAll(pageable)
                .map(categoriaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoriaResponse> buscarPorNombre(String nombre) {
        CategoriaEntity categoria = categoriaRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con nombre: " + nombre));
        return Optional.of(categoriaMapper.toResponse(categoria));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        return categoriaRepository.existsByNombreIgnoreCase(nombre);
    }
}
