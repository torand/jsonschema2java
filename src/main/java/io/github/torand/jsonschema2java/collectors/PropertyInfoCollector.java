/*
 * Copyright (c) 2024 Tore Eide Andersen
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

import io.github.torand.jsonschema2java.Options;
import io.github.torand.jsonschema2java.model.PropertyInfo;
import io.github.torand.jsonschema2java.model.TypeInfo;
import io.github.torand.jsonschema2java.utils.JsonSchemaDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.github.torand.jsonschema2java.utils.StringHelper.nonBlank;
import static java.util.Objects.nonNull;

public class PropertyInfoCollector extends BaseCollector {
    private final TypeInfoCollector typeInfoCollector;

    public PropertyInfoCollector(Options opts, SchemaResolver schemaResolver) {
        super(opts);
        this.typeInfoCollector = new TypeInfoCollector(opts, schemaResolver);
    }

    public PropertyInfo getPropertyInfo(String name, JsonSchemaDef propertyType, boolean required) {
        PropertyInfo propInfo = new PropertyInfo();
        propInfo.name = name;
        propInfo.required = required;

        var nullabilityResolution = required
            ? TypeInfoCollector.NullabilityResolution.FROM_SCHEMA
            : TypeInfoCollector.NullabilityResolution.FORCE_NULLABLE;
        propInfo.type = typeInfoCollector.getTypeInfo(propertyType, nullabilityResolution);

        if (opts.addOpenApiSchemaAnnotations) {
            String schemaAnnotation = getSchemaAnnotation(propertyType, propInfo.type, propInfo.imports);
            propInfo.annotations.add(schemaAnnotation);
        }

        if (opts.addJsonPropertyAnnotations) {
            String jsonPropAnnotation = getJsonPropertyAnnotation(name, propInfo.imports);
            propInfo.annotations.add(jsonPropAnnotation);
        }

        if (propertyType.isDeprecated()) {
            propInfo.deprecationMessage = formatDeprecationMessage(propertyType.extensions());
        }

        return propInfo;
    }

    private String getSchemaAnnotation(JsonSchemaDef propertyType, TypeInfo typeInfo, Set<String> imports) {
        String description = propertyType.description();
        boolean required = !typeInfoCollector.isNullable(propertyType) && !typeInfo.nullable;

        imports.add("org.eclipse.microprofile.openapi.annotations.media.Schema");
        List<String> schemaParams = new ArrayList<>();
        schemaParams.add("description = \"%s\"".formatted(normalizeDescription(description)));
        if (required) {
            schemaParams.add("required = true");
        }
        if (nonBlank(propertyType.defaultValue())) {
            schemaParams.add("defaultValue = \"%s\"".formatted(propertyType.defaultValue()));
        }
        if (nonNull(typeInfo.schemaFormat)) {
            schemaParams.add("format = \"%s\"".formatted(typeInfo.schemaFormat));
        }
        if (nonNull(typeInfo.schemaPattern)) {
            schemaParams.add("pattern = \"%s\"".formatted(typeInfo.schemaPattern));
        }
        if (propertyType.isDeprecated()) {
            schemaParams.add("deprecated = true");
        }
        return "@Schema(%s)".formatted(joinParams(schemaParams));
    }

    private String getJsonPropertyAnnotation(String name, Set<String> imports) {
        imports.add("com.fasterxml.jackson.annotation.JsonProperty");
        return "@JsonProperty(\"%s\")".formatted(name);
    }
}
