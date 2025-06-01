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

import io.github.torand.jsonschema2java.TestHelper;
import io.github.torand.jsonschema2java.generators.Options;
import io.github.torand.jsonschema2java.model.TypeInfo;
import io.github.torand.jsonschema2java.utils.JsonSchemaDef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.github.torand.jsonschema2java.TestHelper.parseJson;
import static org.assertj.core.api.Assertions.assertThat;

public class TypeInfoCollectorTest {

    private TypeInfoCollector collector;

    @BeforeEach
    public void setUp() {
        Options opts = TestHelper.getJavaOptions();
        SchemaResolver schemaResolver = new SchemaResolver(opts);
        collector = new TypeInfoCollector(opts, schemaResolver);
    }

    @Test
    public void shouldMapBooleanProperties() {
        assertNullableBooleanType("""
                {"type": ["boolean", "null"]}
            """);

        assertNonNullableBooleanType("""
                {"type": "boolean"}
            """, "@NotNull");
    }

    @Test
    public void shouldMapIntegerProperties() {
        assertNullableNumericType("""
                {"type": ["integer", "null"]}
            """, "Integer", null);

        assertNonNullableNumericType("""
                {"type": "integer"}
            """, "Integer", null, "@NotNull");

        assertNonNullableNumericType("""
                {"type": "integer", "format": "int32"}
            """, "Integer", "int32", "@NotNull");

        assertNonNullableNumericType("""
                {"type": "integer", "format": "int64"}
            """, "Long", "int64", "@NotNull");

        assertNonNullableNumericType("""
                {"type": "integer", "minimum": 1}
            """, "Integer", null, "@NotNull", "@Min(1)");

        assertNonNullableNumericType("""
                {"type": "integer", "maximum": 10}
            """, "Integer", null, "@NotNull", "@Max(10)");
    }

    @Test
    public void shouldMapNumberProperties() {
        assertNullableNumericType("""
                {"type": ["number", "null"]}
            """, "BigDecimal", null);

        assertNonNullableNumericType("""
                {"type": "number"}
            """, "BigDecimal", null, "@NotNull");

        assertNonNullableNumericType("""
                {"type": "number", "format": "float"}
            """, "Float", "float", "@NotNull");

        assertNonNullableNumericType("""
                {"type": "number", "format": "double"}
            """, "Double", "double", "@NotNull");

        assertNonNullableNumericType("""
                {"type": "number", "minimum": 1}
            """, "BigDecimal", null, "@NotNull", "@Min(1)");

        assertNonNullableNumericType("""
                {"type": "number", "maximum": 10}
            """, "BigDecimal", null, "@NotNull", "@Max(10)");
    }

    @Test
    public void shouldMapStringProperties() {
        assertNullableStringType("""
                {"type": ["string", "null"]}
            """, "String", null, null);

        assertNonNullableStringType("""
                {"type": "string"}
            """, "String", null, null, "@NotBlank");

        assertNonNullableStringType("""
                {"type": "string", "pattern": "[a..z]{1,6}"}
            """, "String", null, "[a..z]{1,6}", "@NotBlank", "@Pattern(regexp = \"[a..z]{1,6}\")");

        assertNonNullableStringType("""
                {"type": "string", "minLength": 1}
            """, "String", null, null, "@NotBlank", "@Size(min = 1)");

        assertNonNullableStringType("""
                {"type": "string", "maxLength": 10}
            """, "String", null, null, "@NotBlank", "@Size(max = 10)");

        assertNonNullableStringType("""
                {"type": "string", "format": "uri"}
            """, "URI", "uri", null, "@NotNull");

        assertNonNullableStringType("""
                {"type": "string", "format": "uuid"}
            """, "UUID", "uuid", null, "@NotNull");

        assertNonNullableStringType("""
                {"type": "string", "format": "duration"}
            """, "Duration", "duration", null, "@NotNull");

        assertNonNullableStringType("""
                {"type": "string", "format": "date"}
            """, "LocalDate", "date", null, "@NotNull", "@JsonFormat(pattern = \"yyyy-MM-dd\")");

        assertNonNullableStringType("""
                {"type": "string", "format": "date-time"}
            """, "LocalDateTime", "date-time", null, "@NotNull", "@JsonFormat(pattern = \"yyyy-MM-dd'T'HH:mm:ss\")");

        assertNonNullableStringType("""
                {"type": "string", "format": "binary"}
            """, "byte[]", "binary", null, "@NotEmpty");

        assertNonNullableStringType("""
                {"type": "string", "format": "email"}
            """, "String", "email", null, "@NotBlank", "@Email");
    }

