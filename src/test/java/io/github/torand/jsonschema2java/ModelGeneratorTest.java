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
package io.github.torand.jsonschema2java;

import io.github.torand.jsonschema2java.generators.ModelGenerator;
import io.github.torand.jsonschema2java.generators.Options;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static io.github.torand.javacommons.lang.StringHelper.isBlank;

class ModelGeneratorTest {

    private static final Map<String, String> SCHEMAS = Map.of(
        "AddressV1", "common",
        "UserTypeV1", "",
        "UserV1", "",
        "InternalUserV1", "",
        "ProductCategoryV1", "",
        "ProductV1", "",
        "OrderStatusV1", "",
        "OrderItemV1", "",
        "OrderV1", ""
    );

    @Test
    void shouldGenerateJavaPojos() {
        Options opts = TestHelper.getJavaOptions();

        ModelGenerator modelGenerator = new ModelGenerator(opts);

        for (String schema : SCHEMAS.keySet()) {
            String modelSubDir = SCHEMAS.get(schema);
            Path schemaFile = Path.of(opts.searchRootDir(), schema+".json");

            modelGenerator.generate(List.of(schemaFile));

            TestHelper.assertMatchingJavaFiles("%s%sDto.java".formatted(isBlank(modelSubDir) ? "" : modelSubDir+"/", schema));
        }
    }

    @Test
    void shouldGenerateKotlinPojos() {
        Options opts = TestHelper.getKotlinOptions();

        ModelGenerator modelGenerator = new ModelGenerator(opts);

        for (String schema : SCHEMAS.keySet()) {
            String modelSubDir = SCHEMAS.get(schema);
            Path schemaFile = Path.of(opts.searchRootDir(), schema+".json");

            modelGenerator.generate(List.of(schemaFile));

            TestHelper.assertMatchingKotlinFiles("%s%sDto.kt".formatted(isBlank(modelSubDir) ? "" : modelSubDir+"/", schema));
        }
    }
}
