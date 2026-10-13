package edu.pioneer.scheduling;

import edu.pioneer.model.Delivery;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Selects a maximum compatible schedule using the SDS earliest-finish rule.
 * Choosing the first compatible finish leaves the most room for later deliveries.
 */
public final class TaskAScheduler {
    public List<Delivery> selectDeliveries(List<Delivery> deliveries) {
        // Sort a copy so all tasks can reuse the accepted dataset unchanged.
        List<Delivery> ordered = new ArrayList<>(deliveries);
        ordered.sort(Comparator.comparingInt(Delivery::getFinishTime));
        List<Delivery> selected = new ArrayList<>();
        int lastFinish = 0;
        for (Delivery delivery : ordered) {
            // Shared endpoints are compatible: a delivery may start at lastFinish.
            if (delivery.getStartTime() >= lastFinish) {
                selected.add(delivery);
                lastFinish = delivery.getFinishTime();
            }
        }
        return selected;
    }
}
