#!/bin/bash

# ============================================
# Script para inicializar Ollama y descargar modelos
# ============================================

echo "=== Inicializando Ollama ==="

# Esperar a que Ollama esté listo
echo "Esperando a que Ollama inicie..."
until curl -s http://localhost:11434/api/tags > /dev/null 2>&1; do
    echo "Ollama no está listo todavía..."
    sleep 2
done

echo "✓ Ollama está listo!"

# Descargar modelo llama3.2
echo ""
echo "=== Descargando modelo llama3.2 ==="
curl -X POST http://localhost:11434/api/pull -d '{"name": "llama3.2"}'

echo ""
echo "=== Modelos disponibles ==="
curl -s http://localhost:11434/api/tags | python3 -m json.tool 2>/dev/null || curl -s http://localhost:11434/api/tags

echo ""
echo "=== Ollama listo para usar! ==="
echo "API endpoint: http://localhost:11434"
echo ""
echo "Para probar:"
echo "  curl -X POST http://localhost:11434/api/generate -d '{\"model\": \"llama3.2\", \"prompt\": \"Hola!\"}'"
