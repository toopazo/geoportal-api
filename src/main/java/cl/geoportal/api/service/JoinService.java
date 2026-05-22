package cl.geoportal.api.service;

import cl.geoportal.api.dto.CatalogLayerDto;
import cl.geoportal.api.dto.JoinResultDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JoinService {

    private final CatalogService catalogService;

    public JoinService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public List<Map<String, Object>> listJoins() {
        List<Map<String, Object>> joins = new ArrayList<>();

        for (CatalogLayerDto layer : catalogService.getCatalog()) {
            for (Map<String, Object> rel : layer.relations()) {
                String tgtId = (String) rel.get("target");
                Optional<CatalogLayerDto> tgt = catalogService.findLayer(tgtId);
                String tgtTitle = tgt.map(CatalogLayerDto::title).orElse(tgtId);
                String joinOn = (String) rel.get("join_on");
                String joinType = (String) rel.get("join_type");
                String joinMode = (String) rel.getOrDefault("join_mode", "left");
                boolean verified = "verified".equals(layer.schemaStatus());

                // join directo: layer → target
                Map<String, Object> forward = new LinkedHashMap<>();
                forward.put("srcId", layer.id());
                forward.put("tgtId", tgtId);
                forward.put("srcTitle", layer.title());
                forward.put("tgtTitle", tgtTitle);
                forward.put("joinOn", joinOn);
                forward.put("joinType", joinType);
                forward.put("joinMode", joinMode);
                forward.put("verified", verified);
                joins.add(forward);

                // join inverso: target → layer (auto-generado)
                Map<String, Object> inverse = new LinkedHashMap<>();
                inverse.put("srcId", tgtId);
                inverse.put("tgtId", layer.id());
                inverse.put("srcTitle", tgtTitle);
                inverse.put("tgtTitle", layer.title());
                inverse.put("joinOn", invertJoinOn(joinOn));
                inverse.put("joinType", invertJoinType(joinType));
                inverse.put("joinMode", joinMode);
                inverse.put("verified", verified);
                joins.add(inverse);
            }
        }

        return joins;
    }

    // TODO: implementar el join real (port de do_join() del FastAPI)
    public JoinResultDto executeJoin(String srcId, String tgtId, int limit, String joinMode) {
        return new JoinResultDto(List.of(), 0, 0, 0, 0);
    }

    private String invertJoinOn(String joinOn) {
        if (joinOn == null) return null;
        String[] parts = joinOn.split("=", 2);
        if (parts.length != 2) return joinOn;
        return parts[1].trim() + " = " + parts[0].trim();
    }

    private String invertJoinType(String joinType) {
        if (joinType == null) return null;
        return switch (joinType) {
            case "many_to_one" -> "one_to_many";
            case "one_to_many" -> "many_to_one";
            default -> joinType;
        };
    }
}
