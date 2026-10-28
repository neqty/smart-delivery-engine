package kz.arsen.delivery.service;

import java.util.*;
import kz.arsen.delivery.model.*;
import kz.arsen.delivery.strategy.RouteStrategy;

/** Strategy Context: holds the interchangeable algorithm and delegates planning. */
public final class RoutePlanner {
    private RouteStrategy strategy;
    public RoutePlanner(RouteStrategy strategy) { setStrategy(strategy); }
    public void setStrategy(RouteStrategy strategy) { this.strategy = Objects.requireNonNull(strategy); }
    public RouteDecision plan(List<Route> routes, DeliveryRequest request) {
        return strategy.selectRoute(routes, request);
    }
}
