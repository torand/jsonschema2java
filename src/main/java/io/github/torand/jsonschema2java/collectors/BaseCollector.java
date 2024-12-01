package io.github.torand.jsonschema2java.collectors;

import io.github.torand.jsonschema2java.Options;

import java.util.List;

import static io.github.torand.jsonschema2java.utils.CollectionHelper.isEmpty;
import static io.github.torand.jsonschema2java.utils.StringHelper.nonBlank;

public abstract class BaseCollector {

    protected final Options opts;

    protected BaseCollector(Options opts) {
        this.opts = opts;
    }

    protected String normalizeDescription(String description) {
        return nonBlank(description) ? description.replaceAll("%", "%%") : "TBD";
    }

    protected String dirPath2PackagePath(String dirPath) {
        return dirPath.replaceAll("\\/", ".");
    }

    protected String modelName2SchemaName(String modelName) {
        return modelName.replaceFirst(opts.pojoNameSuffix+"$", "");
    }

    protected String joinParams(List<String> params) {
        if (isEmpty(params)) {
            return "";
        }

        return String.join(", ", params);
    }
}
