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

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.DialectId;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.keyword.NonValidationKeyword;
import io.github.torand.jsonschema2java.generators.Options;
import io.github.torand.jsonschema2java.utils.JsonSchema2JavaException;
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
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static io.github.torand.javacommons.lang.Exceptions.illegalStateException;
import static io.github.torand.jsonschema2java.collectors.Extensions.EXT_MODEL_SUBDIR;
import static io.github.torand.jsonschema2java.utils.StringUtils.toPascalCase;
import static java.util.Objects.isNull;

/**
 * Resolves (loads) external JSON Schemas referenced in a JSON Schema.
 */
public class SchemaResolver {
    private final Options opts;

    private SchemaRegistry schemaFactory;

    public SchemaResolver(Options opts) {
        this.opts = opts;
    }

    public static List<String> validate(Path schemaFile) {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder()
            // By default, the JDK regular expression implementation which is not ECMA 262 compliant, is used.
            // Note that setting this requires including optional dependencies
            // .regularExpressionFactory(GraalJSRegularExpressionFactory.getInstance());
            // .regularExpressionFactory(JoniRegularExpressionFactory.getInstance());
            .formatAssertionsEnabled(true)
            .build();

        SchemaRegistry metaSchemaFactory = SchemaRegistry.withDialect(Dialects.getDraft202012(),builder -> builder.schemaRegistryConfig(config));

        // Due to the mapping the meta-schema will be retrieved from the classpath at classpath:draft/2020-12/schema.
        Schema metaSchema = metaSchemaFactory.getSchema(SchemaLocation.of(DialectId.DRAFT_2020_12));
        String schemaContent;

        try {
            schemaContent = new String(Files.readAllBytes(schemaFile));
        } catch (IOException e) {
            throw new JsonSchema2JavaException(e);
        }

        List<Error> messages = metaSchema.validate(schemaContent, InputFormat.JSON);

        return messages.stream()
            .map(msg -> "%s %s".formatted(msg.getEvaluationPath().toString(), msg.getMessage()))
            .toList();
    }

    public JsonSchemaDef load(Path schemaFile) {
        Schema schema;
        try (InputStream schemaStream = new FileInputStream(schemaFile.toFile())) {
            SchemaRegistry factory = getSchemaFactory();
            schema = factory.getSchema(schemaStream);
        } catch (IOException e) {
            throw new JsonSchema2JavaException("Failed to open schema file %s".formatted(schemaFile), e);
        }

        String schemaName = getSchemaName(schemaFile);

        return new JsonSchemaDef(schemaName, schema);
    }

    public Optional<JsonSchemaDef> get(URI ref) {
        Schema schema = getSchemaFactory().getSchema(SchemaLocation.of(ref.toString()));

        if (isNull(schema)) {
            return Optional.empty();
        }

        return Optional.of(new JsonSchemaDef(getTypeName(ref), schema));
    }

    public JsonSchemaDef getOrThrow(URI ref) {
        return get(ref).orElseThrow(illegalStateException("Schema %s not found", ref));
    }

    public static String getTypeName(URI ref) {
        String refStr = ref.toString();

        int lastSlashIdx = refStr.lastIndexOf('/');
        if (lastSlashIdx != -1) {
            refStr = refStr.substring(lastSlashIdx + 1);
        }

        int extIdx = refStr.lastIndexOf('.');
        if (extIdx != -1) {
            refStr = refStr.substring(0, extIdx);
        }

        return toPascalCase(refStr);
    }

    public Optional<String> getModelSubpackage(URI ref) {
        JsonSchemaDef schema = getOrThrow(ref);
        return schema.extensions()
            .getString(EXT_MODEL_SUBDIR)
            .map(subdir -> subdir.replace("/", "."));
    }

    public boolean isEnumType(URI ref) {
        return get(ref).map(SchemaResolver::isEnumType).orElse(false);
    }

    public boolean isObjectType(URI ref) {
        return get(ref).map(SchemaResolver::isObjectType).orElse(false);
    }

    public boolean isArrayType(URI ref) {
        return get(ref).map(SchemaResolver::isArrayType).orElse(false);
    }

    public boolean isCompoundType(URI ref) {
        return get(ref).map(SchemaResolver::isCompoundType).orElse(false);
    }

    public boolean isPrimitiveType(URI ref) {
        return get(ref).map(SchemaResolver::isPrimitiveType).orElse(false);
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
            throw new JsonSchema2JavaException(e);
        }

        return schemaFiles;
    }

    private static String getSchemaName(Path schemaFile) {
        String filenameWithExt = schemaFile.getFileName().toString();
        int dotIdx = filenameWithExt.lastIndexOf('.');
        return toPascalCase(filenameWithExt.substring(0, dotIdx));
    }

    private SchemaRegistry getSchemaFactory() {
        if (schemaFactory == null) {
            Dialect.Builder metaSchemaBuilder = Dialect.builder(Dialects.getDraft202012());
            Extensions.KEYWORDS.forEach(extKeyword ->
                metaSchemaBuilder.keyword(new NonValidationKeyword(extKeyword))
            );

            schemaFactory = SchemaRegistry.withDialect(Dialects.getDraft202012(),
                builder -> builder.schemas(createSchemaDataProvider())
            );
        }

        return schemaFactory;
    }

    private Function<String, String> createSchemaDataProvider() {
        return iri -> {
            String schemaIdRootUri = opts.schemaIdRootUri().toString();
            if (schemaIdRootUri.endsWith("/")) {
                schemaIdRootUri = schemaIdRootUri.substring(0, schemaIdRootUri.length() - 1);
            }

            if (iri.startsWith(schemaIdRootUri)) {
                String subIri = iri.substring(schemaIdRootUri.length() + 1); // Skip trailing slash

                int typeNameIdx = subIri.lastIndexOf('/');
                String path = (typeNameIdx == -1) ? "" : subIri.substring(0, typeNameIdx);

                String typeName = getTypeName(URI.create(iri));

                Path schemaFilePath = Paths.get(opts.searchRootDir(), path, "%s.json".formatted(typeName));

                try {
                    return Files.readString(schemaFilePath);
                } catch (IOException e) {
                    throw new JsonSchema2JavaException("Failed to read schema file: %s".formatted(schemaFilePath), e);
                }
            } else {
                throw new JsonSchema2JavaException("Unexpected root URI in $id: %s".formatted(iri));
            }
        };
    }
}
