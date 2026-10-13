package edu.pioneer.input;

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
                    row = parseCsvRow(line);
                } catch (CsvFormatException malformedCsv) {
                    logger.log(source, lineNumber, malformedCsv.position, malformedCsv.id, malformedCsv.getMessage());
                    skippedCount++;
                    continue;
                }
                List<String> fields = row.fields;
                String id = fields.getFirst();
                if (fields.size() != 3) {
                    int characterPosition = fields.size() > 3 ? row.positions.get(3) : line.length() + 1;
                    logger.log(source, lineNumber, characterPosition, id, "Expected exactly three fields");
                    skippedCount++;
                    continue;
                }
                if (id.isBlank()) {
                    logger.log(source, lineNumber, row.positions.getFirst(), id, "ID must be nonblank");
                    skippedCount++;
                    continue;
                }
                int startTime;
                int finishTime;
                try {
                    startTime = Integer.parseInt(fields.get(1));
                } catch (NumberFormatException invalidTime) {
                    int characterPosition = row.positions.get(1);
                    logger.log(source, lineNumber, characterPosition, id, "Invalid start_time integer");
                    skippedCount++;
                    continue;
                }
                try {
                    finishTime = Integer.parseInt(fields.get(2));
                } catch (NumberFormatException invalidTime) {
                    int characterPosition = row.positions.get(2);
                    logger.log(source, lineNumber, characterPosition, id, "Invalid finish_time integer");
                    skippedCount++;
                    continue;
                }
                if (startTime < 0 || finishTime <= startTime) {
                    int characterPosition = startTime < 0 ? row.positions.get(1) : row.positions.get(2);
                    logger.log(source, lineNumber, characterPosition, id, "Invalid interval: require 0 <= start_time < finish_time");
                    skippedCount++;
                    continue;
                }
                if (acceptedIds.contains(id)) {
                    logger.log(source, lineNumber, row.positions.getFirst(), id, "duplicate ID");
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

    private ParsedRow parseCsvRow(String line) throws CsvFormatException {
        List<String> fields = new ArrayList<>();
        List<Integer> positions = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean quoteClosed = false;
        int fieldPosition = 1;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (quoted) {
                if (character == '"' && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    field.append('"');
                    index++;
                } else if (character == '"') {
                    quoted = false;
                    quoteClosed = true;
                } else {
                    field.append(character);
                }
            } else if (character == ',') {
                fields.add(field.toString().trim());
                positions.add(fieldPosition);
                field.setLength(0);
                quoteClosed = false;
                fieldPosition = index + 2;
            } else if (quoteClosed) {
                if (!Character.isWhitespace(character)) {
                    throw new CsvFormatException("Unexpected character after closing quote", index + 1,
                            availableId(fields, field));
                }
            } else if (character == '"') {
                if (!field.toString().isBlank()) {
                    throw new CsvFormatException("Unexpected quote in field", index + 1,
                            availableId(fields, field));
                }
                field.setLength(0);
                quoted = true;
            } else {
                field.append(character);
            }
        }
        if (quoted) {
            throw new CsvFormatException("Unclosed quoted field", line.length() + 1,
                    availableId(fields, field));
        }
        fields.add(field.toString().trim());
        positions.add(fieldPosition);
        return new ParsedRow(fields, positions);
    }

    private String availableId(List<String> fields, StringBuilder field) {
        return fields.isEmpty() ? field.toString().trim() : fields.getFirst();
    }

    private record ParsedRow(List<String> fields, List<Integer> positions) {
    }

    private static final class CsvFormatException extends Exception {
        private final int position;
        private final String id;

        private CsvFormatException(String message, int position, String id) {
            super(message);
            this.position = position;
            this.id = id;
        }
    }
}
