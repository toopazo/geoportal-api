package cl.geoportal.api.service;

import cl.geoportal.api.dto.CatalogLayerDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService {

    // TODO: leer catalog/layers/*.yaml con Jackson YAML al arrancar, cachear en memoria
    public List<CatalogLayerDto> getCatalog() {
        return List.of();
    }
}
