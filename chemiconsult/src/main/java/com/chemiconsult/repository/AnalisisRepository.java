package com.chemiconsult.repository;

import com.chemiconsult.entity.AnalisisDE;
import com.chemiconsult.entity.ClienteDE;
import com.chemiconsult.entity.UserDE;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalisisRepository extends JpaRepository<AnalisisDE, Long> {
    List<AnalisisDE> findAllByUser(UserDE user);
    // NUEVO: busca por cliente directo, sin depender de que tenga usuario asignado
    List<AnalisisDE> findAllByCliente(ClienteDE cliente);
    boolean existsByNumeroProtocolo(String numeroProtocolo);
}