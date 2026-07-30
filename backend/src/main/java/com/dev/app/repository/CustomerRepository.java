package com.dev.app.repository;

import com.dev.app.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Customer.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByCustomerId(String customerId);
    
    Optional<Customer> findByEmail(String email);
    
    boolean existsByEmail(String email);
}
