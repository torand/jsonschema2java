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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

/**
 * Describes a property.
 * @param name the property names.
 * @param type the property type.
 * @param required the required flag.
 * @param annotations the annotations decorating this pojo.
 * @param deprecationMessage the deprecation message, if any.
 */
public record PropertyInfo (
    String name,
    TypeInfo type,
    boolean required,
    List<AnnotationInfo> annotations,
    String deprecationMessage
) implements EntityInfo {

    /**
     * Constructs a {@link PropertyInfo} object.
     * @param name the property name.
     */
    public PropertyInfo(String name) {
        this(name, null, false, emptyList(), null);
    }

    /**
     * Returns a new {@link PropertyInfo} object with specified type.
     * @param type the property type.
     * @return the new and updated {@link PropertyInfo} object.
     */
    public PropertyInfo withType(TypeInfo type) {
        return new PropertyInfo(name, type, required, annotations, deprecationMessage);
    }

    /**
     * Returns a new {@link PropertyInfo} object with specified required flag.
     * @param required the required flag.
     * @return the new and updated {@link PropertyInfo} object.
     */
    public PropertyInfo withRequired(boolean required) {
        return new PropertyInfo(name, type, required, annotations, deprecationMessage);
    }

    /**
     * Returns a new {@link PropertyInfo} object with specified annotation added.
     * @param annotation the annotation to add.
     * @return the new and updated {@link PropertyInfo} object.
     */
    public PropertyInfo withAddedAnnotation(AnnotationInfo annotation) {
        List<AnnotationInfo> newAnnotations = new LinkedList<>(annotations);
        newAnnotations.add(annotation);
        return new PropertyInfo(name, type, required, newAnnotations, deprecationMessage);
    }

    /**
     * Returns a new {@link PropertyInfo} object with specified deprecation message.
     * @param deprecationMessage the deprecation message.
     * @return the new and updated {@link PropertyInfo} object.
     */
    public PropertyInfo withDeprecationMessage(String deprecationMessage) {
        return new PropertyInfo(name, type, required, annotations, deprecationMessage);
    }

    /**
     * Gets whether property is deprecated.
     * @return true if property is deprecated; else false.
     */
    public boolean isDeprecated() {
        return nonNull(deprecationMessage);
    }

    @Override
    public Set<String> aggregatedNormalImports() {
        Set<String> aggregated = new TreeSet<>(type.aggregatedNormalImports());
        annotations.stream().map(a -> a.imports().normalImports()).forEach(aggregated::addAll);
        return aggregated;
    }

    @Override
    public Set<String> aggregatedStaticImports() {
        Set<String> aggregated = new TreeSet<>(type.aggregatedStaticImports());
        annotations.stream().map(a -> a.imports().staticImports()).forEach(aggregated::addAll);
        return aggregated;
    }
}
