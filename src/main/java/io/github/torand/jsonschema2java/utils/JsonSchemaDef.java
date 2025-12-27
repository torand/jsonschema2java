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
package io.github.torand.jsonschema2java.utils;

import com.networknt.schema.Schema;
import io.github.torand.jsonschema2java.collectors.Extensions;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static io.github.torand.javacommons.stream.StreamHelper.streamSafely;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

/**
 * Represents a JSON Schema definition.
 */
public class JsonSchemaDef {
    private final String name;
    private final JsonNode schema;

    public JsonSchemaDef(String name, Schema schema) {
        this.name = name;
        this.schema = schema.getSchemaNode();
    }

    public JsonSchemaDef(String name, JsonNode schema) {
        this.name = name;
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public boolean isEnum() {
        return types().anyMatch("string"::equals) && has("/enum");
    }

    public boolean isClass() {
        return types().anyMatch("object"::equals) || hasAllOf();
    }

    public String description() {
        return schema.at("/description").asText(null);
    }

    public boolean isDeprecated() {
        return isTrue("/deprecated");
    }

    public boolean hasAllOf() {
        return has("/allOf");
    }

    public boolean hasAnyOf() {
        return has("/anyOf");
    }

    public boolean hasOneOf() {
        return has("/oneOf");
    }

    public Stream<JsonSchemaDef> allOf() {
        JsonNode arrayNode = schema.at("/allOf");
        if (isNull(arrayNode) || arrayNode.isNull() || arrayNode.isEmpty()) {
            return Stream.empty();
        }
        return streamSafely(arrayNode.values()).map(e -> new JsonSchemaDef("$", e));
    }

    public Stream<JsonSchemaDef> oneOf() {
        JsonNode arrayNode = schema.at("/oneOf");
        if (isNull(arrayNode) || arrayNode.isNull() || arrayNode.isEmpty()) {
            return Stream.empty();
        }
        return streamSafely(arrayNode.values()).map(e -> new JsonSchemaDef("$", e));
    }

    public URI ref() {
        String refValue = schema.at("/$ref").asText(null);
        return nonNull(refValue) ? URI.create(refValue) : null;
    }

    public boolean hasTypes() {
        return types().findAny().isPresent();
    }

    public boolean hasType(String typeName) {
        return types().anyMatch(typeName::equals);
    }

    public Stream<String> types() {
        return arrayOf("/type");
    }

    public Stream<String> enums() {
        return arrayOf("/enum");
    }

    public boolean isRequired(String propertyName) {
        return required().anyMatch(propertyName::equals);
    }

    public Stream<String> required() {
        return arrayOf("/required");
    }

    public Map<String, JsonSchemaDef> properties() {
        Map<String, JsonSchemaDef> props = new LinkedHashMap<>();
        streamSafely(schema.at("/properties").properties())
            .forEach(e -> props.put(e.getKey(), new JsonSchemaDef("$", e.getValue())));
        return props;
    }

    public String defaultValue() {
        return schema.at("/default").asText(null);
    }

    public String format() {
        return schema.at("/format").asText(null);
    }

    public String pattern() {
        return schema.at("/pattern").asText(null);
    }

    public boolean uniqueItems() {
        return isTrue("/uniqueItems");
    }

    public Integer minItems() {
        return has("/minItems") ? schema.at("/minItems").asInt() : null;
    }

    public Integer maxItems() {
        return has("/maxItems") ? schema.at("/maxItems").asInt() : null;
    }

    public Integer minLength() {
        return has("/minLength") ? schema.at("/minLength").asInt() : null;
    }

    public Integer maxLength() {
        return has("/maxLength") ? schema.at("/maxLength").asInt() : null;
    }

    public BigDecimal minimum() {
        return has("/minimum") ? BigDecimal.valueOf(schema.at("/minimum").asDouble()) : null;
    }

    public BigDecimal maximum() {
        return has("/maximum") ? BigDecimal.valueOf(schema.at("/maximum").asDouble()) : null;
    }

    public JsonSchemaDef items() {
        if (!has("/items")) {
            return null;
        }

        return new JsonSchemaDef("$", schema.at("/items"));
    }

    public Object additionalProperties() {
        if (!has("/additionalProperties")) {
            return null;
        }

        if (schema.at("/additionalProperties").isBoolean()) {
            return schema.at("/additionalProperties").asBoolean();
        }

        return new JsonSchemaDef("$", schema.at("/additionalProperties"));
    }

    public Extensions extensions() {
        Map<String, Object> extensionProps = streamSafely(schema.properties())
            .filter(entry -> entry.getKey().startsWith("x-"))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        return Extensions.extensions(extensionProps);
    }

    private boolean has(String jsonPtrExpr) {
        return !schema.at(jsonPtrExpr).isMissingNode();
    }

    private boolean isTrue(String jsonPtrExpr) {
        return schema.at(jsonPtrExpr).isBoolean() && TRUE.equals(schema.at(jsonPtrExpr).asBoolean());
    }

    private Stream<String> arrayOf(String jsonPtrExpr) {
        JsonNode arrayNode = schema.at(jsonPtrExpr);
        if (isNull(arrayNode) || arrayNode.isNull() || arrayNode.isMissingNode()) {
            return Stream.empty();
        }
        if (arrayNode.isArray()) {
            return streamSafely(arrayNode.values()).map(JsonNode::asText);
        } else {
            return Stream.of(arrayNode.asText());
        }
    }

    @Override
    public String toString() {
        return schema.toString();
    }
}
