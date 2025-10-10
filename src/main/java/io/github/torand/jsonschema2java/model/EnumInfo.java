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

import static java.util.Collections.emptyList;

/**
 * Describes an enum class.
 * @param name the name of the enum class.
 * @param constants the constant values for this enum class.
 * @param modelSubdir the custom subdirectory to place this enum class definition, if any.
 * @param modelSubpackage the custom subpackage to place this enum class definition, if any.
 * @param annotations the annotations decorating this enum class.
 */
public record EnumInfo (
    String name,
    List<String> constants,
    String modelSubdir,
    String modelSubpackage,
    List<AnnotationInfo> annotations
) implements EntityInfo {
    /**
     * Constructs an {@link EnumInfo} object.
     * @param name the enum name.
     * @param constants the enum constants.
     */
    public EnumInfo(String name, List<String> constants) {
        this(name, constants, null, null, emptyList());
    }

    /**
     * Returns a new {@link EnumInfo} object with specified model subdirectory.
     * @param modelSubdir the model subdirectory.
     * @return the new and updated {@link EnumInfo} object.
     */
    public EnumInfo withModelSubdir(String modelSubdir) {
        return new EnumInfo(name, constants, modelSubdir, modelSubpackage, annotations);
    }

    /**
     * Returns a new {@link EnumInfo} object with specified model subpackage.
     * @param modelSubpackage the model subpackage.
     * @return the new and updated {@link EnumInfo} object.
     */
    public EnumInfo withModelSubpackage(String modelSubpackage) {
        return new EnumInfo(name, constants, modelSubdir, modelSubpackage, annotations);
    }

    /**
     * Returns a new {@link EnumInfo} object with specified annotation added.
     * @param annotation the annotation to add.
     * @return the new and updated {@link EnumInfo} object.
     */
    public EnumInfo withAddedAnnotation(AnnotationInfo annotation) {
        List<AnnotationInfo> newAnnotations = new LinkedList<>(this.annotations);
        newAnnotations.add(annotation);
        return new EnumInfo(name, constants, modelSubdir, modelSubpackage, newAnnotations);
    }

    @Override
    public Set<String> aggregatedNormalImports() {
        return ImportInfo.empty().withAddedImports(annotations).normalImports();
    }

    @Override
    public Set<String> aggregatedStaticImports() {
        return ImportInfo.empty().withAddedImports(annotations).staticImports();
    }
}
