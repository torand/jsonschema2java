package io.github.torand.jsonschema2java.writers.java;

import io.github.torand.jsonschema2java.Options;
import io.github.torand.jsonschema2java.model.PojoInfo;
import io.github.torand.jsonschema2java.model.PropertyInfo;
import io.github.torand.jsonschema2java.writers.BaseWriter;
import io.github.torand.jsonschema2java.writers.PojoWriter;

import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.github.torand.jsonschema2java.utils.CollectionHelper.nonEmpty;
import static io.github.torand.jsonschema2java.utils.CollectionHelper.streamConcat;
import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

public class JavaPojoWriter extends BaseWriter implements PojoWriter {

    public JavaPojoWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    @Override
    public void write(PojoInfo pojoInfo) {
        writeLine("package %s;", opts.getModelPackage(pojoInfo.modelSubpackage));
        writeNewLine();

        Set<String> nonJavaImports = new TreeSet<>();
        Set<String> javaImports = new TreeSet<>();

        Consumer<String> collectImport = qt -> { if (isJavaPackage(qt)) javaImports.add(qt); else nonJavaImports.add(qt);};
        Predicate<String> isModelType = qt -> isModelPackage(qt, pojoInfo.modelSubpackage);

        pojoInfo.imports.forEach(collectImport);
        pojoInfo.properties.stream()
            .flatMap(p -> p.imports.stream())
            .forEach(collectImport);
        pojoInfo.properties.stream()
            .flatMap(p -> p.type.typeImports())
            .filter(not(isModelType))
            .forEach(collectImport);
        pojoInfo.properties.stream()
            .flatMap(p -> p.type.annotationImports())
            .filter(not(isModelType))
            .forEach(collectImport);

        if (nonEmpty(nonJavaImports)) {
            nonJavaImports.forEach(ti -> writeLine("import %s;".formatted(ti)));
            writeNewLine();
        }
        if (nonEmpty(javaImports)) {
            javaImports.forEach(ti -> writeLine("import %s;".formatted(ti)));
            writeNewLine();
        }

        if (pojoInfo.isDeprecated()) {
            writeLine("/// @deprecated %s".formatted(pojoInfo.deprecationMessage));
            writeLine("@Deprecated");
        }

        pojoInfo.annotations.forEach(this::writeLine);

        if (opts.pojosAsRecords) {
            writeLine("public record %s (".formatted(pojoInfo.name));
        } else {
            writeLine("public class %s {".formatted(pojoInfo.name));
        }

        AtomicInteger propNo = new AtomicInteger(1);
        pojoInfo.properties.forEach(propInfo -> {
            writeNewLine();
            writePropertyAnnotationLines(propInfo);

            writeIndent(1);
            if (nonNull(propInfo.type.itemType)) {
                String itemTypeWithAnnotations = streamConcat(propInfo.type.itemType.annotations, List.of(propInfo.type.itemType.name))
                    .collect(joining(" "));

                if (!opts.pojosAsRecords) {
                    write("public ");
                }

                if (nonNull(propInfo.type.keyType)) {
                    String keyTypeWithAnnotations = streamConcat(propInfo.type.keyType.annotations, List.of(propInfo.type.keyType.name))
                        .collect(joining(" "));

                    write("%s<%s, %s> %s".formatted(propInfo.type.name, keyTypeWithAnnotations, itemTypeWithAnnotations, propInfo.name));
                } else {
                    write("%s<%s> %s".formatted(propInfo.type.name, itemTypeWithAnnotations, propInfo.name));
                }
            } else {
                if (opts.pojosAsRecords) {
                    write("%s %s".formatted(propInfo.type.name, propInfo.name));
                } else {
                    write("public %s %s".formatted(propInfo.type.name, propInfo.name));
                }
            }

            if (opts.pojosAsRecords) {
                if (propNo.getAndIncrement() < pojoInfo.properties.size()) {
                    writeLine(",");
                } else {
                    writeNewLine();
                }
            } else {
                writeLine(";");
            }
        });

        if (opts.pojosAsRecords) {
            writeLine(") {");
            writeNewLine();
            writeLine("}");
        } else {
            writeNewLine();
            writeNoArgConstructor(pojoInfo.name);
            writeNewLine();
            writeParameterizedConstructor(pojoInfo.name, pojoInfo.properties);
            writeLine("}");
        }
    }

    private void writeNoArgConstructor(String name) {
        writeIndent(1);
        writeLine("public %s() {", name);
        writeIndent(1);
        writeLine("}");
    }

    private void writeParameterizedConstructor(String name, List<PropertyInfo> props) {
        writeIndent(1);
        writeLine("public %s(%s) {", name, props.stream().map(p -> p.type.getFullName() + " " + p.name).collect(joining(", ")));
        props.forEach(p -> {
            writeIndent(2);
            writeLine("this.%s = %s;", p.name, p.name);
        });
        writeIndent(1);
        writeLine("}");
    }

    private void writePropertyAnnotationLines(PropertyInfo propInfo) {
        if (propInfo.isDeprecated()) {
            writeIndent(1);
            writeLine("/// @deprecated %s".formatted(propInfo.deprecationMessage));
            writeIndent(1);
            writeLine("@Deprecated");
        }
        propInfo.annotations.forEach(a -> {
            writeIndent(1);
            writeLine(a);
        });
        propInfo.type.annotations.forEach(a -> {
            writeIndent(1);
            writeLine(a);
        });
    }

    private boolean isModelPackage(String qualifiedType, String pojoModelSubpackage) {
        // Remove class name from qualifiedType value
        int lastDotIdx = qualifiedType.lastIndexOf(".");
        String typePackage = qualifiedType.substring(0, lastDotIdx);

        return opts.getModelPackage(pojoModelSubpackage).equals(typePackage);
    }

    private boolean isJavaPackage(String qualifiedType) {
        return qualifiedType.startsWith("java.");
    }
}
