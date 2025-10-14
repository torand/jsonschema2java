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
import io.github.torand.jsonschema2java.model.PojoInfo;
import io.github.torand.jsonschema2java.model.PropertyInfo;
import io.github.torand.jsonschema2java.utils.JsonSchemaDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.torand.jsonschema2java.collectors.Extensions.EXT_MODEL_SUBDIR;
import static io.github.torand.jsonschema2java.utils.StringUtils.joinCsv;
import static java.util.Objects.nonNull;

/**
 * Collects information about a pojo from a schema.
 */
public class PojoInfoCollector extends BaseCollector {
    private final PropertyInfoCollector propertyInfoCollector;
    private final SchemaResolver schemaResolver;

    public PojoInfoCollector(Options opts, SchemaResolver schemaResolver) {
        super(opts);
        this.schemaResolver = schemaResolver;
        this.propertyInfoCollector = new PropertyInfoCollector(opts, schemaResolver);
    }

    public PojoInfo getPojoInfo(String name, JsonSchemaDef schema) {
        PojoInfo pojoInfo = new PojoInfo(name);

        Optional<String> maybeModelSubdir = schema.extensions().getString(EXT_MODEL_SUBDIR);
        pojoInfo = pojoInfo.withModelSubdir(maybeModelSubdir.orElse(null))
            .withModelSubpackage(maybeModelSubdir.map(this::dirPath2PackagePath).orElse(null));

        if (opts.addMpOpenApiAnnotations()) {
            pojoInfo = pojoInfo.withAddedAnnotation(getSchemaAnnotation(name, schema));
        }

        if (schema.isDeprecated()) {
            pojoInfo = pojoInfo.withDeprecationMessage(formatDeprecationMessage(schema.extensions()));
        }

        pojoInfo = pojoInfo.withAddedProperties(getSchemaProperties(schema));

        if (schema.additionalProperties() instanceof JsonSchemaDef) {
            throw new IllegalStateException("Schema-based 'additionalProperties' not supported for Pojos. Please specify this inside a property schema instead.");
        }

        return pojoInfo;
    }

    private AnnotationInfo getSchemaAnnotation(String name, JsonSchemaDef pojo) {
        List<String> schemaParams = new ArrayList<>();

        schemaParams.add("name = \"%s\"".formatted(modelName2SchemaName(name)));
        schemaParams.add("description = \"%s\"".formatted(normalizeDescription(pojo.description())));
        if (pojo.isDeprecated()) {
            schemaParams.add("deprecated = true");
        }

        return new AnnotationInfo(
            "@Schema(%s)".formatted(joinCsv(schemaParams)),
            "org.eclipse.microprofile.openapi.annotations.media.Schema"
        );
    }

    private List<PropertyInfo> getSchemaProperties(JsonSchemaDef schema) {
        List<PropertyInfo> props = new ArrayList<>();

        if (schema.hasAllOf()) {
            schema.allOf().forEach(subSchema -> props.addAll(getSchemaProperties(subSchema)));
        } else if (nonNull(schema.ref())) {
            JsonSchemaDef refSchema = schemaResolver.getOrThrow(schema.ref());
            return getSchemaProperties(refSchema);
        } else {
            schema.properties().forEach((propName, propSchema) ->
                props.add(propertyInfoCollector.getPropertyInfo(propName, propSchema, schema.isRequired(propName)))
            );
        }

        return props;
    }
}
