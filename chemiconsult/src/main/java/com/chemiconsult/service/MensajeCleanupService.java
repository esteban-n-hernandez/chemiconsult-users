package com.chemiconsult.service;

import com.chemiconsult.repository.MensajeRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Log4j2
@Service
public class MensajeCleanupService {

    @Value("${chat.retention.days:7}")
    private int retentionDays;

    private final MensajeRepository mensajeRepository;

    @Autowired
    public MensajeCleanupService(MensajeRepository mensajeRepository) {
        this.mensajeRepository = mensajeRepository;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void limpiarMensajesViejos() {
        LocalDateTime limite = LocalDateTime.now().minusDays(retentionDays);
        log.info("Limpiando mensajes anteriores a {}", limite);
        mensajeRepository.deleteViejos(limite);
    }
}
