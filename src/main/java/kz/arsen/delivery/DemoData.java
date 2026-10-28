package kz.arsen.delivery;

import java.util.List;
import kz.arsen.delivery.model.*;

public final class DemoData {
    private DemoData() {}
    public static List<Route> routes() {
        Location warehouse = new Location("Astana warehouse", 51.1694, 71.4491);
        Location customer = new Location("Customer", 51.1282, 71.4304);
        return List.of(
                new Route("A", warehouse, customer, 12, 18, 3000, 4.0, 10, 2, 300, true),
                new Route("B", warehouse, customer, 15, 20, 3200, 3.5, 2, 0, 100, true),
                new Route("C", warehouse, customer, 10, 40, 1800, 0.5, 2, 0, 0, true),
                new Route("D", warehouse, customer, 18, 35, 800, 3.0, 5, 0, 0, true),
                new Route("E", warehouse, customer, 8, 28, 2000, 2.0, 5, 2, 100, true),
                new Route("F", warehouse, customer, 14, 27, 1800, 1.4, 2, 1, 0, true),
                new Route("G", warehouse, customer, 7, 10, 500, 0.2, 0, 0, 0, false),
                new Route("H", warehouse, customer, 20, 16, 6000, 5.0, 1, 0, 500, true));
    }
}
