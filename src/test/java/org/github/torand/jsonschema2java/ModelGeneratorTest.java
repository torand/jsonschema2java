package org.github.torand.jsonschema2java;

import org.junit.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.github.torand.jsonschema2java.utils.StringHelper.isBlank;

public class ModelGeneratorTest {

    private static final Map<String, String> SCHEMAS = Map.of(
        "AddressV1", "common",
        "UserTypeV1", "",
        "UserV1", "",
        "ProductCategoryV1", "",
        "ProductV1", "",
        "OrderStatusV1", "",
        "OrderItemV1", "",
        "OrderV1", ""
    );

    @Test
    public void shouldGenerateJavaPojos() {
        Options opts = TestHelper.getJavaOptions();

        ModelGenerator modelGenerator = new ModelGenerator(opts);

        for (String schema : SCHEMAS.keySet()) {
            String modelSubDir = SCHEMAS.get(schema);
            Path schemaFile = Path.of(opts.searchRootDir, schema+".json");

            modelGenerator.generate(List.of(schemaFile));

            TestHelper.assertMatchingJavaFiles("model/%s%sDto.java".formatted(isBlank(modelSubDir) ? "" : modelSubDir+"/", schema));
        }
    }

    @Test
    public void shouldGenerateKotlinPojos() {
        Options opts = TestHelper.getKotlinOptions();

        ModelGenerator modelGenerator = new ModelGenerator(opts);

        for (String schema : SCHEMAS.keySet()) {
            String modelSubDir = SCHEMAS.get(schema);
            Path schemaFile = Path.of(opts.searchRootDir, schema+".json");

            modelGenerator.generate(List.of(schemaFile));

            TestHelper.assertMatchingKotlinFiles("model/%s%sDto.kt".formatted(isBlank(modelSubDir) ? "" : modelSubDir+"/", schema));
        }
    }
}
