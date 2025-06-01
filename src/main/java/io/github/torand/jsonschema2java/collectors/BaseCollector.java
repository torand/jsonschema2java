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

import static io.github.torand.jsonschema2java.utils.StringHelper.nonBlank;

/**
 * Base class for all collectors.
 */
public abstract class BaseCollector {

    protected final Options opts;

    protected BaseCollector(Options opts) {
        this.opts = opts;
    }

    protected String normalizeDescription(String description) {
        return nonBlank(description) ? description.replaceAll("%", "%%") : "TBD";
    }

    protected String dirPath2PackagePath(String dirPath) {
        return dirPath.replaceAll("\\/", ".");
    }

    protected String modelName2SchemaName(String modelName) {
        return modelName.replaceFirst(opts.pojoNameSuffix+"$", "");
    }

    protected String formatDeprecationMessage(Extensions extensions) {
        return extensions.getString(Extensions.EXT_DEPRECATION_MESSAGE).orElse("Deprecated");
    }
}
