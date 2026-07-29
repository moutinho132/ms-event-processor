#!/bin/bash

# Script de inicialización de LocalStack para crear colas SQS y tópicos SNS
# Este script se ejecuta automáticamente cuando LocalStack está listo

echo "=== Inicializando AWS Local Resources en LocalStack ==="

# Esperar a que LocalStack esté completamente listo
sleep 5

# Configurar endpoint de LocalStack
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1
LOCALSTACK_ENDPOINT=http://localhost:4566

echo ""
echo "=== Creando colas SQS ==="

# Crear cola principal
echo "Creando cola principal: ordenes-procesadas-queue..."
aws --endpoint-url=$LOCALSTACK_ENDPOINT sqs create-queue \
    --queue-name ordenes-procesadas-queue \
    --attributes '{"VisibilityTimeout":"30", "MessageRetentionPeriod":"1209600"}' \
    2>/dev/null || echo "Cola ordenes-procesadas-queue ya existe o error al crear"

# Crear cola dead-letter
echo "Creando cola dead-letter: ordenes-procesadas-dlq..."
aws --endpoint-url=$LOCALSTACK_ENDPOINT sqs create-queue \
    --queue-name ordenes-procesadas-dlq \
    --attributes '{"VisibilityTimeout":"30", "MessageRetentionPeriod":"1209600"}' \
    2>/dev/null || echo "Cola ordenes-procesadas-dlq ya existe o error al crear"

echo ""
echo "=== Creando tópicos SNS ==="

# Crear tópico SNS para notificaciones de ordenes completadas
echo "Creando tópico SNS: ordenes-completadas..."
TOPIC_ARN=$(aws --endpoint-url=$LOCALSTACK_ENDPOINT sns create-topic \
    --name ordenes-completadas \
    --output text --query 'TopicArn' 2>/dev/null)
echo "Tópico creado: $TOPIC_ARN"

# Crear suscripción de email (nota: en LocalStack no envía emails reales)
echo "Creando suscripción de email al tópico..."
aws --endpoint-url=$LOCALSTACK_ENDPOINT sns subscribe \
    --topic-arn "$TOPIC_ARN" \
    --protocol email \
    --notification-endpoint orders@example.com \
    2>/dev/null || echo "Suscripción ya existe o error al crear"

echo ""
echo "=== Verificando recursos creados ==="

# Listar colas
echo "Colas SQS:"
aws --endpoint-url=$LOCALSTACK_ENDPOINT sqs list-queues

# Listar tópicos
echo ""
echo "Tópicos SNS:"
aws --endpoint-url=$LOCALSTACK_ENDPOINT sns list-topics

# Listar suscripciones
echo ""
echo "Suscripciones SNS:"
aws --endpoint-url=$LOCALSTACK_ENDPOINT sns list-subscriptions

echo ""
echo "=== Inicialización completada exitosamente ==="
