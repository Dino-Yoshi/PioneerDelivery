package edu.pioneer.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class DeliveryTest {
    @Test
    void preservesSourceIdAndInterval() {
        Delivery delivery = new Delivery("route-7", 0, 15);

        assertAll(
                () -> assertEquals("route-7", delivery.getId()),
                () -> assertEquals(0, delivery.getStartTime()),
                () -> assertEquals(15, delivery.getFinishTime()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsMissingSourceId(String id) {
        assertThrows(IllegalArgumentException.class, () -> new Delivery(id, 0, 15));
    }

    @ParameterizedTest
    @CsvSource({"-1, 1", "0, 0", "3, 3", "4, 3"})
    void rejectsInvalidInterval(int startTime, int finishTime) {
        assertThrows(IllegalArgumentException.class, () -> new Delivery("route-7", startTime, finishTime));
    }
}
