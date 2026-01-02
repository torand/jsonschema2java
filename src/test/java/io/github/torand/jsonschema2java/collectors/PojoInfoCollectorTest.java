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
package io.github.torand.jsonschema2java.collectors;

import io.github.torand.jsonschema2java.TestHelper;
import io.github.torand.jsonschema2java.generators.Options;
import io.github.torand.jsonschema2java.model.PojoInfo;
import io.github.torand.jsonschema2java.utils.JsonSchemaDef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static io.github.torand.jsonschema2java.TestHelper.parseJson;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PojoInfoCollectorTest {

    private SchemaResolver schemaResolver;
    private PojoInfoCollector collector;

    @BeforeEach
    void setUp() {
        Options opts = TestHelper.getJavaOptions();
        schemaResolver = new SchemaResolver(null);
        collector = new PojoInfoCollector(opts, schemaResolver);
    }

    @Test
    void shouldFailForAdditionalProperties() {
        String jsonSchema = """
                {"type": "object", "properties": { "name": {"type": "string"}}, "additionalProperties": {"type": "integer"}}
            """;

        assertThatThrownBy(() -> getPojoInfo(jsonSchema))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Schema-based 'additionalProperties' not supported for Pojos");
    }

    private PojoInfo getPojoInfo(String jsonSchema) {
        JsonNode jsonNode = parseJson(jsonSchema);
        JsonSchemaDef schema = new JsonSchemaDef("Pojos", jsonNode);

        return collector.getPojoInfo("Pojo", schema);
    }
}