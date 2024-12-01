package io.github.torand.jsonschema2java.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.github.torand.jsonschema2java.utils.CollectionHelper.streamConcat;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class TypeInfo {
    public String name;
    public String description;
    public boolean nullable;
    public TypeInfo keyType;
    public boolean primitive;
    public TypeInfo itemType;
    public String schemaFormat;
    public String schemaPattern;
    public List<String> annotations = new ArrayList<>();
    public List<String> annotationImports = new ArrayList<>();
    public List<String> typeImports = new ArrayList<>();

    public boolean isPrimitive() {
        return primitive;
    }

    public boolean isArray() {
        return nonNull(itemType);
    }

    public String getFullName() {
        if (nonNull(keyType) && nonNull(itemType)) {
            return "%s<%s,%s>".formatted(name, keyType.getFullName(), itemType.getFullName());
        } else if (nonNull(itemType)) {
            return "%s<%s>".formatted(name, itemType.getFullName());
        } else {
            return name;
        }
    }

    public Stream<String> typeImports() {
        return isNull(itemType) ? typeImports.stream() : streamConcat(typeImports, itemType.typeImports);
    }

    public Stream<String> annotationImports() {
        return isNull(itemType) ? annotationImports.stream() : streamConcat(annotationImports, itemType.annotationImports);
    }
}