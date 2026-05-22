package cl.geoportal.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class WfsProxyService {

    @Value("${wfs.base.url}")
    private String wfsBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // GeoServer requiere URL workspace-específica: {baseUrl}/{workspace}/wfs
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchLayer(String workspace, String typename, int limit) {
        String endpoint = wfsBaseUrl + "/" + workspace + "/wfs";

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(endpoint)
                .queryParam("service", "WFS")
                .queryParam("version", "2.0.0")
                .queryParam("request", "GetFeature")
                .queryParam("typeNames", typename)
                .queryParam("outputFormat", "application/json");

        if (limit > 0) {
            builder.queryParam("count", limit);
        }

        Map<String, Object> geojson = restTemplate.getForObject(builder.toUriString(), Map.class);
        if (geojson == null) return List.of();

        List<Map<String, Object>> features = (List<Map<String, Object>>) geojson.get("features");
        if (features == null) return List.of();

        return features.stream()
                .map(f -> (Map<String, Object>) f.get("properties"))
                .filter(Objects::nonNull)
                .toList();
    }
}
