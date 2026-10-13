package edu.pioneer.model;

/** An accepted delivery request occupying a fixed time interval. */
public final class Delivery {
    private final String id;
    private final int startTime;
    private final int finishTime;

    public Delivery(String id, int startTime, int finishTime) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Delivery ID must not be blank");
        }
        if (startTime < 0 || finishTime <= startTime) {
            throw new IllegalArgumentException("Delivery interval must satisfy 0 <= startTime < finishTime");
        }
        this.id = id;
        this.startTime = startTime;
        this.finishTime = finishTime;
    }

    public String getId() {
        return id;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getFinishTime() {
        return finishTime;
    }
}
