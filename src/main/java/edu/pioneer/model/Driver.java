package edu.pioneer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** A driver and the deliveries assigned during the application run. */
public final class Driver {
    private static final AtomicInteger nextId = new AtomicInteger(1);

    private final int id = nextId.getAndIncrement();
    private final List<Delivery> deliveries = new ArrayList<>();
    private final List<Delivery> scheduleView = Collections.unmodifiableList(deliveries);
    private int finishTime;

    public int getId() {
        return id;
    }

    public List<Delivery> getDeliveries() {
        return scheduleView;
    }

    public int getFinishTime() {
        return finishTime;
    }

    public void assignDelivery(Delivery delivery) {
        if (delivery.getStartTime() < finishTime) {
            throw new IllegalArgumentException("Delivery overlaps the driver's schedule");
        }
        deliveries.add(delivery);
        finishTime = delivery.getFinishTime();
    }
}
