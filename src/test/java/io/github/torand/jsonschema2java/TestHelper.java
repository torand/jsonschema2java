package io.github.torand.jsonschema2java;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.torand.jsonschema2java.utils.StringHelper.removeLineBreaks;
import static java.util.Objects.isNull;
import static org.junit.Assert.fail;

class TestHelper {

    private TestHelper() {}

    public static JsonSchema loadJsonSchema(String schemaResource) {
        JsonSchema schema = JsonSchemaFactory
            .getInstance(SpecVersion.VersionFlag.V202012)
            .getSchema(TestHelper.class.getResourceAsStream(schemaResource));

        return schema;
    }

    static Options getJavaOptions() {
        Options opts = new Options();
        opts.searchRootDir = "src/test/resources";
        opts.schemaIdRootUri = URI.create("https://my-domain.com/my-api/schemas");
        opts.rootPackage = "io.github.torand.test";
        opts.outputDir = "target/test-output/java";
        opts.addOpenApiSchemaAnnotations = true;
        opts.addJsonPropertyAnnotations = false;
        opts.useKotlinSyntax = false;
        opts.verbose = true;
        return opts;
    }

    static Options getKotlinOptions() {
        Options opts = new Options();
        opts.searchRootDir = "src/test/resources";
        opts.schemaIdRootUri = URI.create("https://my-domain.com/my-api/schemas");
        opts.rootPackage = "io.github.torand.test";
        opts.outputDir = "target/test-output/kotlin";
        opts.addOpenApiSchemaAnnotations = true;
        opts.addJsonPropertyAnnotations = false;
        opts.useKotlinSyntax = true;
        opts.verbose = true;
        return opts;
    }

    public static void assertSnippet(String path, String expectedSnippet) {
        try {
            Path actualPath = Path.of("target/test-output/" + path);
            String content = Files.readString(actualPath);
            MatcherAssert.assertThat(content, CoreMatchers.containsString(expectedSnippet));
        } catch (IOException e) {
            throw new RuntimeException("Could not find file by name %s".formatted(path), e);
        }
    }

    static void assertMatchingJavaFiles(String filename) {
        Path expectedPath = getResourcePath("expected-output/java/%s".formatted(filename));
        Path actualPath = Path.of("target/test-output/java/%s".formatted(filename));

        assertMatchingFiles(expectedPath, actualPath);
    }

    static void assertMatchingKotlinFiles(String filename) {
        Path expectedPath = getResourcePath("expected-output/kotlin/%s".formatted(filename));
        Path actualPath = Path.of("target/test-output/kotlin/%s".formatted(filename));

        assertMatchingFiles(expectedPath, actualPath);
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
