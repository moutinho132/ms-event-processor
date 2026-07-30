package com.dev.app.controller;

import com.dev.app.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para el chat con IA.
 * 
 * Proporciona un endpoint para que los usuarios puedan hacer preguntas
 * sobre órdenes y recibir respuestas generadas por IA.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "API de chat con IA para consultas sobre órdenes")
public class ChatController {

    private final ChatService chatService;

    /**
     * Envía una pregunta al chatbot y recibe una respuesta.
     * 
     * @param request Request con la pregunta del usuario
     * @return Respuesta generada por la IA
     */
    @PostMapping("/ask")
    @Operation(
        summary = "Enviar pregunta al chatbot",
        description = "Envía una pregunta en lenguaje natural y recibe una respuesta contextualizada"
    )
    @ApiResponse(responseCode = "200", description = "Respuesta generada exitosamente")
    public ResponseEntity<Map<String, Object>> askQuestion(@RequestBody ChatRequest request) {
        log.info("💬 Pregunta recibida: {}", request.question());
        
        String answer = chatService.chat(request.question());
        
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "question", request.question(),
                "answer", answer
        ));
    }
    
    /**
     * Obtiene sugerencias de preguntas frecuentes.
     * 
     * @return Lista de preguntas sugeridas
     */
    @GetMapping("/suggestions")
    @Operation(
        summary = "Obtener sugerencias de preguntas",
        description = "Retorna una lista de preguntas frecuentes que los usuarios pueden hacer"
    )
    @ApiResponse(responseCode = "200", description = "Lista de sugerencias")
    public ResponseEntity<Map<String, Object>> getSuggestions() {
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "suggestions", java.util.List.of(
                        "¿Cuántas órdenes pendientes hay?",
                        "¿Cuál es el ingreso total de órdenes entregadas?",
                        "¿Cuáles son las últimas órdenes?",
                        "¿Cuántas órdenes se han cancelado?",
                        "Dame un resumen del estado del sistema",
                        "¿Cuál es el producto más vendido?",
                        "¿Cómo está la situación de las órdenes hoy?"
                )
        ));
    }
    
    /**
     * Record para el request del chat.
     */
    public record ChatRequest(String question) {}
}
