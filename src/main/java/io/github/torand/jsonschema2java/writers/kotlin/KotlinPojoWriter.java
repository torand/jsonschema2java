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
package io.github.torand.jsonschema2java.writers.kotlin;

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
import java.util.function.Predicate;

import static io.github.torand.jsonschema2java.utils.CollectionHelper.nonEmpty;
import static io.github.torand.jsonschema2java.utils.CollectionHelper.streamConcat;
import static io.github.torand.jsonschema2java.utils.CollectionHelper.streamSafely;
import static io.github.torand.jsonschema2java.utils.KotlinTypeMapper.toKotlinNative;
import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

public class KotlinPojoWriter extends BaseWriter implements PojoWriter {

    public KotlinPojoWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    @Override
    public void write(PojoInfo pojoInfo) {
        writeLine("package %s", opts.getModelPackage(pojoInfo.modelSubpackage));
        writeNewLine();

        Predicate<String> isModelType = qt -> isModelPackage(qt, pojoInfo.modelSubpackage);

        Set<String> imports = new TreeSet<>();
        imports.addAll(pojoInfo.imports);
        pojoInfo.properties.stream()
            .flatMap(p -> p.imports.stream())
            .forEach(imports::add);
        pojoInfo.properties.stream()
            .flatMap(p -> p.type.typeImports())
            .filter(not(isModelType))
            .forEach(imports::add);
        pojoInfo.properties.stream()
            .flatMap(p -> p.type.annotationImports())
            .filter(not(isModelType))
            .forEach(imports::add);

        imports.removeIf(i -> i.equals("java.util.List"));
        imports.removeIf(i -> i.equals("java.util.Map"));

        if (nonEmpty(imports)) {
            imports.forEach(ti -> writeLine("import %s".formatted(ti)));
            writeNewLine();
        }

        if (pojoInfo.isDeprecated()) {
            writeLine("@Deprecated(\"%s\")".formatted(pojoInfo.deprecationMessage));
        }

        pojoInfo.annotations.forEach(this::writeLine);
        writeLine("@JvmRecord");

        writeLine("data class %s (".formatted(pojoInfo.name));

        AtomicInteger propNo = new AtomicInteger(1);
        pojoInfo.properties.forEach(propInfo -> {
            writeNewLine();
            writePropertyAnnotationLines(propInfo);

            writeIndent(1);
            write("val %s: ", escapeReservedKeywords(propInfo.name));

            String typeName = toKotlinNative(propInfo.type.name);

            if (nonNull(propInfo.type.itemType)) {
                String itemTypeWithAnnotations = streamConcat(propInfo.type.itemType.annotations, List.of(propInfo.type.itemType.name))
                    .collect(joining(" "));

                if (nonNull(propInfo.type.keyType)) {
                    String keyTypeWithAnnotations = streamConcat(propInfo.type.keyType.annotations, List.of(propInfo.type.keyType.name))
                        .collect(joining(" "));

                    write("%s<%s, %s>".formatted(typeName, keyTypeWithAnnotations, itemTypeWithAnnotations));
                } else {
                    write("%s<%s>".formatted(typeName, itemTypeWithAnnotations));
                }
            } else {
                write("%s".formatted(typeName));
            }

            if (!propInfo.required || propInfo.type.nullable) {
                write("? = null");
            }

            if (propNo.getAndIncrement() < pojoInfo.properties.size()) {
                writeLine(",");
            } else {
                writeNewLine();
            }
        });

        writeLine(")");
    }

    private void writePropertyAnnotationLines(PropertyInfo propInfo) {
        if (propInfo.isDeprecated()) {
            writeIndent(1);
            writeLine("@Deprecated(\"%s\")".formatted(propInfo.deprecationMessage));
        }
        streamSafely(propInfo.annotations)
            .map(this::prefixPropertyAnnotation)
            .forEach(a -> {
                writeIndent(1);
                writeLine(a);
            });
        streamSafely(propInfo.type.annotations)
            .map(this::prefixPropertyAnnotation)
            .forEach(a -> {
                writeIndent(1);
                writeLine(a);
            });
    }

    private String prefixPropertyAnnotation(String annotation) {
        if (annotation.startsWith("@JsonProperty")) {
            return annotation;
        }

        return "@field:"+annotation.substring(1);
    }

    private boolean isModelPackage(String qualifiedType, String pojoModelSubpackage) {
        // Remove class name from qualifiedType value
        int lastDotIdx = qualifiedType.lastIndexOf(".");
        String typePackage = qualifiedType.substring(0, lastDotIdx);

        return opts.getModelPackage(pojoModelSubpackage).equals(typePackage);
    }

    private static String escapeReservedKeywords(String name) {
        return RESERVED_KEYWORDS.contains(name) ? "`%s`".formatted(name) : name;
    }

    private static final Set<String> RESERVED_KEYWORDS = Set.of(
        "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in", "interface",
        "is", "null", "object", "package", "return", "super", "this", "throw", "true", "try", "typealias",
        "typeof", "val", "var", "when", "while"
    );
}
