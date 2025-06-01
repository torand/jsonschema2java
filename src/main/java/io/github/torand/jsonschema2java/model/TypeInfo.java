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

import static io.github.torand.jsonschema2java.utils.CollectionHelper.streamConcat;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Describes a type.
 */
public class TypeInfo {
    public String name;
    public String description;
    public boolean nullable;
    public TypeInfo keyType;
    public boolean primitive;
    public TypeInfo itemType;
    public String schemaFormat;
    public String schemaPattern;
    public List<String> annotations = new ArrayList<>();
    public List<String> annotationImports = new ArrayList<>();
    public List<String> typeImports = new ArrayList<>();

    public boolean isPrimitive() {
        return primitive;
    }

    public boolean isArray() {
        return nonNull(itemType);
    }

    public String getFullName() {
        if (nonNull(keyType) && nonNull(itemType)) {
            return "%s<%s,%s>".formatted(name, keyType.getFullName(), itemType.getFullName());
        } else if (nonNull(itemType)) {
            return "%s<%s>".formatted(name, itemType.getFullName());
        } else {
            return name;
        }
    }

    public Stream<String> typeImports() {
        return isNull(itemType) ? typeImports.stream() : streamConcat(typeImports, itemType.typeImports);
    }

    public Stream<String> annotationImports() {
        return isNull(itemType) ? annotationImports.stream() : streamConcat(annotationImports, itemType.annotationImports);
    }
}