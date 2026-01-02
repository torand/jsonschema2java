/*
 * Copyright (c) 2024-2026 Tore Eide Andersen
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

import static io.github.torand.javacommons.lang.StringHelper.nonBlank;
import static io.github.torand.jsonschema2java.utils.KotlinTypeMapper.toKotlinNative;

/**
 * Base class for all collectors.
 */
public abstract class BaseCollector {

    /**
     * The plugin options.
     */
    protected final Options opts;

    /**
     * Constructs a {@link BaseCollector} object.
     *
     * @param opts the plugin options.
     */
    protected BaseCollector(Options opts) {
        this.opts = opts;
    }

    /**
     * Modifies the description text for further use in code generation.
     *
     * @param description the original description text.
     * @return the normalized description.
     */
    protected String normalizeDescription(String description) {
        return nonBlank(description) ? description.replace("%", "%%") : "TBD";
    }

    /**
     * Converts a directory path into a package path.
     *
     * @param dirPath the directory path.
     * @return the package path.
     */
    protected String dirPath2PackagePath(String dirPath) {
        return dirPath.replace("/", ".");
    }

    /**
     * Converts a model (pojo) name into a schema name.
     *
     * @param modelName the model name.
     * @return the schema name.
     */
    protected String modelName2SchemaName(String modelName) {
        return modelName.replaceFirst(opts.pojoNameSuffix() + "$", "");
    }

    /**
     * Formats given class name to language specific class reference.
     *
     * @param className the class name.
     * @return the class reference.
     */
    protected String formatClassRef(String className) {
        return opts.useKotlinSyntax()
            ? "%s::class".formatted(toKotlinNative(className))
            : "%s.class".formatted(className);
    }

    /**
     * Formats a deprecation message.
     *
     * @param extensions the JSON Schema extensions containing a custom deprecation message, or not.
     * @return the formatted deprecation message.
     */
    protected String formatDeprecationMessage(Extensions extensions) {
        return extensions.getString(Extensions.EXT_DEPRECATION_MESSAGE).orElse("Deprecated");
    }
}
