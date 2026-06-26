package com.chemiconsult.repository;

import com.chemiconsult.entity.ClienteDE;
import com.chemiconsult.enums.TipoClienteEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<ClienteDE, Long> {

    List<ClienteDE> findByActivoTrue();

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCuit(String cuit);

    boolean existsByDni(String dni);

    List<ClienteDE> findByTipoCliente(TipoClienteEnum tipoCliente);
}
