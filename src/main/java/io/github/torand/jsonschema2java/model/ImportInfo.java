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

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import static io.github.torand.javacommons.stream.StreamHelper.streamSafely;
import static java.util.Collections.emptySet;

/**
 * Describes a collection of imports and static imports.
 * @param normalImports the normal (non-static) imports.
 * @param staticImports the static imports.
 */
public record ImportInfo(
    Set<String> normalImports,
    Set<String> staticImports
) implements ImportsSupplier {

    /**
     * Creates an {@link ImportInfo} object with no imports.
     * @return the empty {@link ImportInfo} object.
     */
    public static ImportInfo empty() {
        return new ImportInfo();
    }

    /**
     * Constructs an {@link ImportInfo} object.
     */
    private ImportInfo() {
        this(emptySet(), emptySet());
    }

    @Override
    public ImportInfo imports() {
        return this;
    }

    /**
     * Returns a new {@link ImportInfo} object with specified normal import added.
     * @param normalImport the import to add.
     * @return the new and updated {@link ImportInfo} object.
     */
    public ImportInfo withAddedNormalImport(String normalImport) {
        Set<String> newNormalImports = new TreeSet<>(this.normalImports);
        newNormalImports.add(normalImport);
        return new ImportInfo(newNormalImports, staticImports);
    }

    /**
     * Returns a new {@link ImportInfo} object with specified static import added.
     * @param staticImport the static import to add.
     * @return the new and updated {@link ImportInfo} object.
     */
    public ImportInfo withAddedStaticImport(String staticImport) {
        Set<String> newStaticImports = new TreeSet<>(this.staticImports);
        newStaticImports.add(staticImport);
        return new ImportInfo(normalImports, newStaticImports);
    }

    /**
     * Returns a new {@link ImportInfo} object with all imports from specified supplier added.
     * @param importSupplier the imports to add.
     * @return the new and updated {@link ImportInfo} object.
     */
    public ImportInfo withAddedImports(ImportsSupplier importSupplier) {
        Set<String> newNormalImports = new TreeSet<>(this.normalImports);
        newNormalImports.addAll(importSupplier.imports().normalImports());
        Set<String> newStaticImports = new TreeSet<>(this.staticImports);
        newStaticImports.addAll(importSupplier.imports().staticImports());

        return new ImportInfo(newNormalImports, newStaticImports);
    }

    /**
     * Returns a new {@link ImportInfo} object with all imports from specified suppliers added.
     * @param importSuppliers the imports to add.
     * @return the new and updated {@link ImportInfo} object.
     */
    public ImportInfo withAddedImports(Collection<? extends ImportsSupplier> importSuppliers) {
        Set<String> newNormalImports = new TreeSet<>(this.normalImports);
        streamSafely(importSuppliers).map(ImportsSupplier::imports).map(ImportInfo::normalImports).forEach(newNormalImports::addAll);
        Set<String> newStaticImports = new TreeSet<>(this.staticImports);
        streamSafely(importSuppliers).map(ImportsSupplier::imports).map(ImportInfo::staticImports).forEach(newStaticImports::addAll);

        return new ImportInfo(newNormalImports, newStaticImports);
    }
}
