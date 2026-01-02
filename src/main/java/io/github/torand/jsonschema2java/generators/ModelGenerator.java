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
package io.github.torand.jsonschema2java.generators;

import io.github.torand.jsonschema2java.collectors.EnumInfoCollector;
import io.github.torand.jsonschema2java.collectors.PojoInfoCollector;
import io.github.torand.jsonschema2java.collectors.SchemaResolver;
import io.github.torand.jsonschema2java.model.EnumInfo;
import io.github.torand.jsonschema2java.model.PojoInfo;
import io.github.torand.jsonschema2java.utils.JsonSchema2JavaException;
import io.github.torand.jsonschema2java.utils.JsonSchemaDef;
import io.github.torand.jsonschema2java.writers.EnumWriter;
import io.github.torand.jsonschema2java.writers.PojoWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static io.github.torand.jsonschema2java.utils.StringUtils.pluralSuffix;
import static io.github.torand.jsonschema2java.writers.WriterFactory.createEnumWriter;
import static io.github.torand.jsonschema2java.writers.WriterFactory.createPojoWriter;

/**
 * Generates source code for models (pojos).
 */
public class ModelGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ModelGenerator.class);

    private final Options opts;
    private final SchemaResolver schemaResolver;

    public ModelGenerator(Options opts) {
        this.opts = opts;
        this.schemaResolver = new SchemaResolver(opts);
    }

    public void generate(List<Path> schemaFiles) {
        int enumCount = 0;
        int pojoCount = 0;

        for (Path schemaFile : schemaFiles) {
            JsonSchemaDef schema = schemaResolver.load(schemaFile);
            String pojoName = schema.getName() + opts.pojoNameSuffix();

            if (schema.isEnum()) {
                generateEnumFile(pojoName, schema);
                enumCount++;
            }

            if (schema.isClass()) {
                generatePojoFile(pojoName, schema);
                pojoCount++;
            }
        }

        logger.info("Generated {} enum{}, {} pojo{} in directory {}", enumCount, pluralSuffix(enumCount), pojoCount, pluralSuffix(pojoCount), opts.getModelOutputDir(null));
    }

    private void generateEnumFile(String name, JsonSchemaDef schema) {
        if (opts.verbose()) {
            logger.info("Generating model enum {}", name);
        }

        EnumInfoCollector enumInfoCollector = new EnumInfoCollector(opts);
        EnumInfo enumInfo = enumInfoCollector.getEnumInfo(name, schema);

        String enumFilename = name + opts.getFileExtension();
        try (EnumWriter enumWriter = createEnumWriter(enumFilename, opts, enumInfo.modelSubdir())) {
            enumWriter.write(enumInfo);
        } catch (IOException e) {
            throw new JsonSchema2JavaException("Failed to write file %s".formatted(enumFilename), e);
        }
    }

    private void generatePojoFile(String name, JsonSchemaDef schema) {
        if (opts.verbose()) {
            logger.info("Generating model class {}", name);
        }

        PojoInfoCollector pojoInfoCollector = new PojoInfoCollector(opts, schemaResolver);
        PojoInfo pojoInfo = pojoInfoCollector.getPojoInfo(name, schema);

        String pojoFilename = name + opts.getFileExtension();
        try (PojoWriter pojoWriter = createPojoWriter(pojoFilename, opts, pojoInfo.modelSubdir())) {
            pojoWriter.write(pojoInfo);
        } catch (IOException e) {
            throw new JsonSchema2JavaException("Failed to write file %s".formatted(pojoFilename), e);
        }
    }
}
