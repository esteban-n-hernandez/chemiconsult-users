package com.chemiconsult.repository;

import com.chemiconsult.entity.ClienteContactoDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteContactoRepository extends JpaRepository<ClienteContactoDE, Long> {
    List<ClienteContactoDE> findByClienteIdAndActivoTrue(Long clienteId);

    @EntityGraph(attributePaths = "cliente")
    Optional<ClienteContactoDE> findByUser_Id(Long userId);
}