package edu.pioneer.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class DriverTest {
    @Test
    void newDriversHaveDistinctSequentialRunIdsAndEmptySchedules() {
        Driver first = new Driver();
        Driver second = new Driver();

        assertAll(
                () -> assertTrue(first.getId() >= 1),
                () -> assertEquals(first.getId() + 1, second.getId()),
                () -> assertTrue(first.getDeliveries().isEmpty()),
                () -> assertEquals(0, first.getFinishTime()));
    }

    @Test
    void assignsDeliveriesThatShareAnEndpointAndUpdatesFinishTime() {
        Driver driver = new Driver();
        Delivery first = new Delivery("first", 1, 3);
        Delivery second = new Delivery("second", 3, 5);

        driver.assignDelivery(first);
        driver.assignDelivery(second);

        assertAll(
                () -> assertEquals(List.of(first, second), driver.getDeliveries()),
                () -> assertEquals(5, driver.getFinishTime()));
    }

    @Test
    void rejectsOverlappingDeliveryWithoutChangingSchedule() {
        Driver driver = new Driver();
        Delivery first = new Delivery("first", 1, 4);
        driver.assignDelivery(first);

        assertThrows(IllegalArgumentException.class,
                () -> driver.assignDelivery(new Delivery("overlap", 3, 6)));

        assertAll(
                () -> assertEquals(List.of(first), driver.getDeliveries()),
                () -> assertEquals(4, driver.getFinishTime()));
    }

    @Test
    void exposesAnUnmodifiableLiveScheduleView() {
        Driver driver = new Driver();
        List<Delivery> schedule = driver.getDeliveries();
        Delivery delivery = new Delivery("first", 1, 4);

        assertThrows(UnsupportedOperationException.class, () -> schedule.add(delivery));
        driver.assignDelivery(delivery);

        assertEquals(List.of(delivery), schedule);
    }
}
