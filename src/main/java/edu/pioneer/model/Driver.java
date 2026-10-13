package edu.pioneer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A driver and the deliveries assigned during the application run. */
public final class Driver {
    // IDs label drivers across the application run, including later scheduling calls.
    private static int nextId = 1;
    // updates the id post assingment
    private final int id = nextId++;
    
    // aggregation of deliveries
    private final List<Delivery> deliveries = new ArrayList<>();

    // tuple of the deliveries and their times
    private final List<Delivery> scheduleView = Collections.unmodifiableList(deliveries);

    // current delivery's finish time
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
    

    /* 
     * if the start time of the new deliver of this driver is less than the 
     * finishTime of the driver's current delivery, then the delivery cannot be added.
     * otherwise, the driver can take the delivery if the start time is 
     * greater or equal to the current finish time.
     */
    public void assignDelivery(Delivery delivery) {
        if (delivery.getStartTime() < finishTime) {
            throw new IllegalArgumentException("Delivery overlaps the driver's schedule");
        }
        deliveries.add(delivery);
        finishTime = delivery.getFinishTime();
    }
}
