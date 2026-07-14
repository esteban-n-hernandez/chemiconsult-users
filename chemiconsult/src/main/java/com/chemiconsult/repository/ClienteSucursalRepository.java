package com.chemiconsult.repository;

import com.chemiconsult.entity.ClienteSucursalDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteSucursalRepository extends JpaRepository<ClienteSucursalDE, Long> {
    List<ClienteSucursalDE> findByClienteIdAndActivoTrue(Long clienteId);
    List<ClienteSucursalDE> findByClienteId(Long clienteId);
}