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
package io.github.torand.jsonschema2java.collectors;

import com.networknt.schema.AbsoluteIri;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.NonValidationKeyword;
import com.networknt.schema.SchemaId;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.resource.SchemaMapper;
import io.github.torand.jsonschema2java.generators.Options;
import io.github.torand.jsonschema2java.utils.JsonSchemaDef;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.torand.jsonschema2java.collectors.Extensions.EXT_MODEL_SUBDIR;
import static io.github.torand.jsonschema2java.utils.Exceptions.illegalStateException;
import static io.github.torand.jsonschema2java.utils.StringHelper.toPascalCase;
import static java.util.Objects.isNull;

/**
 * Resolves (loads) external JSON Schemas referenced in a JSON Schema.
 */
public class SchemaResolver {
    private final Options opts;

    private JsonSchemaFactory schemaFactory;

    public SchemaResolver(Options opts) {
        this.opts = opts;
    }

    public static List<String> validate(Path schemaFile) {
        JsonSchemaFactory metaSchemaFactory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig.Builder builder = SchemaValidatorsConfig.builder();

        // By default the JDK regular expression implementation which is not ECMA 262 compliant is used.
        // Note that setting this requires including optional dependencies
        // builder.regularExpressionFactory(GraalJSRegularExpressionFactory.getInstance());
        // builder.regularExpressionFactory(JoniRegularExpressionFactory.getInstance());
        SchemaValidatorsConfig config = builder.build();

        // Due to the mapping the meta-schema will be retrieved from the classpath at classpath:draft/2020-12/schema.
        JsonSchema metaSchema = metaSchemaFactory.getSchema(SchemaLocation.of(SchemaId.V202012), config);
        String schemaContent;

        try {
            schemaContent = new String(Files.readAllBytes(schemaFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Set<ValidationMessage> messages = metaSchema.validate(schemaContent, InputFormat.JSON, executionContext -> {
            // By default since Draft 2019-09 the format keyword only generates annotations and not assertions
            executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
        });

        return messages.stream()
            .map(msg -> "%s %s".formatted(msg.getEvaluationPath().toString(), msg.getMessage()))
            .toList();
    }

    public JsonSchemaDef load(Path schemaFile) {
        JsonSchema schema;
        try (InputStream schemaStream = new FileInputStream(schemaFile.toFile())) {
            JsonSchemaFactory factory = getSchemaFactory();
            schema = factory.getSchema(schemaStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open schema file %s".formatted(schemaFile), e);
        }

        String schemaName = getSchemaName(schemaFile);

        return new JsonSchemaDef(schemaName, schema);
    }

    public Optional<JsonSchemaDef> get(URI $ref) {
        JsonSchema schema = getSchemaFactory().getSchema($ref);

        if (isNull(schema)) {
            return Optional.empty();
        }

        return Optional.of(new JsonSchemaDef(getTypeName($ref), schema));
    }

    public JsonSchemaDef getOrThrow(URI $ref) {
        return get($ref).orElseThrow(illegalStateException("Schema %s not found", $ref));
    }

    public static String getTypeName(URI $ref) {
        String $refStr = $ref.toString();

        int lastSlashIdx = $refStr.lastIndexOf('/');
        if (lastSlashIdx != -1) {
            $refStr = $refStr.substring(lastSlashIdx + 1);
        }

        int extIdx = $refStr.lastIndexOf('.');
        if (extIdx != -1) {
            $refStr = $refStr.substring(0, extIdx);
        }

        return toPascalCase($refStr);
    }

    public Optional<String> getModelSubpackage(URI $ref) {
        JsonSchemaDef schema = getOrThrow($ref);
        return schema.extensions()
            .getString(EXT_MODEL_SUBDIR)
            .map(subdir -> subdir.replaceAll("\\/", "."));
    }

    public boolean isEnumType(URI $ref) {
        return get($ref).map(SchemaResolver::isEnumType).orElse(false);
    }

    public boolean isObjectType(URI $ref) {
        return get($ref).map(SchemaResolver::isObjectType).orElse(false);
    }

    public boolean isArrayType(URI $ref) {
        return get($ref).map(SchemaResolver::isArrayType).orElse(false);
    }

    public boolean isCompoundType(URI $ref) {
        return get($ref).map(SchemaResolver::isCompoundType).orElse(false);
    }

    public boolean isPrimitiveType(URI $ref) {
        return get($ref).map(SchemaResolver::isPrimitiveType).orElse(false);
    }

    public static boolean isEnumType(JsonSchemaDef schema) {
        return schema.isEnum();
    }

    public static boolean isObjectType(JsonSchemaDef schema) {
        return schema.hasType("object");
    }

    public static boolean isArrayType(JsonSchemaDef schema) {
        return schema.hasType("array");
    }

    public static boolean isCompoundType(JsonSchemaDef schema) {
        return schema.hasAllOf();
    }

    /**
     * Indicates if schema represents a non-enumerated primitive JSON type, i.e. string, number, integer or boolean
     */
    public static boolean isPrimitiveType(JsonSchemaDef schema) {
        return !isEnumType(schema) && !isObjectType(schema) && !isArrayType(schema) && !isCompoundType(schema);
    }

    public static List<Path> findSchemaFiles(Path rootDir, String pattern) {
        List<Path> schemaFiles = new ArrayList<>();

        FileSystem fs = FileSystems.getDefault();
        PathMatcher matcher = fs.getPathMatcher(pattern);

        FileVisitor<Path> matcherVisitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attribs) {
                Path name = file.getFileName();
                if (matcher.matches(name)) {
                    schemaFiles.add(rootDir.resolve(name.toString()));
                }
                return FileVisitResult.CONTINUE;
            }
        };

        try {
            Files.walkFileTree(rootDir, matcherVisitor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return schemaFiles;
    }

    private static String getSchemaName(Path schemaFile) {
        String filenameWithExt = schemaFile.getFileName().toString();
        int dotIdx = filenameWithExt.lastIndexOf('.');
        return toPascalCase(filenameWithExt.substring(0, dotIdx));
    }

    private JsonSchemaFactory getSchemaFactory() {
        if (schemaFactory == null) {
            JsonMetaSchema.Builder metaSchemaBuilder = JsonMetaSchema.builder(JsonMetaSchema.getV202012());
            Extensions.KEYWORDS.forEach(extKeyword ->
                metaSchemaBuilder.keyword(new NonValidationKeyword(extKeyword))
            );

            schemaFactory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
                builder -> builder
                    .metaSchema(metaSchemaBuilder.build())
                    .schemaMappers(schemaMappers -> schemaMappers.add(createSchemaMapper()))
            );
        }

        return schemaFactory;
    }

    private SchemaMapper createSchemaMapper() {
        return absoluteIRI -> {
            String iri = absoluteIRI.toString();

            String schemaIdRootUri = opts.schemaIdRootUri.toString();
            if (schemaIdRootUri.endsWith("/")) {
                schemaIdRootUri = schemaIdRootUri.substring(0, schemaIdRootUri.length() - 1);
            }

            if (iri.startsWith(schemaIdRootUri)) {
                String subIri = iri.substring(schemaIdRootUri.length() + 1); // Skip trailing slash

                int typeNameIdx = subIri.lastIndexOf('/');
                String path = (typeNameIdx == -1) ? "" : subIri.substring(0, typeNameIdx);

                String typeName = getTypeName(URI.create(iri));
                return AbsoluteIri.of("file:" + Path.of(opts.searchRootDir, path, "%s.json".formatted(typeName)));
            } else {
                throw new RuntimeException("Unexpected root URI in $id: %s".formatted(iri));
            }
        };
    }
}
