package kz.arsen.delivery;

import org.junit.jupiter.api.Test;
import java.util.*;
import kz.arsen.delivery.model.*;
import kz.arsen.delivery.service.*;
import kz.arsen.delivery.strategy.*;
import static org.junit.jupiter.api.Assertions.*;

class RoutingTest {
    private final List<Route> routes = DemoData.routes();
    private final FastestRouteStrategy fastest = new FastestRouteStrategy();
    private final CheapestRouteStrategy cheapest = new CheapestRouteStrategy();
    private final EcoFriendlyRouteStrategy eco = new EcoFriendlyRouteStrategy();
    private final BalancedRouteStrategy balanced = new BalancedRouteStrategy(.4, .3, .1, .2);
    private DeliveryRequest request(DeliveryPriority priority, double budget, int deadline, boolean preferred,
                                    double emissions, Set<String> prohibited) {
        return new DeliveryRequest(priority, budget, deadline, preferred, emissions, prohibited);
    }
    private DeliveryRequest standard() { return request(DeliveryPriority.STANDARD, 6000, 60, false, 10, Set.of()); }
    private StrategySelector selector() {
        return new StrategySelector(Map.of(DeliveryPriority.URGENT, fastest, DeliveryPriority.ECONOMY, cheapest,
                DeliveryPriority.ECO, eco, DeliveryPriority.STANDARD, balanced));
    }
    private Route route(String id, double distance, int minutes, double baseCost, double emission, int traffic, boolean available) {
        var location = new Location("Test", 0, 0);
        return new Route(id, location, location, distance, minutes, baseCost, emission, traffic, 0, 0, available);
    }
    @Test void fastestIncludesTrafficInsteadOfOnlyBaseTime() {
        var a = route("A", 1, 10, 100, 1, 30, true);
        var b = route("B", 1, 20, 100, 1, 0, true);
        assertEquals("B", fastest.selectRoute(List.of(a, b), standard()).route().id());
    }
    @Test void weatherAlsoContributesToEffectiveTime() {
        var l = new Location("Test", 0, 0);
        var a = new Route("A", l, l, 1, 10, 100, 1, 0, 20, 0, true);
        var b = route("B", 1, 20, 100, 1, 0, true);
        assertEquals("B", fastest.selectRoute(List.of(a, b), standard()).route().id());
    }
    @Test void cheapestUsesTotalCostInsteadOfBaseFee() {
        var a = route("A", 40, 10, 1, 1, 0, true);
        var b = route("B", 1, 10, 500, 1, 0, true);
        assertEquals("B", cheapest.selectRoute(List.of(a, b), standard()).route().id());
    }
    @Test void urgentPricingIncludesSurchargeAndAdditionalFees() {
        var urgent = request(DeliveryPriority.URGENT, 10000, 60, false, 10, Set.of());
        assertEquals(5000, DeliveryPricing.totalCost(routes.get(1), urgent), 1e-9);
    }
    @Test void ecoSelectsLowestEnvironmentalScore() {
        RouteDecision result = eco.selectRoute(routes, standard());
        assertEquals("C", result.route().id());
        assertEquals(1.45, result.score(), 1e-9);
    }
    @Test void ecoWeightsCanChangeTheSelectedRoute() {
        var candidates = List.of(route("A", 10, 10, 100, .1, 0, true), route("B", 1, 10, 100, 2, 0, true));
        assertEquals("A", new EcoFriendlyRouteStrategy(1, 0).selectRoute(candidates, standard()).route().id());
        assertEquals("B", new EcoFriendlyRouteStrategy(0, 1).selectRoute(candidates, standard()).route().id());
    }
    @Test void balancedHasHandCalculatedWinnerAndScore() {
        var a = route("A", 10, 10, 100, 4, 0, true); // quote 900
        var b = route("B", 20, 20, 100, 1, 0, true); // quote 1700
        var result = balanced.selectRoute(List.of(a, b), standard());
        assertEquals("A", result.route().id());
        assertEquals(.2, result.score(), 1e-9);
        assertEquals(.8, result.candidateScores().get("B"), 1e-9);
    }
    @Test void changingBalancedWeightsChangesWinner() {
        var candidates = List.of(route("A", 1, 10, 1000, 1, 0, true), route("B", 1, 30, 100, 1, 0, true));
        assertEquals("A", new BalancedRouteStrategy(1, 0, 0, 0).selectRoute(candidates, standard()).route().id());
        assertEquals("B", new BalancedRouteStrategy(0, 1, 0, 0).selectRoute(candidates, standard()).route().id());
    }
    @Test void balancedNormalizesOnlyFeasibleRoutes() {
        var good = List.of(route("A", 10, 10, 100, 4, 0, true), route("B", 20, 20, 100, 1, 0, true));
        var extended = new ArrayList<>(good);
        extended.add(route("X", 100000, 10000, 1, 9999, 0, false));
        assertEquals(balanced.selectRoute(good, standard()).candidateScores(), balanced.selectRoute(extended, standard()).candidateScores());
    }
    @Test void onePlannerSupportsThreeRuntimeSwitches() {
        var planner = new RoutePlanner(fastest);
        assertEquals("B", planner.plan(routes, standard()).route().id());
        planner.setStrategy(cheapest);
        assertEquals("D", planner.plan(routes, standard()).route().id());
        planner.setStrategy(eco);
        assertEquals("C", planner.plan(routes, standard()).route().id());
        planner.setStrategy(balanced);
        assertEquals("F", planner.plan(routes, standard()).route().id());
    }
    @Test void selectorMapsAllPriorities() {
        assertSame(fastest, selector().select(request(DeliveryPriority.URGENT, 6000, 60, false, 10, Set.of())));
        assertSame(cheapest, selector().select(request(DeliveryPriority.ECONOMY, 6000, 60, false, 10, Set.of())));
        assertSame(eco, selector().select(request(DeliveryPriority.ECO, 6000, 60, false, 10, Set.of())));
        assertSame(balanced, selector().select(standard()));
    }
    @Test void selectorRespectsEcoPreferenceButUrgencyWins() {
        assertSame(eco, selector().select(request(DeliveryPriority.STANDARD, 6000, 60, true, 10, Set.of())));
        assertSame(fastest, selector().select(request(DeliveryPriority.URGENT, 6000, 60, true, 10, Set.of())));
    }
    @Test void unavailableRouteNeverWinsForAnyStrategy() {
        for (RouteStrategy strategy : List.of(fastest, cheapest, eco, balanced, new DistanceFirstStrategy())) {
            var result = strategy.selectRoute(routes, standard());
            assertNotEquals("G", result.route().id());
            assertEquals(List.of("Route unavailable"), result.rejections().get("G"));
        }
    }
    @Test void lowerBudgetChangesFastestRoute() {
        assertEquals("B", fastest.selectRoute(routes, standard()).route().id());
        assertEquals("F", fastest.selectRoute(routes, request(DeliveryPriority.STANDARD, 3000, 60, false, 10, Set.of())).route().id());
    }
    @Test void deadlineIncludesTrafficAndWeather() {
        var result = cheapest.selectRoute(routes, request(DeliveryPriority.STANDARD, 6000, 25, false, 10, Set.of()));
        assertEquals("B", result.route().id());
        assertTrue(result.rejections().get("A").contains("Delivery deadline exceeded"));
    }
    @Test void emissionLimitIsAHardConstraint() {
        var result = fastest.selectRoute(routes, request(DeliveryPriority.STANDARD, 6000, 60, false, 1, Set.of()));
        assertEquals("C", result.route().id());
        assertTrue(result.rejections().get("B").contains("Emission limit exceeded"));
    }
    @Test void prohibitedRouteCannotWin() {
        var result = fastest.selectRoute(routes, request(DeliveryPriority.STANDARD, 6000, 60, false, 10, Set.of("B")));
        assertEquals("A", result.route().id());
        assertTrue(result.rejections().get("B").contains("Route prohibited for this delivery"));
    }
    @Test void noValidRouteHasExplicitExceptionWithReasons() {
        var exception = assertThrows(NoSuitableRouteException.class, () -> fastest.selectRoute(routes,
                request(DeliveryPriority.STANDARD, 0, 0, false, 0, Set.of())));
        assertEquals(8, exception.getRejections().size());
        assertTrue(exception.getRejections().get("B").contains("Budget exceeded"));
    }
    @Test void emptyListFailsExplicitly() {
        assertThrows(NoSuitableRouteException.class, () -> fastest.selectRoute(List.of(), standard()));
    }
    @Test void invalidBalancedWeightsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(.4, .4, .4, .4));
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(-1, 1, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(Double.NaN, 0, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(Double.POSITIVE_INFINITY, 0, 0, 0));
    }
    @Test void fifthStrategySelectsShortestFeasibleRoute() {
        assertEquals("E", new DistanceFirstStrategy().selectRoute(routes, standard()).route().id());
    }
    @Test void comparisonShowsInterchangeableAlgorithmsOnSameRequest() {
        var results = new StrategyComparison(List.of(fastest, cheapest)).compare(routes, standard());
        assertEquals(List.of("B", "D"), results.stream().map(d -> d.route().id()).toList());
        assertEquals(List.of("Fastest", "Cheapest"), results.stream().map(RouteDecision::strategyName).toList());
    }
    @Test void exactBudgetDeadlineAndEmissionBoundaryIsAllowed() {
        var route = route("A", 1, 10, 100, 1, 0, true);
        var exact = request(DeliveryPriority.STANDARD, 180, 10, false, 1, Set.of());
        assertEquals(route, fastest.selectRoute(List.of(route), exact).route());
    }
    @Test void equalMetricsAvoidDivisionByZeroAndUseIdTieBreak() {
        var a = route("A", 1, 10, 100, 1, 0, true);
        var b = route("B", 1, 10, 100, 1, 0, true);
        var result = balanced.selectRoute(List.of(b, a), standard());
        assertEquals("A", result.route().id());
        assertEquals(0, result.score(), 1e-9);
    }
    @Test void negativeDistanceCostAndMissingRequestAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> route("A", -1, 10, 100, 1, 0, true));
        assertThrows(IllegalArgumentException.class, () -> route("A", 1, 10, -100, 1, 0, true));
        assertThrows(NullPointerException.class, () -> fastest.selectRoute(routes, null));
    }
    @Test void duplicateIdsAreRejectedBeforeScoring() {
        assertThrows(IllegalArgumentException.class, () -> fastest.selectRoute(List.of(routes.get(0), routes.get(0)), standard()));
    }
    @Test void requestCopiesItsProhibitedRouteSet() {
        Set<String> prohibited = new HashSet<>();
        var request = request(DeliveryPriority.STANDARD, 6000, 60, false, 10, prohibited);
        prohibited.add("B");
        assertTrue(request.prohibitedRouteIds().isEmpty());
    }
    @Test void decisionExplainsWinnerAndIsImmutable() {
        var result = fastest.selectRoute(routes, standard());
        assertTrue(result.reason().contains("traffic 2"));
        assertEquals(22, result.score(), 1e-9);
        assertThrows(UnsupportedOperationException.class, () -> result.rejections().get("G").add("changed"));
        assertThrows(UnsupportedOperationException.class, () -> result.candidateScores().put("X", 0.0));
    }
    @Test void incompleteSelectorAndNullStrategyAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new StrategySelector(Map.of(DeliveryPriority.URGENT, fastest)));
        var planner = new RoutePlanner(fastest);
        assertThrows(NullPointerException.class, () -> planner.setStrategy(null));
        assertEquals("B", planner.plan(routes, standard()).route().id());
    }
    @Test void invalidCoordinatesAndTimeOverflowAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Location("X", 91, 0));
        assertThrows(IllegalArgumentException.class, () -> route("X", 1, Integer.MAX_VALUE, 1, 1, 1, true));
    }
}
