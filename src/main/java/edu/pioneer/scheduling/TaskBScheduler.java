package edu.pioneer.scheduling;

import edu.pioneer.model.Delivery;
import edu.pioneer.model.Driver;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Assigns all deliveries with the SDS start-time scan and finish-time min-heap.
 * If the earliest-finishing driver is still busy, every existing driver is busy.
 */
public final class TaskBScheduler {
    public List<Driver> assignDrivers(List<Delivery> deliveries) {
        // Process start times without changing the accepted dataset's order.
        List<Delivery> ordered = new ArrayList<>(deliveries);
        ordered.sort(Comparator.comparingInt(Delivery::getStartTime));
        List<Driver> drivers = new ArrayList<>();
        PriorityQueue<Driver> availableByFinish = new PriorityQueue<>(Comparator.comparingInt(Driver::getFinishTime));

        /*
         * The heap contains every created driver at its current finish time.
         * Poll a reused driver before updating it, then reinsert it afterward.
         */
        for (Delivery delivery : ordered) {
            Driver driver;
            if (!availableByFinish.isEmpty() && availableByFinish.peek().getFinishTime() <= delivery.getStartTime()) {
                driver = availableByFinish.poll();
            } else {
                driver = new Driver();
                drivers.add(driver);
            }
            driver.assignDelivery(delivery);
            availableByFinish.add(driver);
        }
        return drivers;
    }
}
