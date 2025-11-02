package com.cusca.shopmoney_pg.controllers;

import com.cusca.shopmoney_pg.models.dto.request.ProductoImagenRequest;
import com.cusca.shopmoney_pg.models.dto.request.ProductoRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateProductoRequest;
import com.cusca.shopmoney_pg.models.dto.request.update.UpdateProductoImagen;
import com.cusca.shopmoney_pg.models.dto.response.ProductoResponse;
import com.cusca.shopmoney_pg.models.enums.EstadoProducto;
import com.cusca.shopmoney_pg.services.catalog.IProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Gestión de productos del catálogo")
public class ProductoController {
    private final IProductoService productoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear nuevo producto", description = "Permite crear un nuevo producto en el catálogo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe un producto con ese nombre")
    })
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse response = productoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @Operation(summary = "Listar productos", description = "Obtiene todos los productos con paginación")
    @ApiResponse(responseCode = "200", description = "Productos obtenidos exitosamente")
    public ResponseEntity<Page<ProductoResponse>> listarTodos(
            @PageableDefault(size = 10) @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        Page<ProductoResponse> productos = productoService.listarTodos(pageable);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @Operation(summary = "Obtener producto por ID", description = "Obtiene un producto específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ProductoResponse> obtenerPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nombre")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @Operation(summary = "Buscar productos por nombre", description = "Busca productos que contengan el nombre especificado")
    @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente")
    public ResponseEntity<Page<ProductoResponse>> buscarPorNombreContaining(
            @RequestParam String nombre,
            @PageableDefault(size = 10) @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        Page<ProductoResponse> productos = productoService.buscarPorNombreContaining(nombre, pageable);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @Operation(summary = "Obtener productos por estado", description = "Obtiene productos filtrados por estado")
    @ApiResponse(responseCode = "200", description = "Productos obtenidos exitosamente")
    public ResponseEntity<Page<ProductoResponse>> obtenerPorEstado(
            @PathVariable EstadoProducto estado,
            @PageableDefault(size = 10) @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        Page<ProductoResponse> productos = productoService.buscarPorEstado(estado, pageable);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/categoria/{categoriaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @Operation(summary = "Obtener productos por categoría", description = "Obtiene productos de una categoría específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Productos obtenidos exitosamente"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<Page<ProductoResponse>> obtenerPorCategoria(
            @PathVariable Long categoriaId,
            @PageableDefault(size = 10) @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        Page<ProductoResponse> productos = productoService.buscarPorCategoria(categoriaId, pageable);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/precio")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @Operation(summary = "Buscar productos por rango de precio", description = "Obtiene productos dentro de un rango de precios")
    @ApiResponse(responseCode = "200", description = "Productos obtenidos exitosamente")
    public ResponseEntity<Page<ProductoResponse>> buscarPorRangoPrecio(
            @RequestParam BigDecimal precioMin,
            @RequestParam BigDecimal precioMax,
            @PageableDefault(size = 10) @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        Page<ProductoResponse> productos = productoService.buscarPorRangoPrecio(precioMin, precioMax, pageable);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/mas-vendidos")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @Operation(summary = "Obtener productos más vendidos", description = "Obtiene los productos más vendidos ordenados por cantidad de ventas")
    @ApiResponse(responseCode = "200", description = "Productos más vendidos obtenidos exitosamente")
    public ResponseEntity<Page<ProductoResponse>> obtenerMasVendidos(
            @PageableDefault(size = 10) @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        Page<ProductoResponse> productos = productoService.obtenerProductosMasVendidos(pageable);
        return ResponseEntity.ok(productos);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar producto", description = "Actualiza un producto existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "409", description = "Ya existe un producto con ese nombre")
    })
    public ResponseEntity<ProductoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductoRequest request) {
        ProductoResponse response = productoService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar producto", description = "Elimina un producto existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear producto con imagen")
    public ResponseEntity<ProductoResponse> crearConImagen(
            @RequestParam("nombre") @NotBlank String nombre,
            @RequestParam("descripcion") @NotBlank String descripcion,
            @RequestParam("precioUnitario") @DecimalMin("0.01") BigDecimal precioUnitario,
            @RequestParam("categoriaId") @NotNull Long categoriaId,
            @RequestParam("estado") EstadoProducto estado,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {

        // Crear el request object
        ProductoImagenRequest request = new ProductoImagenRequest();
        request.setNombre(nombre);
        request.setDescripcion(descripcion);
        request.setPrecioUnitario(precioUnitario);
        request.setCategoriaId(categoriaId);
        request.setEstado(estado);

        ProductoResponse response = productoService.crearConImagen(request, imagen);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar producto con imagen")
    public ResponseEntity<ProductoResponse> actualizarConImagen(
            @PathVariable Long id,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "precioUnitario", required = false) BigDecimal precioUnitario,
            @RequestParam(value = "categoriaId", required = false) Long categoriaId,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {

        // Crear el request object para imagen
        UpdateProductoImagen request = new UpdateProductoImagen();
        request.setNombre(nombre);
        request.setDescripcion(descripcion);
        request.setPrecioUnitario(precioUnitario);
        request.setCategoriaId(categoriaId);
        if (estado != null) {
            request.setEstado(EstadoProducto.valueOf(estado.toUpperCase()));
        }

        ProductoResponse response = productoService.actualizarConImagen(id, request, imagen);
        return ResponseEntity.ok(response);
    }
}
