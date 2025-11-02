package com.cusca.shopmoney_pg.services.catalog;

import com.cusca.shopmoney_pg.models.dto.request.ProductoImagenRequest;
import com.cusca.shopmoney_pg.models.dto.request.ProductoRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateProductoRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateProductoImagen;
import com.cusca.shopmoney_pg.models.dto.response.ProductoResponse;
import com.cusca.shopmoney_pg.models.entities.CategoriaEntity;
import com.cusca.shopmoney_pg.models.entities.ProductoEntity;
import com.cusca.shopmoney_pg.models.enums.EstadoProducto;
import com.cusca.shopmoney_pg.repositories.CategoriaRepository;
import com.cusca.shopmoney_pg.repositories.ProductoRepository;
import com.cusca.shopmoney_pg.services.images.CloudinaryService;
import com.cusca.shopmoney_pg.utils.exceptions.ResourceAlreadyExistsException;
import com.cusca.shopmoney_pg.utils.exceptions.ResourceNotFoundException;
import com.cusca.shopmoney_pg.utils.mappers.ProductoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductoServiceImpl implements IProductoService {
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoMapper productoMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    public ProductoResponse crear(ProductoRequest request) {
        // Verificar que no exista un producto con el mismo nombre
        if (productoRepository.existsByNombre(request.getNombre())) {
            throw new ResourceAlreadyExistsException("Ya existe un producto con el nombre: " + request.getNombre());
        }

        // Verificar que la categoría existe
        CategoriaEntity categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.getCategoriaId()));

        ProductoEntity producto = productoMapper.toEntity(request);
        producto.setCategoria(categoria);

        // Establecer estado por defecto si no se proporciona
        if (producto.getEstado() == null) {
            producto.setEstado(EstadoProducto.DISPONIBLE);
        }

        ProductoEntity productoGuardado = productoRepository.save(producto);

        return productoMapper.toResponse(productoGuardado);
    }

    @Override
    public ProductoResponse actualizar(Long id, UpdateProductoRequest request) {
        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        // Verificar que no exista otro producto con el mismo nombre (solo si se proporciona nombre)
        if (request.getNombre() != null) {
            Optional<ProductoEntity> productoExistente = productoRepository.findByNombreIgnoreCase(request.getNombre());
            if (productoExistente.isPresent() && !productoExistente.get().getId().equals(id)) {
                throw new ResourceAlreadyExistsException("Ya existe otro producto con el nombre: " + request.getNombre());
            }
        }

        // Solo verificar y asignar categoría si se proporciona categoriaId
        if (request.getCategoriaId() != null) {
            CategoriaEntity categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.getCategoriaId()));
            producto.setCategoria(categoria);
        }

        // Actualizar los campos proporcionados
        productoMapper.updateEntity(request, producto);

        ProductoEntity productoActualizado = productoRepository.save(producto);

        return productoMapper.toResponse(productoActualizado);
    }

    @Override
    public void eliminar(Long id) {
        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // Eliminar imagen de Cloudinary si existe
        if (producto.getImagenUrl() != null) {
            cloudinaryService.deleteImage(producto.getImagenUrl());
        }

        productoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductoResponse> buscarPorId(Long id) {
        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        return Optional.of(productoMapper.toResponse(producto));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> listarTodos(Pageable pageable) {
        return productoRepository.findAll(pageable)
                .map(productoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> buscarPorNombreContaining(String nombre, Pageable pageable) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre, pageable)
                .map(productoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> buscarPorEstado(EstadoProducto estado, Pageable pageable) {
        return productoRepository.findByEstado(estado, pageable)
                .map(productoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> buscarPorCategoria(Long categoriaId, Pageable pageable) {
        // Verificar que la categoría existe
        if (!categoriaRepository.existsById(categoriaId)) {
            throw new ResourceNotFoundException("Categoría no encontrada con ID: " + categoriaId);
        }

        return productoRepository.findByCategoriaId(categoriaId, pageable)
                .map(productoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> buscarPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax, Pageable pageable) {
       return productoRepository.findByPrecioUnitarioBetween(precioMin, precioMax, pageable)
                .map(productoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> obtenerProductosMasVendidos(Pageable pageable) {
        return productoRepository.findProductosMasVendidos(pageable)
                .map(productoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        return productoRepository.existsByNombre(nombre);
    }

    @Override
    public ProductoResponse crearConImagen(ProductoImagenRequest request, MultipartFile imagen) {
        // Verificar que no exista un producto con el mismo nombre
        if (productoRepository.existsByNombre(request.getNombre())) {
            throw new ResourceAlreadyExistsException("Ya existe un producto con el nombre: " + request.getNombre());
        }

        // Verificar que la categoría existe
        CategoriaEntity categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.getCategoriaId()));

        // Usar el mapper para mantener consistencia
        ProductoEntity producto = productoMapper.toEntityWhithImage(request);
        producto.setCategoria(categoria);

        // Establecer estado por defecto si no se proporciona
        if (producto.getEstado() == null) {
            producto.setEstado(EstadoProducto.DISPONIBLE);
        }

        // Subir imagen si se proporciona
        if (imagen != null && !imagen.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(imagen);
            producto.setImagenUrl(imageUrl);
        }

        ProductoEntity productoGuardado = productoRepository.save(producto);
        return productoMapper.toResponse(productoGuardado);
    }

    @Override
    public ProductoResponse actualizarConImagen(Long id, UpdateProductoImagen request, MultipartFile imagen) {
        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        if (request.getNombre() != null) {
            Optional<ProductoEntity> productoExistente = productoRepository.findByNombreIgnoreCase(request.getNombre());
            if (productoExistente.isPresent() && !productoExistente.get().getId().equals(id)) {
                throw new ResourceAlreadyExistsException("Ya existe otro producto con el nombre: " + request.getNombre());
            }
        }

        if (request.getCategoriaId() != null) {
            CategoriaEntity categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.getCategoriaId()));
            producto.setCategoria(categoria);
        }

        String imagenAnterior = producto.getImagenUrl();

        // Actualizar campos básicos (ANTES de manejar imagen)
        productoMapper.updateEntityWithImage(request, producto);

        // Manejar imagen (esto sobrescribe cualquier imagenUrl del request)
        if (imagen != null && !imagen.isEmpty()) {
            // Eliminar imagen anterior si existe
            if (imagenAnterior != null) {
                cloudinaryService.deleteImage(imagenAnterior);
            }

            // Subir nueva imagen y FORZAR la nueva URL
            String nuevaImagenUrl = cloudinaryService.uploadImage(imagen);
            producto.setImagenUrl(nuevaImagenUrl); // Esto sobrescribe el imagenUrl del request
        } else {
            // Si no hay nueva imagen, restaurar la imagen anterior
            // (por si el request.imagenUrl era null o diferente)
            producto.setImagenUrl(imagenAnterior);
        }

        ProductoEntity productoActualizado = productoRepository.save(producto);
        return productoMapper.toResponse(productoActualizado);
    }
}