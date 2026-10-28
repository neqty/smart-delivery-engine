package kz.arsen.delivery.strategy;

import java.util.List;
import java.util.function.ToDoubleFunction;
import kz.arsen.delivery.model.*;
import kz.arsen.delivery.service.DeliveryPricing;

public final class BalancedRouteStrategy extends ScoredRouteStrategy {
    private final double timeWeight, costWeight, distanceWeight, emissionWeight;
    public BalancedRouteStrategy(double time, double cost, double distance, double emission) {
        Validation.nonNegative(time, "Time weight");
        Validation.nonNegative(cost, "Cost weight");
        Validation.nonNegative(distance, "Distance weight");
        Validation.nonNegative(emission, "Emission weight");
        if (Math.abs(time + cost + distance + emission - 1.0) > 1e-9)
            throw new IllegalArgumentException("Balanced weights must sum to 1.0");
        timeWeight = time; costWeight = cost; distanceWeight = distance; emissionWeight = emission;
    }
    public String getStrategyName() { return "Balanced"; }
    // Normalize only feasible routes. Constant columns contribute zero, avoiding division by zero.
    private double normalized(Route route, List<Route> candidates, ToDoubleFunction<Route> metric) {
        double min = candidates.stream().mapToDouble(metric).min().orElseThrow();
        double max = candidates.stream().mapToDouble(metric).max().orElseThrow();
        return max == min ? 0 : (metric.applyAsDouble(route) - min) / (max - min);
    }
    protected double score(Route route, DeliveryRequest request, List<Route> candidates) {
        return timeWeight * normalized(route, candidates, Route::effectiveMinutes)
                + costWeight * normalized(route, candidates, r -> DeliveryPricing.totalCost(r, request))
                + distanceWeight * normalized(route, candidates, Route::distanceKm)
                + emissionWeight * normalized(route, candidates, Route::carbonEmission);
    }
    protected String explain(Route route, DeliveryRequest request, List<Route> candidates) {
        return "Minimum weighted score: time %.4f * %.2f + cost %.4f * %.2f + distance %.4f * %.2f + emissions %.4f * %.2f = %.4f".formatted(
                normalized(route, candidates, Route::effectiveMinutes), timeWeight,
                normalized(route, candidates, r -> DeliveryPricing.totalCost(r, request)), costWeight,
                normalized(route, candidates, Route::distanceKm), distanceWeight,
                normalized(route, candidates, Route::carbonEmission), emissionWeight, score(route, request, candidates));
    }
}
