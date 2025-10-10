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
package io.github.torand.jsonschema2java.generators;

import java.net.URI;

import static io.github.torand.javacommons.lang.StringHelper.isBlank;

/**
 * Contains configuration of the source code generators.
 * @param searchRootDir
 * @param outputDir the root directory of output.
 * @param schemaIdRootUri
 * @param rootPackage the root package of classes and enums.
 * @param pojoNameSuffix the Pojo name suffix.
 * @param pojosAsRecords the flag to use Java records for Pojos.
 * @param addMpOpenApiAnnotations the flag to generate Microprofile OpenAPI annotations.
 * @param addJsonPropertyAnnotations the flag to generate Jackson JSON property annotations.
 * @param addJakartaBeanValidationAnnotations the flag to generate Jakarta Bean Validation annotations.
 * @param useKotlinSyntax the flag to generate Kotlin source code.
 * @param indentWithTab the flag to output indents with the tab character.
 * @param indentSize the number of spaces for each indent level, when not using the tab character.
 * @param verbose the flag to enable verbose logging.
 */
public record Options (
    String searchRootDir,
    String outputDir,
    URI schemaIdRootUri,
    String rootPackage,
    String pojoNameSuffix,
    boolean pojosAsRecords,
    boolean addMpOpenApiAnnotations,
    boolean addJsonPropertyAnnotations,
    boolean addJakartaBeanValidationAnnotations,
    boolean useKotlinSyntax,
    boolean indentWithTab,
    int indentSize,
    boolean verbose
 ) {
    /**
     * Returns the default settings.
     * @return the default settings.
     */
    public static Options defaults() {
        return new Options(
            null,
            null,
            null,
            null,
            "Dto",
            true,
            false,
            true,
            true,
            false,
            false,
            4,
            false
        );
    }

    private Options with(String searchRootDir, String outputDir, URI schemaIdRootUri, String rootPackage, boolean addMpOpenApiAnnotations, boolean addJsonPropertyAnnotations, boolean useKotlinSyntax, boolean verbose) {
        return new Options(
            searchRootDir,
            outputDir,
            schemaIdRootUri,
            rootPackage,
            this.pojoNameSuffix,
            this.pojosAsRecords,
            addMpOpenApiAnnotations,
            addJsonPropertyAnnotations,
            this.addJakartaBeanValidationAnnotations,
            useKotlinSyntax,
            this.indentWithTab,
            this.indentSize,
            verbose
        );
    }

    /**
     * Returns a new {@link Options} object with specified search root directory.
     * @param searchRootDir the search root directory.
     * @return the new and updated {@link Options} object.
     */
    public Options withSearchRootDir(String searchRootDir) {
        return with(searchRootDir, this.outputDir, this.schemaIdRootUri, this.rootPackage, this.addMpOpenApiAnnotations, this.addJsonPropertyAnnotations, this.useKotlinSyntax, this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified output directory.
     * @param outputDir the output directory.
     * @return the new and updated {@link Options} object.
     */
    public Options withOutputDir(String outputDir) {
        return with(this.searchRootDir, outputDir, this.schemaIdRootUri, this.rootPackage, this.addMpOpenApiAnnotations, this.addJsonPropertyAnnotations, this.useKotlinSyntax, this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified schema id root URI.
     * @param schemaIdRootUri the schema id root URI.
     * @return the new and updated {@link Options} object.
     */
    public Options withSchemaIdRootUri(URI schemaIdRootUri) {
        return with(this.searchRootDir, this.outputDir, schemaIdRootUri, this.rootPackage, this.addMpOpenApiAnnotations, this.addJsonPropertyAnnotations, this.useKotlinSyntax, this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified root package.
     * @param rootPackage the root package.
     * @return the new and updated {@link Options} object.
     */
    public Options withRootPackage(String rootPackage) {
        return with(this.searchRootDir, this.outputDir, this.schemaIdRootUri, rootPackage, this.addMpOpenApiAnnotations, this.addJsonPropertyAnnotations, this.useKotlinSyntax, this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified add Microprofile OpenAPI annotations flag.
     * @param addMpOpenApiAnnotations the add Microprofile OpenAPI annotations flag.
     * @return the new and updated {@link Options} object.
     */
    public Options withAddMpOpenApiAnnotations(boolean addMpOpenApiAnnotations) {
        return with(this.searchRootDir, this.outputDir, this.schemaIdRootUri, this.rootPackage, addMpOpenApiAnnotations, this.addJsonPropertyAnnotations, this.useKotlinSyntax, this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified add JSON property annotations flag.
     * @param addJsonPropertyAnnotations the add JSON property annotations flag.
     * @return the new and updated {@link Options} object.
     */
    public Options withAddJsonPropertyAnnotations(boolean addJsonPropertyAnnotations) {
        return with(this.searchRootDir, this.outputDir, this.schemaIdRootUri, this.rootPackage, this.addMpOpenApiAnnotations, addJsonPropertyAnnotations, this.useKotlinSyntax, this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified use Kotlin syntax flag.
     * @param useKotlinSyntax the use Kotlin syntax flag.
     * @return the new and updated {@link Options} object.
     */
    public Options withUseKotlinSyntax(boolean useKotlinSyntax) {
        return with(this.searchRootDir, this.outputDir, this.schemaIdRootUri, this.rootPackage, this.addMpOpenApiAnnotations, this.addJsonPropertyAnnotations, useKotlinSyntax, this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified verbose flag.
     * @param verbose the verbose flag.
     * @return the new and updated {@link Options} object.
     */
    public Options withVerbose(boolean verbose) {
        return with(this.searchRootDir, this.outputDir, this.schemaIdRootUri, this.rootPackage, this.addMpOpenApiAnnotations, this.addJsonPropertyAnnotations, this.useKotlinSyntax, verbose);
    }

    /**
     * Returns a new {@link Options} object with specified model output directory.
     * @param customSubdir the model output directory.
     * @return the new and updated {@link Options} object.
     */
    public String getModelOutputDir(String customSubdir) {
        return outputDir + (isBlank(customSubdir) ? "" : "/"+customSubdir);
    }

    /**
     * Returns a new {@link Options} object with specified model package.
     * @param customSubpackage the model subpackage.
     * @return the new and updated {@link Options} object.
     */
    public String getModelPackage(String customSubpackage) {
        return rootPackage + (isBlank(customSubpackage) ? "" : "."+customSubpackage);
    }

    /**
     * Gets the language specific code file extension.
     * @return the language specific code file extension.
     */
    public String getFileExtension() {
        return useKotlinSyntax ? ".kt" : ".java";
    }
}
