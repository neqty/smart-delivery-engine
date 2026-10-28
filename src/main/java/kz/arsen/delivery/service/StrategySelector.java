package kz.arsen.delivery.service;

import java.util.*;
import kz.arsen.delivery.model.*;
import kz.arsen.delivery.strategy.RouteStrategy;

/** Business policy chooses an algorithm; it does not score routes. */
public final class StrategySelector {
    private final Map<DeliveryPriority, RouteStrategy> strategies;
    public StrategySelector(Map<DeliveryPriority, RouteStrategy> strategies) {
        this.strategies = Map.copyOf(strategies);
        if (!this.strategies.keySet().containsAll(EnumSet.allOf(DeliveryPriority.class)))
            throw new IllegalArgumentException("Each priority needs a strategy");
    }
    public RouteStrategy select(DeliveryRequest request) {
        Objects.requireNonNull(request);
        // Urgency overrides a soft eco preference; all hard constraints still apply.
        DeliveryPriority key = request.ecoPreferred() && request.priority() != DeliveryPriority.URGENT
                ? DeliveryPriority.ECO : request.priority();
        return strategies.get(key);
    }
}
