package com.chemiconsult.repository;

import com.chemiconsult.entity.AnalisisDE;
import com.chemiconsult.entity.ClienteDE;
import com.chemiconsult.entity.UserDE;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AnalisisRepository extends JpaRepository<AnalisisDE, Long> {
    List<AnalisisDE> findAllByUser(UserDE user);
    List<AnalisisDE> findAllByCliente(ClienteDE cliente);
    boolean existsByNumeroProtocolo(String numeroProtocolo);
    List<AnalisisDE> findAllByFechaIngresoBetweenOrderByFechaIngresoAsc(LocalDate desde, LocalDate hasta);
}