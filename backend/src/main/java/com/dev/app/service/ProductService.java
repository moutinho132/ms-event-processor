package com.dev.app.service;

import com.dev.app.dto.ProductDto;
import com.dev.app.dto.TopProductDto;
import com.dev.app.entity.Product;
import com.dev.app.entity.StockMovement;
import com.dev.app.repository.ProductRepository;
import com.dev.app.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio para gestión de productos con S3 y Stock.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final S3Service s3Service;

    /**
     * Obtiene todos los productos activos.
     */
    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrue();
    }

    /**
     * Obtiene un producto por ID.
     */
    public Optional<Product> getProductById(String productId) {
        return productRepository.findByProductId(productId);
    }

    /**
     * Obtiene productos por categoría.
     */
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    /**
     * Crea un nuevo producto.
     */
    @Transactional
    public Product createProduct(ProductDto dto) {
        log.info("📦 Creando producto: {}", dto.getName());
        
        Product product = Product.builder()
                .productId(UUID.randomUUID().toString())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "USD")
                .stock(dto.getStock() != null ? dto.getStock() : 0)
                .category(dto.getCategory())
                .imageUrl(dto.getImageUrl())
                .active(true)
                .build();
        
        return productRepository.save(product);
    }

    /**
     * Crea un producto con imagen.
     */
    @Transactional
    public Product createProductWithImage(ProductDto dto, MultipartFile image) throws IOException {
        log.info("📦 Creando producto con imagen: {}", dto.getName());
        
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = s3Service.uploadFile(image, "products");
        }
        
        Product product = Product.builder()
                .productId(UUID.randomUUID().toString())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "USD")
                .stock(dto.getStock() != null ? dto.getStock() : 0)
                .category(dto.getCategory())
                .imageUrl(imageUrl)
                .active(true)
                .build();
        
        return productRepository.save(product);
    }

    /**
     * Actualiza un producto existente.
     */
    @Transactional
    public Optional<Product> updateProduct(String productId, ProductDto dto) {
        log.info("📝 Actualizando producto: {}", productId);
        
        return productRepository.findByProductId(productId).map(product -> {
            product.setName(dto.getName());
            product.setDescription(dto.getDescription());
            product.setPrice(dto.getPrice());
            if (dto.getCurrency() != null) product.setCurrency(dto.getCurrency());
            if (dto.getStock() != null) product.setStock(dto.getStock());
            product.setCategory(dto.getCategory());
            if (dto.getActive() != null) product.setActive(dto.getActive());
            
            return productRepository.save(product);
        });
    }

    /**
     * Actualiza la imagen de un producto.
     */
    @Transactional
    public Optional<Product> updateProductImage(String productId, MultipartFile image) throws IOException {
        log.info("🖼️ Actualizando imagen del producto: {}", productId);
        
        return productRepository.findByProductId(productId).map(product -> {
            try {
                if (product.getImageUrl() != null) {
                    s3Service.deleteFile(product.getImageUrl());
                }
                
                String imageUrl = s3Service.uploadFile(image, "products");
                product.setImageUrl(imageUrl);
                
                return productRepository.save(product);
            } catch (IOException e) {
                log.error("❌ Error al subir imagen: {}", e.getMessage());
                throw new RuntimeException("Error al subir imagen", e);
            }
        });
    }

    /**
     * Elimina (desactiva) un producto.
     */
    @Transactional
    public boolean deleteProduct(String productId) {
        log.info("🗑️ Eliminando producto: {}", productId);
        
        return productRepository.findByProductId(productId).map(product -> {
            product.setActive(false);
            productRepository.save(product);
            return true;
        }).orElse(false);
    }

    /**
     * Actualiza el stock de un producto.
     */
    @Transactional
    public Product updateStock(String productId, int quantity, StockMovement.MovementType type, 
                               String referenceId, String notes, String userId) {
        log.info("📦 Actualizando stock: {} {} {}", productId, type, quantity);
        
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productId));
        
        int previousStock = product.getStock();
        int newStock;
        
        switch (type) {
            case IN, RETURN -> newStock = previousStock + quantity;
            case OUT, SALE -> {
                if (previousStock < quantity) {
                    throw new RuntimeException("Stock insuficiente. Disponible: " + previousStock);
                }
                newStock = previousStock - quantity;
            }
            case ADJUSTMENT -> newStock = quantity;
            default -> newStock = previousStock;
        }
        
        product.setStock(newStock);
        productRepository.save(product);
        
        StockMovement movement = StockMovement.builder()
                .movementId(UUID.randomUUID().toString())
                .productId(productId)
                .type(type)
                .quantity(quantity)
                .previousStock(previousStock)
                .newStock(newStock)
                .referenceId(referenceId)
                .referenceType(type.name())
                .notes(notes)
                .createdBy(userId)
                .build();
        
        stockMovementRepository.save(movement);
        
        return product;
    }

    /**
     * Obtiene los productos más vendidos.
     */
    public List<TopProductDto> getTopSellingProducts(int limit, int days) {
        log.info("📊 Obteniendo top {} productos en últimos {} días", limit, days);
        
        Instant startDate = Instant.now().minus(days, ChronoUnit.DAYS);
        Instant endDate = Instant.now();
        
        List<Object[]> results = stockMovementRepository.findTopSellingProducts(
                startDate, endDate, PageRequest.of(0, limit));
        
        List<TopProductDto> topProducts = new ArrayList<>();
        int rank = 1;
        
        for (Object[] row : results) {
            String productId = (String) row[0];
            Long totalSold = (Long) row[1];
            
            Optional<Product> productOpt = productRepository.findByProductId(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                
                topProducts.add(TopProductDto.builder()
                        .productId(productId)
                        .productName(product.getName())
                        .category(product.getCategory())
                        .imageUrl(product.getImageUrl())
                        .price(product.getPrice())
                        .totalSold(totalSold.intValue())
                        .totalRevenue(product.getPrice().multiply(BigDecimal.valueOf(totalSold)))
                        .currentStock(product.getStock())
                        .rank(rank++)
                        .build());
            }
        }
        
        return topProducts;
    }

    /**
     * Obtiene productos con bajo stock.
     */
    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findByActiveTrue().stream()
                .filter(p -> p.getStock() < threshold)
                .toList();
    }

    /**
     * Obtiene el historial de movimientos de un producto.
     */
    public List<StockMovement> getProductStockHistory(String productId, int limit) {
        return stockMovementRepository.findLatestByProductId(productId, PageRequest.of(0, limit));
    }
}
