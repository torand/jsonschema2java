package io.github.torand.jsonschema2java.model;

import io.github.torand.jsonschema2java.utils.CollectionHelper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Objects.nonNull;

public class PojoInfo {
    public String name;

    public String modelSubdir;
    public String modelSubpackage;

    public Set<String> imports = new TreeSet<>();
    public Set<String> annotations = new LinkedHashSet<>();

    public List<PropertyInfo> properties = new ArrayList<>();

    public String deprecationMessage = null;

    public boolean isDeprecated() {
        return nonNull(deprecationMessage);
    }

    public boolean isEmpty() {
        return CollectionHelper.isEmpty(properties);
    }
}
