package io.github.torand.jsonschema2java.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Objects.nonNull;

public class PropertyInfo {
    public String name;
    public TypeInfo type = new TypeInfo();
    public boolean required;

    public Set<String> imports = new TreeSet<>();
    public Set<String> annotations = new LinkedHashSet<>();

    public String deprecationMessage = null;

    public boolean isDeprecated() {
        return nonNull(deprecationMessage);
    }
}
