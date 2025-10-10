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
import io.github.torand.jsonschema2java.model.TypeInfo;
import io.github.torand.jsonschema2java.utils.JsonSchemaDef;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.torand.javacommons.lang.Exceptions.illegalStateException;
import static io.github.torand.javacommons.lang.StringHelper.nonBlank;
import static io.github.torand.jsonschema2java.collectors.Extensions.*;
import static io.github.torand.jsonschema2java.collectors.TypeInfoCollector.NullabilityResolution.FORCE_NOT_NULLABLE;
import static io.github.torand.jsonschema2java.collectors.TypeInfoCollector.NullabilityResolution.FORCE_NULLABLE;
import static io.github.torand.jsonschema2java.utils.StringUtils.getClassNameFromFqn;
import static io.github.torand.jsonschema2java.utils.StringUtils.joinCsv;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;

/**
 * Collects information about a type from a schema.
 */
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
                JsonSchemaDef firstSchema = schema.allOf().findFirst().orElseThrow();
                return getTypeInfo(firstSchema, nullable ? FORCE_NULLABLE : FORCE_NOT_NULLABLE);
            }

            URI ref = schema.ref();
            if (nonNull(ref)) {
                TypeInfo typeInfo;

                if (schemaResolver.isPrimitiveType(ref)) {
                    JsonSchemaDef refSchema = schemaResolver.getOrThrow(ref);
                    typeInfo = getTypeInfo(refSchema, nullable ? FORCE_NULLABLE : FORCE_NOT_NULLABLE);
                } else {
                    typeInfo = new TypeInfo()
                        .withName(SchemaResolver.getTypeName(ref) + opts.pojoNameSuffix())
                        .withNullable(nullable);

                    String modelSubpackage = schemaResolver.getModelSubpackage(ref).orElse(null);
                    typeInfo = typeInfo.withAddedNormalImport(opts.getModelPackage(modelSubpackage) + "." + typeInfo.name());

                    if (!schemaResolver.isEnumType(schema.ref())) {
                        AnnotationInfo validAnnotation = getValidAnnotation();
                        typeInfo = typeInfo.withAddedAnnotation(validAnnotation);
                    }
                    if (!nullable) {
                        AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                        typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
                    }
                }

                if (nonBlank(schema.description())) {
                    typeInfo = typeInfo.withDescription(schema.description());
                }

                return typeInfo;
            } else if (schema.hasAllOf()) {
                throw new IllegalStateException("No types, no $ref: %s".formatted(schema.toString()));
            }
        }

        return getJsonType(schema, nullabilityResolution);
    }

    public Optional<JsonSchemaDef> getNonNullableSubSchema(List<JsonSchemaDef> subSchemas) {
        return subSchemas.stream()
            .filter(not(this::isNullable))
            .findFirst();
    }

    private TypeInfo getJsonType(JsonSchemaDef schema, NullabilityResolution nullabilityResolution) {
        TypeInfo typeInfo = new TypeInfo()
            .withDescription(schema.description())
            .withPrimitive(true)
            .withNullable(isNullable(schema, nullabilityResolution));

        String jsonType = schema.types()
            .filter(not("null"::equals))
            .findFirst()
            .orElseThrow(illegalStateException("Unexpected types: %s", schema.toString()));

        if ("string".equals(jsonType)) {
            typeInfo = populateJsonStringType(typeInfo, schema);
        } else if ("number".equals(jsonType)) {
            typeInfo = populateJsonNumberType(typeInfo, schema);
        } else if ("integer".equals(jsonType)) {
            typeInfo = populateJsonIntegerType(typeInfo, schema);
        } else if ("boolean".equals(jsonType)) {
            typeInfo = populateJsonBooleanType(typeInfo);
        } else if ("array".equals(jsonType)) {
            typeInfo = populateJsonArrayType(typeInfo, schema);
        } else if ("object".equals(jsonType) && schema.additionalProperties() instanceof JsonSchemaDef) {
            typeInfo = populateJsonMapType(typeInfo, schema);
        } else {
            // Schema not expected to be defined "inline" using type 'object'
            throw new IllegalStateException("Unexpected schema: %s".formatted(schema.toString()));
        }

        Optional<String> maybeJsonSerializer = schema.extensions().getString(EXT_JSON_SERIALIZER);
        if (maybeJsonSerializer.isPresent()) {
            AnnotationInfo jsonSerializeAnnotation = getJsonSerializeAnnotation(maybeJsonSerializer.get());
            typeInfo = typeInfo.withAddedAnnotation(jsonSerializeAnnotation);
        }

        Optional<String> maybeValidationConstraint = schema.extensions().getString(EXT_VALIDATION_CONSTRAINT);
        if (maybeValidationConstraint.isPresent()) {
            AnnotationInfo validationConstraintAnnotation = new AnnotationInfo(
                "@%s".formatted(getClassNameFromFqn(maybeValidationConstraint.get())),
                maybeValidationConstraint.get()
            );
            typeInfo = typeInfo.withAddedAnnotation(validationConstraintAnnotation);
        }

        return typeInfo;
    }

    private TypeInfo populateJsonStringType(TypeInfo typeInfo, JsonSchemaDef schema) {
        if ("uri".equals(schema.format())) {
            typeInfo = typeInfo.withName("URI")
                .withSchemaFormat(schema.format())
                .withAddedNormalImport("java.net.URI");
            if (!typeInfo.nullable() && opts.addJakartaBeanValidationAnnotations()) {
                AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
            }
        } else if ("uuid".equals(schema.format())) {
            typeInfo = typeInfo.withName("UUID")
                .withSchemaFormat(schema.format())
                .withAddedNormalImport("java.util.UUID");
            if (!typeInfo.nullable() && opts.addJakartaBeanValidationAnnotations()) {
                AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
            }
        } else if ("duration".equals(schema.format())) {
            typeInfo = typeInfo.withName("Duration")
                .withSchemaFormat(schema.format())
                .withAddedNormalImport("java.time.Duration");
            if (!typeInfo.nullable() && opts.addJakartaBeanValidationAnnotations()) {
                AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
            }
        } else if ("date".equals(schema.format())) {
            typeInfo = typeInfo.withName("LocalDate")
                .withSchemaFormat(schema.format())
                .withAddedNormalImport("java.time.LocalDate");
            if (!typeInfo.nullable() && opts.addJakartaBeanValidationAnnotations()) {
                AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
            }
            AnnotationInfo jsonFormatAnnotation = getJsonFormatAnnotation("yyyy-MM-dd");
            typeInfo = typeInfo.withAddedAnnotation(jsonFormatAnnotation);
        } else if ("date-time".equals(schema.format())) {
            typeInfo = typeInfo.withName("LocalDateTime")
                .withSchemaFormat(schema.format())
                .withAddedNormalImport("java.time.LocalDateTime");
            if (!typeInfo.nullable() && opts.addJakartaBeanValidationAnnotations()) {
                AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
            }
            AnnotationInfo jsonFormatAnnotation = getJsonFormatAnnotation("yyyy-MM-dd'T'HH:mm:ss");
            typeInfo = typeInfo.withAddedAnnotation(jsonFormatAnnotation);
        } else if ("email".equals(schema.format())) {
            typeInfo = typeInfo.withName("String")
                .withSchemaFormat(schema.format());
            if (opts.addJakartaBeanValidationAnnotations()) {
                if (!typeInfo.nullable()) {
                    AnnotationInfo notBlankAnnotation = getNotBlankAnnotation();
                    typeInfo = typeInfo.withAddedAnnotation(notBlankAnnotation);
                }
                AnnotationInfo emailAnnotation = getEmailAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(emailAnnotation);
            }
        } else if ("binary".equals(schema.format())) {
            typeInfo = typeInfo.withName("byte[]")
                .withSchemaFormat(schema.format());
            if (opts.addJakartaBeanValidationAnnotations()) {
                if (!typeInfo.nullable()) {
                    AnnotationInfo notEmptyAnnotation = getNotEmptyAnnotation();
                    typeInfo = typeInfo.withAddedAnnotation(notEmptyAnnotation);
                }
                if (nonNull(schema.minItems()) || nonNull(schema.maxItems())) {
                    AnnotationInfo sizeAnnotaion = getArraySizeAnnotation(schema);
                    typeInfo = typeInfo.withAddedAnnotation(sizeAnnotaion);
                }
            }
        } else {
            typeInfo = typeInfo.withName("String")
                .withSchemaFormat(schema.format());
            if (opts.addJakartaBeanValidationAnnotations()) {
                if (!typeInfo.nullable()) {
                    AnnotationInfo notBlankAnnotation = getNotBlankAnnotation();
                    typeInfo = typeInfo.withAddedAnnotation(notBlankAnnotation);
                }
                if (nonBlank(schema.pattern())) {
                    typeInfo = typeInfo.withSchemaPattern(schema.pattern())
                        .withAddedAnnotation(getPatternAnnotation(schema));
                }
                if (nonNull(schema.minLength()) || nonNull(schema.maxLength())) {
                    AnnotationInfo sizeAnnotation = getStringSizeAnnotation(schema);
                    typeInfo = typeInfo.withAddedAnnotation(sizeAnnotation);
                }
            }
        }

        return typeInfo;
    }

    private TypeInfo populateJsonNumberType(TypeInfo typeInfo, JsonSchemaDef schema) {
        if ("double".equals(schema.format())) {
            typeInfo = typeInfo.withName("Double");
        } else if ("float".equals(schema.format())) {
            typeInfo = typeInfo.withName("Float");
        } else {
            typeInfo = typeInfo.withName("BigDecimal")
                .withAddedNormalImport("java.math.BigDecimal");
        }
        typeInfo = typeInfo.withSchemaFormat(schema.format());
        if (opts.addJakartaBeanValidationAnnotations()) {
            if (!typeInfo.nullable()) {
                AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
            }
            if ("BigDecimal".equals(typeInfo.name())) {
                if (nonNull(schema.minimum())) {
                    AnnotationInfo minAnnotation = getMinAnnotation(schema);
                    typeInfo = typeInfo.withAddedAnnotation(minAnnotation);
                }
                if (nonNull(schema.maximum())) {
                    AnnotationInfo maxAnnotation = getMaxAnnotation(schema);
                    typeInfo = typeInfo.withAddedAnnotation(maxAnnotation);
                }
            }
        }

        return typeInfo;
    }

    private TypeInfo populateJsonIntegerType(TypeInfo typeInfo, JsonSchemaDef schema) {
        typeInfo = typeInfo.withName("int64".equals(schema.format()) ? "Long" :"Integer")
            .withSchemaFormat(schema.format());

        if (opts.addJakartaBeanValidationAnnotations()) {
            if (!typeInfo.nullable()) {
                AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
            }
            if (nonNull(schema.minimum())) {
                AnnotationInfo minAnnotation = getMinAnnotation(schema);
                typeInfo = typeInfo.withAddedAnnotation(minAnnotation);
            }
            if (nonNull(schema.maximum())) {
                AnnotationInfo maxAnnotation = getMaxAnnotation(schema);
                typeInfo = typeInfo.withAddedAnnotation(maxAnnotation);
            }
        }

        return typeInfo;
    }

    private TypeInfo populateJsonBooleanType(TypeInfo typeInfo) {
        typeInfo = typeInfo.withName("Boolean");
        if (!typeInfo.nullable() && opts.addJakartaBeanValidationAnnotations()) {
            AnnotationInfo notNullAnnotation = getNotNullAnnotation();
            typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
        }

        return typeInfo;
    }

    private TypeInfo populateJsonArrayType(TypeInfo typeInfo, JsonSchemaDef schema) {
        typeInfo = typeInfo.withPrimitive(false);
        if (TRUE.equals(schema.uniqueItems())) {
            typeInfo = typeInfo.withName("Set")
                .withAddedNormalImport("java.util.Set");
        } else {
            typeInfo = typeInfo.withName("List")
                .withAddedNormalImport("java.util.List");
        }

        if (opts.addJakartaBeanValidationAnnotations()) {
            AnnotationInfo validAnnotation = getValidAnnotation();
            typeInfo = typeInfo.withAddedAnnotation(validAnnotation);
        }

        TypeInfo itemType = getTypeInfo(schema.items());

        if (opts.addJakartaBeanValidationAnnotations()) {
            if (!typeInfo.nullable()) {
                AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
            }
            if (nonNull(schema.minItems()) || nonNull(schema.maxItems())) {
                AnnotationInfo sizeAnnotation = getArraySizeAnnotation(schema);
                typeInfo = typeInfo.withAddedAnnotation(sizeAnnotation);
            }
        }

        return typeInfo.withItemType(itemType);
    }

    private TypeInfo populateJsonMapType(TypeInfo typeInfo, JsonSchemaDef schema) {
        typeInfo = typeInfo.withName("Map")
            .withAddedNormalImport("java.util.Map");

        if (opts.addJakartaBeanValidationAnnotations()) {
            AnnotationInfo validAnnotation = getValidAnnotation();
            typeInfo = typeInfo.withAddedAnnotation(validAnnotation);
        }

        TypeInfo keyTypeInfo = new TypeInfo().withName("String");
        if (opts.addJakartaBeanValidationAnnotations()) {
            AnnotationInfo notBlankAnnotation = getNotBlankAnnotation();
            keyTypeInfo = keyTypeInfo.withAddedAnnotation(notBlankAnnotation);
        }

        typeInfo = typeInfo.withKeyType(keyTypeInfo)
            .withItemType(getTypeInfo((JsonSchemaDef)schema.additionalProperties()));

        if (opts.addJakartaBeanValidationAnnotations()) {
            if (!typeInfo.nullable()) {
                AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
            }
            if (nonNull(schema.minItems()) || nonNull(schema.maxItems())) {
                AnnotationInfo sizeAnnotation = getArraySizeAnnotation(schema);
                typeInfo = typeInfo.withAddedAnnotation(sizeAnnotation);
            }
        }

        return typeInfo;
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
            } else if (nonNull(schema.ref())) {
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

    private AnnotationInfo getJsonSerializeAnnotation(String jsonSerializer) {
        return new AnnotationInfo("@JsonSerialize(using = %s)".formatted(getJsonSerializerClass(jsonSerializer)))
            .withAddedNormalImport("com.fasterxml.jackson.databind.annotation.JsonSerialize")
            .withAddedNormalImport(jsonSerializer);
    }

    private AnnotationInfo getJsonFormatAnnotation(String pattern) {
        return new AnnotationInfo(
            "@JsonFormat(pattern = \"%s\")".formatted(pattern),
            "com.fasterxml.jackson.annotation.JsonFormat");
    }

    private AnnotationInfo getValidAnnotation() {
        return new AnnotationInfo(
            "@Valid",
            "jakarta.validation.Valid");
    }

    private AnnotationInfo getNotNullAnnotation() {
        return new AnnotationInfo(
            "@NotNull",
            "jakarta.validation.constraints.NotNull");
    }

    private AnnotationInfo getNotBlankAnnotation() {
        return new AnnotationInfo(
            "@NotBlank",
            "jakarta.validation.constraints.NotBlank");
    }

    private AnnotationInfo getNotEmptyAnnotation() {
        return new AnnotationInfo(
            "@NotEmpty",
            "jakarta.validation.constraints.NotEmpty");
    }

    private AnnotationInfo getMinAnnotation(JsonSchemaDef schema) {
        return new AnnotationInfo(
            "@Min(%d)".formatted(schema.minimum().longValue()),
            "jakarta.validation.constraints.Min");
    }

    private AnnotationInfo getMaxAnnotation(JsonSchemaDef schema) {
        return new AnnotationInfo(
            "@Max(%d)".formatted(schema.maximum().longValue()),
            "jakarta.validation.constraints.Max");
    }

    private AnnotationInfo getPatternAnnotation(JsonSchemaDef schema) {
        return new AnnotationInfo(
            "@Pattern(regexp = \"%s\")".formatted(schema.pattern()),
            "jakarta.validation.constraints.Pattern");
    }

    private AnnotationInfo getEmailAnnotation() {
        return new AnnotationInfo(
            "@Email",
            "jakarta.validation.constraints.Email");
    }

    private AnnotationInfo getArraySizeAnnotation(JsonSchemaDef schema) {
        List<String> sizeParams = new ArrayList<>();
        if (nonNull(schema.minItems())) {
            sizeParams.add("min = %d".formatted(schema.minItems()));
        }
        if (nonNull(schema.maxItems())) {
            sizeParams.add("max = %d".formatted(schema.maxItems()));
        }
        return new AnnotationInfo(
            "@Size(%s)".formatted(joinCsv(sizeParams)),
            "jakarta.validation.constraints.Size");
    }

    private AnnotationInfo getStringSizeAnnotation(JsonSchemaDef schema) {
        List<String> sizeParams = new ArrayList<>();
        if (nonNull(schema.minLength())) {
            sizeParams.add("min = %d".formatted(schema.minLength()));
        }
        if (nonNull(schema.maxLength())) {
            sizeParams.add("max = %d".formatted(schema.maxLength()));
        }
        return new AnnotationInfo(
            "@Size(%s)".formatted(joinCsv(sizeParams)),
            "jakarta.validation.constraints.Size");
    }

    private String getJsonSerializerClass(String jsonSerializerFqn) {
        String className = getClassNameFromFqn(jsonSerializerFqn);
        return formatClassRef(className);
    }
}
