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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.emptyList;

/**
 * Defines basic information about an entity.
 */
public interface EntityInfo {

    /**
     * Gets the complete set of normal imports for this entity and its sub-entities.
     * @return the aggregated set of normal imports.
     */
    Set<String> aggregatedNormalImports();

    /**
     * Gets the complete set of static imports for this entity and its sub-entities.
     * @return the aggregated set of static imports.
     */
    Set<String> aggregatedStaticImports();

    /**
     * Gets the complete set of all imports for this entity and its sub-entities.
     * @return the aggregated set of all imports.
     */
    default Set<String> aggregatedImports() {
        Set<String> imports = new TreeSet<>();
        imports.addAll(aggregatedNormalImports());
        imports.addAll(aggregatedStaticImports());
        return imports;
    }

    /**
     * Gets the annotations decorating this entity.
     * @return the annotations decorating this entity.
     */
    default List<AnnotationInfo> annotations() {
        return emptyList();
    }

    /**
     * Gets the annotations decorating this entity, formatted as strings.
     * @return the annotation strings decorating this entity.
     */
    default List<String> annotationsAsStrings() {
        return annotations().stream().map(AnnotationInfo::annotation).toList();
    }
}
