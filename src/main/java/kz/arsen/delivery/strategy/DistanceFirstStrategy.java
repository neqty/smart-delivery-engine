package kz.arsen.delivery.strategy;

import java.util.List;
import kz.arsen.delivery.model.*;

/** Fifth algorithm, added without changing the planner, selector, or safety pipeline. */
public final class DistanceFirstStrategy extends ScoredRouteStrategy {
    public String getStrategyName() { return "Distance-first"; }
    protected double score(Route route, DeliveryRequest request, List<Route> candidates) {
        return route.distanceKm();
    }
    protected String explain(Route route, DeliveryRequest request, List<Route> candidates) {
        return "Minimum feasible distance: %.2f km".formatted(route.distanceKm());
    }
}
