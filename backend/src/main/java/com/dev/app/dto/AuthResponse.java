package com.dev.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta de autenticación.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String status;
    private String message;
    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private CustomerDto customer;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerDto {
        private String customerId;
        private String name;
        private String email;
        private String phone;
        private String role;
    }
}
