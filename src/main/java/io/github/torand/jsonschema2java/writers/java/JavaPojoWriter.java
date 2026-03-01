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
package io.github.torand.jsonschema2java.writers.java;

import io.github.torand.jsonschema2java.generators.Options;
import io.github.torand.jsonschema2java.model.AnnotatedTypeName;
import io.github.torand.jsonschema2java.model.PojoInfo;
import io.github.torand.jsonschema2java.model.PropertyInfo;
import io.github.torand.jsonschema2java.utils.PackageUtils;
import io.github.torand.jsonschema2java.writers.BaseWriter;
import io.github.torand.jsonschema2java.writers.PojoWriter;

import java.io.Writer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;
import static io.github.torand.jsonschema2java.utils.PackageUtils.isFqnInPackage;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

/**
 * Writes Java code for a pojo.
 */
public class JavaPojoWriter extends BaseWriter implements PojoWriter {

    public JavaPojoWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    @Override
    public void write(PojoInfo pojoInfo) {
        writeLine("package %s;", opts.getModelPackage(pojoInfo.modelSubpackage()));
        writeNewLine();

        writeNonJavaImports(pojoInfo);
        writeJavaImports(pojoInfo);

        if (pojoInfo.isDeprecated()) {
            writeLine("/// @deprecated %s".formatted(pojoInfo.deprecationMessage()));
            writeLine("@Deprecated");
        }

        pojoInfo.annotations().forEach(a -> writeLine(a.annotation()));

        if (opts.pojosAsRecords()) {
            writeLine("public record %s (".formatted(pojoInfo.name()));
        } else {
            writeLine("public class %s {".formatted(pojoInfo.name()));
        }

        AtomicInteger propNo = new AtomicInteger(1);
        pojoInfo.properties().forEach(propInfo -> {
            writeNewLine();
            writePropertyAnnotationLines(propInfo);
            writePropertyTypeAndNameLines(propInfo);

            if (opts.pojosAsRecords()) {
                if (propNo.getAndIncrement() < pojoInfo.properties().size()) {
                    writeLine(",");
                } else {
                    writeNewLine();
                }
            } else {
                writeLine(";");
            }
        });

        if (opts.pojosAsRecords()) {
            writeLine(") {");
            writeNewLine();
            writeLine("}");
        } else {
            writeNewLine();
            writeNoArgConstructor(pojoInfo.name());
            if (nonEmpty(pojoInfo.properties())) {
                writeNewLine();
                writeParameterizedConstructor(pojoInfo.name(), pojoInfo.properties());
            }
            writeLine("}");
        }
    }

    private void writeJavaImports(PojoInfo pojoInfo) {
        List<String> imports = pojoInfo.aggregatedNormalImports().stream()
            .filter(PackageUtils::isJavaPackage)
            .map("import %s;"::formatted)
            .toList();

        if (nonEmpty(imports)) {
            imports.forEach(this::writeLine);
            writeNewLine();
        }
    }

    private void writeNonJavaImports(PojoInfo pojoInfo) {
        Predicate<String> isInSamePackage = fqn -> isFqnInPackage(fqn, opts.getModelPackage(pojoInfo.modelSubpackage()));

        List<String> imports = pojoInfo.aggregatedNormalImports().stream()
            .filter(not(PackageUtils::isJavaPackage))
            .filter(not(isInSamePackage))
            .map("import %s;"::formatted)
            .toList();

        if (nonEmpty(imports)) {
            imports.forEach(this::writeLine);
            writeNewLine();
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
        writeLine("public %s(%s) {", name, props.stream().map(p -> p.type().getFullName() + " " + p.name()).collect(joining(", ")));
        props.forEach(p -> {
            writeIndent(2);
            writeLine("this.%s = %s;", p.name(), p.name());
        });
        writeIndent(1);
        writeLine("}");
    }

    private void writePropertyAnnotationLines(PropertyInfo propInfo) {
        if (propInfo.isDeprecated()) {
            writeIndent(1);
            writeLine("/// @deprecated %s".formatted(propInfo.deprecationMessage()));
            writeIndent(1);
            writeLine("@Deprecated");
        }
        propInfo.annotations().forEach(a -> {
            writeIndent(1);
            writeLine(a.annotation());
        });
    }

    private void writePropertyTypeAndNameLines(PropertyInfo propInfo) {
        AnnotatedTypeName annotatedTypeName = propInfo.type().getAnnotatedFullName();

        annotatedTypeName.annotations().forEach(a -> {
            writeIndent(1);
            writeLine(a);
        });

        writeIndent(1);
        if (opts.pojosAsRecords()) {
            write("%s %s".formatted(annotatedTypeName.typeName(), propInfo.name()));
        } else {
            write("public %s %s".formatted(annotatedTypeName.typeName(), propInfo.name()));
        }
    }
}
