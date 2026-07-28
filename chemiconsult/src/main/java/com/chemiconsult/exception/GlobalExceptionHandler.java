package com.chemiconsult.exception;

import com.chemiconsult.to.ApiErrorTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    // El cliente cerró la conexión antes de que el servidor terminara de escribir
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleClientAbort(AsyncRequestNotUsableException ex, HttpServletRequest request) {
        log.debug("Cliente desconectado durante la respuesta en {}", request.getRequestURI());
    }

    // Recurso estático no encontrado (favicon.ico, rutas inexistentes, etc.)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorTO> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        log.debug("Recurso no encontrado: {}", request.getRequestURI());
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), null, request);
    }

    // Excepciones de negocio lanzadas desde los servicios
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorTO> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.warn("Error de negocio en {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Error de negocio", ex.getMessage(), null, request);
    }

    // Error de serialización JSON (referencias circulares, etc.)
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<ApiErrorTO> handleMessageNotWritable(HttpMessageNotWritableException ex, HttpServletRequest request) {
        String detail = ex.getMostSpecificCause().getMessage();
        log.error("Error al serializar respuesta en {}: {}", request.getRequestURI(), detail);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error al generar la respuesta",
                "No se pudo procesar la respuesta del servidor. Contacte al administrador.",
                detail,
                request
        );
    }

    // Fallback para cualquier error no contemplado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorTO> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor",
                "Ocurrió un error inesperado. Contacte al administrador.",
                ex.getClass().getSimpleName() + ": " + ex.getMessage(),
                request
        );
    }

    private ResponseEntity<ApiErrorTO> buildResponse(HttpStatus status, String error, String message,
                                                      String detail, HttpServletRequest request) {
        ApiErrorTO body = ApiErrorTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .detail(detail)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
