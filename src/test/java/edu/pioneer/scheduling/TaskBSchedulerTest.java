package edu.pioneer.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.pioneer.model.Delivery;
import edu.pioneer.model.Driver;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TaskBSchedulerTest {
    private final TaskBScheduler scheduler = new TaskBScheduler();

    @Test
    void emptyInputNeedsNoDrivers() {
        assertEquals(0, scheduler.assignDrivers(List.of()).size());
    }

    @Test
    void oneDeliveryNeedsOneDriver() {
        assertEquals(1, scheduler.assignDrivers(List.of(new Delivery("only", 1, 3))).size());
    }

    @Test
    void sharedEndpointsUseOneDriver() {
        Delivery first = new Delivery("first", 1, 3);
        Delivery second = new Delivery("second", 3, 5);
        Delivery third = new Delivery("third", 5, 7);

        var drivers = scheduler.assignDrivers(List.of(first, second, third));

        assertEquals(1, drivers.size());
        assertEquals(List.of(first, second, third), drivers.getFirst().getDeliveries());
    }

    @Test
    void simultaneousOverlapsNeedThreeDrivers() {
        Delivery first = new Delivery("first", 1, 5);
        Delivery second = new Delivery("second", 2, 6);
        Delivery third = new Delivery("third", 3, 7);

        var drivers = scheduler.assignDrivers(List.of(first, second, third));

        assertEquals(3, drivers.size());
        assertEquals(3, drivers.stream().mapToInt(driver -> driver.getDeliveries().size()).sum());
    }

    @Test
    void unorderedDeliveriesAreScheduledInStartTimeOrder() {
        Delivery late = new Delivery("late", 5, 8);
        Delivery early = new Delivery("early", 1, 4);
        Delivery middle = new Delivery("middle", 2, 5);
        Delivery next = new Delivery("next", 4, 6);
        Delivery last = new Delivery("last", 6, 9);

        var drivers = scheduler.assignDrivers(List.of(late, early, middle, next, last));

        assertEquals(2, drivers.size());
        assertEquals(5, drivers.stream().mapToInt(driver -> driver.getDeliveries().size()).sum());
    }

    @Test
    void staggeredOverlapsNeedTwoDrivers() {
        List<Delivery> deliveries = List.of(new Delivery("first", 1, 4), new Delivery("second", 2, 5), new Delivery("third", 4, 7), new Delivery("fourth", 5, 8));

        assertEquals(2, scheduler.assignDrivers(deliveries).size());
    }

    @Test
    void everyDeliveryIsAssignedOnceAndEachDriverIsConflictFree() {
        List<Delivery> deliveries = List.of(new Delivery("late", 6, 9), new Delivery("first", 1, 4), new Delivery("middle", 2, 5), new Delivery("next", 4, 6));

        List<Driver> drivers = scheduler.assignDrivers(deliveries);
        List<Delivery> assigned = drivers.stream().flatMap(driver -> driver.getDeliveries().stream()).toList();

        assertEquals(deliveries.size(), assigned.size());
        assertEquals(Set.copyOf(deliveries), Set.copyOf(assigned));
        for (Driver driver : drivers) {
            List<Delivery> schedule = driver.getDeliveries();
            for (int index = 1; index < schedule.size(); index++) {
                assertTrue(schedule.get(index - 1).getFinishTime() <= schedule.get(index).getStartTime());
            }
        }
    }

    @Test
    void schedulingPreservesInputOrderAndDeliveryObjects() {
        Delivery later = new Delivery("later", 5, 8);
        Delivery earlier = new Delivery("earlier", 1, 4);
        List<Delivery> deliveries = new ArrayList<>(List.of(later, earlier));

        List<Driver> drivers = scheduler.assignDrivers(deliveries);

        assertEquals(List.of(later, earlier), deliveries);
        assertSame(later, drivers.getFirst().getDeliveries().getLast());
        assertSame(earlier, drivers.getFirst().getDeliveries().getFirst());
        assertEquals(5, later.getStartTime());
        assertEquals(8, later.getFinishTime());
    }
}
