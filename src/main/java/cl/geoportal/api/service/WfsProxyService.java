package cl.geoportal.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class WfsProxyService {

    @Value("${wfs.base.url}")
    private String wfsBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // TODO: implementar fetch completo:
    //   - construir URL WFS con workspace + typename + outputFormat=application/json
    //   - aplicar filtro CQL si es necesario
    //   - parsear GeoJSON response → List<Map<String, Object>>
    //   - respetar limit
    public List<Map<String, Object>> fetchLayer(String workspace, String typename, int limit) {
        return List.of();
    }
}
