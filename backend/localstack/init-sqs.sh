#!/bin/bash

# Script de inicialización de LocalStack para crear recursos AWS locales
# Este script se ejecuta automáticamente cuando LocalStack está listo

echo "=== Inicializando AWS Local Resources en LocalStack ==="

# Esperar a que LocalStack esté completamente listo
sleep 10

# Configurar endpoint de LocalStack
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1
LOCALSTACK_ENDPOINT=http://localhost:4566

echo ""
echo "=== Creando tablas DynamoDB ==="

# Crear tabla DynamoDB para Customers
echo "Creando tabla DynamoDB: Customers..."
aws --endpoint-url=$LOCALSTACK_ENDPOINT dynamodb create-table \
    --table-name Customers \
    --attribute-definitions AttributeName=customerId,AttributeType=S \
    --key-schema AttributeName=customerId,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    2>/dev/null || echo "Tabla Customers ya existe"

# Crear tabla DynamoDB para Orders
echo "Creando tabla DynamoDB: Orders..."
aws --endpoint-url=$LOCALSTACK_ENDPOINT dynamodb create-table \
    --table-name Orders \
    --attribute-definitions AttributeName=orderId,AttributeType=S \
    --key-schema AttributeName=orderId,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    2>/dev/null || echo "Tabla Orders ya existe"

echo ""
echo "=== Creando buckets S3 ==="

# Crear bucket S3 para archivos de ordenes
echo "Creando bucket S3: orders-bucket..."
aws --endpoint-url=$LOCALSTACK_ENDPOINT s3 mb s3://orders-bucket \
    2>/dev/null || echo "Bucket orders-bucket ya existe"

# Crear bucket S3 para uploads
echo "Creando bucket S3: uploads-bucket..."
aws --endpoint-url=$LOCALSTACK_ENDPOINT s3 mb s3://uploads-bucket \
    2>/dev/null || echo "Bucket uploads-bucket ya existe"

# Subir archivo de ejemplo
echo "Subiendo archivo de ejemplo a S3..."
echo '{"orderId":"test-123","status":"example"}' > /tmp/example-order.json
aws --endpoint-url=$LOCALSTACK_ENDPOINT s3 cp /tmp/example-order.json s3://orders-bucket/examples/ \
    2>/dev/null || echo "Archivo ya existe"

echo ""
echo "=== Creando colas SQS ==="

# Crear cola principal
echo "Creando cola principal: ordenes-procesadas-queue..."
aws --endpoint-url=$LOCALSTACK_ENDPOINT sqs create-queue \
    --queue-name ordenes-procesadas-queue \
    --attributes '{"VisibilityTimeout":"30", "MessageRetentionPeriod":"1209600"}' \
    2>/dev/null || echo "Cola ordenes-procesadas-queue ya existe"

# Crear cola dead-letter
echo "Creando cola dead-letter: ordenes-procesadas-dlq..."
aws --endpoint-url=$LOCALSTACK_ENDPOINT sqs create-queue \
    --queue-name ordenes-procesadas-dlq \
    --attributes '{"VisibilityTimeout":"30", "MessageRetentionPeriod":"1209600"}' \
    2>/dev/null || echo "Cola ordenes-procesadas-dlq ya existe"

# Crear cola para emails
echo "Creando cola: email-queue..."
aws --endpoint-url=$LOCALSTACK_ENDPOINT sqs create-queue \
    --queue-name email-queue \
    --attributes '{"VisibilityTimeout":"60"}' \
    2>/dev/null || echo "Cola email-queue ya existe"

echo ""
echo "=== Creando tópicos SNS ==="

# Crear tópico SNS para notificaciones de ordenes completadas
echo "Creando tópico SNS: ordenes-completadas..."
TOPIC_ARN=$(aws --endpoint-url=$LOCALSTACK_ENDPOINT sns create-topic \
    --name ordenes-completadas \
    --output text --query 'TopicArn' 2>/dev/null)
echo "Tópico creado: $TOPIC_ARN"

# Crear tópico SNS para alertas
echo "Creando tópico SNS: system-alerts..."
ALERT_TOPIC_ARN=$(aws --endpoint-url=$LOCALSTACK_ENDPOINT sns create-topic \
    --name system-alerts \
    --output text --query 'TopicArn' 2>/dev/null)
