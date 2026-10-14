package edu.pioneer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.pioneer.input.DeliveryFileReader;
import edu.pioneer.input.ProcessingReport;
import edu.pioneer.scheduling.TaskAScheduler;
import edu.pioneer.scheduling.TaskBScheduler;
import edu.pioneer.scheduling.TaskCScheduler;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RequiredCaseCsvTest {
    @ParameterizedTest(name = "Task A case {0}")
    @MethodSource("taskACases")
    void taskACsvMatchesRequiredCase(int caseNumber, List<String> intervals, int expectedSelected) throws IOException {
        ProcessingReport report = readRequiredCase("ATestValues", caseNumber, intervals);

        assertEquals(expectedSelected, new TaskAScheduler().selectDeliveries(report.getDeliveries()).size());
    }

    private static Stream<Arguments> taskACases() {
        return Stream.of(
                Arguments.of(1, List.of(), 0),
                Arguments.of(2, List.of("1-3"), 1),
                Arguments.of(3, List.of("1-3", "3-5", "5-7"), 3),
                Arguments.of(4, List.of("1-5", "2-6", "3-7"), 1),
                Arguments.of(5, List.of("5-7", "1-3", "3-5", "2-4"), 3),
                Arguments.of(6, List.of("1-4", "2-4", "4-6"), 2));
    }

    @ParameterizedTest(name = "Task B case {0}")
    @MethodSource("taskBCases")
    void taskBCsvMatchesRequiredCase(int caseNumber, List<String> intervals, int expectedDrivers) throws IOException {
        ProcessingReport report = readRequiredCase("BTestValues", caseNumber, intervals);

        assertEquals(expectedDrivers, new TaskBScheduler().assignDrivers(report.getDeliveries()).size());
    }

    private static Stream<Arguments> taskBCases() {
        return Stream.of(
                Arguments.of(1, List.of(), 0),
                Arguments.of(2, List.of("1-3"), 1),
                Arguments.of(3, List.of("1-3", "3-5", "5-7"), 1),
                Arguments.of(4, List.of("1-5", "2-6", "3-7"), 3),
                Arguments.of(5, List.of("1-4", "2-5", "4-7", "5-8"), 2),
                Arguments.of(6, List.of("5-8", "1-4", "2-5", "4-6", "6-9"), 2));
    }

    @ParameterizedTest(name = "Task C case {0}")
    @MethodSource("taskCCases")
    void taskCCsvMatchesRequiredCase(int caseNumber, List<String> intervals, int expectedRejected) throws IOException {
        ProcessingReport report = readRequiredCase("CTestValues", caseNumber, intervals);

        assertEquals(expectedRejected, new TaskCScheduler().countRejectedDeliveries(report.getDeliveries()));
    }

    private static Stream<Arguments> taskCCases() {
        return Stream.of(
                Arguments.of(1, List.of(), 0),
                Arguments.of(2, List.of("1-3"), 0),
                Arguments.of(3, List.of("1-3", "3-5", "5-7"), 0),
                Arguments.of(4, List.of("1-5", "2-6", "3-7"), 2),
                Arguments.of(5, List.of("5-7", "1-3", "3-5", "2-4"), 1),
                Arguments.of(6, List.of("1-2", "2-3", "3-4", "1-3"), 1));
    }

    private static ProcessingReport readRequiredCase(String directory, int caseNumber, List<String> expectedIntervals)
            throws IOException {
        Path path = Path.of("data", directory, "case-" + caseNumber + ".csv");
        ProcessingReport report = new DeliveryFileReader().read(path);

        assertEquals(expectedIntervals, report.getDeliveries().stream()
                .map(delivery -> delivery.getStartTime() + "-" + delivery.getFinishTime())
                .toList());
        assertEquals(0, report.getSkippedCount());
        return report;
    }
}
