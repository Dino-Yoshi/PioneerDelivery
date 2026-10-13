package edu.pioneer.scheduling;

import edu.pioneer.model.Delivery;
import edu.pioneer.model.Driver;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/** Assigns fixed-interval deliveries to the minimum number of drivers. */
public final class TaskBScheduler {
    public List<Driver> assignDrivers(List<Delivery> deliveries) {
        List<Delivery> ordered = new ArrayList<>(deliveries);
        ordered.sort(Comparator.comparingInt(Delivery::getStartTime));
        List<Driver> drivers = new ArrayList<>();
        PriorityQueue<Driver> availableByFinish = new PriorityQueue<>(
                Comparator.comparingInt(Driver::getFinishTime));

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
