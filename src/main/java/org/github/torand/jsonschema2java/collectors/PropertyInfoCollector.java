package org.github.torand.jsonschema2java.collectors;

import org.github.torand.jsonschema2java.Options;
import org.github.torand.jsonschema2java.model.PropertyInfo;
import org.github.torand.jsonschema2java.model.TypeInfo;
import org.github.torand.jsonschema2java.utils.JsonSchemaDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Objects.nonNull;
import static org.github.torand.jsonschema2java.collectors.Extensions.EXT_DEPRECATION_MESSAGE;
import static org.github.torand.jsonschema2java.utils.StringHelper.nonBlank;

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

        if (opts.useOpenApiSchemaAnnotations) {
            String schemaAnnotation = getSchemaAnnotation(propertyType, propInfo.type, propInfo.imports);
            propInfo.annotations.add(schemaAnnotation);
        }

        if (opts.useJsonPropertyAnnotations) {
            String jsonPropAnnotation = getJsonPropertyAnnotation(name, propInfo.imports);
            propInfo.annotations.add(jsonPropAnnotation);
        }

        if (propertyType.isDeprecated()) {
            propInfo.deprecationMessage = propertyType.extensions().getString(EXT_DEPRECATION_MESSAGE).orElse("Deprecated");
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
