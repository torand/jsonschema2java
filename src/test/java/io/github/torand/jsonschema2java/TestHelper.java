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

import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;
import io.github.torand.jsonschema2java.generators.Options;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.torand.jsonschema2java.utils.StringUtils.removeLineBreaks;
import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class TestHelper {

    private TestHelper() {}

    public static Schema loadJsonSchema(String schemaResource) {
        return SchemaRegistry.withDialect(Dialects.getDraft202012())
            .getSchema(TestHelper.class.getResourceAsStream(schemaResource));
    }

    public static Options getJavaOptions() {
        return Options.defaults()
            .withSearchRootDir("src/test/resources")
            .withSchemaIdRootUri(URI.create("https://my-domain.com/my-api/schemas"))
            .withRootPackage("io.github.torand.test.model")
            .withOutputDir("target/test-output/java/model")
            .withAddMpOpenApiAnnotations(true)
            .withAddJsonPropertyAnnotations(false)
            .withUseKotlinSyntax(false)
            .withVerbose(true);
    }

    public static Options getKotlinOptions() {
        return Options.defaults()
            .withSearchRootDir("src/test/resources")
            .withSchemaIdRootUri(URI.create("https://my-domain.com/my-api/schemas"))
            .withRootPackage("io.github.torand.test.model")
            .withOutputDir("target/test-output/kotlin/model")
            .withAddMpOpenApiAnnotations(true)
            .withAddJsonPropertyAnnotations(false)
            .withUseKotlinSyntax(true)
            .withVerbose(true);
    }

    public static void assertSnippet(String path, String expectedSnippet) {
        try {
            Path actualPath = Path.of("target/test-output/" + path);
            String content = Files.readString(actualPath);
            assertThat(content).contains(expectedSnippet);
        } catch (IOException e) {
            throw new RuntimeException("Could not find file by name %s".formatted(path), e);
        }
    }

    public static void assertMatchingJavaFiles(String filename) {
        Path expectedPath = getResourcePath("expected-output/java/model/%s".formatted(filename));
        Path actualPath = Path.of("target/test-output/java/model/%s".formatted(filename));

        assertMatchingFiles(expectedPath, actualPath);
    }

    public static void assertMatchingKotlinFiles(String filename) {
        Path expectedPath = getResourcePath("expected-output/kotlin/model/%s".formatted(filename));
        Path actualPath = Path.of("target/test-output/kotlin/model/%s".formatted(filename));

        assertMatchingFiles(expectedPath, actualPath);
    }

    public static JsonNode parseJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.reader().readTree(json);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    private static void assertMatchingFiles(Path expectedPath, Path actualPath) {
        try {
            int mismatchPos = (int)Files.mismatch(expectedPath, actualPath);
            if (mismatchPos != -1) {
                System.out.printf("Unexpected content in %s at position %d:%n", actualPath, mismatchPos);
                printDiff(expectedPath, actualPath, mismatchPos);
                fail("Actual file %s does not match expected file %s".formatted(actualPath, expectedPath));
            }
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    private static void printDiff(Path expected, Path actual, int mismatchPos) throws IOException {
        String expectedContent = Files.readString(expected);
        String actualContent = Files.readString(actual);

        int printFrom = Math.max(0, mismatchPos - 50);
        int printTo = Math.min(mismatchPos + 60, Math.min(expectedContent.length(), actualContent.length()));

        System.out.printf("Expected content : %s%n", removeLineBreaks(expectedContent.substring(printFrom, printTo)));
        System.out.printf("Actual content   : %s%n", removeLineBreaks(actualContent.substring(printFrom, printTo)));
        System.out.printf("                   %s^%s%n", "-".repeat(mismatchPos-printFrom), "-".repeat(printTo-mismatchPos));
    }

    private static URI getResourceUri(String name) {
        try {
            URL resource = TestHelper.class.getResource("/" + name);
            if (isNull(resource)) {
                throw new IllegalArgumentException("Resource %s not found".formatted(name));
            }
            return resource.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to create URI for resource " + name, e);
        }
    }

    private static Path getResourcePath(String name) {
        return Paths.get(getResourceUri(name));
    }
}
