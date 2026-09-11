package com.chemiconsult.repository;

import com.chemiconsult.entity.MensajeDE;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MensajeRepository extends JpaRepository<MensajeDE, Long> {

    // Últimos N mensajes de una conversación (DESC para limitar, se invierte en servicio)
    @Query("""
        SELECT m FROM MensajeDE m
        WHERE (m.emisor.id = :u1 AND m.receptor.id = :u2)
           OR (m.emisor.id = :u2 AND m.receptor.id = :u1)
        ORDER BY m.id DESC
        """)
    List<MensajeDE> findUltimos(@Param("u1") Long u1, @Param("u2") Long u2, Pageable pageable);

    // Mensajes anteriores a un id dado (scroll hacia arriba)
    @Query("""
        SELECT m FROM MensajeDE m
        WHERE ((m.emisor.id = :u1 AND m.receptor.id = :u2)
            OR (m.emisor.id = :u2 AND m.receptor.id = :u1))
           AND m.id < :antesDeId
        ORDER BY m.id DESC
        """)
    List<MensajeDE> findAntesDe(@Param("u1") Long u1, @Param("u2") Long u2,
                                @Param("antesDeId") Long antesDeId, Pageable pageable);

    // Mensajes nuevos desde un id dado (polling)
    @Query("""
        SELECT m FROM MensajeDE m
        WHERE ((m.emisor.id = :u1 AND m.receptor.id = :u2)
            OR (m.emisor.id = :u2 AND m.receptor.id = :u1))
           AND m.id > :despuesDeId
        ORDER BY m.id ASC
        """)
    List<MensajeDE> findDespuesDe(@Param("u1") Long u1, @Param("u2") Long u2,
                                  @Param("despuesDeId") Long despuesDeId);

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
        JOIN FETCH m.emisor
        JOIN FETCH m.receptor
        WHERE m.emisor.id = :userId OR m.receptor.id = :userId
        ORDER BY m.id DESC
        """)
    List<MensajeDE> findTodosDelUsuario(@Param("userId") Long userId);

    @Query("""
        SELECT m.emisor.id, COUNT(m) FROM MensajeDE m
        WHERE m.receptor.id = :receptorId AND m.leido = false
        GROUP BY m.emisor.id
        """)
    List<Object[]> countNoLeidosPorEmisor(@Param("receptorId") Long receptorId);

    // ID del último mensaje enviado por emisorId a receptorId que fue leído
    @Query("""
        SELECT MAX(m.id) FROM MensajeDE m
        WHERE m.emisor.id = :emisorId AND m.receptor.id = :receptorId AND m.leido = true
        """)
    Long findUltimoLeidoId(@Param("emisorId") Long emisorId, @Param("receptorId") Long receptorId);

    // Para limpieza programada: todos los mensajes más viejos que la fecha dada
    @Modifying
    @Query("DELETE FROM MensajeDE m WHERE m.fechaEnvio < :limite")
    void deleteViejos(@Param("limite") LocalDateTime limite);
}
