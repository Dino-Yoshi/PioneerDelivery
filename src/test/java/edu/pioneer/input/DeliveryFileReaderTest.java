package edu.pioneer.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DeliveryFileReaderTest {
    @TempDir
    Path tempDirectory;

    @Test
    void readsValidUnorderedCsvWithIncidentalWhitespace() throws IOException {
        Path input = tempDirectory.resolve("deliveries.CSV");
        Files.writeString(input, " later , 5 , 9 \n early, 1, 3\n");

        ProcessingReport report = new DeliveryFileReader().read(input);

        assertEquals(2, report.getTotalRows());
        assertEquals(2, report.getAcceptedCount());
        assertEquals(0, report.getSkippedCount());
        assertEquals("later", report.getDeliveries().get(0).getId());
        assertEquals(5, report.getDeliveries().get(0).getStartTime());
        assertEquals(9, report.getDeliveries().get(0).getFinishTime());
        assertEquals("early", report.getDeliveries().get(1).getId());
        assertNull(report.getDiagnosticLogPath());
    }

    @Test
    void skipsMalformedRowsAndContinuesWithReadableDiagnostics() throws IOException {
        Path input = tempDirectory.resolve("mixed.csv");
        Files.writeString(input, "good,1,3\n bad,not-a-time,5\nnext,3,6\n");

        ProcessingReport report = new DeliveryFileReader().read(input);

        assertEquals(3, report.getTotalRows());
        assertEquals(2, report.getAcceptedCount());
        assertEquals(1, report.getSkippedCount());
        assertEquals("next", report.getDeliveries().get(1).getId());
        String diagnostics = Files.readString(report.getDiagnosticLogPath());
        assertTrue(diagnostics.contains("mixed.csv"));
        assertTrue(diagnostics.contains("line 2"));
        assertTrue(diagnostics.contains("bad"));
        assertTrue(diagnostics.contains("start_time"));
        assertTrue(diagnostics.contains("character 6"));
    }

    @Test
    void validatesFieldCountIdsAndIntervalsWithoutReservingSkippedIds() throws IOException {
        Path input = tempDirectory.resolve("invalid.csv");
        Files.writeString(input, "\n \nmissing,1\n,1,2\nalpha,-1,2\nalpha,2,2\nalpha,2,4\nalpha,4,6\nbeta,4,7,extra\ngamma,4,nope\n");

        ProcessingReport report = new DeliveryFileReader().read(input);

        assertEquals(8, report.getTotalRows());
        assertEquals(1, report.getAcceptedCount());
        assertEquals(7, report.getSkippedCount());
        assertEquals("alpha", report.getDeliveries().getFirst().getId());
        assertEquals(2, report.getDeliveries().getFirst().getStartTime());
        String diagnostics = Files.readString(report.getDiagnosticLogPath());
        assertEquals(7, diagnostics.lines().count());
        assertTrue(diagnostics.contains("line 3"));
        assertTrue(diagnostics.contains("fields"));
        assertTrue(diagnostics.contains("nonblank"));
        assertTrue(diagnostics.contains("interval"));
        assertTrue(diagnostics.contains("duplicate"));
        assertTrue(diagnostics.contains("finish_time"));
    }

    @Test
    void openedEmptyCsvIsASuccessfulEmptyDataset() throws IOException {
        Path input = tempDirectory.resolve("empty.csv");
        Files.createFile(input);

        ProcessingReport report = new DeliveryFileReader().read(input);

        assertEquals(0, report.getTotalRows());
        assertEquals(0, report.getAcceptedCount());
        assertEquals(0, report.getSkippedCount());
        assertTrue(report.getDeliveries().isEmpty());
        assertNull(report.getDiagnosticLogPath());
    }

    @Test
    void rejectsNonCsvNameBeforeAttemptingToOpenIt() {
        Path missingNonCsv = tempDirectory.resolve("missing.txt");

        IOException error = assertThrows(IOException.class, () -> new DeliveryFileReader().read(missingNonCsv));

        assertTrue(error.getMessage().contains(".csv"));
    }

    @Test
    void missingCsvIsAFileLoadError() {
        Path missingCsv = tempDirectory.resolve("missing.csv");

        assertThrows(IOException.class, () -> new DeliveryFileReader().read(missingCsv));
    }

    @Test
    void unreadableCsvSourceIsAFileLoadError() throws IOException {
        Path directoryNamedCsv = tempDirectory.resolve("folder.csv");
        Files.createDirectory(directoryNamedCsv);

        assertThrows(IOException.class, () -> new DeliveryFileReader().read(directoryNamedCsv));
    }

    @Test
    void acceptsQuotedCsvIdContainingCommaAndEscapedQuote() throws IOException {
        Path input = tempDirectory.resolve("quoted.csv");
        Files.writeString(input, "\"route, west\",1,3\n\"route \"\"east\"\"\",3,5\n");

        ProcessingReport report = new DeliveryFileReader().read(input);

        assertEquals(2, report.getAcceptedCount());
        assertEquals("route, west", report.getDeliveries().get(0).getId());
        assertEquals("route \"east\"", report.getDeliveries().get(1).getId());
    }

    @Test
    void skipsMalformedQuotedRowAndKeepsLaterValidRows() throws IOException {
        Path input = tempDirectory.resolve("quotes.csv");
        Files.writeString(input, "\"unterminated,1,2\nvalid,2,4\n");

        ProcessingReport report = new DeliveryFileReader().read(input);

        assertEquals(2, report.getTotalRows());
        assertEquals(1, report.getAcceptedCount());
        assertEquals(1, report.getSkippedCount());
        assertEquals("valid", report.getDeliveries().getFirst().getId());
        String diagnostics = Files.readString(report.getDiagnosticLogPath());
        assertTrue(diagnostics.contains("Unclosed quoted field"));
        assertTrue(diagnostics.contains("unterminated"));
    }

    @Test
    void acceptedDatasetCannotBeChangedThroughTheReport() throws IOException {
        Path input = tempDirectory.resolve("immutable.csv");
        Files.writeString(input, "one,1,2\n");

        ProcessingReport report = new DeliveryFileReader().read(input);

        assertThrows(UnsupportedOperationException.class, () -> report.getDeliveries().clear());
        assertEquals(1, report.getAcceptedCount());
    }
}
