package com.dev.app.repository;

import com.dev.app.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Product.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findByProductId(String productId);
    
    List<Product> findByCategory(String category);
    
    List<Product> findByActiveTrue();
}
