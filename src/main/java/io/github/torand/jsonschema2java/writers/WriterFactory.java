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
package io.github.torand.jsonschema2java.writers;

import io.github.torand.jsonschema2java.generators.Options;
import io.github.torand.jsonschema2java.writers.java.JavaEnumWriter;
import io.github.torand.jsonschema2java.writers.java.JavaPojoWriter;
import io.github.torand.jsonschema2java.writers.kotlin.KotlinEnumWriter;
import io.github.torand.jsonschema2java.writers.kotlin.KotlinPojoWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

/**
 * Provides factory methods to create code writers.
 */
public final class WriterFactory {
    private WriterFactory() {}

    public static EnumWriter createEnumWriter(String filename, Options opts, String modelSubdir) throws IOException {
        Writer fileWriter = createFileWriter(filename, opts.getModelOutputDir(modelSubdir));
        return opts.useKotlinSyntax() ? new KotlinEnumWriter(fileWriter, opts) : new JavaEnumWriter(fileWriter, opts);
    }

    public static PojoWriter createPojoWriter(String filename, Options opts, String modelSubdir) throws IOException {
        Writer fileWriter = createFileWriter(filename, opts.getModelOutputDir(modelSubdir));
        return opts.useKotlinSyntax() ? new KotlinPojoWriter(fileWriter, opts) : new JavaPojoWriter(fileWriter, opts);
    }

    private static Writer createFileWriter(String filename, String directory) throws IOException {
        Path outputPath = Path.of(directory);
        File outputPathFile = outputPath.toFile();
        if (!outputPathFile.exists()) {
            outputPathFile.mkdirs();
        }

        File outputFile = new File(outputPathFile, filename);
        return new FileWriter(outputFile);
    }
}
