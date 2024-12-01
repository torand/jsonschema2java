package io.github.torand.jsonschema2java;

import java.net.URI;

import static io.github.torand.jsonschema2java.utils.StringHelper.isBlank;

public class Options {
    public String searchRootDir;
    public String outputDir;
    public URI schemaIdRootUri;
    public String rootPackage;
    public String pojoNameSuffix = "Dto";
    public boolean pojosAsRecords = true;
    public boolean addOpenApiSchemaAnnotations = false;
    public boolean addJsonPropertyAnnotations = true;
    public boolean useKotlinSyntax = false;
    public boolean verbose = false;

    public String getModelOutputDir(String customSubdir) {
        return outputDir + "/model" + (isBlank(customSubdir) ? "" : "/"+customSubdir);
    }

    public String getModelPackage(String customSubpackage) {
        return rootPackage + ".model" + (isBlank(customSubpackage) ? "" : "."+customSubpackage);
    }

    public String getFileExtension() {
        return useKotlinSyntax ? ".kt" : ".java";
    }
}
