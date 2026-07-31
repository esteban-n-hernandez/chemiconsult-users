package com.chemiconsult.service;

import com.chemiconsult.entity.NumeradorDE;
import com.chemiconsult.repository.NumeradorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NumeradorService {

    private final NumeradorRepository numeradorRepository;

    public NumeradorService(NumeradorRepository numeradorRepository) {
        this.numeradorRepository = numeradorRepository;
    }

    /** Devuelve el próximo valor sin incrementarlo (solo para preview en el form). */
    @Transactional(readOnly = true)
    public long getPreview(String nombre) {
        NumeradorDE num = numeradorRepository.findByNombre(nombre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Numerador no encontrado: " + nombre));
        return num.getValor() + 1;
    }

    /** Incrementa el contador y devuelve el nuevo valor. Usa lock pesimista para evitar duplicados. */
    @Transactional
    public long generarSiguiente(String nombre) {
        NumeradorDE num = numeradorRepository.findByNombreForUpdate(nombre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Numerador no encontrado: " + nombre));
        num.setValor(num.getValor() + 1);
        numeradorRepository.save(num);
        return num.getValor();
    }

    /**
     * Si el valor ingresado es mayor al actual, avanza el contador hasta ese valor.
     * Llamar cuando el usuario provee un número de protocolo manual.
     */
    @Transactional
    public void sincronizarSiMayor(String nombre, long valor) {
        NumeradorDE num = numeradorRepository.findByNombreForUpdate(nombre).orElse(null);
        if (num != null && valor > num.getValor()) {
            num.setValor(valor);
            numeradorRepository.save(num);
        }
    }

    @Transactional(readOnly = true)
    public java.util.List<NumeradorDE> listar() {
        return numeradorRepository.findAll();
    }

    @Transactional
    public void actualizar(Long id, long nuevoValor) {
        NumeradorDE num = numeradorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Numerador no encontrado"));
        num.setValor(nuevoValor);
        numeradorRepository.save(num);
    }
}
