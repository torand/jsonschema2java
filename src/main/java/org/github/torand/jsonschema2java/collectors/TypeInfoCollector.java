package org.github.torand.jsonschema2java.collectors;

import org.github.torand.jsonschema2java.Options;
import org.github.torand.jsonschema2java.model.TypeInfo;
import org.github.torand.jsonschema2java.utils.JsonSchemaDef;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.github.torand.jsonschema2java.collectors.Extensions.EXT_JSON_SERIALIZER;
import static org.github.torand.jsonschema2java.collectors.Extensions.EXT_NULLABLE;
import static org.github.torand.jsonschema2java.collectors.Extensions.EXT_VALIDATION_CONSTRAINT;
import static org.github.torand.jsonschema2java.collectors.TypeInfoCollector.NullabilityResolution.FORCE_NOT_NULLABLE;
import static org.github.torand.jsonschema2java.collectors.TypeInfoCollector.NullabilityResolution.FORCE_NULLABLE;
import static org.github.torand.jsonschema2java.utils.Exceptions.illegalStateException;
import static org.github.torand.jsonschema2java.utils.StringHelper.nonBlank;

public class TypeInfoCollector extends BaseCollector {
    public enum NullabilityResolution {FROM_SCHEMA, FORCE_NULLABLE, FORCE_NOT_NULLABLE}

    private final SchemaResolver schemaResolver;

    public TypeInfoCollector(Options opts, SchemaResolver schemaResolver) {
        super(opts);
        this.schemaResolver = schemaResolver;
    }

    public TypeInfo getTypeInfo(JsonSchemaDef schema) {
        return getTypeInfo(schema, NullabilityResolution.FROM_SCHEMA);
    }

    public TypeInfo getTypeInfo(JsonSchemaDef schema, NullabilityResolution nullabilityResolution) {
        if (!schema.hasTypes()) {

            boolean nullable = isNullable(schema, nullabilityResolution);

            if (schema.hasAnyOf()) {
                throw new IllegalStateException("Schema 'anyOf' not supported");
            }

            if (schema.hasOneOf()) {
                // Limited support for 'oneOf' in properties: use the first non-nullable subschema
                JsonSchemaDef subSchema = getNonNullableSubSchema(schema.oneOf().toList())
                    .orElseThrow(illegalStateException("Schema 'oneOf' must contain a non-nullable sub-schema"));

                return getTypeInfo(subSchema, nullable ? FORCE_NULLABLE : FORCE_NOT_NULLABLE);
            }

            if (schema.allOf().count() == 1) {
                // 'allOf' only supported if it contains single type
                return getTypeInfo(schema.allOf().findFirst().get(), nullable ? FORCE_NULLABLE : FORCE_NOT_NULLABLE);
            }

            URI $ref = schema.$ref();
            if (nonNull($ref)) {
                TypeInfo typeInfo;

                if (schemaResolver.isPrimitiveType($ref)) {
                    JsonSchemaDef $refSchema = schemaResolver.getOrThrow($ref);
                    typeInfo = getTypeInfo($refSchema, nullable ? FORCE_NULLABLE : FORCE_NOT_NULLABLE);
                } else {
                    typeInfo = new TypeInfo();
                    typeInfo.nullable = nullable;

                    typeInfo.name = schemaResolver.getTypeName($ref) + opts.pojoNameSuffix;
                    String modelSubpackage = schemaResolver.getModelSubpackage($ref).orElse(null);
                    typeInfo.typeImports.add(opts.getModelPackage(modelSubpackage) + "." + typeInfo.name);
                    if (!schemaResolver.isEnumType(schema.$ref())) {
                        typeInfo.annotations.add("@Valid");
                        typeInfo.annotationImports.add("jakarta.validation.Valid");
                    }
                    if (!nullable) {
                        typeInfo.annotations.add("@NotNull");
                        typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
                    }
                }

                if (nonBlank(schema.description())) {
                    typeInfo.description = schema.description();
                }

                return typeInfo;
            } else if (schema.hasAllOf()) {
                throw new IllegalStateException("No types, no $ref: %s".formatted(schema.toString()));
            }
        }

        return getJsonType(schema, nullabilityResolution);
    }

