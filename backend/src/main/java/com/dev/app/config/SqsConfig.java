package com.dev.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

/**
 * Configuración de AWS SQS, SNS y S3 Clients.
 * 
 * Configura los clientes SQS, SNS y S3 con soporte para LocalStack en desarrollo
 * y pruebas locales.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Configuration
public class SqsConfig {
    
    @Value("${spring.cloud.aws.region.static}")
    private String region;
    
    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKey;
    
    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretKey;
    
    @Value("${spring.cloud.aws.sqs.endpoint}")
    private String sqsEndpoint;
    
    /**
     * Configura el cliente SQS con endpoint personalizado para LocalStack.
     * 
     * En producción, se usará el endpoint default de AWS.
     * En desarrollo/testing, se usará LocalStack endpoint.
     * 
     * @return SqsClient configurado
     */
    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .endpointOverride(URI.create(sqsEndpoint))
                .build();
    }
    
    /**
     * Configura el cliente SNS con endpoint personalizado para LocalStack.
     * 
     * @return SnsClient configurado
     */
    @Bean
    public SnsClient snsClient() {
        return SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .endpointOverride(URI.create(sqsEndpoint))
                .build();
    }
    
    /**
     * Configura el cliente S3 con endpoint personalizado para LocalStack.
     * 
     * @return S3Client configurado
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .endpointOverride(URI.create(sqsEndpoint))
                .forcePathStyle(true) // Necesario para LocalStack
                .build();
    }
    
}
