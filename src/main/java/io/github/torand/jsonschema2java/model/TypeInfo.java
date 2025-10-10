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
package io.github.torand.jsonschema2java.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static io.github.torand.javacommons.stream.StreamHelper.streamSafely;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

/**
 * Describes a type.
 * @param name the type name.
 * @param description the type description.
 * @param nullable the nullable flag.
 * @param keyType the key type, if this is a map type.
 * @param primitive the primitive flag.
 * @param itemType the item type, if this is an array type or map type.
 * @param schemaFormat the OpenAPI schema format.
 * @param schemaPattern the OpenAPI schema pattern.
 * @param annotations the annotations decorating this type.
 * @param imports the imports required by the type.
 */
public record TypeInfo (
    String name,
    String description,
    boolean nullable,
    TypeInfo keyType,
    boolean primitive,
    TypeInfo itemType,
    String schemaFormat,
    String schemaPattern,
    List<AnnotationInfo> annotations,
    ImportInfo imports
) implements EntityInfo, ImportsSupplier {

    /**
     * Constructs an {@link TypeInfo} object.
     */
    public TypeInfo() {
        this(null, null, false, null, false, null, null, null, emptyList(), ImportInfo.empty());
    }

    /**
     * Returns a new {@link TypeInfo} object with specified name.
     * @param name the name.
     * @return the new and updated {@link TypeInfo} object.
     */
    public TypeInfo withName(String name) {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, annotations, imports);
    }

    /**
     * Returns a new {@link TypeInfo} object with specified description.
     * @param description the description.
     * @return the new and updated {@link TypeInfo} object.
     */
    public TypeInfo withDescription(String description) {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, annotations, imports);
    }

    /**
     * Returns a new {@link TypeInfo} object with specified nullable flag.
     * @param nullable the nullable flag.
     * @return the new and updated {@link TypeInfo} object.
     */
    public TypeInfo withNullable(boolean nullable) {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, annotations, imports);
    }

    /**
     * Returns a new {@link TypeInfo} object with specified key type.
     * @param keyType the key type.
     * @return the new and updated {@link TypeInfo} object.
     */
    public TypeInfo withKeyType(TypeInfo keyType) {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, annotations, imports);
    }

    /**
     * Returns a new {@link TypeInfo} object with specified primitive flag.
     * @param primitive the primitive flag.
     * @return the new and updated {@link TypeInfo} object.
     */
    public TypeInfo withPrimitive(boolean primitive) {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, annotations, imports);
    }

    /**
     * Returns a new {@link TypeInfo} object with specified item type.
     * @param itemType the item type.
     * @return the new and updated {@link TypeInfo} object.
     */
    public TypeInfo withItemType(TypeInfo itemType) {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, annotations, imports);
    }

    /**
     * Returns a new {@link TypeInfo} object with specified OpenAPI schema format.
     * @param schemaFormat the OpenAPI schema format.
     * @return the new and updated {@link TypeInfo} object.
     */
    public TypeInfo withSchemaFormat(String schemaFormat) {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, annotations, imports);
    }

    /**
     * Returns a new {@link TypeInfo} object with specified OpenAPI schema pattern.
     * @param schemaPattern the OpenAPI schema pattern.
     * @return the new and updated {@link TypeInfo} object.
     */
    public TypeInfo withSchemaPattern(String schemaPattern) {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, annotations, imports);
    }

    /**
     * Returns a new {@link TypeInfo} object with specified annotation added.
     * @param annotation the annotation to add.
     * @return the new and updated {@link TypeInfo} object.
     */
    public TypeInfo withAddedAnnotation(AnnotationInfo annotation) {
        List<AnnotationInfo> newAnnotations = new LinkedList<>(annotations);
        newAnnotations.add(annotation);
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, newAnnotations, imports);
    }

    /**
     * Returns a new {@link PojoInfo} object with no annotations.
     * @return the new and updated {@link PojoInfo} object.
     */
    public TypeInfo withNoAnnotations() {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, emptyList(), imports);
    }

    /**
     * Returns a new {@link TypeInfo} object with specified normal import added.
     * @param normalImport the import to add.
     * @return the new and updated {@link TypeInfo} object.
     */
    public TypeInfo withAddedNormalImport(String normalImport) {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, annotations, imports.withAddedNormalImport(normalImport));
    }

    /**
     * Gets whether this is an array type.
     * @return true if this is an array type; else false.
     */
    public boolean isArray() {
        return isNull(keyType) && nonNull(itemType);
    }

    /**
     * Gets the full name of Java/Kotlin type including generic composites.
     * @return the full name of Java/Kotlin type
     */
    public String getFullName() {
        if (nonNull(keyType) && nonNull(itemType)) {
            return "%s<%s,%s>".formatted(name, keyType.getFullName(), itemType.getFullName());
        } else if (nonNull(itemType)) {
            return "%s<%s>".formatted(name, itemType.getFullName());
        } else {
            return name;
        }
    }

    /**
     * Gets full type name, including bean validation annotations.
     * @return the annotations and type name items.
     */
    public AnnotatedTypeName getAnnotatedFullName() {
        List<String> annotatedFullName = new ArrayList<>();
        streamSafely(annotations)
            .map(AnnotationInfo::annotation)
            .forEach(annotatedFullName::add);

        if (nonNull(itemType)) {
            String itemTypeWithAnnotations = itemType.getAnnotatedFullName().items()
                .filter(not("@Valid"::equals))
                .collect(joining(" "));

            if (nonNull(keyType)) {
                String keyTypeWithAnnotations = keyType.getAnnotatedFullName().items()
                    .filter(not("@Valid"::equals))
                    .collect(joining(" "));

                annotatedFullName.add("%s<%s, %s>".formatted(name, keyTypeWithAnnotations, itemTypeWithAnnotations));
            } else {
                annotatedFullName.add("%s<%s>".formatted(name, itemTypeWithAnnotations));
            }
        } else {
            annotatedFullName.add(name);
        }

        return new AnnotatedTypeName(annotatedFullName);
    }

    @Override
    public Set<String> aggregatedNormalImports() {
        ImportInfo imports = this.imports.withAddedImports(annotations);
        if (nonNull(keyType)) {
            imports = imports.withAddedImports(keyType);
        }
        if (nonNull(itemType)) {
            imports = imports.withAddedImports(itemType);
        }

        return imports.normalImports();
    }

    @Override
    public Set<String> aggregatedStaticImports() {
        ImportInfo imports = this.imports.withAddedImports(annotations);
        if (nonNull(keyType)) {
            imports = imports.withAddedImports(keyType);
        }
        if (nonNull(itemType)) {
            imports = imports.withAddedImports(itemType);
        }

        return imports.staticImports();
    }
}