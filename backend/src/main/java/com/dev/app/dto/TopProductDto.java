package com.dev.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para productos más vendidos.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDto {
    
    private String productId;
    private String productName;
    private String category;
    private String imageUrl;
    private BigDecimal price;
    private Integer totalSold;
    private BigDecimal totalRevenue;
    private Integer currentStock;
    
    // Ranking
    private Integer rank;
}
