package org.github.torand.jsonschema2java;

import org.github.torand.jsonschema2java.collectors.EnumInfoCollector;
import org.github.torand.jsonschema2java.collectors.PojoInfoCollector;
import org.github.torand.jsonschema2java.collectors.SchemaResolver;
import org.github.torand.jsonschema2java.model.EnumInfo;
import org.github.torand.jsonschema2java.model.PojoInfo;
import org.github.torand.jsonschema2java.utils.JsonSchemaDef;
import org.github.torand.jsonschema2java.writers.EnumWriter;
import org.github.torand.jsonschema2java.writers.PojoWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.github.torand.jsonschema2java.utils.StringHelper.pluralSuffix;
import static org.github.torand.jsonschema2java.writers.WriterFactory.createEnumWriter;
import static org.github.torand.jsonschema2java.writers.WriterFactory.createPojoWriter;

public class ModelGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ModelGenerator.class);

    private final Options opts;
    private final SchemaResolver schemaResolver;

    public ModelGenerator(Options opts) {
        this.opts = opts;
        this.schemaResolver = new SchemaResolver(opts);
    }

    public void generate(List<Path> schemaFiles) {
        int enumCount = 0;
        int pojoCount = 0;

        for (Path schemaFile : schemaFiles) {
            JsonSchemaDef schema = schemaResolver.load(schemaFile);
            String pojoName = schema.getName() + opts.pojoNameSuffix;

            if (schema.isEnum()) {
                generateEnumFile(pojoName, schema);
                enumCount++;
            }

            if (schema.isClass()) {
                generatePojoFile(pojoName, schema);
                pojoCount++;
            }
        }

        logger.info("Generated {} enum{}, {} pojo{} in directory {}", enumCount, pluralSuffix(enumCount), pojoCount, pluralSuffix(pojoCount), opts.getModelOutputDir(null));
    }

    private void generateEnumFile(String name, JsonSchemaDef schema) {
        if (opts.verbose) {
            logger.info("Generating model enum {}", name);
        }

        EnumInfoCollector enumInfoCollector = new EnumInfoCollector(opts);
        EnumInfo enumInfo = enumInfoCollector.getEnumInfo(name, schema);

        String enumFilename = name + opts.getFileExtension();
        try (EnumWriter enumWriter = createEnumWriter(enumFilename, opts, enumInfo.modelSubdir)) {
            enumWriter.write(enumInfo);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file %s".formatted(enumFilename), e);
        }
    }

    private void generatePojoFile(String name, JsonSchemaDef schema) {
        if (opts.verbose) {
            logger.info("Generating model class {}", name);
        }

        PojoInfoCollector pojoInfoCollector = new PojoInfoCollector(opts, schemaResolver);
        PojoInfo pojoInfo = pojoInfoCollector.getPojoInfo(name, schema);

        String pojoFilename = name + opts.getFileExtension();
        try (PojoWriter pojoWriter = createPojoWriter(pojoFilename, opts, pojoInfo.modelSubdir)) {
            pojoWriter.write(pojoInfo);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file %s".formatted(pojoFilename), e);
        }
    }
}
