package com.chemiconsult.brevo.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Service
public class BrevoEmailService {

    private final RestClient restClient;
    private final String senderEmail;
    private final String senderName;

    public BrevoEmailService(
            @Value("${brevo.api.key}") String apiKey,
            @Value("${brevo.sender.email}") String senderEmail,
            @Value("${brevo.sender.name}") String senderName) {

        this.senderEmail = senderEmail;
        this.senderName = senderName;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Envía un mail simple en HTML a un destinatario puntual.
     * No lanza excepción si falla — un error de mail nunca debe romper el flujo principal.
     */
    public void enviarMail(String destinatarioEmail, String destinatarioNombre, String asunto, String htmlContent) {
        if (destinatarioEmail == null || destinatarioEmail.isBlank()) {
            log.warn("Intento de enviar mail sin destinatario — se omite el envío");
            return;
        }

        Map<String, Object> body = Map.of(
                "sender", Map.of("email", senderEmail, "name", senderName),
                "to", List.of(Map.of("email", destinatarioEmail, "name", destinatarioNombre != null ? destinatarioNombre : destinatarioEmail)),
                "subject", asunto,
                "htmlContent", htmlContent
        );

        // Fire-and-forget: no bloquea el hilo de la request principal
        CompletableFuture.runAsync(() -> {
            try {
                restClient.post()
                        .uri("/smtp/email")
                        .body(body)
                        .retrieve()
                        .toBodilessEntity();
                log.info("Mail enviado a {} — asunto: {}", destinatarioEmail, asunto);
            } catch (Exception err) {
                log.error("Error al enviar mail a {}: {}", destinatarioEmail, err.getMessage());
            }
        });
    }
}
