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
import java.util.List;
import java.util.stream.Stream;

import static io.github.torand.javacommons.collection.CollectionHelper.tailOf;
import static io.github.torand.javacommons.contract.Requires.requireNonEmpty;

/**
 * Describes a full type name including its bean validation annotations.
 */
public class AnnotatedTypeName {
    private final List<String> annotatedTypeNameItems;

    /**
     * Constructs an {@link AnnotatedTypeName} object.
     * Note! Any bean validation annotations on subtypes (key or item) of a compound type is expected
     * to be embedded into the type name.
     * @param annotatedTypeNameItems the annotation and type name items.
     */
    public AnnotatedTypeName(List<String> annotatedTypeNameItems) {
        requireNonEmpty(annotatedTypeNameItems, "No items specified");
        this.annotatedTypeNameItems = new ArrayList<>(annotatedTypeNameItems);
    }

    /**
     * Returns whether the main type has any bean validation annotations.
     * @return true if the main type has any bean validation annotations; else false.
     */
    public boolean hasAnnotations() {
        return annotatedTypeNameItems.size() >= 2;
    }

    /**
     * Gets the main type bean validation annotations.
     * @return the main type bean validation annotations.
     */
    public Stream<String> annotations() {
        if (!hasAnnotations()) {
            return Stream.empty();
        }
        return annotatedTypeNameItems.stream().limit(annotatedTypeNameItems.size()-1L);
    }

    /**
     * Gets the full type name including any bean validation annotations in subtypes.
     * @return the full type name.
     */
    public String typeName() {
        return tailOf(annotatedTypeNameItems);
    }

    /**
     * Gets the annotation and type name items.
     * @return the annotation and type name items.
     */
    public Stream<String> items() {
        return annotatedTypeNameItems.stream();
    }
}
