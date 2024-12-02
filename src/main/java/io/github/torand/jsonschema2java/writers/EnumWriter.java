package io.github.torand.jsonschema2java.writers;

import io.github.torand.jsonschema2java.model.EnumInfo;

import java.io.IOException;

public interface EnumWriter extends AutoCloseable {
    void write(EnumInfo enumInfo);

    @Override
    void close() throws IOException;
}
