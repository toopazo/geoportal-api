package cl.geoportal.api.dto;

import java.util.List;
import java.util.Map;

public record CatalogLayerDto(
        String id,
        String title,
        String sourceType,
        int featureCount,
        String schemaStatus,
        Map<String, Object> source,
        List<Map<String, Object>> columns,
        List<Map<String, Object>> relations
) {}
