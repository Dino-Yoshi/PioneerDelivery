package edu.pioneer.scheduling;

import edu.pioneer.model.Delivery;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Selects a maximum compatible delivery schedule for one driver. */
public final class TaskAScheduler {
    public List<Delivery> selectDeliveries(List<Delivery> deliveries) {
        List<Delivery> ordered = new ArrayList<>(deliveries);
        ordered.sort(Comparator.comparingInt(Delivery::getFinishTime));
        List<Delivery> selected = new ArrayList<>();
        int lastFinish = 0;
        for (Delivery delivery : ordered) {
            if (delivery.getStartTime() >= lastFinish) {
                selected.add(delivery);
                lastFinish = delivery.getFinishTime();
            }
        }
        return selected;
    }
}
