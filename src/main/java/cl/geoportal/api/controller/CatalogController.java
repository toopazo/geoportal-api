package cl.geoportal.api.controller;

import cl.geoportal.api.dto.CatalogLayerDto;
import cl.geoportal.api.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/catalog")
    public List<CatalogLayerDto> catalog() {
        return catalogService.getCatalog();
    }
}
