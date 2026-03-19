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
package io.github.torand.jsonschema2java.writers.kotlin;

import io.github.torand.jsonschema2java.generators.Options;
import io.github.torand.jsonschema2java.model.AnnotatedTypeName;
import io.github.torand.jsonschema2java.model.AnnotationInfo;
import io.github.torand.jsonschema2java.model.PojoInfo;
import io.github.torand.jsonschema2java.model.PropertyInfo;
import io.github.torand.jsonschema2java.utils.PackageUtils;
import io.github.torand.jsonschema2java.writers.BaseWriter;
import io.github.torand.jsonschema2java.writers.PojoWriter;

import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static io.github.torand.javacommons.collection.CollectionHelper.isEmpty;
import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;
import static io.github.torand.javacommons.stream.StreamHelper.streamSafely;
import static io.github.torand.jsonschema2java.utils.KotlinTypeMapper.toKotlinNative;
import static io.github.torand.jsonschema2java.utils.PackageUtils.isFqnInPackage;
import static io.github.torand.jsonschema2java.utils.StringUtils.escape;
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
            writeLine("@Deprecated(\"%s\")".formatted(escape(pojoInfo.deprecationMessage())));
        }

        pojoInfo.annotations().forEach(a -> writeLine(a.annotation()));
        writeLine("@JvmRecord");

        writeLine("data class %s (".formatted(pojoInfo.name()));
        if (isEmpty(pojoInfo.properties())) {
            // Empty data classes not allowed in Kotlin
            writeNewLine();
            writeIndent(1);
            writeLine("val placeholder: String = \"\"");
        } else {
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
        }

        writeLine(")");
    }

    private void writeImports(PojoInfo pojoInfo) {
        Predicate<String> isInSamePackage = fqn -> isFqnInPackage(fqn, opts.getModelPackage(pojoInfo.modelSubpackage()));

        List<String> imports = pojoInfo.aggregatedNormalImports().stream()
            .filter(not(PackageUtils::isFundamentalJavaClass))
            .filter(not(isInSamePackage))
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
            writeLine("@Deprecated(\"%s\")".formatted(escape(propInfo.deprecationMessage())));
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

    private static String escapeReservedKeywords(String name) {
        return RESERVED_KEYWORDS.contains(name) ? "`%s`".formatted(name) : name;
    }

    private static final Set<String> RESERVED_KEYWORDS = Set.of(
        "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in", "interface",
        "is", "null", "object", "package", "return", "super", "this", "throw", "true", "try", "typealias",
        "typeof", "val", "var", "when", "while"
    );
}
