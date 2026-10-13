package edu.pioneer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.pioneer.input.DeliveryFileReader;
import edu.pioneer.input.ProcessingReport;
import edu.pioneer.model.Delivery;
import edu.pioneer.model.Driver;
import edu.pioneer.scheduling.TaskAScheduler;
import edu.pioneer.scheduling.TaskBScheduler;
import edu.pioneer.scheduling.TaskCScheduler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SubmissionIntegrationTest {
    @Test
    void sampleFileCanBeLoadedAndAnalyzedByAllThreeTasks() throws IOException {
        ProcessingReport report = new DeliveryFileReader().read(Path.of("data", "sample-deliveries.csv"));

        assertEquals(7, report.getAcceptedCount());
        assertEquals(3, new TaskAScheduler().selectDeliveries(report.getDeliveries()).size());
        assertEquals(3, new TaskBScheduler().assignDrivers(report.getDeliveries()).size());
        assertEquals(4, new TaskCScheduler().countRejectedDeliveries(report.getDeliveries()));
    }

    @Test
    void skippedRowsNeverEnterSchedulingResults(@TempDir Path tempDirectory) throws IOException {
        Path input = tempDirectory.resolve("mixed.csv");
        Files.writeString(input, "late,5,7\ninvalid,2,2\nearly,1,3\noverlap,2,4\nmiddle,3,5\nearly,7,9\n");
        ProcessingReport report = new DeliveryFileReader().read(input);
        List<Delivery> accepted = report.getDeliveries();

        List<Delivery> selected = new TaskAScheduler().selectDeliveries(accepted);
        List<Driver> drivers = new TaskBScheduler().assignDrivers(accepted);
        List<Delivery> assigned = drivers.stream().flatMap(driver -> driver.getDeliveries().stream()).toList();
        int rejected = new TaskCScheduler().countRejectedDeliveries(accepted);

        assertEquals(6, report.getTotalRows());
        assertEquals(4, report.getAcceptedCount());
        assertEquals(2, report.getSkippedCount());
        assertEquals(3, selected.size());
        assertEquals(2, drivers.size());
        assertEquals(1, rejected);
        assertEquals(accepted.size() - selected.size(), rejected);
        assertTrue(Set.copyOf(accepted).containsAll(selected));
        assertEquals(accepted.size(), assigned.size());
        assertEquals(Set.copyOf(accepted), Set.copyOf(assigned));
        assertEquals(List.of("late:5-7", "early:1-3", "overlap:2-4", "middle:3-5"),
                accepted.stream().map(delivery -> delivery.getId() + ":" + delivery.getStartTime()
                        + "-" + delivery.getFinishTime()).toList());
    }
}
