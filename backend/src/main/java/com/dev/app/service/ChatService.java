package com.dev.app.service;

import com.dev.app.entity.Order;
import com.dev.app.entity.OrderStatus;
import com.dev.app.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio de chat con IA para responder preguntas sobre órdenes.
 * 
 * Utiliza Spring AI con Ollama (local) para proporcionar un asistente virtual
 * que puede responder preguntas sobre el estado de órdenes, estadísticas
 * y consultas generales del sistema.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final OllamaChatModel ollamaChatModel;
    private final OrderRepository orderRepository;
    
    @Value("${spring.ai.ollama.chat.model:llama3.2}")
    private String ollamaModel;

    /**
     * Procesa una pregunta del usuario y genera una respuesta usando IA.
     * 
     * @param question Pregunta del usuario
     * @return Respuesta generada por la IA
     */
    public String chat(String question) {
        log.info("🤖 Procesando pregunta con Ollama ({}): {}", ollamaModel, question);
        
        // Obtener contexto de datos reales
        String context = buildDataContext();
        
        // Crear el prompt del sistema con contexto
        String systemPrompt = """
                Eres un asistente virtual amigable y helpful para un sistema de gestión de órdenes.
                
                Tu nombre es "OrderBot" y trabajas para la plataforma de e-commerce.
                
                Reglas importantes:
                - Responde siempre en español de manera clara y concisa
                - Usa los datos proporcionados del sistema para dar respuestas precisas
                - Si no tienes información específica, indícalo claramente
                - Sé amable y profesional en todo momento
                - Usa emojis ocasionalmente para hacer la conversación más amigable
                - Si te preguntan sobre una orden específica, proporciona toda la información disponible
                
                Contexto actual del sistema:
                %s
                
                Fecha y hora actual: %s
                """.formatted(context, java.time.LocalDateTime.now().toString());
        
        try {
            // Usar Ollama directamente
            String response = ChatClient.builder(ollamaChatModel)
                    .build()
                    .prompt()
                    .system(systemPrompt)
                    .user(question)
                    .call()
                    .content();
            
            log.info("✅ Respuesta generada exitosamente con Ollama");
            return response;
            
        } catch (Exception e) {
            log.error("❌ Error al procesar pregunta con Ollama: {}", e.getMessage());
            
            // Fallback: respuesta basada en reglas si Ollama falla
            return generateFallbackResponse(question);
        }
    }
    
    /**
     * Genera una respuesta basada en reglas si el modelo de IA no está disponible.
     */
    private String generateFallbackResponse(String question) {
        List<Order> allOrders = orderRepository.findAll();
        long totalOrders = allOrders.size();
        long pendingOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long deliveredOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelledOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        
        String lowerQuestion = question.toLowerCase();
        
        if (lowerQuestion.contains("cuántas") || lowerQuestion.contains("cuantas") || lowerQuestion.contains("total")) {
            return String.format("📊 Actualmente hay %d órdenes en el sistema:\n\n" +
                    "• Pendientes: %d\n" +
                    "• Entregadas: %d\n" +
                    "• Canceladas: %d\n\n" +
                    "¿Necesitas más detalles? 😊", 
                    totalOrders, pendingOrders, deliveredOrders, cancelledOrders);
        }
        
        if (lowerQuestion.contains("pendiente") || lowerQuestion.contains("pendientes")) {
            return String.format("⏳ Hay %d órdenes pendientes de procesar.", pendingOrders);
        }
        
        if (lowerQuestion.contains("cancelad")) {
            return String.format("❌ Se han cancelado %d órdenes.", cancelledOrders);
        }
        
        if (lowerQuestion.contains("entregad") || lowerQuestion.contains("completad")) {
            return String.format("✅ Se han entregado %d órdenes exitosamente.", deliveredOrders);
        }
        
        if (lowerQuestion.contains("hola") || lowerQuestion.contains("buenas")) {
            return "¡Hola! 👋 Soy OrderBot, tu asistente virtual. Puedo ayudarte con información sobre las órdenes del sistema. ¿Qué necesitas saber?";
        }
        
        // Respuesta por defecto con estadísticas
        return String.format("🤖 Soy OrderBot, tu asistente. Actualmente el sistema tiene:\n\n" +
                "• Total de órdenes: %d\n" +
                "• Pendientes: %d\n" +
                "• Entregadas: %d\n" +
                "• Canceladas: %d\n\n" +
                "¿En qué puedo ayudarte? 😊", 
                totalOrders, pendingOrders, deliveredOrders, cancelledOrders);
    }
    
    /**
     * Construye un contexto con datos reales del sistema para la IA.
     */
    private String buildDataContext() {
        List<Order> allOrders = orderRepository.findAll();
        
        // Estadísticas generales
        long totalOrders = allOrders.size();
        long pendingOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long confirmedOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CONFIRMED).count();
        long processingOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PROCESSING).count();
        long shippedOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPED).count();
        long deliveredOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelledOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        
        // Montos
        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal pendingAmount = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.CONFIRMED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Últimas 5 órdenes
        StringBuilder recentOrders = new StringBuilder();
        allOrders.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .forEach(order -> recentOrders.append(String.format(
                        "- Orden %s: %s, $%s, Estado: %s%n",
                        order.getOrderId().substring(0, 8),
                        order.getProductName() != null ? order.getProductName() : "Sin producto",
                        order.getTotalAmount(),
                        order.getStatus()
                )));
        
        return """
                📊 ESTADÍSTICAS DEL SISTEMA:
                
                Total de órdenes: %d
                
                Por estado:
                - Pendientes: %d
                - Confirmadas: %d
                - Procesando: %d
                - Enviadas: %d
                - Entregadas: %d
                - Canceladas: %d
                
                Ingresos:
                - Total entregado: $%s
                - Pendiente de procesar: $%s
                
                📦 Últimas 5 órdenes:
                %s
                """.formatted(
                        totalOrders,
                        pendingOrders, confirmedOrders, processingOrders, shippedOrders, deliveredOrders, cancelledOrders,
                        totalRevenue, pendingAmount,
                        recentOrders.toString()
                );
    }
}