    public <T> Optional<JsonSchemaDef> getNonNullableSubSchema(List<JsonSchemaDef> subSchemas) {
        return subSchemas.stream()
            .filter(subSchema -> !isNullable(subSchema))
            .findFirst();
    }

    private TypeInfo getJsonType(JsonSchemaDef schema, NullabilityResolution nullabilityResolution) {
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.description = schema.description();
        typeInfo.primitive = true;
        typeInfo.nullable = isNullable(schema, nullabilityResolution);

        String jsonType = schema.types()
            .filter(t -> !"null".equals(t))
            .findFirst()
            .orElseThrow(illegalStateException("Unexpected types: %s", schema.toString()));

        if ("string".equals(jsonType)) {
            populateJsonStringType(typeInfo, schema);
        } else if ("number".equals(jsonType)) {
            populateJsonNumberType(typeInfo, schema);
        } else if ("integer".equals(jsonType)) {
            populateJsonIntegerType(typeInfo, schema);
        } else if ("boolean".equals(jsonType)) {
            populateJsonBooleanType(typeInfo);
        } else if ("array".equals(jsonType)) {
            populateJsonArrayType(typeInfo, schema);
        } else if ("object".equals(jsonType) && schema.additionalProperties() instanceof JsonSchemaDef) {
            populateJsonMapType(typeInfo, schema);
        } else {
            // Schema not expected to be defined "inline" using type 'object'
            throw new IllegalStateException("Unexpected schema: %s".formatted(schema.toString()));
        }

        schema.extensions().getString(EXT_JSON_SERIALIZER)
            .ifPresent(jsonSerializer -> {
                typeInfo.annotations.add("@JsonSerialize(using = %s)".formatted(getJsonSerializerClass(jsonSerializer)));
                typeInfo.annotationImports.add("com.fasterxml.jackson.databind.annotation.JsonSerialize");
                typeInfo.annotationImports.add(jsonSerializer);
            });

        schema.extensions().getString(EXT_VALIDATION_CONSTRAINT)
            .ifPresent(validationConstraint -> {
                typeInfo.annotations.add("@%s".formatted(getClassNameFromFqn(validationConstraint)));
                typeInfo.annotationImports.add(validationConstraint);
            });

        return typeInfo;
    }

