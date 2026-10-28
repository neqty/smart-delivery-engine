package kz.arsen.delivery.model;

import java.util.Objects;
import java.util.Set;

public record DeliveryRequest(DeliveryPriority priority, double maxBudget,
                              int maxDeliveryMinutes, boolean ecoPreferred,
                              double maxEmissionKg, Set<String> prohibitedRouteIds) {
    public DeliveryRequest {
        Objects.requireNonNull(priority, "Priority is required");
        Validation.nonNegative(maxBudget, "Budget");
        Validation.nonNegative(maxEmissionKg, "Emission limit");
        if (maxDeliveryMinutes < 0) throw new IllegalArgumentException("Deadline cannot be negative");
        prohibitedRouteIds = Set.copyOf(Objects.requireNonNull(prohibitedRouteIds));
    }
}
