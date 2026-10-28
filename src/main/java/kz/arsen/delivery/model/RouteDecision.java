package kz.arsen.delivery.model;

import java.util.List;
import java.util.Map;

/** Immutable audit trail: score for every feasible route and every rejected constraint. */
public record RouteDecision(Route route, String strategyName, double score, String reason,
                            Map<String, Double> candidateScores,
                            Map<String, List<String>> rejections) {
    public RouteDecision {
        candidateScores = Map.copyOf(candidateScores);
        rejections = rejections.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                Map.Entry::getKey, e -> List.copyOf(e.getValue())));
    }
}
