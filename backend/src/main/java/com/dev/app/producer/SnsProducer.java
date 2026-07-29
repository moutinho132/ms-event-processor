package com.dev.app.producer;

import com.dev.app.entity.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Productor de notificaciones para AWS SNS.
 * 
 * Envía notificaciones por email cuando una orden es completada.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SnsProducer {
    
    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    
    @Value("${sns.topic.name:ordenes-completadas}")
    private String topicName;
    
    @Value("${sns.topic.email:orders@example.com}")
    private String notificationEmail;
    
    private String topicArn;
    
    /**
     * Inicializa el tópico SNS y la suscripción de email.
     * Se llama automáticamente al procesar una orden completada.
     */
    public void initializeTopicIfNeeded() {
        if (topicArn != null) {
            return;
        }
        
        try {
            // Crear el tópico si no existe
            CreateTopicRequest createTopicRequest = CreateTopicRequest.builder()
                    .name(topicName)
                    .build();
            
            topicArn = snsClient.createTopic(createTopicRequest).topicArn();
            log.info("Tópico SNS creado/obtenido: {}", topicArn);
            
            // Crear suscripción de email si está configurada
            if (notificationEmail != null && !notificationEmail.isEmpty()) {
                SubscribeRequest subscribeRequest = SubscribeRequest.builder()
                        .topicArn(topicArn)
                        .protocol("email")
                        .endpoint(notificationEmail)
                        .build();
                
                snsClient.subscribe(subscribeRequest);
                log.info("Suscripción de email creada para: {}", notificationEmail);
            }
            
        } catch (Exception e) {
            log.error("Error inicializando tópico SNS: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Envía una notificación cuando una orden es completada.
     * 
     * @param order Orden completada
     */
    public void sendOrderCompletedNotification(Order order) {
        log.info("Enviando notificación de orden completada - OrderId: {}", order.getOrderId());
        
        try {
            // Inicializar tópico si es necesario
            initializeTopicIfNeeded();
            
            // Crear mensaje de email
            String subject = String.format("Orden Completada #%s", 
                    order.getOrderId().substring(0, 8));
            
            String message = buildEmailMessage(order);
            
            // Publicar en SNS
            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(topicArn)
                    .subject(subject)
                    .message(message)
                    .build();
            
            PublishResponse response = snsClient.publish(publishRequest);
            
            log.info("Notificación enviada exitosamente - MessageId: {}, OrderId: {}", 
                    response.messageId(), order.getOrderId());
            
        } catch (Exception e) {
            log.error("Error enviando notificación SNS - OrderId: {}, Error: {}", 
                    order.getOrderId(), e.getMessage(), e);
            // No lanzar excepción para no interrumpir el flujo
        }
    }
    
    /**
     * Construye el mensaje de email para la notificación.
     * 
     * @param order Orden completada
     * @return Mensaje formateado
     */
    private String buildEmailMessage(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("¡Tu orden ha sido procesada exitosamente!\n\n");
        sb.append("========================================\n");
        sb.append("DETALLES DE LA ORDEN\n");
        sb.append("========================================\n\n");
        sb.append(String.format("ID de Orden: %s\n", order.getOrderId()));
        sb.append(String.format("ID de Cliente: %s\n", order.getCustomerId()));
        sb.append(String.format("Estado: %s\n", order.getStatus()));
        sb.append(String.format("Monto Total: %s %s\n", order.getTotalAmount(), order.getCurrency()));
        
        if (order.getProductName() != null) {
            sb.append(String.format("\nProducto: %s\n", order.getProductName()));
            sb.append(String.format("Cantidad: %d\n", order.getQuantity()));
            sb.append(String.format("Precio Unitario: %s %s\n", order.getUnitPrice(), order.getCurrency()));
        }
        
        sb.append("\n========================================\n");
        sb.append("Gracias por tu compra!\n");
        sb.append("========================================\n");
        
        return sb.toString();
    }
    
    /**
     * Envía una notificación cuando una orden es cancelada.
     * 
     * @param order Orden cancelada
     * @param reason Razón de cancelación
     */
    public void sendOrderCancelledNotification(Order order, String reason) {
        log.info("Enviando notificación de orden cancelada - OrderId: {}", order.getOrderId());
        
        try {
            initializeTopicIfNeeded();
            
            String subject = String.format("Orden Cancelada #%s", 
                    order.getOrderId().substring(0, 8));
            
            StringBuilder sb = new StringBuilder();
            sb.append("Tu orden ha sido cancelada.\n\n");
            sb.append(String.format("ID de Orden: %s\n", order.getOrderId()));
            sb.append(String.format("Estado: %s\n", order.getStatus()));
            if (reason != null && !reason.isEmpty()) {
                sb.append(String.format("Razón: %s\n", reason));
            }
            sb.append("\nSi tienes preguntas, contacta a soporte.\n");
            
            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(topicArn)
                    .subject(subject)
                    .message(sb.toString())
                    .build();
            
            snsClient.publish(publishRequest);
            log.info("Notificación de cancelación enviada - OrderId: {}", order.getOrderId());
            
        } catch (Exception e) {
            log.error("Error enviando notificación de cancelación: {}", e.getMessage(), e);
        }
    }
}
