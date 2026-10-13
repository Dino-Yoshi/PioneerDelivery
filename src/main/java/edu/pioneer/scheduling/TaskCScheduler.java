package edu.pioneer.scheduling;

import edu.pioneer.model.Delivery;
import java.util.List;

/** Counts the minimum deliveries to reject when one driver is available. */
public final class TaskCScheduler {
    private final TaskAScheduler taskAScheduler = new TaskAScheduler();

    public int countRejectedDeliveries(List<Delivery> deliveries) {
        return deliveries.size() - taskAScheduler.selectDeliveries(deliveries).size();
    }
}
