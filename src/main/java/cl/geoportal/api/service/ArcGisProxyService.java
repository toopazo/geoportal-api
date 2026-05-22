package cl.geoportal.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ArcGisProxyService {

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchLayer(String serviceUrl, int layerId, int limit) {
        String countParam = limit > 0 ? "&resultRecordCount=" + limit : "";
        // URI.create() evita que RestTemplate re-codifique los % (doble-encoding)
        URI uri = URI.create(serviceUrl + "/" + layerId
                + "/query?where=1%3D1&outFields=*&f=geojson" + countParam);

        Map<String, Object> geojson = restTemplate.getForObject(uri, Map.class);
        if (geojson == null) return List.of();

        List<Map<String, Object>> features = (List<Map<String, Object>>) geojson.get("features");
        if (features == null) return List.of();

        return features.stream()
                .map(f -> (Map<String, Object>) f.get("properties"))
                .filter(Objects::nonNull)
                .toList();
    }
}
