package kz.arsen.delivery.service;

import java.util.*;
import kz.arsen.delivery.model.*;

public final class RouteConstraints {
    private RouteConstraints() {}
    public static List<String> violations(Route route, DeliveryRequest request) {
        List<String> reasons = new ArrayList<>();
        if (!route.available()) reasons.add("Route unavailable");
        if (DeliveryPricing.totalCost(route, request) > request.maxBudget()) reasons.add("Budget exceeded");
        if (route.effectiveMinutes() > request.maxDeliveryMinutes()) reasons.add("Delivery deadline exceeded");
        if (route.carbonEmission() > request.maxEmissionKg()) reasons.add("Emission limit exceeded");
        if (request.prohibitedRouteIds().contains(route.id())) reasons.add("Route prohibited for this delivery");
        return List.copyOf(reasons);
    }
}
