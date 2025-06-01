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
package io.github.torand.jsonschema2java;

import io.github.torand.jsonschema2java.collectors.SchemaResolver;
import io.github.torand.jsonschema2java.generators.ModelGenerator;
import io.github.torand.jsonschema2java.generators.Options;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static io.github.torand.jsonschema2java.collectors.SchemaResolver.findSchemaFiles;
import static io.github.torand.jsonschema2java.utils.CollectionHelper.isEmpty;

/**
 * Generates source code for model classes based on JSON Schema files
 */
@Mojo( name = "generate", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class JsonSchema2JavaMojo extends AbstractMojo {
    private static final Logger logger = LoggerFactory.getLogger(JsonSchema2JavaMojo.class);

    /**
     * Root directory to search for schema files from.
     */
    @Parameter(property = "searchRootDir", defaultValue = "${project.basedir}")
    private String searchRootDir;

    /**
     * Schema file path search pattern. Supports 'glob' patterns.
     */
    @Parameter(property = "searchFilePattern", required = true )
    private String searchFilePattern;

    /**
     * Root URI of $id property in schema files, Path elements beyond this value must correspond to subdirectories inside searchRootDir parameter.
     */
    @Parameter(property = "schemaIdRootUri", required = true )
    private URI schemaIdRootUri;

    /**
     * Root directory of output.
     */
    @Parameter( property = "outputDir", defaultValue = "${project.build.directory}" )
    private String outputDir;

    /**
     * Root package of classes and enums.
     */
    @Parameter( property = "rootPackage", required = true )
    private String rootPackage;

    /**
     * Pojo name suffix.
     */
    @Parameter( property = "pojoNameSuffix", defaultValue = "Dto" )
    private String pojoNameSuffix;

    /**
     * Use Java records for pojos.
     */
    @Parameter( property = "pojosAsRecords", defaultValue = "true" )
    private boolean pojosAsRecords;

    /**
     * Generate Microprofile OpenAPI schema annotations.
     */
    @Parameter( property = "addOpenApiSchemaAnnotations", defaultValue = "false" )
    private boolean addOpenApiSchemaAnnotations;

    /**
     * Generate Jackson JSON property annotations.
     */
    @Parameter( property = "addJsonPropertyAnnotations", defaultValue = "true" )
    private boolean addJsonPropertyAnnotations;

    /**
     * Generate Jakarta Bean Validation annotations.
     */
    @Parameter( property = "addJakartaBeanValidationAnnotations", defaultValue = "true" )
    private boolean addJakartaBeanValidationAnnotations;

    /**
     * Generate Kotlin source code.
     */
    @Parameter( property = "useKotlinSyntax", defaultValue = "false" )
    private boolean useKotlinSyntax;

    /**
     * Whether to output indents with the tab character.
     */
    @Parameter( property = "indentWithTab", defaultValue = "false" )
    private boolean indentWithTab;

    /**
     * Whether to output indents with the tab character.
     */
    @Parameter( property = "indentSize", defaultValue = "4" )
    private int indentSize;

    /**
     * Enable verbose logging.
     */
    @Parameter( property = "verbose", defaultValue = "false" )
    private boolean verbose;

    public void execute() throws MojoExecutionException {
        Options opts = new Options();
        opts.searchRootDir = searchRootDir;
        opts.schemaIdRootUri = schemaIdRootUri;
        opts.rootPackage = rootPackage;
        opts.outputDir = outputDir;
        opts.pojoNameSuffix = pojoNameSuffix;
        opts.pojosAsRecords = pojosAsRecords;
        opts.addOpenApiSchemaAnnotations = addOpenApiSchemaAnnotations;
        opts.addJsonPropertyAnnotations = addJsonPropertyAnnotations;
        opts.addJakartaBeanValidationAnnotations = addJakartaBeanValidationAnnotations;
        opts.useKotlinSyntax = useKotlinSyntax;
        opts.indentWithTab = indentWithTab;
        opts.indentSize = indentSize;
        opts.verbose = verbose;

        List<Path> schemaFiles = findSchemaFiles(Path.of(searchRootDir), "glob:" + searchFilePattern);
        if (isEmpty(schemaFiles)) {
            logger.info("No JSON Schema files found in {}", searchRootDir);
            return;
        }

        if (opts.verbose) {
            logger.info("Validating schema files");
        }

        validateSchemaFiles(schemaFiles);

        if (opts.verbose) {
            logger.info("Generating source code");
        }

        ModelGenerator modelGenerator = new ModelGenerator(opts);
        modelGenerator.generate(schemaFiles);
    }

    private void validateSchemaFiles(List<Path> schemaFiles) throws MojoExecutionException {
        for (Path schemaFile : schemaFiles) {
            List<String> messages = SchemaResolver.validate(schemaFile);
            if (!messages.isEmpty()) {
                logger.error("File {} is not a valid JSON Schema file:", schemaFile);
                messages.forEach(logger::error);
                throw new MojoExecutionException("JSON Schema validation failed");
            }
        }
    }
}
