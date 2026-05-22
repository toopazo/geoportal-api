package cl.geoportal.api.service;

import cl.geoportal.api.dto.CatalogLayerDto;
import cl.geoportal.api.dto.JoinResultDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class JoinService {

    private final CatalogService catalogService;
    private final LayerService layerService;

    public JoinService(CatalogService catalogService, LayerService layerService) {
        this.catalogService = catalogService;
        this.layerService = layerService;
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

                joins.add(buildJoinEntry(
                        layer.id(), tgtId, layer.title(), tgtTitle,
                        joinOn, joinType, joinMode, verified));

                joins.add(buildJoinEntry(
                        tgtId, layer.id(), tgtTitle, layer.title(),
                        invertJoinOn(joinOn), invertJoinType(joinType), joinMode, verified));
            }
        }

        return joins;
    }

    @SuppressWarnings("unchecked")
    public JoinResultDto executeJoin(String srcId, String tgtId, int limit, String joinMode) {
        // Buscar relación: directa (src → tgt) o inversa (tgt → src)
        CatalogLayerDto srcLayer = catalogService.findLayer(srcId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Capa origen no encontrada: " + srcId));

        Optional<Map<String, Object>> directRel = srcLayer.relations().stream()
                .filter(r -> tgtId.equals(r.get("target")))
                .findFirst();

        String srcCol, tgtCol;
        Map<String, Object> transforms;

        if (directRel.isPresent()) {
            Map<String, Object> rel = directRel.get();
            String[] parts = parseJoinOn((String) rel.get("join_on"));
            srcCol = parts[0];
            tgtCol = parts[1];
            transforms = (Map<String, Object>) rel.get("join_transform");
        } else {
            // Buscar relación inversa: tgt → src
            CatalogLayerDto tgtLayer = catalogService.findLayer(tgtId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Capa destino no encontrada: " + tgtId));

            Map<String, Object> inverseRel = tgtLayer.relations().stream()
                    .filter(r -> srcId.equals(r.get("target")))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "No existe relación entre " + srcId + " y " + tgtId));

            // En la relación inversa, los roles de columna se invierten
            String[] parts = parseJoinOn((String) inverseRel.get("join_on"));
            tgtCol = parts[0];   // lo que era tgt_col pasa a ser src (porque invertimos)
            srcCol = parts[1];
            transforms = (Map<String, Object>) inverseRel.get("join_transform");
            // Los transforms también se invierten
            if (transforms != null) {
                Map<String, Object> inv = new HashMap<>();
                if (transforms.containsKey("src")) inv.put("tgt", transforms.get("src"));
                if (transforms.containsKey("tgt")) inv.put("src", transforms.get("tgt"));
                transforms = inv;
            }
        }

        String srcTransform = transforms != null ? (String) transforms.get("src") : null;
        String tgtTransform = transforms != null ? (String) transforms.get("tgt") : null;

        // Fetch datos: src con límite, tgt completo (para lookup)
        List<Map<String, Object>> srcRows = layerService.fetchRaw(srcId, limit);
        List<Map<String, Object>> tgtRows = layerService.fetchRaw(tgtId, 0);

        // Construir lookup desde tgt
        Map<String, Map<String, Object>> lookup = new LinkedHashMap<>();
        for (Map<String, Object> row : tgtRows) {
            String key = applyTransform(row.get(tgtCol), tgtTransform);
            if (key != null) lookup.put(key, row);
        }

        // Ejecutar join
        List<Map<String, Object>> result = new ArrayList<>();
        int matched = 0, unmatched = 0;

        for (Map<String, Object> srcRow : srcRows) {
            String srcKey = applyTransform(srcRow.get(srcCol), srcTransform);
            Map<String, Object> tgtRow = srcKey != null ? lookup.get(srcKey) : null;

            if (tgtRow != null) {
                Map<String, Object> merged = new LinkedHashMap<>(srcRow);
                tgtRow.forEach((k, v) -> merged.put("tgt_" + k, v));
                result.add(merged);
                matched++;
            } else if ("left".equalsIgnoreCase(joinMode)) {
                result.add(new LinkedHashMap<>(srcRow));
                unmatched++;
            }
            // inner join: si no hay match, se omite la fila
        }

        return new JoinResultDto(result, srcRows.size(), tgtRows.size(), matched, unmatched);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String[] parseJoinOn(String joinOn) {
        String[] parts = joinOn.split("=", 2);
        return new String[]{parts[0].trim(), parts[1].trim()};
    }

    private String applyTransform(Object value, String transform) {
        if (value == null) return null;
        String str = value.toString();
        if (transform == null || transform.isBlank()) return str;
        if (transform.startsWith("zfill:")) {
            int n = Integer.parseInt(transform.substring(6).trim());
            return String.format("%0" + n + "d", Long.parseLong(str));
        }
        return str;
    }

    private Map<String, Object> buildJoinEntry(String srcId, String tgtId,
                                               String srcTitle, String tgtTitle,
                                               String joinOn, String joinType,
                                               String joinMode, boolean verified) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("srcId", srcId);
        entry.put("tgtId", tgtId);
        entry.put("srcTitle", srcTitle);
        entry.put("tgtTitle", tgtTitle);
        entry.put("joinOn", joinOn);
        entry.put("joinType", joinType);
        entry.put("joinMode", joinMode);
        entry.put("verified", verified);
        return entry;
    }

    private String invertJoinOn(String joinOn) {
        if (joinOn == null) return null;
        String[] parts = joinOn.split("=", 2);
        return parts.length == 2 ? parts[1].trim() + " = " + parts[0].trim() : joinOn;
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
