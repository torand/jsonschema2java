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

import io.github.torand.jsonschema2java.generators.Options;
import io.github.torand.jsonschema2java.model.AnnotationInfo;
import io.github.torand.jsonschema2java.model.PropertyInfo;
import io.github.torand.jsonschema2java.model.TypeInfo;
import io.github.torand.jsonschema2java.utils.JsonSchemaDef;

import java.util.ArrayList;
import java.util.List;

import static io.github.torand.jsonschema2java.utils.StringUtils.joinCsv;
import static java.util.Objects.nonNull;

/**
 * Collects information about a property from a schema.
 */
public class PropertyInfoCollector extends BaseCollector {
    private final TypeInfoCollector typeInfoCollector;

    public PropertyInfoCollector(Options opts, SchemaResolver schemaResolver) {
        super(opts);
        this.typeInfoCollector = new TypeInfoCollector(opts, schemaResolver);
    }

    public PropertyInfo getPropertyInfo(String name, JsonSchemaDef propertyType, boolean required) {
        PropertyInfo propInfo = new PropertyInfo(name)
            .withRequired(required);

        var nullabilityResolution = required
            ? TypeInfoCollector.NullabilityResolution.FROM_SCHEMA
            : TypeInfoCollector.NullabilityResolution.FORCE_NULLABLE;
        propInfo = propInfo.withType(typeInfoCollector.getTypeInfo(propertyType, nullabilityResolution));

        if (opts.addMpOpenApiAnnotations()) {
            AnnotationInfo schemaAnnotation = getSchemaAnnotation(propertyType, propInfo.type());
            propInfo = propInfo.withAddedAnnotation(schemaAnnotation);
        }

        if (opts.addJsonPropertyAnnotations()) {
            AnnotationInfo jsonPropAnnotation = getJsonPropertyAnnotation(name);
            propInfo = propInfo.withAddedAnnotation(jsonPropAnnotation);
        }

        if (propertyType.isDeprecated()) {
            propInfo = propInfo.withDeprecationMessage(formatDeprecationMessage(propertyType.extensions()));
        }

        return propInfo;
    }

    private AnnotationInfo getSchemaAnnotation(JsonSchemaDef propertyType, TypeInfo typeInfo) {
        boolean required = !typeInfoCollector.isNullable(propertyType) && !typeInfo.nullable();

        List<String> schemaParams = new ArrayList<>();
        schemaParams.add("description = \"%s\"".formatted(normalizeDescription(propertyType.description())));
        if (required) {
            schemaParams.add("required = true");
        }
        if (nonNull(propertyType.defaultValue())) {
            schemaParams.add("defaultValue = \"%s\"".formatted(propertyType.defaultValue()));
        }
        if (nonNull(typeInfo.schemaFormat())) {
            schemaParams.add("format = \"%s\"".formatted(typeInfo.schemaFormat()));
        }
        if (nonNull(typeInfo.schemaPattern())) {
            schemaParams.add("pattern = \"%s\"".formatted(typeInfo.schemaPattern()));
        }
        if (propertyType.isDeprecated()) {
            schemaParams.add("deprecated = true");
        }

        return new AnnotationInfo(
            "@Schema(%s)".formatted(joinCsv(schemaParams)),
            "org.eclipse.microprofile.openapi.annotations.media.Schema"
        );
    }

    private AnnotationInfo getJsonPropertyAnnotation(String name) {
        return new AnnotationInfo(
            "@JsonProperty(\"%s\")".formatted(name),
            "com.fasterxml.jackson.annotation.JsonProperty"
        );
    }
}
