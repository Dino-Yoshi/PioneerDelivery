package edu.pioneer.scheduling;

import edu.pioneer.model.Delivery;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TaskCSchedulerTest {
    private final TaskCScheduler scheduler = new TaskCScheduler();

    @Test
    void noDeliveriesRequireNoRejections() {
        assertEquals(0, scheduler.countRejectedDeliveries(List.of()));
    }

    @Test
    void threeMutuallyOverlappingDeliveriesRequireTwoRejections() {
        List<Delivery> deliveries = List.of(new Delivery("A", 1, 5), new Delivery("B", 2, 6), new Delivery("C", 3, 7));

        assertEquals(2, scheduler.countRejectedDeliveries(deliveries));
    }

    @Test
    void oneDeliveryRequiresNoRejections() {
        assertEquals(0, scheduler.countRejectedDeliveries(List.of(new Delivery("A", 1, 3))));
    }

    @Test
    void threeDeliveriesWithSharedEndpointsRequireNoRejections() {
        List<Delivery> deliveries = List.of(new Delivery("A", 1, 3), new Delivery("B", 3, 5), new Delivery("C", 5, 7));

        assertEquals(0, scheduler.countRejectedDeliveries(deliveries));
    }

    @Test
    void unorderedDeliveriesRequireOneRejection() {
        List<Delivery> deliveries = List.of(new Delivery("A", 5, 7), new Delivery("B", 1, 3), new Delivery("C", 3, 5), new Delivery("D", 2, 4));

        assertEquals(1, scheduler.countRejectedDeliveries(deliveries));
    }

    @Test
    void longerDeliveryLosesToThreeCompatibleShorterDeliveries() {
        List<Delivery> deliveries = List.of(new Delivery("A", 1, 2), new Delivery("B", 2, 3), new Delivery("C", 3, 4), new Delivery("D", 1, 3));

        assertEquals(1, scheduler.countRejectedDeliveries(deliveries));
    }

    @Test
    void rejectionCountEqualsAcceptedCountMinusTaskASelection() {
        List<Delivery> deliveries = List.of(new Delivery("A", 4, 6), new Delivery("B", 1, 4), new Delivery("C", 2, 4), new Delivery("D", 6, 8), new Delivery("E", 5, 7));

        int selectedCount = new TaskAScheduler().selectDeliveries(deliveries).size();

        assertEquals(deliveries.size() - selectedCount, scheduler.countRejectedDeliveries(deliveries));
    }

    @Test
    void countingDoesNotReorderOrReplaceAcceptedRecords() {
        Delivery late = new Delivery("late", 5, 7);
        Delivery early = new Delivery("early", 1, 3);
        Delivery overlapping = new Delivery("overlapping", 2, 4);
        List<Delivery> deliveries = new ArrayList<>(List.of(late, early, overlapping));

        scheduler.countRejectedDeliveries(deliveries);

        assertEquals(List.of(late, early, overlapping), deliveries);
    }
}
