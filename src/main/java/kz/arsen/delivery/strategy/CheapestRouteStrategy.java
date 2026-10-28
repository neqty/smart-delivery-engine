package kz.arsen.delivery.strategy;

import java.util.List;
import kz.arsen.delivery.model.*;
import kz.arsen.delivery.service.DeliveryPricing;

public final class CheapestRouteStrategy extends ScoredRouteStrategy {
    public String getStrategyName() { return "Cheapest"; }
    protected double score(Route route, DeliveryRequest request, List<Route> candidates) {
        return DeliveryPricing.totalCost(route, request);
    }
    protected String explain(Route route, DeliveryRequest request, List<Route> candidates) {
        return "Minimum total cost: base %.2f + %.2f km * %.2f + fees %.2f + urgent surcharge %.2f = %.2f KZT".formatted(
                route.cost(), route.distanceKm(), DeliveryPricing.PER_KM, route.additionalFees(),
                request.priority() == DeliveryPriority.URGENT ? DeliveryPricing.URGENT_SURCHARGE : 0,
                DeliveryPricing.totalCost(route, request));
    }
}
