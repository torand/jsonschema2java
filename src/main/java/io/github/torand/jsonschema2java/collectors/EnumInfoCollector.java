package io.github.torand.jsonschema2java.collectors;

import io.github.torand.jsonschema2java.Options;
import io.github.torand.jsonschema2java.model.EnumInfo;
import io.github.torand.jsonschema2java.utils.JsonSchemaDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.torand.jsonschema2java.collectors.Extensions.EXT_MODEL_SUBDIR;

public class EnumInfoCollector extends BaseCollector {

    public EnumInfoCollector(Options opts) {
        super(opts);
    }

    public EnumInfo getEnumInfo(String name, JsonSchemaDef schema) {
        EnumInfo enumInfo = new EnumInfo();
        enumInfo.name = name;

        Optional<String> maybeModelSubdir = schema.extensions().getString(EXT_MODEL_SUBDIR);
        enumInfo.modelSubdir = maybeModelSubdir.orElse(null);
        enumInfo.modelSubpackage = maybeModelSubdir.map(this::dirPath2PackagePath).orElse(null);

        if (opts.addOpenApiSchemaAnnotations) {
            enumInfo.annotations.add(getSchemaAnnotation(name, schema, enumInfo.imports));
        }

        if (schema.isDeprecated()) {
            enumInfo.annotations.add("@Deprecated");
        }

        schema.enums().forEach(enumInfo.constants::add);

        return enumInfo;
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
}
