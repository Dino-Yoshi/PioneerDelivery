package edu.pioneer.input;

import java.util.ArrayList;
import java.util.List;

/** Parses one CSV row while retaining field positions for diagnostics. */
final class CsvRowParser {
    private CsvRowParser() {
    }

    static ParsedRow parse(String line) throws CsvFormatException {
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

    private static String availableId(List<String> fields, StringBuilder field) {
        return fields.isEmpty() ? field.toString().trim() : fields.getFirst();
    }

    record ParsedRow(List<String> fields, List<Integer> positions) {
    }

    static final class CsvFormatException extends Exception {
        private final int position;
        private final String id;

        private CsvFormatException(String message, int position, String id) {
            super(message);
            this.position = position;
            this.id = id;
        }

        int position() {
            return position;
        }

        String id() {
            return id;
        }
    }
}
