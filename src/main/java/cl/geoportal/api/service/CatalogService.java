package cl.geoportal.api.service;

import cl.geoportal.api.dto.CatalogLayerDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);
    private static final String LAYERS_PATTERN = "classpath:catalog/layers/*.yaml";

    private List<CatalogLayerDto> catalog = new ArrayList<>();

    @PostConstruct
    public void loadCatalog() {
        ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
        yaml.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources(LAYERS_PATTERN);

            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    Map<String, Object> raw = yaml.readValue(is, new TypeReference<>() {});
                    catalog.add(toDto(raw));
                    log.info("Loaded layer: {}", resource.getFilename());
                } catch (Exception e) {
                    log.warn("Could not parse {}: {}", resource.getFilename(), e.getMessage());
                }
            }

            log.info("Catalog loaded: {} layers", catalog.size());

        } catch (Exception e) {
            log.error("Failed to load catalog: {}", e.getMessage());
        }
    }

    public List<CatalogLayerDto> getCatalog() {
        return catalog;
    }

    public Optional<CatalogLayerDto> findLayer(String id) {
        return catalog.stream().filter(l -> l.id().equals(id)).findFirst();
    }

    @SuppressWarnings("unchecked")
    private CatalogLayerDto toDto(Map<String, Object> raw) {
        String id = (String) raw.getOrDefault("id", "unknown");
        String schemaStatus = (String) raw.getOrDefault("schema_status", "unknown");

        // title: catalog.identificacion.titulo, fallback al id
        String title = id;
        var catalogMeta = (Map<String, Object>) raw.get("catalog");
        if (catalogMeta != null) {
            var identificacion = (Map<String, Object>) catalogMeta.get("identificacion");
            if (identificacion != null) {
                Object t = identificacion.get("titulo");
                if (t != null) title = t.toString();
            }
        }

        // source.type
        String sourceType = "unknown";
        var source = (Map<String, Object>) raw.get("source");
        if (source != null) {
            sourceType = (String) source.getOrDefault("type", "unknown");
        }

        // feature_count (puede venir como Integer o Long dependiendo del parser)
        int featureCount = 0;
        Object fc = raw.get("feature_count");
        if (fc instanceof Number n) featureCount = n.intValue();

        // columns: Map<nombre, propiedades> → List con "name" añadido
        var columnsRaw = (Map<String, Object>) raw.getOrDefault("columns", Map.of());
        if (columnsRaw == null) columnsRaw = Map.of();

        List<Map<String, Object>> columns = columnsRaw.entrySet().stream()
                .map(entry -> {
                    var col = new LinkedHashMap<String, Object>();
                    var props = entry.getValue();
                    if (props instanceof Map<?, ?> propsMap) {
                        propsMap.forEach((k, v) -> col.put(k.toString(), v));
                    }
                    col.put("name", entry.getKey());
                    return (Map<String, Object>) col;
                })
                .toList();

        // source: mapa completo para que LayerService extraiga workspace/typename/service_url/etc.
        if (source == null) source = Map.of();

        // relations: lista tal cual del YAML, o vacía si no existe
        var relations = (List<Map<String, Object>>) raw.get("relations");
        if (relations == null) relations = List.of();

        return new CatalogLayerDto(id, title, sourceType, featureCount, schemaStatus, source, columns, relations);
    }
}
