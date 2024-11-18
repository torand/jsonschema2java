package org.github.torand.jsonschema2java.writers;

import org.github.torand.jsonschema2java.Options;

import java.io.IOException;
import java.io.Writer;

public abstract class BaseWriter implements AutoCloseable {
    private static final String INDENT = "    ";

    private final Writer writer;
    protected final Options opts;

    public BaseWriter(Writer writer, Options opts) {
        this.writer = writer;
        this.opts = opts;
    }

    protected void write(String format, Object... args) {
        try {
            writer.append(format.formatted(args));
        } catch (IOException e) {
            throw new RuntimeException("Failed to append to writer", e);
        }
    }

    protected void writeLine(String format, Object... args) {
        try {
            writer.append(format.formatted(args)).append("\n");
        } catch (IOException e) {
            throw new RuntimeException("Failed to append to writer", e);
        }
    }

    protected void writeNewLine() {
        try {
            writer.append("\n");
        } catch (IOException e) {
            throw new RuntimeException("Failed to append to writer", e);
        }
    }

    protected void writeIndent(int levels) {
        for (int level = 0; level < levels; level++) {
            try {
                writer.append(INDENT);
            } catch (IOException e) {
                throw new RuntimeException("Failed to append to writer", e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
