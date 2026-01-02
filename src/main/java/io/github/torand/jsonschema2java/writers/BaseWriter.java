/*
 * Copyright (c) 2024-2026 Tore Eide Andersen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.torand.jsonschema2java.writers;

import io.github.torand.jsonschema2java.generators.Options;
import io.github.torand.jsonschema2java.utils.JsonSchema2JavaException;

import java.io.IOException;
import java.io.Writer;

/**
 * Base class for all code writers.
 */
public abstract class BaseWriter implements AutoCloseable {

    private final Writer writer;

    /**
     * The plugin options.
     */
    protected final Options opts;

    /**
     * Constructs a {@link BaseWriter} object.
     * @param writer the java io writer to wrap.
     * @param opts the plugin options.
     */
    protected BaseWriter(Writer writer, Options opts) {
        this.writer = writer;
        this.opts = opts;
    }

    /**
     * Writes a formatted string without end-of-line.
     * @param format the format.
     * @param args the arguments.
     */
    protected void write(String format, Object... args) {
        try {
            writer.append(format.formatted(args));
        } catch (IOException e) {
            throw new JsonSchema2JavaException("Failed to append to writer", e);
        }
    }

    /**
     * Writes a formatted string with end-of-line.
     * @param format the format.
     * @param args the arguments.
     */
    protected void writeLine(String format, Object... args) {
        try {
            writer.append(format.formatted(args)).append("\n");
        } catch (IOException e) {
            throw new JsonSchema2JavaException("Failed to append to writer", e);
        }
    }

    /**
     * Writes end-of-line.
     */
    protected void writeNewLine() {
        try {
            writer.append("\n");
        } catch (IOException e) {
            throw new JsonSchema2JavaException("Failed to append to writer", e);
        }
    }

    /**
     * Writes specified number of indent levels.
     * @param levels the number of indent levels.
     */
    protected void writeIndent(int levels) {
        String indent = "\t";
        if (!opts.indentWithTab()) {
            indent = " ".repeat(opts.indentSize());
        }

        for (int level = 0; level < levels; level++) {
            try {
                writer.append(indent);
            } catch (IOException e) {
                throw new JsonSchema2JavaException("Failed to append to writer", e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
