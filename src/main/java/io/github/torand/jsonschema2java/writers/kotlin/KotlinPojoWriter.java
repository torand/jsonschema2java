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
import io.github.torand.jsonschema2java.model.AnnotatedTypeName;
import io.github.torand.jsonschema2java.model.AnnotationInfo;
import io.github.torand.jsonschema2java.model.PojoInfo;
import io.github.torand.jsonschema2java.model.PropertyInfo;
import io.github.torand.jsonschema2java.writers.BaseWriter;
import io.github.torand.jsonschema2java.writers.PojoWriter;

import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;
import static io.github.torand.javacommons.stream.StreamHelper.streamSafely;
import static io.github.torand.jsonschema2java.utils.KotlinTypeMapper.toKotlinNative;
import static java.util.function.Predicate.not;

/**
 * Writes Kotlin code for a pojo.
 */
public class KotlinPojoWriter extends BaseWriter implements PojoWriter {

    public KotlinPojoWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    @Override
    public void write(PojoInfo pojoInfo) {
        writeLine("package %s", opts.getModelPackage(pojoInfo.modelSubpackage()));
        writeNewLine();

        writeImports(pojoInfo);

        if (pojoInfo.isDeprecated()) {
            writeLine("@Deprecated(\"%s\")".formatted(pojoInfo.deprecationMessage()));
        }

        pojoInfo.annotations().forEach(a -> writeLine(a.annotation()));
        writeLine("@JvmRecord");

        writeLine("data class %s (".formatted(pojoInfo.name()));

        AtomicInteger propNo = new AtomicInteger(1);
        pojoInfo.properties().forEach(propInfo -> {
            writeNewLine();
            writePropertyAnnotationLines(propInfo);
            writePropertyTypeAndName(propInfo);

            if (propNo.getAndIncrement() < pojoInfo.properties().size()) {
                writeLine(",");
            } else {
                writeNewLine();
            }
        });

        writeLine(")");
    }

    private void writeImports(PojoInfo pojoInfo) {
        List<String> imports = pojoInfo.aggregatedNormalImports().stream()
            .filter(ni -> !isInPackage(ni, pojoInfo.modelSubpackage()))
            .filter(not("java.util.List"::equals))
            .filter(not("java.util.Map"::equals))
            .map("import %s"::formatted)
            .toList();

        if (nonEmpty(imports)) {
            imports.forEach(this::writeLine);
            writeNewLine();
        }
    }

    private void writePropertyAnnotationLines(PropertyInfo propInfo) {
        if (propInfo.isDeprecated()) {
            writeIndent(1);
            writeLine("@Deprecated(\"%s\")".formatted(propInfo.deprecationMessage()));
        }
        streamSafely(propInfo.annotations())
            .map(AnnotationInfo::annotation)
            .map(this::prefixPropertyAnnotation)
            .forEach(a -> {
                writeIndent(1);
                writeLine(a);
            });
    }

    private void writePropertyTypeAndName(PropertyInfo propInfo) {
        AnnotatedTypeName annotatedTypeName = propInfo.type().getAnnotatedFullName();

        annotatedTypeName.annotations()
            .map(this::prefixPropertyAnnotation)
            .forEach(a -> {
                writeIndent(1);
                writeLine(a);
            });

        writeIndent(1);
        write("val %s: ", escapeReservedKeywords(propInfo.name()));
        write(toKotlinNative(annotatedTypeName.typeName()));

        if (!propInfo.required() || propInfo.type().nullable()) {
            write("? = null");
        }
    }

    private String prefixPropertyAnnotation(String annotation) {
        if (annotation.startsWith("@JsonProperty")) {
            return annotation;
        }

        return "@field:"+annotation.substring(1);
    }

    private boolean isInPackage(String qualifiedType, String pojoModelSubpackage) {
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
