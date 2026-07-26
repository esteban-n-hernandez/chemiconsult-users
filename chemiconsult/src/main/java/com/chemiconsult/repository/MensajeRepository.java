package com.chemiconsult.repository;

import com.chemiconsult.entity.MensajeDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensajeRepository extends JpaRepository<MensajeDE, Long> {

    @Query("""
        SELECT m FROM MensajeDE m
        WHERE (m.emisor.id = :u1 AND m.receptor.id = :u2)
           OR (m.emisor.id = :u2 AND m.receptor.id = :u1)
        ORDER BY m.fechaEnvio ASC
        """)
    List<MensajeDE> findConversacion(@Param("u1") Long u1, @Param("u2") Long u2);

    @Query("""
        SELECT COUNT(m) FROM MensajeDE m
        WHERE m.receptor.id = :receptorId AND m.leido = false
        """)
    long countNoLeidos(@Param("receptorId") Long receptorId);

    @Query("""
        SELECT COUNT(m) FROM MensajeDE m
        WHERE m.receptor.id = :receptorId AND m.emisor.id = :emisorId AND m.leido = false
        """)
    long countNoLeidosDe(@Param("receptorId") Long receptorId, @Param("emisorId") Long emisorId);

    @Modifying
    @Query("""
        UPDATE MensajeDE m SET m.leido = true
        WHERE m.receptor.id = :receptorId AND m.emisor.id = :emisorId AND m.leido = false
        """)
    void marcarLeidosDe(@Param("receptorId") Long receptorId, @Param("emisorId") Long emisorId);

    @Query("""
        SELECT m FROM MensajeDE m
        WHERE m.emisor.id = :userId OR m.receptor.id = :userId
        ORDER BY m.fechaEnvio DESC
        """)
    List<MensajeDE> findTodosDelUsuario(@Param("userId") Long userId);
}
