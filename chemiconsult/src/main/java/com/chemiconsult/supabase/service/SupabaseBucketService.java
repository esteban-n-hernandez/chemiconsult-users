package com.chemiconsult.supabase.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Service
public class SupabaseBucketService {

    private final RestClient restClient;
    private final String serviceRoleKey;

    public SupabaseBucketService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-role-key}") String serviceRoleKey) {
        this.serviceRoleKey = serviceRoleKey;
        this.restClient = RestClient.builder()
                .baseUrl(supabaseUrl + "/storage/v1")
                .build();
    }

    /**
     * Descarga un archivo de un bucket privado.
     * bucket: ej "sample-documents"
     * path: ej "3f9a.../informe.pdf" (la ruta dentro del bucket)
     */
    public byte[] descargarArchivo(String bucket, String path) {
        return restClient.get()
                .uri("/object/{bucket}/{path}", bucket, path)
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .accept(MediaType.APPLICATION_PDF, MediaType.ALL)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(),
                        (request, response) -> {
                            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Archivo no encontrado en Supabase");
                        })
                .body(byte[].class);
    }

    public void subirArchivo(String bucket, String path, MultipartFile archivo) {
        try {
            restClient.put()
                    .uri("/object/{bucket}/{path}", bucket, path)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .header("Content-Type", "application/pdf")
                    .body(archivo.getBytes())
                    .retrieve()
                    .toBodilessEntity();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el archivo subido", e);
        }
    }

    public void subirArchivoBytes(String bucket, String path, byte[] bytes) {
        restClient.put()
                .uri("/object/{bucket}/{path}", bucket, path)
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .header("Content-Type", "application/pdf")
                .body(bytes)
                .retrieve()
                .toBodilessEntity();
    }

}