    private void populateJsonStringType(TypeInfo typeInfo, JsonSchemaDef schema) {
        if ("uri".equals(schema.format())) {
            typeInfo.name = "URI";
            typeInfo.schemaFormat = schema.format();
            typeInfo.typeImports.add("java.net.URI");
            if (!typeInfo.nullable) {
                typeInfo.annotations.add("@NotNull");
                typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
            }
        } else if ("uuid".equals(schema.format())) {
            typeInfo.name = "UUID";
            typeInfo.schemaFormat = schema.format();
            typeInfo.typeImports.add("java.util.UUID");
            if (!typeInfo.nullable) {
                typeInfo.annotations.add("@NotNull");
                typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
            }
        } else if ("duration".equals(schema.format())) {
            typeInfo.name = "Duration";
            typeInfo.schemaFormat = schema.format();
            typeInfo.typeImports.add("java.time.Duration");
            if (!typeInfo.nullable) {
                typeInfo.annotations.add("@NotNull");
                typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
            }
            typeInfo.annotations.add("@JsonFormat(pattern = \"yyyy-MM-dd\")");
            typeInfo.annotationImports.add("com.fasterxml.jackson.annotation.JsonFormat");
        } else if ("date".equals(schema.format())) {
            typeInfo.name = "LocalDate";
            typeInfo.schemaFormat = schema.format();
            typeInfo.typeImports.add("java.time.LocalDate");
            if (!typeInfo.nullable) {
                typeInfo.annotations.add("@NotNull");
                typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
            }
            typeInfo.annotations.add("@JsonFormat(pattern = \"yyyy-MM-dd\")");
            typeInfo.annotationImports.add("com.fasterxml.jackson.annotation.JsonFormat");
        } else if ("date-time".equals(schema.format())) {
            typeInfo.name = "LocalDateTime";
            typeInfo.schemaFormat = schema.format();
            typeInfo.typeImports.add("java.time.LocalDateTime");
            if (!typeInfo.nullable) {
                typeInfo.annotations.add("@NotNull");
                typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
            }
            typeInfo.annotations.add("@JsonFormat(pattern = \"yyyy-MM-dd'T'HH:mm:ss\")");
            typeInfo.annotationImports.add("com.fasterxml.jackson.annotation.JsonFormat");
        } else if ("email".equals(schema.format())) {
            typeInfo.name = "String";
            typeInfo.schemaFormat = schema.format();
            if (!typeInfo.nullable) {
                typeInfo.annotations.add("@NotBlank");
                typeInfo.annotationImports.add("jakarta.validation.constraints.NotBlank");
            }
            typeInfo.annotations.add("@Email");
            typeInfo.annotationImports.add("jakarta.validation.constraints.Email");
        } else if ("binary".equals(schema.format())) {
            typeInfo.name = "byte[]";
            typeInfo.schemaFormat = schema.format();
            if (!typeInfo.nullable) {
                typeInfo.annotations.add("@NotEmpty");
                typeInfo.annotationImports.add("jakarta.validation.constraints.NotEmpty");
            }
            if (nonNull(schema.minItems()) || nonNull(schema.maxItems())) {
                String sizeAnnotaion = getArraySizeAnnotation(schema, typeInfo.annotationImports);
                typeInfo.annotations.add(sizeAnnotaion);
            }
        } else {
            typeInfo.name = "String";
            typeInfo.schemaFormat = schema.format();
            if (!typeInfo.nullable) {
                typeInfo.annotations.add("@NotBlank");
                typeInfo.annotationImports.add("jakarta.validation.constraints.NotBlank");
            }
            if (nonBlank(schema.pattern())) {
                typeInfo.schemaPattern = schema.pattern();
                typeInfo.annotations.add("@Pattern(regexp = \"%s\")".formatted(schema.pattern()));
                typeInfo.annotationImports.add("jakarta.validation.constraints.Pattern");
            }
            if (nonNull(schema.minLength()) || nonNull(schema.maxLength())) {
                String sizeAnnotation = getStringSizeAnnotation(schema, typeInfo.annotationImports);
                typeInfo.annotations.add(sizeAnnotation);
            }
        }
    }

    private void populateJsonNumberType(TypeInfo typeInfo, JsonSchemaDef schema) {
        if ("double".equals(schema.format())) {
            typeInfo.name = "Double";
        } else if ("float".equals(schema.format())) {
            typeInfo.name = "Float";
        } else {
            typeInfo.name = "BigDecimal";
            typeInfo.typeImports.add("java.math.BigDecimal");
        }
        typeInfo.schemaFormat = schema.format();
        if (!typeInfo.nullable) {
            typeInfo.annotations.add("@NotNull");
            typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
        }
    }

    private void populateJsonIntegerType(TypeInfo typeInfo, JsonSchemaDef schema) {
        if ("int64".equals(schema.format())) {
            typeInfo.name = "Long";
        } else {
            typeInfo.name = "Integer";
        }
        typeInfo.schemaFormat = schema.format();
        if (!typeInfo.nullable && isNull(schema.minimum()) && isNull(schema.maximum())) {
            typeInfo.annotations.add("@NotNull");
            typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
        }
        if (nonNull(schema.minimum())) {
            typeInfo.annotations.add("@Min(%d)".formatted(schema.minimum().longValue()));
            typeInfo.annotationImports.add("jakarta.validation.constraints.Min");
        }
        if (nonNull(schema.maximum())) {
            typeInfo.annotations.add("@Max(%d)".formatted(schema.maximum().longValue()));
            typeInfo.annotationImports.add("jakarta.validation.constraints.Max");
        }
    }

    private void populateJsonBooleanType(TypeInfo typeInfo) {
        typeInfo.name = "Boolean";
        if (!typeInfo.nullable) {
            typeInfo.annotations.add("@NotNull");
            typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
        }
    }

