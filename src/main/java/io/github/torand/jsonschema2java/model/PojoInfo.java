/*
 * Copyright (c) 2024 Tore Eide Andersen
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

import io.github.torand.jsonschema2java.utils.CollectionHelper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Objects.nonNull;

public class PojoInfo {
    public String name;

    public String modelSubdir;
    public String modelSubpackage;

    public Set<String> imports = new TreeSet<>();
    public Set<String> annotations = new LinkedHashSet<>();

    public List<PropertyInfo> properties = new ArrayList<>();

    public String deprecationMessage = null;

    public boolean isDeprecated() {
        return nonNull(deprecationMessage);
    }

    public boolean isEmpty() {
        return CollectionHelper.isEmpty(properties);
    }
}
