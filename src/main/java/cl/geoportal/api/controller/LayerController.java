package cl.geoportal.api.controller;

import cl.geoportal.api.service.LayerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LayerController {

    private final LayerService layerService;

    public LayerController(LayerService layerService) {
        this.layerService = layerService;
    }

    @GetMapping("/layers/{id}")
    public Map<String, Object> layer(
            @PathVariable String id,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) List<String> columns) {
        return layerService.getLayer(id, limit, columns);
    }
}
