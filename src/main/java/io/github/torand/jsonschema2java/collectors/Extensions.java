package io.github.torand.jsonschema2java.collectors;

import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.github.torand.jsonschema2java.utils.StringHelper.nonBlank;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class Extensions {
    public static final String EXT_JSON_SERIALIZER = "x-json-serializer";
    public static final String EXT_VALIDATION_CONSTRAINT = "x-validation-constraint";
    public static final String EXT_NULLABLE = "x-nullable";
    public static final String EXT_MODEL_SUBDIR = "x-model-subdir";
    public static final String EXT_DEPRECATION_MESSAGE = "x-deprecation-message";

    public static final Set<String> KEYWORDS = Set.of(
        EXT_JSON_SERIALIZER,
        EXT_VALIDATION_CONSTRAINT,
        EXT_NULLABLE,
        EXT_MODEL_SUBDIR,
        EXT_DEPRECATION_MESSAGE
    );

    private final Map<String, Object> extensions;

    public static Extensions extensions(Map<String, Object> extensions) {
        return new Extensions(extensions);
    }

    public Extensions(Map<String, Object> extensions) {
        this.extensions = nonNull(extensions) ? extensions : emptyMap();
    }

    public Optional<String> getString(String name) {
        Object value = extensions.get(name);
        if (isNull(value)) {
            return Optional.empty();
        }
        if (!(value instanceof TextNode)) {
            throw new RuntimeException("Value of extension %s is not a String".formatted(name));
        }
        if (nonBlank(((TextNode)value).asText())) {
            return Optional.of(((TextNode)value).asText());
        }

        return Optional.empty();
    }

    public Optional<Boolean> getBoolean(String name) {
        Object value = extensions.get(name);
        if (isNull(value)) {
            return Optional.empty();
        }
        if (!(value instanceof BooleanNode)) {
            throw new RuntimeException("Value of extension %s is not a Boolean".formatted(name));
        }

        return Optional.of(((BooleanNode)value).asBoolean());
    }
}
