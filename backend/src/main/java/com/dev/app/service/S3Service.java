package com.dev.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para gestión de archivos en S3.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    
    @Value("${aws.s3.bucket:uploads-bucket}")
    private String bucketName;
    
    @Value("${aws.region:us-east-1}")
    private String region;

    /**
     * Sube un archivo a S3.
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename(), folder);
        
        log.info("📤 Subiendo archivo a S3: {}", fileName);
        
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();
        
        s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        
        String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
        
        log.info("✅ Archivo subido exitosamente: {}", url);
        
        return url;
    }

    /**
     * Sube múltiples archivos a S3.
     */
    public List<String> uploadFiles(List<MultipartFile> files, String folder) throws IOException {
        List<String> urls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            urls.add(uploadFile(file, folder));
        }
        
        return urls;
    }

    /**
     * Elimina un archivo de S3.
     */
    public boolean deleteFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            
            log.info("🗑️ Eliminando archivo de S3: {}", key);
            
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.deleteObject(deleteRequest);
            
            log.info("✅ Archivo eliminado exitosamente");
            return true;
            
        } catch (Exception e) {
            log.error("❌ Error al eliminar archivo: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Lista archivos en una carpeta.
     */
    public List<String> listFiles(String folder) {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(folder)
                .build();
        
        ListObjectsV2Response response = s3Client.listObjectsV2(listRequest);
        
        return response.contents().stream()
                .map(obj -> String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, obj.key()))
                .toList();
    }

    /**
     * Genera un nombre único para el archivo.
     */
    private String generateFileName(String originalName, String folder) {
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        return String.format("%s/%s%s", folder, UUID.randomUUID(), extension);
    }

    /**
     * Extrae la key del archivo desde la URL.
     */
    private String extractKeyFromUrl(String url) {
        // https://bucket.s3.region.amazonaws.com/folder/file.jpg
        return url.substring(url.indexOf(".amazonaws.com/") + 15);
    }
}
