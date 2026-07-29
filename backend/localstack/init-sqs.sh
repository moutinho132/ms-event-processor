#!/bin/bash

# Script de inicialización de LocalStack para crear colas SQS
# Este script se ejecuta automáticamente cuando LocalStack está listo

echo "=== Inicializando colas SQS en LocalStack ==="

# Esperar a que LocalStack esté completamente listo
sleep 5

# Configurar endpoint de LocalStack
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1
LOCALSTACK_ENDPOINT=http://localhost:4566

echo "Creando cola principal: ordenes-procesadas-queue..."

# Crear cola principal
aws --endpoint-url=$LOCALSTACK_ENDPOINT sqs create-queue \
    --queue-name ordenes-procesadas-queue \
    --attributes '{"VisibilityTimeout":"30", "MessageRetentionPeriod":"1209600"}' \
    2>/dev/null || echo "Cola ordenes-procesadas-queue ya existe o error al crear"

echo "Creando cola dead-letter: ordenes-procesadas-dlq..."

# Crear cola dead-letter
aws --endpoint-url=$LOCALSTACK_ENDPOINT sqs create-queue \
    --queue-name ordenes-procesadas-dlq \
    --attributes '{"VisibilityTimeout":"30", "MessageRetentionPeriod":"1209600"}' \
    2>/dev/null || echo "Cola ordenes-procesadas-dlq ya existe o error al crear"

echo "=== Colas SQS creadas exitosamente ==="

# Listar colas para verificación
echo "Listando colas creadas:"
aws --endpoint-url=$LOCALSTACK_ENDPOINT sqs list-queues

echo "=== Inicialización completada ==="
