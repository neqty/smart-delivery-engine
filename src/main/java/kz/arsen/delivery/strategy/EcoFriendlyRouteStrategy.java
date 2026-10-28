package kz.arsen.delivery.strategy;

import java.util.List;
import kz.arsen.delivery.model.*;

public final class EcoFriendlyRouteStrategy extends ScoredRouteStrategy {
    private final double emissionWeight;
    private final double distanceWeight;
    public EcoFriendlyRouteStrategy() { this(0.9, 0.1); }
    public EcoFriendlyRouteStrategy(double emissionWeight, double distanceWeight) {
        Validation.nonNegative(emissionWeight, "Emission weight");
        Validation.nonNegative(distanceWeight, "Distance weight");
        if (!Double.isFinite(emissionWeight + distanceWeight) || emissionWeight + distanceWeight == 0)
            throw new IllegalArgumentException("At least one eco weight must be positive");
        this.emissionWeight = emissionWeight;
        this.distanceWeight = distanceWeight;
    }
    public String getStrategyName() { return "Eco-friendly"; }
    protected double score(Route route, DeliveryRequest request, List<Route> candidates) {
        return route.carbonEmission() * emissionWeight + route.distanceKm() * distanceWeight;
    }
    protected String explain(Route route, DeliveryRequest request, List<Route> candidates) {
        return "Minimum eco score: %.2f kg * %.2f + %.2f km * %.2f = %.4f".formatted(
                route.carbonEmission(), emissionWeight, route.distanceKm(), distanceWeight, score(route, request, candidates));
    }
}
