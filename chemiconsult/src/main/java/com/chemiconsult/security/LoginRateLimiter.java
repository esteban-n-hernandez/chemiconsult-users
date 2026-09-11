package com.chemiconsult.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_INTENTOS = 5;
    private static final Duration VENTANA = Duration.ofMinutes(1);

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean intentoPermitido(String ip) {
        Bucket bucket = buckets.computeIfAbsent(ip, this::nuevoBucket);
        return bucket.tryConsume(1);
    }

    private Bucket nuevoBucket(String ip) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(MAX_INTENTOS)
                        .refillGreedy(MAX_INTENTOS, VENTANA)
                        .build())
                .build();
    }
}