    private void populateJsonArrayType(TypeInfo typeInfo, JsonSchemaDef schema) {
        typeInfo.primitive = false;
        typeInfo.name = "List";
        typeInfo.typeImports.add("java.util.List");
        typeInfo.annotations.add("@Valid");
        typeInfo.annotationImports.add("jakarta.validation.Valid");

        typeInfo.itemType = getTypeInfo(schema.items());
        typeInfo.itemType.annotations.clear();
        typeInfo.itemType.annotations.add("@NotNull");
        typeInfo.itemType.annotationImports.clear();
        typeInfo.itemType.annotationImports.add("jakarta.validation.constraints.NotNull");

        if (!typeInfo.nullable) {
            typeInfo.annotations.add("@NotNull");
            typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
        }
        if (nonNull(schema.minItems()) || nonNull(schema.maxItems())) {
            String sizeAnnotation = getArraySizeAnnotation(schema, typeInfo.annotationImports);
            typeInfo.annotations.add(sizeAnnotation);
        }
    }

    private void populateJsonMapType(TypeInfo typeInfo, JsonSchemaDef schema) {
        typeInfo.name = "Map";
        typeInfo.typeImports.add("java.util.Map");
        typeInfo.annotations.add("@Valid");
        typeInfo.annotationImports.add("jakarta.validation.Valid");

        typeInfo.keyType = new TypeInfo();
        typeInfo.keyType.name = "String";
        typeInfo.itemType = getTypeInfo((JsonSchemaDef)schema.additionalProperties());

        if (!typeInfo.nullable) {
            typeInfo.annotations.add("@NotNull");
            typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
        }
        if (nonNull(schema.minItems()) || nonNull(schema.maxItems())) {
            String sizeAnnotation = getArraySizeAnnotation(schema, typeInfo.annotationImports);
            typeInfo.annotations.add(sizeAnnotation);
        }
    }

    private String getClassNameFromFqn(String fqn) {
        int lastDotIdx = fqn.lastIndexOf(".");
        if (lastDotIdx == -1) {
            throw new IllegalStateException("Unexpected fully qualified class name: %s".formatted(fqn));
        }
        return fqn.substring(lastDotIdx+1);
    }

    private String getJsonSerializerClass(String jsonSerializerFqn) {
        String className = getClassNameFromFqn(jsonSerializerFqn);
        return opts.useKotlinSyntax ? className+"::class" : className+".class";
    }

    private boolean isNullable(JsonSchemaDef schema, NullabilityResolution resolution) {
        return switch(resolution) {
            case FROM_SCHEMA -> isNullable(schema);
            case FORCE_NULLABLE -> true;
            case FORCE_NOT_NULLABLE -> false;
        };
    }

    public boolean isNullable(JsonSchemaDef schema) {
        if (!schema.hasTypes()) {
            if (schema.hasAllOf()) {
                return schema.allOf().allMatch(this::isNullable);
            } else if (schema.hasOneOf()) {
                return schema.oneOf().anyMatch(this::isNullable);
            } else if (nonNull(schema.$ref())) {
                return isNullableByExtension(schema);
            } else {
                throw new IllegalStateException("No types, no $ref: %s".formatted(schema.toString()));
            }
        }

        return schema.hasType("null") || isNullableByExtension(schema);
    }

    private boolean isNullableByExtension(JsonSchemaDef schema) {
        return schema.extensions().getBoolean(EXT_NULLABLE).orElse(false);
    }

    private String getArraySizeAnnotation(JsonSchemaDef schema, List<String> imports) {
        List<String> sizeParams = new ArrayList<>();
        if (nonNull(schema.minItems())) {
            sizeParams.add("min = %d".formatted(schema.minItems()));
        }
        if (nonNull(schema.maxItems())) {
            sizeParams.add("max = %d".formatted(schema.maxItems()));
        }
        imports.add("jakarta.validation.constraints.Size");
        return "@Size(%s)".formatted(joinParams(sizeParams));
    }

    private String getStringSizeAnnotation(JsonSchemaDef schema, List<String> imports) {
        List<String> sizeParams = new ArrayList<>();
        if (nonNull(schema.minLength())) {
            sizeParams.add("min = %d".formatted(schema.minLength()));
        }
        if (nonNull(schema.maxLength())) {
            sizeParams.add("max = %d".formatted(schema.maxLength()));
        }
        imports.add("jakarta.validation.constraints.Size");
        return "@Size(%s)".formatted(joinParams(sizeParams));
    }
}
