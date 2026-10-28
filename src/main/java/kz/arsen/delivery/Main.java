package kz.arsen.delivery;

import java.util.*;
import kz.arsen.delivery.model.*;
import kz.arsen.delivery.service.*;
import kz.arsen.delivery.strategy.*;

public final class Main {
    private Main() {}
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        var fastest = new FastestRouteStrategy();
        var cheapest = new CheapestRouteStrategy();
        var eco = new EcoFriendlyRouteStrategy();
        var balanced = new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2);
        List<RouteStrategy> strategies = List.of(fastest, cheapest, eco, balanced, new DistanceFirstStrategy());
        List<Route> routes = DemoData.routes();
        var request = request(DeliveryPriority.STANDARD, 6000, 60, false);
        System.out.println("SMART DELIVERY ENGINE | 8 candidate routes");
        System.out.println("\nCOMPARISON | budget 6000 KZT | deadline 60 min");
        new StrategyComparison(strategies).compare(routes, request).forEach(d -> print(d, request));
        System.out.println("\nRUNTIME SWITCHING | one RoutePlanner, three switches");
        var planner = new RoutePlanner(fastest);
        print(planner.plan(routes, request), request);
        for (RouteStrategy strategy : List.of(cheapest, eco, balanced)) {
            planner.setStrategy(strategy);
            print(planner.plan(routes, request), request);
        }
        var selector = new StrategySelector(Map.of(DeliveryPriority.URGENT, fastest,
                DeliveryPriority.ECONOMY, cheapest, DeliveryPriority.ECO, eco,
                DeliveryPriority.STANDARD, balanced));
        System.out.println("\nAUTOMATIC SELECTION | different requests");
        for (DeliveryRequest current : List.of(request(DeliveryPriority.URGENT, 10000, 25, false),
                request(DeliveryPriority.ECONOMY, 2500, 60, false),
                request(DeliveryPriority.STANDARD, 4000, 60, true))) {
            planner.setStrategy(selector.select(current));
            System.out.println("Request: " + current);
            print(planner.plan(routes, current), current);
        }
        System.out.println("\nEXPECTED FAILURES");
        demonstrateFailure("Empty routes", () -> fastest.selectRoute(List.of(), request));
        demonstrateFailure("No feasible route", () -> fastest.selectRoute(routes, request(DeliveryPriority.STANDARD, 0, 0, false)));
        demonstrateFailure("Invalid weights", () -> new BalancedRouteStrategy(1, 1, 1, 1));
    }
    private static DeliveryRequest request(DeliveryPriority priority, double budget, int deadline, boolean eco) {
        return new DeliveryRequest(priority, budget, deadline, eco, 10, Set.of());
    }
    private static void print(RouteDecision decision, DeliveryRequest request) {
        Route route = decision.route();
        System.out.printf("%s -> Route %s | time %d min | cost %.2f KZT | distance %.1f km | CO2 %.2f kg | score %.4f%n",
                decision.strategyName(), route.id(), route.effectiveMinutes(), DeliveryPricing.totalCost(route, request),
                route.distanceKm(), route.carbonEmission(), decision.score());
        System.out.println("  Reason: " + decision.reason());
        decision.candidateScores().entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.printf("  Feasible %s: score %.4f%n", e.getKey(), e.getValue()));
        decision.rejections().entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.println("  Rejected " + e.getKey() + ": " + String.join(", ", e.getValue())));
    }
    private static void demonstrateFailure(String label, Runnable action) {
        try { action.run(); }
        catch (NoSuitableRouteException | IllegalArgumentException exception) {
            System.out.println(label + ": " + exception.getMessage());
        }
    }
}
