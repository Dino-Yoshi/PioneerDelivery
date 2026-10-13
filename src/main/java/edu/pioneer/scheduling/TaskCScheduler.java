package edu.pioneer.scheduling;

import edu.pioneer.model.Delivery;
import java.util.List;

/**
 * Counts the complement of Task A's maximum one-driver schedule.
 * If Task A keeps k of n accepted deliveries, the minimum rejected is n - k.
 */
public final class TaskCScheduler {
    private final TaskAScheduler taskAScheduler = new TaskAScheduler();

    public int countRejectedDeliveries(List<Delivery> deliveries) {
        List<Delivery> selected = taskAScheduler.selectDeliveries(deliveries);
        return deliveries.size() - selected.size();
    }
}
