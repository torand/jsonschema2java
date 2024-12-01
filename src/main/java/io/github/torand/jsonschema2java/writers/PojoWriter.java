package io.github.torand.jsonschema2java.writers;

import io.github.torand.jsonschema2java.model.PojoInfo;

import java.io.IOException;

public interface PojoWriter extends AutoCloseable {
    void write(PojoInfo pojoInfo);

    @Override
    void close() throws IOException;
}
