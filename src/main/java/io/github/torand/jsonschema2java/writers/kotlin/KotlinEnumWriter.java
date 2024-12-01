package io.github.torand.jsonschema2java.writers.kotlin;

import io.github.torand.jsonschema2java.Options;
import io.github.torand.jsonschema2java.model.EnumInfo;
import io.github.torand.jsonschema2java.writers.BaseWriter;
import io.github.torand.jsonschema2java.writers.EnumWriter;

import java.io.Writer;

import static io.github.torand.jsonschema2java.utils.CollectionHelper.nonEmpty;

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
        writeLine(String.join(", ", enumInfo.constants));
        writeLine("}");
    }
}
