package cl.geoportal.api.service;

import cl.geoportal.api.dto.JoinResultDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class JoinService {

    private final LayerService layerService;
    private final CatalogService catalogService;

    public JoinService(LayerService layerService, CatalogService catalogService) {
        this.layerService = layerService;
        this.catalogService = catalogService;
    }

    // TODO: port de la lógica do_join() de FastAPI:
    //   - buscar relación en el catálogo (directa o inversa)
    //   - fetch src + tgt via LayerService
    //   - aplicar transforms (zfill:N)
    //   - ejecutar left/inner join en memoria
    //   - retornar estadísticas
    public List<Map<String, Object>> listJoins() {
        return List.of();
    }

    public JoinResultDto executeJoin(String srcId, String tgtId, int limit, String joinMode) {
        return new JoinResultDto(List.of(), 0, 0, 0, 0);
    }
}
