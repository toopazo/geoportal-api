package cl.geoportal.api.controller;

import cl.geoportal.api.dto.JoinResultDto;
import cl.geoportal.api.service.JoinService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class JoinController {

    private final JoinService joinService;

    public JoinController(JoinService joinService) {
        this.joinService = joinService;
    }

    @GetMapping("/joins")
    public List<Map<String, Object>> joins() {
        return joinService.listJoins();
    }

    @GetMapping("/joins/{srcId}/{tgtId}")
    public JoinResultDto executeJoin(
            @PathVariable String srcId,
            @PathVariable String tgtId,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "left") String joinMode) {
        return joinService.executeJoin(srcId, tgtId, limit, joinMode);
    }
}
