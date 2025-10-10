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

import io.github.torand.javacommons.collection.CollectionHelper;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

/**
 * Describes a pojo.
 * @param name the class name.
 * @param modelSubdir the model subdirectory to place the pojo.
 * @param modelSubpackage the model subpackage of the pojo.
 * @param annotations the annotations decorating this pojo.
 * @param properties the properties of this pojo.
 * @param deprecationMessage the deprecation message, if any.
 */
public record PojoInfo (
    String name,
    String modelSubdir,
    String modelSubpackage,
    List<AnnotationInfo> annotations,
    List<PropertyInfo> properties,
    String deprecationMessage
) implements EntityInfo {

    /**
     * Constructs a {@link PojoInfo} object.
     * @param name the class name.
     */
    public PojoInfo(String name) {
        this(name, null, null, emptyList(), emptyList(), null);
    }

    /**
     * Returns a new {@link PojoInfo} object with specified model subdirectory.
     * @param modelSubdir the model subdirectory.
     * @return the new and updated {@link PojoInfo} object.
     */
    public PojoInfo withModelSubdir(String modelSubdir) {
        return new PojoInfo(name, modelSubdir, modelSubpackage, annotations, properties, deprecationMessage);
    }

    /**
     * Returns a new {@link PojoInfo} object with specified model subpackage.
     * @param modelSubpackage the model subpackage.
     * @return the new and updated {@link PojoInfo} object.
     */
    public PojoInfo withModelSubpackage(String modelSubpackage) {
        return new PojoInfo(name, modelSubpackage, modelSubpackage, annotations, properties, deprecationMessage);
    }

    /**
     * Returns a new {@link PojoInfo} object with specified annotation added.
     * @param annotation the annotation to add.
     * @return the new and updated {@link PojoInfo} object.
     */
    public PojoInfo withAddedAnnotation(AnnotationInfo annotation) {
        List<AnnotationInfo> newAnnotations = new LinkedList<>(annotations);
        newAnnotations.add(annotation);
        return new PojoInfo(name, modelSubpackage, modelSubpackage, newAnnotations, properties, deprecationMessage);
    }

    /**
     * Returns a new {@link PojoInfo} object with specified properties added.
     * @param properties the properties to add.
     * @return the new and updated {@link PojoInfo} object.
     */
    public PojoInfo withAddedProperties(Collection<PropertyInfo> properties) {
        List<PropertyInfo> newProperties = new LinkedList<>(this.properties);
        newProperties.addAll(properties);
        return new PojoInfo(name, modelSubpackage, modelSubpackage, annotations, newProperties, deprecationMessage);
    }

    /**
     * Returns a new {@link PojoInfo} object with specified deprecation message.
     * @param deprecationMessage the deprecation message.
     * @return the new and updated {@link PojoInfo} object.
     */
    public PojoInfo withDeprecationMessage(String deprecationMessage) {
        return new PojoInfo(name, modelSubpackage, modelSubpackage, annotations, properties, deprecationMessage);
    }

    /**
     * Gets whether pojo is deprecated.
     * @return true if pojo is deprecated; else false.
     */
    public boolean isDeprecated() {
        return nonNull(deprecationMessage);
    }

    /**
     * Gets whether pojo has no properties.
     * @return true if pojo has no properties; else false.
     */
    public boolean isEmpty() {
        return CollectionHelper.isEmpty(properties);
    }

    @Override
    public Set<String> aggregatedNormalImports() {
        Set<String> aggregated = new TreeSet<>();
        properties.stream().map(p -> p.aggregatedNormalImports()).forEach(aggregated::addAll);
        annotations.stream().map(a -> a.imports().normalImports()).forEach(aggregated::addAll);
        return aggregated;
    }

    @Override
    public Set<String> aggregatedStaticImports() {
        Set<String> aggregated = new TreeSet<>();
        properties.stream().map(p -> p.aggregatedStaticImports()).forEach(aggregated::addAll);
        annotations.stream().map(a -> a.imports().staticImports()).forEach(aggregated::addAll);
        return aggregated;
    }
}
