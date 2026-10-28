package kz.arsen.delivery.strategy;

import java.util.List;
import kz.arsen.delivery.model.*;

public final class FastestRouteStrategy extends ScoredRouteStrategy {
    public String getStrategyName() { return "Fastest"; }
    protected double score(Route route, DeliveryRequest request, List<Route> candidates) {
        return route.effectiveMinutes();
    }
    protected String explain(Route route, DeliveryRequest request, List<Route> candidates) {
        return "Minimum effective time: %d + traffic %d + weather %d = %d min".formatted(
                route.estimatedMinutes(), route.trafficPenaltyMinutes(), route.weatherPenaltyMinutes(), route.effectiveMinutes());
    }
}
