package kz.arsen.delivery.service;

import kz.arsen.delivery.model.*;

/** Shared quote calculation keeps budget checks and cost ranking consistent (KZT). */
public final class DeliveryPricing {
    public static final double PER_KM = 80;
    public static final double URGENT_SURCHARGE = 500;
    private DeliveryPricing() {}
    public static double totalCost(Route route, DeliveryRequest request) {
        double total = route.cost() + route.distanceKm() * PER_KM + route.additionalFees()
                + (request.priority() == DeliveryPriority.URGENT ? URGENT_SURCHARGE : 0);
        if (!Double.isFinite(total)) throw new IllegalArgumentException("Total cost overflow");
        return total;
    }
}
