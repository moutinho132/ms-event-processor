package com.dev.app.controller;

import com.dev.app.dto.ProductDto;
import com.dev.app.dto.TopProductDto;
import com.dev.app.entity.Product;
import com.dev.app.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de productos.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "API para gestión de productos")
public class ProductController {

    private final ProductService productService;

    /**
     * Lista todos los productos activos.
     */
    @GetMapping
    @Operation(summary = "Listar productos", description = "Obtiene todos los productos activos")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * Obtiene un producto por ID.
     */
    @GetMapping("/{productId}")
    @Operation(summary = "Obtener producto", description = "Obtiene un producto por su ID")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ResponseEntity<?> getProductById(@PathVariable String productId) {
        return productService.getProductById(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo producto.
     */
    @PostMapping
    @Operation(summary = "Crear producto", description = "Crea un nuevo producto")
    @ApiResponse(responseCode = "201", description = "Producto creado exitosamente")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductDto dto) {
        log.info("📦 Creando producto: {}", dto.getName());
        Product product = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * Actualiza un producto.
     */
    @PutMapping("/{productId}")
    @Operation(summary = "Actualizar producto", description = "Actualiza un producto existente")
    @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ResponseEntity<?> updateProduct(
            @PathVariable String productId,
            @Valid @RequestBody ProductDto dto) {
        return productService.updateProduct(productId, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina un producto.
     */
    @DeleteMapping("/{productId}")
    @Operation(summary = "Eliminar producto", description = "Desactiva un producto")
    @ApiResponse(responseCode = "200", description = "Producto eliminado exitosamente")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable String productId) {
        boolean deleted = productService.deleteProduct(productId);
        
        Map<String, Object> response = new LinkedHashMap<>();
        if (deleted) {
            response.put("status", "SUCCESS");
            response.put("message", "Producto eliminado exitosamente");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "ERROR");
            response.put("message", "Producto no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Crea un producto con imagen.
     */
    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear producto con imagen", description = "Crea un nuevo producto con imagen en S3")
    @ApiResponse(responseCode = "201", description = "Producto creado exitosamente")
    public ResponseEntity<?> createProductWithImage(
            @RequestPart("product") ProductDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        log.info("📦 Creando producto con imagen: {}", dto.getName());
        
        try {
            Product product = productService.createProductWithImage(dto, image);
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        } catch (IOException e) {
            log.error("❌ Error al subir imagen: {}", e.getMessage());
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Error al subir la imagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Actualiza la imagen de un producto.
     */
    @PostMapping(value = "/{productId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar imagen", description = "Actualiza la imagen de un producto en S3")
    @ApiResponse(responseCode = "200", description = "Imagen actualizada exitosamente")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ResponseEntity<?> updateProductImage(
            @PathVariable String productId,
            @RequestPart("image") MultipartFile image) {
        
        try {
            return productService.updateProductImage(productId, image)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IOException e) {
            log.error("❌ Error al subir imagen: {}", e.getMessage());
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Error al subir la imagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Obtiene productos por categoría.
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Productos por categoría", description = "Obtiene productos filtrados por categoría")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    /**
     * Obtiene los productos más vendidos.
     */
    @GetMapping("/top-selling")
    @Operation(summary = "Top productos vendidos", description = "Obtiene los productos más vendidos")
    public ResponseEntity<List<TopProductDto>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(productService.getTopSellingProducts(limit, days));
    }

    /**
     * Obtiene productos con bajo stock.
     */
    @GetMapping("/low-stock")
    @Operation(summary = "Bajo stock", description = "Obtiene productos con stock bajo")
    public ResponseEntity<List<Product>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(productService.getLowStockProducts(threshold));
    }
}
