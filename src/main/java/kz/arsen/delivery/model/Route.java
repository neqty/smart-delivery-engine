package kz.arsen.delivery.model;

import java.util.Objects;

/** Cost is the base fee; extra fees and per-km charges are calculated separately. */
public record Route(String id, Location origin, Location destination, double distanceKm,
                    int estimatedMinutes, double cost, double carbonEmission,
                    int trafficPenaltyMinutes, int weatherPenaltyMinutes,
                    double additionalFees, boolean available) {
    public Route {
        Validation.text(id, "Route id");
        Objects.requireNonNull(origin, "Origin");
        Objects.requireNonNull(destination, "Destination");
        Validation.nonNegative(distanceKm, "Distance");
        Validation.nonNegative(cost, "Base cost");
        Validation.nonNegative(carbonEmission, "Emissions");
        Validation.nonNegative(additionalFees, "Additional fees");
        if (estimatedMinutes < 0 || trafficPenaltyMinutes < 0 || weatherPenaltyMinutes < 0)
            throw new IllegalArgumentException("Time values cannot be negative");
        if ((long) estimatedMinutes + trafficPenaltyMinutes + weatherPenaltyMinutes > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Total time is too large");
    }
    public int effectiveMinutes() {
        return estimatedMinutes + trafficPenaltyMinutes + weatherPenaltyMinutes;
    }
}
