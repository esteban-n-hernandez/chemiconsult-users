package com.chemiconsult.repository;

import com.chemiconsult.entity.ClienteDE;
import com.chemiconsult.enums.TipoClienteEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<ClienteDE, Long> {

    List<ClienteDE> findByActivoTrue();

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCuit(String cuit);

    boolean existsByDni(String dni);

    List<ClienteDE> findByTipoCliente(TipoClienteEnum tipoCliente);

    Optional<ClienteDE> findByUser_Id(Long userId);

    @Query("""
        SELECT c FROM ClienteDE c
        WHERE c.activo = true
          AND (
            LOWER(COALESCE(c.nombre, '') || ' ' || COALESCE(c.apellido, '')) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(COALESCE(c.razonSocial, '')) LIKE LOWER(CONCAT('%', :q, '%'))
            OR COALESCE(c.cuit, '') LIKE CONCAT('%', :q, '%')
            OR COALESCE(c.cuil, '') LIKE CONCAT('%', :q, '%')
          )
        ORDER BY c.razonSocial, c.apellido, c.nombre
        LIMIT 10
        """)
    List<ClienteDE> buscar(@Param("q") String q);
}