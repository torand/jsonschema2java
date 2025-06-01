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
package io.github.torand.jsonschema2java.writers.kotlin;

import io.github.torand.jsonschema2java.generators.Options;
import io.github.torand.jsonschema2java.model.EnumInfo;
import io.github.torand.jsonschema2java.writers.BaseWriter;
import io.github.torand.jsonschema2java.writers.EnumWriter;

import java.io.Writer;

import static io.github.torand.jsonschema2java.utils.CollectionHelper.nonEmpty;
import static io.github.torand.jsonschema2java.utils.StringHelper.joinCsv;

/**
 * Writes Kotlin code for an enum.
 */
public class KotlinEnumWriter extends BaseWriter implements EnumWriter {

    public KotlinEnumWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    @Override
    public void write(EnumInfo enumInfo) {
        writeLine("package %s", opts.getModelPackage(enumInfo.modelSubpackage));
        writeNewLine();

        if (nonEmpty(enumInfo.imports)) {
            enumInfo.imports.forEach(ti -> writeLine("import %s".formatted(ti)));
            writeNewLine();
        }

        enumInfo.annotations.forEach(this::writeLine);

        writeLine("enum class %s {".formatted(enumInfo.name));
        writeIndent(1);
        writeLine(joinCsv(enumInfo.constants));
        writeLine("}");
    }
}
