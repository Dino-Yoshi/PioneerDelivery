package edu.pioneer.input;

import edu.pioneer.input.CsvRowParser.CsvFormatException;
import edu.pioneer.input.CsvRowParser.ParsedRow;
import edu.pioneer.model.Delivery;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Loads one validated dataset from a headerless delivery CSV file. */
public final class DeliveryFileReader {
    public ProcessingReport read(Path source) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("Source path must not be null");
        }
        Path filename = source.getFileName();
        if (filename == null || !filename.toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new IOException("File must have a .csv extension: " + source);
        }

        List<Delivery> deliveries = new ArrayList<>();
        Set<String> acceptedIds = new HashSet<>();
        int totalRows = 0;
        int skippedCount = 0;
        Path diagnosticLogPath;
        try (BufferedReader reader = Files.newBufferedReader(source);
                DiagnosticLogger logger = new DiagnosticLogger()) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                totalRows++;
                ParsedRow row;
                try {
                    row = CsvRowParser.parse(line);
                } catch (CsvFormatException malformedCsv) {
                    logger.log(source, lineNumber, malformedCsv.position(), malformedCsv.id(), malformedCsv.getMessage());
                    skippedCount++;
                    continue;
                }
                List<String> fields = row.fields();
                String id = fields.getFirst();
                if (fields.size() != 3) {
                    int characterPosition = fields.size() > 3 ? row.positions().get(3) : line.length() + 1;
                    logger.log(source, lineNumber, characterPosition, id, "Expected exactly three fields");
                    skippedCount++;
                    continue;
                }
                if (id.isBlank()) {
                    logger.log(source, lineNumber, row.positions().getFirst(), id, "ID must be nonblank");
                    skippedCount++;
                    continue;
                }
                int startTime;
                int finishTime;
                try {
                    startTime = Integer.parseInt(fields.get(1));
                } catch (NumberFormatException invalidTime) {
                    int characterPosition = row.positions().get(1);
                    logger.log(source, lineNumber, characterPosition, id, "Invalid start_time integer");
                    skippedCount++;
                    continue;
                }
                try {
                    finishTime = Integer.parseInt(fields.get(2));
                } catch (NumberFormatException invalidTime) {
                    int characterPosition = row.positions().get(2);
                    logger.log(source, lineNumber, characterPosition, id, "Invalid finish_time integer");
                    skippedCount++;
                    continue;
                }
                if (startTime < 0 || finishTime <= startTime) {
                    int characterPosition = startTime < 0 ? row.positions().get(1) : row.positions().get(2);
                    logger.log(source, lineNumber, characterPosition, id, "Invalid interval: require 0 <= start_time < finish_time");
                    skippedCount++;
                    continue;
                }
                if (acceptedIds.contains(id)) {
                    logger.log(source, lineNumber, row.positions().getFirst(), id, "duplicate ID");
                    skippedCount++;
                    continue;
                }
                deliveries.add(new Delivery(id, startTime, finishTime));
                acceptedIds.add(id);
            }
            diagnosticLogPath = logger.getPath();
        }
        return new ProcessingReport(deliveries, totalRows, skippedCount, diagnosticLogPath);
    }
}
