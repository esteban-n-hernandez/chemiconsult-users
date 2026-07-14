package com.chemiconsult.repository;

import com.chemiconsult.entity.ClienteSucursalContactoDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteSucursalContactoRepository extends JpaRepository<ClienteSucursalContactoDE, Long> {

    List<ClienteSucursalContactoDE> findBySucursalId(Long sucursalId);

    @Query("SELECT r FROM ClienteSucursalContactoDE r " +
            "WHERE r.sucursal.id = :sucursalId AND r.contacto.id = :contactoId")
    Optional<ClienteSucursalContactoDE> findBySucursalIdAndContactoId(
            @Param("sucursalId") Long sucursalId,
            @Param("contactoId") Long contactoId);
}