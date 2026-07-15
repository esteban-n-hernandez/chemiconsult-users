package com.chemiconsult.brevo.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class BrevoEmailService {

    private final WebClient webClient;
    private final String senderEmail;
    private final String senderName;

    public BrevoEmailService(
            @Value("${brevo.api.key}") String apiKey,
            @Value("${brevo.sender.email}") String senderEmail,
            @Value("${brevo.sender.name}") String senderName) {

        this.senderEmail = senderEmail;
        this.senderName = senderName;

        this.webClient = WebClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Envía un mail simple en HTML a un destinatario puntual.
     * No lanza excepción si falla — un error de mail nunca debe romper el flujo
     * principal (crear una tarea, dar de alta una muestra, etc.). Solo lo logea.
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

        webClient.post()
                .uri("/smtp/email")
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(res -> log.info("Mail enviado a {} — asunto: {}", destinatarioEmail, asunto))
                .doOnError(err -> log.error("Error al enviar mail a {}: {}", destinatarioEmail, err.getMessage()))
                .onErrorResume(err -> reactor.core.publisher.Mono.empty()) // no propaga el error
                .subscribe();
    }
}