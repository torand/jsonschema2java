/*
 * Copyright (c) 2024-2025 Tore Eide Andersen
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
package io.github.torand.jsonschema2java.collectors;

import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.torand.jsonschema2java.utils.JsonSchema2JavaException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.github.torand.javacommons.lang.StringHelper.nonBlank;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Handles custom JSON Schema extensions.
 */
public class Extensions {
    /**
     * Fully qualified classname of a JSON serializer class for the schema.
     */
    public static final String EXT_JSON_SERIALIZER = "x-json-serializer";

    /**
     * Fully qualified classname of an annotation class to validate the schema.
     */
    public static final String EXT_VALIDATION_CONSTRAINT = "x-validation-constraint";

    /**
     * If `true` the type of the schema/property can be `null`.
     */
    public static final String EXT_NULLABLE = "x-nullable";

    /**
     * Subdirectory to place the generated DTO model class.
     */
    public static final String EXT_MODEL_SUBDIR = "x-model-subdir";

    /**
     * Describing why something is deprecated, and what to use instead.
     */
    public static final String EXT_DEPRECATION_MESSAGE = "x-deprecation-message";

    public static final Set<String> KEYWORDS = Set.of(
        EXT_JSON_SERIALIZER,
        EXT_VALIDATION_CONSTRAINT,
        EXT_NULLABLE,
        EXT_MODEL_SUBDIR,
        EXT_DEPRECATION_MESSAGE
    );

    private final Map<String, Object> extensionsByName;

    /**
     * Returns an {@link Extensions} object processing the specified JSON Schema extension map.
     * @param extensionsByName the JSON Schema extensions.
     * @return the {@link Extensions} object.
     */
    public static Extensions extensions(Map<String, Object> extensionsByName) {
        return new Extensions(extensionsByName);
    }

    private Extensions(Map<String, Object> extensionsByName) {
        this.extensionsByName = nonNull(extensionsByName) ? extensionsByName : emptyMap();
    }

    /**
     * Gets value of a string extension property.
     * @param name the extension property name.
     * @return the extension property string value, if found; else empty.
     */
    public Optional<String> getString(String name) {
        Object value = extensionsByName.get(name);
        if (isNull(value)) {
            return Optional.empty();
        }
        if (!(value instanceof TextNode)) {
            throw new JsonSchema2JavaException("Value of extension %s is not a String".formatted(name));
        }
        if (nonBlank(((TextNode)value).asText())) {
            return Optional.of(((TextNode)value).asText());
        }

        return Optional.empty();
    }

    /**
     * Gets value of a boolean extension property.
     * @param name the extension property name.
     * @return the extension property value, if found; else empty.
     */
    public Optional<Boolean> getBoolean(String name) {
        Object value = extensionsByName.get(name);
        if (isNull(value)) {
            return Optional.empty();
        }
        if (!(value instanceof BooleanNode)) {
            throw new JsonSchema2JavaException("Value of extension %s is not a Boolean".formatted(name));
        }

        return Optional.of(((BooleanNode)value).asBoolean());
    }
}
