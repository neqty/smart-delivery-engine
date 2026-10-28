package kz.arsen.delivery.strategy;

import java.util.List;
import kz.arsen.delivery.model.*;

public interface RouteStrategy {
    RouteDecision selectRoute(List<Route> routes, DeliveryRequest request);
    String getStrategyName();
}
