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

import io.github.torand.jsonschema2java.generators.Options;
import io.github.torand.jsonschema2java.model.AnnotationInfo;
import io.github.torand.jsonschema2java.model.EnumInfo;
import io.github.torand.jsonschema2java.utils.JsonSchemaDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.torand.jsonschema2java.collectors.Extensions.EXT_MODEL_SUBDIR;
import static io.github.torand.jsonschema2java.utils.StringUtils.joinCsv;

/**
 * Collects information about an enum from a schema.
 */
public class EnumInfoCollector extends BaseCollector {

    public EnumInfoCollector(Options opts) {
        super(opts);
    }

    public EnumInfo getEnumInfo(String name, JsonSchemaDef schema) {
        Optional<String> maybeModelSubdir = schema.extensions().getString(EXT_MODEL_SUBDIR);

        EnumInfo enumInfo = new EnumInfo(name, schema.enums().toList())
            .withModelSubdir(maybeModelSubdir.orElse(null))
            .withModelSubpackage(maybeModelSubdir.map(this::dirPath2PackagePath).orElse(null));

        if (opts.addMpOpenApiAnnotations()) {
            enumInfo = enumInfo.withAddedAnnotation(getSchemaAnnotation(name, schema));
        }

        if (schema.isDeprecated()) {
            enumInfo = enumInfo.withAddedAnnotation(new AnnotationInfo("@Deprecated"));
        }

        return enumInfo;
    }

    private AnnotationInfo getSchemaAnnotation(String name, JsonSchemaDef pojo) {
        String description = pojo.description();

        List<String> schemaParams = new ArrayList<>();
        schemaParams.add("name = \"%s\"".formatted(modelName2SchemaName(name)));
        schemaParams.add("description = \"%s\"".formatted(normalizeDescription(description)));
        if (pojo.isDeprecated()) {
            schemaParams.add("deprecated = true");
        }

        return new AnnotationInfo(
            "@Schema(%s)".formatted(joinCsv(schemaParams)),
            "org.eclipse.microprofile.openapi.annotations.media.Schema"
        );
    }
}
