package edu.pioneer.scheduling;

import edu.pioneer.model.Delivery;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TaskASchedulerTest {
    private final TaskAScheduler scheduler = new TaskAScheduler();

    @Test
    void noDeliveriesProducesAnEmptySchedule() {
        assertTrue(scheduler.selectDeliveries(List.of()).isEmpty());
    }

    @Test
    void oneDeliveryIsSelectedWithoutReplacingTheRecord() {
        Delivery delivery = new Delivery("A", 1, 3);

        assertSame(delivery, scheduler.selectDeliveries(List.of(delivery)).getFirst());
    }

    @Test
    void overlappingDeliveriesAllowOnlyOneSelection() {
        List<Delivery> deliveries = List.of(
                new Delivery("A", 1, 5),
                new Delivery("B", 2, 6),
                new Delivery("C", 3, 7));

        assertEquals(1, scheduler.selectDeliveries(deliveries).size());
    }

    @Test
    void unorderedInputStillProducesTheMaximumCompatibleSchedule() {
        List<Delivery> deliveries = List.of(
                new Delivery("A", 5, 7),
                new Delivery("B", 1, 3),
                new Delivery("C", 3, 5),
                new Delivery("D", 2, 4));

        List<Delivery> selected = scheduler.selectDeliveries(deliveries);

        assertEquals(3, selected.size());
        assertCompatible(selected);
    }

    @Test
    void deliveriesCanShareAnEndpoint() {
        List<Delivery> deliveries = List.of(
                new Delivery("A", 1, 3),
                new Delivery("B", 3, 5),
                new Delivery("C", 5, 7));

        List<Delivery> selected = scheduler.selectDeliveries(deliveries);

        assertEquals(3, selected.size());
        assertCompatible(selected);
    }

    @Test
    void equalFinishTimesProduceACompatibleMaximumSchedule() {
        List<Delivery> deliveries = List.of(
                new Delivery("A", 1, 4),
                new Delivery("B", 2, 4),
                new Delivery("C", 4, 6));

        List<Delivery> selected = scheduler.selectDeliveries(deliveries);

        assertEquals(2, selected.size());
        assertCompatible(selected);
    }

    @Test
    void repeatedRunsChooseTheSameScheduleWhenFinishTimesTie() {
        List<Delivery> deliveries = List.of(
                new Delivery("A", 1, 4),
                new Delivery("B", 2, 4),
                new Delivery("C", 4, 6));

        assertEquals(scheduler.selectDeliveries(deliveries), scheduler.selectDeliveries(deliveries));
    }

    @Test
    void selectingDoesNotReorderOrReplaceAcceptedRecords() {
        Delivery late = new Delivery("late", 5, 7);
        Delivery early = new Delivery("early", 1, 3);
        Delivery middle = new Delivery("middle", 3, 5);
        List<Delivery> deliveries = new ArrayList<>(List.of(late, early, middle));

        List<Delivery> selected = scheduler.selectDeliveries(deliveries);

        assertEquals(List.of(late, early, middle), deliveries);
        assertEquals(List.of(early, middle, late), selected);
        assertSame(early, selected.getFirst());
        assertEquals("early", selected.getFirst().getId());
        assertEquals(1, selected.getFirst().getStartTime());
        assertEquals(3, selected.getFirst().getFinishTime());
    }

    private static void assertCompatible(List<Delivery> selected) {
        for (int index = 1; index < selected.size(); index++) {
            assertTrue(selected.get(index).getStartTime() >= selected.get(index - 1).getFinishTime());
        }
    }
}
