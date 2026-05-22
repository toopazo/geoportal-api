package cl.geoportal.api.dto;

import java.util.List;
import java.util.Map;

public record JoinResultDto(
        List<Map<String, Object>> rows,
        int totalSrc,
        int totalTgt,
        int matched,
        int unmatched
) {}
