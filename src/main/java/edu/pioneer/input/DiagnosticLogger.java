package edu.pioneer.input;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Writes the source, position, readable ID, and reason for each skipped row. */
final class DiagnosticLogger implements AutoCloseable {
    private Path path;
    private BufferedWriter writer;

    Path getPath() {
        return path;
    }

    void log(Path source, int lineNumber, int characterPosition, String id, String reason) throws IOException {
        // Create a log only for a file that actually has a skipped row.
        if (writer == null) {
            path = Files.createTempFile("pioneer-delivery-", ".log");
            writer = Files.newBufferedWriter(path);
        }
        writer.write(source.getFileName() + ", line " + lineNumber + ", character " + characterPosition
                + ", id " + (id == null || id.isBlank() ? "<unavailable>" : id) + ": " + reason);
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }
}
