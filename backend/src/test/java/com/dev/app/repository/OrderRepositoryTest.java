package com.dev.app.repository;

import com.dev.app.entity.Order;
import com.dev.app.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitarios para OrderRepository.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class OrderRepositoryTest {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Test
    void shouldSaveAndFindOrderById() {
        // Given
        String orderId = UUID.randomUUID().toString();
        Order order = Order.builder()
                .orderId(orderId)
                .customerId(UUID.randomUUID().toString())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .processedAt(Instant.now())
                .build();
        
        // When
        Order savedOrder = orderRepository.save(order);
        Optional<Order> foundOrder = orderRepository.findById(savedOrder.getId());
        
        // Then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getOrderId()).isEqualTo(orderId);
        assertThat(foundOrder.get().getStatus()).isEqualTo(OrderStatus.PENDING);
    }
    
    @Test
    void shouldFindByOrderId() {
        // Given
        String orderId = UUID.randomUUID().toString();
        Order order = Order.builder()
                .orderId(orderId)
                .customerId(UUID.randomUUID().toString())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .processedAt(Instant.now())
                .build();
        
        orderRepository.save(order);
        
        // When
        Optional<Order> foundOrder = orderRepository.findByOrderId(orderId);
        
        // Then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getOrderId()).isEqualTo(orderId);
    }
    
    @Test
    void shouldReturnEmptyWhenOrderNotFound() {
        // Given
        String nonExistentOrderId = UUID.randomUUID().toString();
        
        // When
        Optional<Order> foundOrder = orderRepository.findByOrderId(nonExistentOrderId);
        
        // Then
        assertThat(foundOrder).isEmpty();
    }
    
    @Test
    void shouldFindByCustomerId() {
        // Given
        String customerId = UUID.randomUUID().toString();
        Order order1 = Order.builder()
                .orderId(UUID.randomUUID().toString())
                .customerId(customerId)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .processedAt(Instant.now())
                .build();
        
        Order order2 = Order.builder()
                .orderId(UUID.randomUUID().toString())
                .customerId(customerId)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("200.00"))
                .currency("USD")
                .processedAt(Instant.now())
                .build();
        
        orderRepository.saveAll(List.of(order1, order2));
        
        // When
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        
        // Then
        assertThat(orders).hasSize(2);
        assertThat(orders).allMatch(o -> o.getCustomerId().equals(customerId));
    }
    
    @Test
    void shouldFindByStatus() {
        // Given
        Order order1 = Order.builder()
                .orderId(UUID.randomUUID().toString())
                .customerId(UUID.randomUUID().toString())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .processedAt(Instant.now())
                .build();
        
        Order order2 = Order.builder()
                .orderId(UUID.randomUUID().toString())
                .customerId(UUID.randomUUID().toString())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("200.00"))
                .currency("USD")
                .processedAt(Instant.now())
                .build();
        
        orderRepository.saveAll(List.of(order1, order2));
        
        // When
        List<Order> orders = orderRepository.findByStatus(OrderStatus.PENDING);
        
        // Then
        assertThat(orders).hasSize(2);
        assertThat(orders).allMatch(o -> o.getStatus() == OrderStatus.PENDING);
    }
    
    @Test
    void shouldCheckExistsByOrderId() {
        // Given
        String orderId = UUID.randomUUID().toString();
        Order order = Order.builder()
                .orderId(orderId)
                .customerId(UUID.randomUUID().toString())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .processedAt(Instant.now())
                .build();
        
        orderRepository.save(order);
        
        // When
        boolean exists = orderRepository.existsByOrderId(orderId);
        boolean notExists = orderRepository.existsByOrderId(UUID.randomUUID().toString());
        
        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
    
}
