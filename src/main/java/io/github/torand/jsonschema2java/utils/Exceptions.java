package io.github.torand.jsonschema2java.utils;

import java.util.function.Supplier;

public final class Exceptions {
    private Exceptions() {}

    public static Supplier<IllegalStateException> illegalStateException(String message, Object... args) {
        return () -> new IllegalStateException(message.formatted(args));
    }
}
