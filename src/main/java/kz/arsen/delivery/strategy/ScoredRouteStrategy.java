package kz.arsen.delivery.strategy;

import java.util.*;
import kz.arsen.delivery.model.*;
import kz.arsen.delivery.service.*;

/** Common safety pipeline. Concrete strategies own only scoring and its explanation. */
public abstract class ScoredRouteStrategy implements RouteStrategy {
    @Override
    public final RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        Objects.requireNonNull(request, "Delivery request is required");
        List<Route> snapshot = List.copyOf(Objects.requireNonNull(routes, "Routes are required"));
        Set<String> ids = new HashSet<>();
        List<Route> feasible = new ArrayList<>();
        Map<String, List<String>> rejected = new LinkedHashMap<>();
        for (Route route : snapshot) {
            if (!ids.add(route.id())) throw new IllegalArgumentException("Duplicate route id: " + route.id());
            List<String> reasons = RouteConstraints.violations(route, request);
            if (reasons.isEmpty()) feasible.add(route); else rejected.put(route.id(), reasons);
        }
        if (feasible.isEmpty()) throw new NoSuitableRouteException("No suitable route for " + getStrategyName(), rejected);
        List<Route> candidates = List.copyOf(feasible);
        Map<String, Double> scores = new LinkedHashMap<>();
        for (Route route : candidates) {
            double value = score(route, request, candidates);
            if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite strategy score");
            scores.put(route.id(), value);
        }
        Route winner = candidates.stream().min(Comparator
                .comparingDouble((Route route) -> scores.get(route.id())).thenComparing(Route::id)).orElseThrow();
        return new RouteDecision(winner, getStrategyName(), scores.get(winner.id()),
                explain(winner, request, candidates), scores, rejected);
    }
    protected abstract double score(Route route, DeliveryRequest request, List<Route> candidates);
    protected abstract String explain(Route route, DeliveryRequest request, List<Route> candidates);
}
