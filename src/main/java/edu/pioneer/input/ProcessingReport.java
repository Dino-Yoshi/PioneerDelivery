package edu.pioneer.input;

import edu.pioneer.model.Delivery;
import java.nio.file.Path;
import java.util.List;

/**
 * A snapshot of accepted records and counts from one successfully opened CSV.
 * The diagnostic path is present only when at least one row was skipped.
 */
public final class ProcessingReport {
    private final List<Delivery> deliveries;
    private final int totalRows;
    private final int skippedCount;
    private final Path diagnosticLogPath;

    ProcessingReport(List<Delivery> deliveries, int totalRows, int skippedCount, Path diagnosticLogPath) {
        this.deliveries = List.copyOf(deliveries);
        this.totalRows = totalRows;
        this.skippedCount = skippedCount;
        this.diagnosticLogPath = diagnosticLogPath;
    }

    public List<Delivery> getDeliveries() {
        return deliveries;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getAcceptedCount() {
        return deliveries.size();
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    /** Returns the actual log location, or null when no rows were skipped. */
    public Path getDiagnosticLogPath() {
        return diagnosticLogPath;
    }
}
