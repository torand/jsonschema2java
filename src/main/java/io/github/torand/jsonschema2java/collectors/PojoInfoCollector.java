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
import io.github.torand.jsonschema2java.model.PojoInfo;
import io.github.torand.jsonschema2java.model.PropertyInfo;
import io.github.torand.jsonschema2java.utils.JsonSchemaDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.torand.jsonschema2java.collectors.Extensions.EXT_MODEL_SUBDIR;
import static java.util.Objects.nonNull;

public class PojoInfoCollector extends BaseCollector {
    private final PropertyInfoCollector propertyInfoCollector;
    private final SchemaResolver schemaResolver;

    public PojoInfoCollector(Options opts, SchemaResolver schemaResolver) {
        super(opts);
        this.schemaResolver = schemaResolver;
        this.propertyInfoCollector = new PropertyInfoCollector(opts, schemaResolver);
    }

    public PojoInfo getPojoInfo(String name, JsonSchemaDef schema) {
        PojoInfo pojoInfo = new PojoInfo();
        pojoInfo.name = name;

        Optional<String> maybeModelSubdir = schema.extensions().getString(EXT_MODEL_SUBDIR);
        pojoInfo.modelSubdir = maybeModelSubdir.orElse(null);
        pojoInfo.modelSubpackage = maybeModelSubdir.map(this::dirPath2PackagePath).orElse(null);

        if (opts.addOpenApiSchemaAnnotations) {
            pojoInfo.annotations.add(getSchemaAnnotation(name, schema, pojoInfo.imports));
        }

        if (schema.isDeprecated()) {
            pojoInfo.deprecationMessage = formatDeprecationMessage(schema.extensions());
        }

        pojoInfo.properties = getSchemaProperties(schema);

        return pojoInfo;
    }

    private String getSchemaAnnotation(String name, JsonSchemaDef pojo, Set<String> imports) {
        String description = pojo.description();

        imports.add("org.eclipse.microprofile.openapi.annotations.media.Schema");
        List<String> schemaParams = new ArrayList<>();
        schemaParams.add("name = \"%s\"".formatted(modelName2SchemaName(name)));
        schemaParams.add("description = \"%s\"".formatted(normalizeDescription(description)));
        if (pojo.isDeprecated()) {
            schemaParams.add("deprecated = true");
        }
        return "@Schema(%s)".formatted(joinParams(schemaParams));
    }

    private List<PropertyInfo> getSchemaProperties(JsonSchemaDef schema) {
        List<PropertyInfo> props = new ArrayList<>();

        if (schema.hasAllOf()) {
            schema.allOf().forEach(subSchema -> props.addAll(getSchemaProperties(subSchema)));
        } else if (nonNull(schema.$ref())) {
            JsonSchemaDef $refSchema = schemaResolver.getOrThrow(schema.$ref());
            return getSchemaProperties($refSchema);
        } else {
            schema.properties().forEach((propName, propSchema) ->
                props.add(propertyInfoCollector.getPropertyInfo(propName, propSchema, schema.isRequired(propName)))
            );
        }

        return props;
    }
}
