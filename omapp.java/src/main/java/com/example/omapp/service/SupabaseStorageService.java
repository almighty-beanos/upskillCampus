package com.example.omapp.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * Talks directly to the Supabase Storage REST API (no supabase-java SDK dependency needed).
 * Uses SUPABASE_URL / SUPABASE_KEY / SUPABASE_BUCKET from application.properties.
 * SUPABASE_KEY must be a service-role key — anon keys will be blocked by storage RLS
 * for arbitrary merchant-initiated writes/deletes.
 */
@Service
public class SupabaseStorageService {

    @Value("${SUPABASE_URL}")
    private String supabaseUrl;

    @Value("${SUPABASE_KEY}")
    private String supabaseKey;

    @Value("${SUPABASE_BUCKET}")
    private String bucket;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Uploads a file to {merchantId}/{itemId}/{randomFilename} and returns the
     * public URL to store in the goods/services mediaUrls column.
     */
    public String uploadFile(MultipartFile file, Integer merchantId, Integer itemId) {
        // 1. Generate path. Supabase handles the virtual directory creation automatically.
        String extension = extensionOf(file.getOriginalFilename());
        String objectPath = merchantId + "/" + itemId + "/" + UUID.randomUUID() + (extension.startsWith(".") ? extension : "." + extension);
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + objectPath;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(supabaseKey);
        headers.set("apikey", supabaseKey);
        headers.set("x-upsert", "true"); // Overwrites if the UUID collides (virtually impossible)

        // 2. Safe Content-Type extraction
        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        if (file.getContentType() != null) {
            try {
                contentType = MediaType.parseMediaType(file.getContentType());
            } catch (InvalidMediaTypeException e) {
                // Fallback to application/octet-stream if parsing fails
            }
        }
        headers.setContentType(contentType);

        try {
            HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
            ResponseEntity<String> response = restTemplate.exchange(uploadUrl, HttpMethod.POST, entity, String.class);

            // 3. Status checking
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Supabase upload failed with status " + response.getStatusCode());
            }

            return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + objectPath;

        } catch (IOException e) {
            throw new RuntimeException("Could not read the uploaded file", e);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to upload media to Supabase: " + e.getMessage(), e);
        }
    }

    /** Deletes the object referenced by a public media URL previously stored in mediaUrls. */
    public void deleteFile(String publicUrl) {
        String objectPath = extractObjectPath(publicUrl);
        if (objectPath == null) {
            // URL doesn't match this bucket's public prefix — nothing we can safely delete.
            return;
        }

        String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucket;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(supabaseKey);
        headers.set("apikey", supabaseKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<String> prefixes = new ArrayList<>();
        prefixes.add(objectPath);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(Map.of("prefixes", prefixes), headers);

        try {
            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to delete media from Supabase: " + e.getMessage(), e);
        }
    }

    private String extractObjectPath(String publicUrl) {
        String marker = "/storage/v1/object/public/" + bucket + "/";
        int idx = publicUrl.indexOf(marker);
        return idx == -1 ? null : publicUrl.substring(idx + marker.length());
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}