echo "Tópico creado: $ALERT_TOPIC_ARN"

# Crear suscripción de email
echo "Creando suscripción de email al tópico..."
aws --endpoint-url=$LOCALSTACK_ENDPOINT sns subscribe \
    --topic-arn "$TOPIC_ARN" \
    --protocol email \
    --notification-endpoint orders@example.com \
    2>/dev/null || echo "Suscripción ya existe"

echo ""
echo "=== Creando Lambda Functions ==="

# Crear rol IAM para Lambda
echo "Creando rol IAM para Lambda..."
aws --endpoint-url=$LOCALSTACK_ENDPOINT iam create-role \
    --role-name lambda-order-notification-role \
    --assume-role-policy-document '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"lambda.amazonaws.com"},"Action":"sts:AssumeRole"}]}' \
    2>/dev/null || echo "Rol ya existe"

# Crear función Lambda order-notification
echo "Creando función Lambda: order-notification..."
zip -j /tmp/lambda-order.zip /var/lib/lambda/order-notification.js 2>/dev/null || echo "Zip ya existe"

aws --endpoint-url=$LOCALSTACK_ENDPOINT lambda create-function \
    --function-name order-notification \
    --runtime nodejs20.x \
    --role arn:aws:iam::000000000000:role/lambda-order-notification-role \
    --handler order-notification.handler \
    --zip-file fileb:///tmp/lambda-order.zip \
    2>/dev/null || echo "Lambda order-notification ya existe"

# Suscribir Lambda al tópico SNS
echo "Suscribiendo Lambda al tópico SNS..."
aws --endpoint-url=$LOCALSTACK_ENDPOINT sns subscribe \
    --topic-arn "$TOPIC_ARN" \
    --protocol lambda \
    --notification-endpoint arn:aws:lambda:us-east-1:000000000000:function:order-notification \
    2>/dev/null || echo "Suscripción Lambda ya existe"

echo ""
echo "=== Creando CloudWatch Alarms (métricas) ==="

# Publicar métricas de ejemplo
echo "Publicando métricas de ejemplo en CloudWatch..."
aws --endpoint-url=$LOCALSTACK_ENDPOINT cloudwatch put-metric-data \
    --namespace "MS-Event-Processor/Orders" \
    --metric-data MetricName=OrdersProcessed,Value=150,Unit=Count \
    --dimensions "Service=OrderProcessor" \
    2>/dev/null || echo "Métrica ya publicada"

aws --endpoint-url=$LOCALSTACK_ENDPOINT cloudwatch put-metric-data \
    --namespace "MS-Event-Processor/Latency" \
    --metric-data MetricName=ProcessingTime,Value=45.5,Unit=Milliseconds \
    --dimensions "Service=OrderProcessor" \
    2>/dev/null || echo "Métrica ya publicada"

echo ""
echo "=== Verificando recursos creados ==="

echo ""
echo "📦 Buckets S3:"
aws --endpoint-url=$LOCALSTACK_ENDPOINT s3 ls

echo ""
echo "📋 Tablas DynamoDB:"
aws --endpoint-url=$LOCALSTACK_ENDPOINT dynamodb list-tables

echo ""
echo "📨 Colas SQS:"
aws --endpoint-url=$LOCALSTACK_ENDPOINT sqs list-queues

echo ""
echo "📢 Tópicos SNS:"
aws --endpoint-url=$LOCALSTACK_ENDPOINT sns list-topics

echo ""
echo "⚡ Funciones Lambda:"
aws --endpoint-url=$LOCALSTACK_ENDPOINT lambda list-functions

echo ""
echo "📊 Métricas CloudWatch:"
aws --endpoint-url=$LOCALSTACK_ENDPOINT cloudwatch list-metrics --namespace "MS-Event-Processor"

echo ""
echo "=== Inicialización completada exitosamente ==="
echo ""
echo "🌐 ============================================"
echo "   LOCALSTACK WEB UI: http://localhost:4566"
echo "   (Abre esta URL en tu navegador)"
echo "🌐 ============================================"
echo ""
echo "💡 Comandos útiles:"
echo "   aws --endpoint-url=http://localhost:4566 s3 ls"
echo "   aws --endpoint-url=http://localhost:4566 dynamodb list-tables"
echo "   aws --endpoint-url=http://localhost:4566 sqs list-queues"
