package cl.geoportal.api.service;

import cl.geoportal.api.dto.CatalogLayerDto;
import cl.geoportal.api.repository.DpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LayerService {

    private final CatalogService catalogService;
    private final WfsProxyService wfsProxyService;
    private final ArcGisProxyService arcGisProxyService;
    private final DpaRepository dpaRepository;

    public LayerService(CatalogService catalogService,
                        WfsProxyService wfsProxyService,
                        ArcGisProxyService arcGisProxyService,
                        DpaRepository dpaRepository) {
        this.catalogService = catalogService;
        this.wfsProxyService = wfsProxyService;
        this.arcGisProxyService = arcGisProxyService;
        this.dpaRepository = dpaRepository;
    }

    public Map<String, Object> getLayer(String id, int limit, List<String> columns) {
        List<Map<String, Object>> rows = fetchRaw(id, limit);
        if (columns != null && !columns.isEmpty()) {
            rows = filterColumns(rows, columns);
        }
        return Map.of("layer_id", id, "count", rows.size(), "rows", rows);
    }

    // Usado por JoinService: fetch sin filtro de columnas, limit=0 significa sin límite
    public List<Map<String, Object>> fetchRaw(String id, int limit) {
        CatalogLayerDto layer = catalogService.findLayer(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Capa no encontrada: " + id));

        return switch (layer.sourceType()) {
            case "wfs" -> {
                String workspace = (String) layer.source().get("workspace");
                String typename = (String) layer.source().get("typename");
                yield wfsProxyService.fetchLayer(workspace, typename, limit);
            }
            case "arcgis_rest" -> {
                String serviceUrl = (String) layer.source().get("service_url");
                Object layerIdObj = layer.source().get("layer_id");
                int layerId = layerIdObj instanceof Number n ? n.intValue()
                        : Integer.parseInt(layerIdObj.toString());
                yield arcGisProxyService.fetchLayer(serviceUrl, layerId, limit);
            }
            case "static" -> fetchStatic(id, limit);
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de fuente no soportado: " + layer.sourceType());
        };
    }

    private List<Map<String, Object>> fetchStatic(String layerId, int limit) {
        // Dispatch por id conocido — agregar más repositorios aquí cuando se agreguen capas estáticas
        if (layerId.startsWith("division_politica_administrativa")) {
            var entities = limit > 0
                    ? dpaRepository.findAll().stream().limit(limit).toList()
                    : dpaRepository.findAll();
            return entities.stream()
                    .map(e -> {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("CUT_COM", e.getCutCom());
                        row.put("CUT_REG", e.getCutReg());
                        row.put("CUT_PROV", e.getCutProv());
                        row.put("REGION", e.getRegion());
                        row.put("PROVINCIA", e.getProvincia());
                        row.put("COMUNA", e.getComuna());
                        row.put("SUPERFICIE", e.getSuperficie());
                        return row;
                    })
                    .collect(Collectors.toList());
        }
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Capa estática sin repositorio configurado: " + layerId);
    }

    private List<Map<String, Object>> filterColumns(List<Map<String, Object>> rows,
                                                    List<String> columns) {
        Set<String> keep = new HashSet<>(columns);
        return rows.stream()
                .map(row -> row.entrySet().stream()
                        .filter(e -> keep.contains(e.getKey()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (a, b) -> a,
                                LinkedHashMap::new)))
                .collect(Collectors.toList());
    }
}
