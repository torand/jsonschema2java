package org.github.torand.jsonschema2java.collectors;

import org.github.torand.jsonschema2java.Options;
import org.github.torand.jsonschema2java.model.PojoInfo;
import org.github.torand.jsonschema2java.model.PropertyInfo;
import org.github.torand.jsonschema2java.utils.JsonSchemaDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.nonNull;
import static org.github.torand.jsonschema2java.collectors.Extensions.EXT_DEPRECATION_MESSAGE;
import static org.github.torand.jsonschema2java.collectors.Extensions.EXT_MODEL_SUBDIR;

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

        if (opts.useOpenApiSchemaAnnotations) {
            pojoInfo.annotations.add(getSchemaAnnotation(name, schema, pojoInfo.imports));
        }

        if (schema.isDeprecated()) {
            pojoInfo.deprecationMessage = schema.extensions().getString(EXT_DEPRECATION_MESSAGE).orElse("Deprecated");
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
