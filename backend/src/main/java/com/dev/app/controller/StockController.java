package com.dev.app.controller;

import com.dev.app.dto.StockMovementDto;
import com.dev.app.dto.TopProductDto;
import com.dev.app.entity.Product;
import com.dev.app.entity.StockMovement;
import com.dev.app.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de stock.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
@Tag(name = "Stock", description = "API para gestión de inventario")
public class StockController {

    private final ProductService productService;

    /**
     * Actualiza el stock de un producto.
     */
    @PostMapping("/movement")
    @Operation(summary = "Movimiento de stock", description = "Registra un movimiento de stock")
    @ApiResponse(responseCode = "200", description = "Movimiento registrado exitosamente")
    public ResponseEntity<?> registerMovement(@Valid @RequestBody StockMovementDto dto) {
        log.info("📦 Registrando movimiento de stock: {}", dto);
        
        try {
            Product product = productService.updateStock(
                    dto.getProductId(),
                    dto.getQuantity(),
                    dto.getType(),
                    dto.getReferenceId(),
                    dto.getNotes(),
                    dto.getCreatedBy()
            );
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
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

    /**
     * Obtiene el historial de movimientos de un producto.
     */
    @GetMapping("/history/{productId}")
    @Operation(summary = "Historial de stock", description = "Obtiene el historial de movimientos de un producto")
    public ResponseEntity<List<StockMovement>> getStockHistory(
            @PathVariable String productId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(productService.getProductStockHistory(productId, limit));
    }

    /**
     * Realiza un ajuste de inventario.
     */
    @PostMapping("/adjust/{productId}")
    @Operation(summary = "Ajuste de inventario", description = "Realiza un ajuste de stock")
    public ResponseEntity<?> adjustStock(
            @PathVariable String productId,
            @RequestParam int newQuantity,
            @RequestParam String notes,
            @RequestParam(required = false) String userId) {
        
        try {
            Product product = productService.updateStock(
                    productId,
                    newQuantity,
                    StockMovement.MovementType.ADJUSTMENT,
                    null,
                    notes,
                    userId
            );
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Entrada de stock.
     */
    @PostMapping("/in/{productId}")
    @Operation(summary = "Entrada de stock", description = "Registra entrada de productos")
    public ResponseEntity<?> stockIn(
            @PathVariable String productId,
            @RequestParam int quantity,
            @RequestParam(required = false) String referenceId,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String userId) {
        
        try {
            Product product = productService.updateStock(
                    productId,
                    quantity,
                    StockMovement.MovementType.IN,
                    referenceId,
                    notes,
                    userId
            );
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Salida de stock.
     */
    @PostMapping("/out/{productId}")
    @Operation(summary = "Salida de stock", description = "Registra salida de productos")
    public ResponseEntity<?> stockOut(
            @PathVariable String productId,
            @RequestParam int quantity,
            @RequestParam(required = false) String referenceId,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String userId) {
        
        try {
            Product product = productService.updateStock(
                    productId,
                    quantity,
                    StockMovement.MovementType.OUT,
                    referenceId,
                    notes,
                    userId
            );
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
