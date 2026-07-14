package com.chemiconsult.repository;

import com.chemiconsult.entity.ClienteContactoDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteContactoRepository extends JpaRepository<ClienteContactoDE, Long> {
    List<ClienteContactoDE> findByClienteIdAndActivoTrue(Long clienteId);
}