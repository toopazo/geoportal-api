package cl.geoportal.api.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class LayerService {

    private final WfsProxyService wfsProxyService;

    public LayerService(WfsProxyService wfsProxyService) {
        this.wfsProxyService = wfsProxyService;
    }

    // TODO: despachar a JPA (capas estáticas en Neon) o WfsProxyService (capas vivas)
    // según source.type del YAML: static/arcgis_rest → JPA, wfs → proxy
    public Map<String, Object> getLayer(String id, int limit, List<String> columns) {
        return Map.of("rows", List.of(), "layer_id", id);
    }
}
