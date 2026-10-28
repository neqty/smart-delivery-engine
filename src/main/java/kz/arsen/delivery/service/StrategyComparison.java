package kz.arsen.delivery.service;

import java.util.*;
import kz.arsen.delivery.model.*;
import kz.arsen.delivery.strategy.RouteStrategy;

public final class StrategyComparison {
    private final List<RouteStrategy> strategies;
    public StrategyComparison(List<RouteStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
        if (this.strategies.isEmpty()) throw new IllegalArgumentException("At least one strategy is required");
    }
    public List<RouteDecision> compare(List<Route> routes, DeliveryRequest request) {
        return strategies.stream().map(strategy -> strategy.selectRoute(routes, request)).toList();
    }
}
