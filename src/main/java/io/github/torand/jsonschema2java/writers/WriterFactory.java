package io.github.torand.jsonschema2java.writers;

import io.github.torand.jsonschema2java.Options;
import io.github.torand.jsonschema2java.writers.java.JavaEnumWriter;
import io.github.torand.jsonschema2java.writers.java.JavaPojoWriter;
import io.github.torand.jsonschema2java.writers.kotlin.KotlinEnumWriter;
import io.github.torand.jsonschema2java.writers.kotlin.KotlinPojoWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

public final class WriterFactory {
    private WriterFactory() {}

    public static EnumWriter createEnumWriter(String filename, Options opts, String modelSubdir) throws IOException {
        Writer fileWriter = createFileWriter(filename, opts.getModelOutputDir(modelSubdir));
        return opts.useKotlinSyntax ? new KotlinEnumWriter(fileWriter, opts) : new JavaEnumWriter(fileWriter, opts);
    }

    public static PojoWriter createPojoWriter(String filename, Options opts, String modelSubdir) throws IOException {
        Writer fileWriter = createFileWriter(filename, opts.getModelOutputDir(modelSubdir));
        return opts.useKotlinSyntax ? new KotlinPojoWriter(fileWriter, opts) : new JavaPojoWriter(fileWriter, opts);
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
