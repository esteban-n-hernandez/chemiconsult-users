package com.chemiconsult.service;

import com.chemiconsult.entity.PreciosDE;
import com.chemiconsult.mapper.PricingMapper;
import com.chemiconsult.repository.PricingRepository;
import com.chemiconsult.to.PreciosTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class PricingService {

    PricingRepository pricingRepository;

    public List<PreciosTO> getPricing() {
        log.info("Getting pricing information");
        return pricingRepository.findAll().stream().map(PricingMapper::toTO).toList();
    }

    public PreciosTO addPrecio(PreciosTO preciosTO) {
        log.info("Adding new pricing information: {}", preciosTO);
        return PricingMapper.toTO(pricingRepository.save(PricingMapper.toEntity(preciosTO)));
    }

    public PreciosTO updatePrecio(Long id, PreciosTO preciosTO) {
        PreciosDE existing = pricingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Precio no encontrado: " + id));
        existing.setNombreServicio(preciosTO.getNombreServicio());
        existing.setPrecio(preciosTO.getPrecio());
        existing.setPrecioConImpuesto(preciosTO.getPrecioConImpuesto());
        existing.setDescripcion(preciosTO.getDescripcion());
        return PricingMapper.toTO(pricingRepository.save(existing));
    }

    public void deletePrecio(Long id) {
        if (!pricingRepository.existsById(id)) {
            throw new EntityNotFoundException("Precio no encontrado: " + id);
        }
        pricingRepository.deleteById(id);
    }

    public List<PreciosTO> aplicarFactor(Double factor, List<Long> ids) {
        List<PreciosDE> precios = resolverPrecios(ids);
        precios.forEach(p -> {
            if (p.getPrecio() != null)
                p.setPrecio(Math.round(p.getPrecio() * factor * 100.0) / 100.0);
            if (p.getPrecioConImpuesto() != null)
                p.setPrecioConImpuesto(Math.round(p.getPrecioConImpuesto() * factor * 100.0) / 100.0);
        });
        return pricingRepository.saveAll(precios).stream().map(PricingMapper::toTO).toList();
    }

    public List<PreciosTO> recalcularImpuesto(Double porcentaje, List<Long> ids) {
        List<PreciosDE> precios = resolverPrecios(ids);
        double multiplicador = 1 + porcentaje / 100.0;
        precios.forEach(p -> {
            if (p.getPrecio() != null)
                p.setPrecioConImpuesto(Math.round(p.getPrecio() * multiplicador * 100.0) / 100.0);
        });
        return pricingRepository.saveAll(precios).stream().map(PricingMapper::toTO).toList();
    }

    private List<PreciosDE> resolverPrecios(List<Long> ids) {
        return (ids == null || ids.isEmpty())
                ? pricingRepository.findAll()
                : pricingRepository.findAllById(ids);
    }

    @Autowired
    public PricingService(PricingRepository pricingRepository) {
        this.pricingRepository = pricingRepository;
    }

}