    @Test
    public void shouldMapArrayProperties() {
        assertNullableArrayType("""
                {"type": ["array", "null"], "items": {"type": "string"}}
            """, "List", "String", "@Valid");

        assertNonNullableArrayType("""
                {"type": "array", "items": {"type": "string"}}
            """, "List", "String", "@Valid", "@NotNull");

        assertNonNullableArrayType("""
                {"type": "array", "items": {"type": "string"}, "uniqueItems": true}
            """, "Set", "String", "@Valid", "@NotNull");

        assertNonNullableArrayType("""
                {"type": "array", "items": {"type": "string"}, "minItems": 1}
            """, "List", "String", "@Valid", "@NotNull", "@Size(min = 1)");

        assertNonNullableArrayType("""
                {"type": "array", "items": {"type": "string"}, "maxItems": 10}
            """, "List", "String", "@Valid", "@NotNull", "@Size(max = 10)");
    }

    private void assertNullableBooleanType(String jsonSchema, String... expectedAnnotations) {
        TypeInfo typeInfo = getTypeInfo(jsonSchema);
        assertPrimitiveType(typeInfo, "Boolean", null, null, true, expectedAnnotations);
    }

    private void assertNonNullableBooleanType(String jsonSchema, String... expectedAnnotations) {
        TypeInfo typeInfo = getTypeInfo(jsonSchema);
        assertPrimitiveType(typeInfo, "Boolean", null, null, false, expectedAnnotations);
    }

    private void assertNullableNumericType(String jsonSchema, String expectedTypeName, String expectedFormat, String... expectedAnnotations) {
        TypeInfo typeInfo = getTypeInfo(jsonSchema);
        assertPrimitiveType(typeInfo, expectedTypeName, expectedFormat, null, true, expectedAnnotations);
    }

    private void assertNonNullableNumericType(String jsonSchema, String expectedTypeName, String expectedFormat, String... expectedAnnotations) {
        TypeInfo typeInfo = getTypeInfo(jsonSchema);
        assertPrimitiveType(typeInfo, expectedTypeName, expectedFormat, null, false, expectedAnnotations);
    }

    private void assertNullableStringType(String jsonSchema, String expectedTypeName, String expectedFormat, String expectedPattern, String... expectedAnnotations) {
        TypeInfo typeInfo = getTypeInfo(jsonSchema);
        assertPrimitiveType(typeInfo, expectedTypeName, expectedFormat, expectedPattern, true, expectedAnnotations);
    }

    private void assertNonNullableStringType(String jsonSchema, String expectedTypeName, String expectedFormat, String expectedPattern, String... expectedAnnotations) {
        TypeInfo typeInfo = getTypeInfo(jsonSchema);
        assertPrimitiveType(typeInfo, expectedTypeName, expectedFormat, expectedPattern, false, expectedAnnotations);
    }

    private void assertNonNullableArrayType(String jsonSchema, String expectedTypeName, String expectedItemTypeName, String... expectedAnnotations) {
        TypeInfo typeInfo = getTypeInfo(jsonSchema);
        assertThat(typeInfo.name).isEqualTo(expectedTypeName);
        assertThat(typeInfo.itemType.name).isEqualTo(expectedItemTypeName);
        assertThat(typeInfo.nullable).isFalse();
        assertThat(typeInfo.primitive).isFalse();
        assertThat(typeInfo.annotations).containsExactly(expectedAnnotations);
    }

    private void assertNullableArrayType(String jsonSchema, String expectedTypeName, String expectedItemTypeName, String... expectedAnnotations) {
        TypeInfo typeInfo = getTypeInfo(jsonSchema);
        assertThat(typeInfo.name).isEqualTo(expectedTypeName);
        assertThat(typeInfo.itemType.name).isEqualTo(expectedItemTypeName);
        assertThat(typeInfo.nullable).isTrue();
        assertThat(typeInfo.primitive).isFalse();
        assertThat(typeInfo.annotations).containsExactly(expectedAnnotations);
    }

    private void assertPrimitiveType(TypeInfo typeInfo, String expectedTypeName, String expectedFormat, String expectedPattern, boolean expectedNullable, String... expectedAnnotations) {
        assertThat(typeInfo.name).isEqualTo(expectedTypeName);
        assertThat(typeInfo.schemaFormat).isEqualTo(expectedFormat);
        assertThat(typeInfo.schemaPattern).isEqualTo(expectedPattern);
        assertThat(typeInfo.nullable).isEqualTo(expectedNullable);
        assertThat(typeInfo.primitive).isTrue();
        assertThat(typeInfo.annotations).containsExactly(expectedAnnotations);
    }

    private TypeInfo getTypeInfo(String jsonSchema) {
        JsonSchemaDef schema = new JsonSchemaDef("NA", parseJson(jsonSchema));
        return collector.getTypeInfo(schema);
    }
}