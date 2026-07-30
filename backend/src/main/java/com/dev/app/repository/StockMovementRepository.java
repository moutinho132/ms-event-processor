package com.dev.app.repository;

import com.dev.app.entity.StockMovement;
import com.dev.app.entity.StockMovement.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repositorio para movimientos de stock.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    
    List<StockMovement> findByProductId(String productId);
    
    Page<StockMovement> findByProductId(String productId, Pageable pageable);
    
    List<StockMovement> findByType(MovementType type);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.productId = :productId ORDER BY sm.createdAt DESC")
    List<StockMovement> findLatestByProductId(String productId, Pageable pageable);
    
    @Query("SELECT sm.productId, SUM(sm.quantity) as totalSold FROM StockMovement sm " +
           "WHERE sm.type = 'SALE' AND sm.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY sm.productId ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProducts(Instant startDate, Instant endDate, Pageable pageable);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.createdAt BETWEEN :startDate AND :endDate")
    Page<StockMovement> findByDateRange(Instant startDate, Instant endDate, Pageable pageable);
    
    @Query("SELECT SUM(sm.quantity) FROM StockMovement sm WHERE sm.productId = :productId AND sm.type = 'SALE'")
    Integer getTotalSoldByProductId(String productId);
}
