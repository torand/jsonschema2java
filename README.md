JSONSchema2Java
===============

[![CI](https://github.com/torand/jsonschema2java/actions/workflows/continuous-integration.yml/badge.svg)](https://github.com/torand/jsonschema2java/actions/workflows/continuous-integration.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.torand/jsonschema2java.svg?label=maven%20central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aio.github.torand%20a%3Ajsonschema2java)
[![Javadocs](https://javadoc.io/badge2/io.github.torand/jsonschema2java/javadoc.svg)](https://javadoc.io/doc/io.github.torand/jsonschema2java)
[![Coverage](https://coveralls.io/repos/github/torand/jsonschema2java/badge.svg?branch=main)](https://coveralls.io/github/torand/jsonschema2java?branch=main)
[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-orange)](LICENSE)

A Maven plugin to generate Java models (POJOs) from [JSON Schema](https://json-schema.org/) files.

## Table of contents

- [Overview](#Overview)
- [Usage](#Usage)
- [Type mapping](#Type-mapping)
- [Guidelines](#Guidelines)
- [Limitations](#Limitations)
- [License](#License)

## Overview

Include this Maven plugin in any Java project processing JSON payloads to enable a [Contract First](https://dzone.com/articles/designing-rest-api-what-is-contract-first) build workflow.
The current version supports the JSON Schema specification version "2020-12".

The JSON Schema files are read, parsed and validated using the [networknt/json-schema-validator](https://github.com/networknt/json-schema-validator) library.
For each JSON Schema file a Java class or enum definition is written to a specified output directory.
The generated source code is compatible with Java 17+ and optionally includes annotations from the following libraries:

* [Microprofile OpenAPI](https://download.eclipse.org/microprofile/microprofile-open-api-2.0/microprofile-openapi-spec-2.0.html)
* [Jakarta Bean Validation](https://jakarta.ee/specifications/bean-validation/)
* [Jackson](https://github.com/FasterXML/jackson)

## Usage

### Include in a Maven POM file

```xml
<build>
  <plugins>
    <plugin>
      <groupId>io.github.torand</groupId>
      <artifactId>jsonschema2java</artifactId>
      <version>1.0.0</version>
      <executions>
        <execution>
          <id>generate</id>
          <phase>generate-sources</phase>
          <goals>
            <goal>generate</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <searchRootDir>.</searchRootDir>
        <searchFilePattern>*.json</searchFilePattern>
        <schemaIdRootUri>https://my-domain.com/my-api/schemas</schemaIdRootUri>
        <outputDir>target/jsonschema2java</outputDir>
        <rootPackage>io.github.torand.mymodel</rootPackage>
      </configuration>
    </plugin>
  </plugins>
</build>
```

### Run from the command line

```bash
$ mvn io.github.torand:jsonschema2java:1.0.0:generate \
  -DsearchRootDir=. \
  -DsearchFilePattern=*.json \
  -DschemaIdRootUri=https://my-domain.com/my-api/schemas \
  -DoutputDir=target/jsonschema2java \
  -DrootPackage=io.github.torand.mymodel
```

## Configuration

| Parameter                           | Default           | Description                                                                                                                         |
|-------------------------------------|-------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| searchRootDir                       | Project root dir  | Root directory to search for schema files                                                                                           |
| searchFilePattern                   |                   | Schema file path search pattern. Supports [glob](https://github.com/begin/globbing/blob/master/cheatsheet.md) patterns              |
| schemaIdRootUri                     |                   | Root URI of $id property in schema files. Path elements beyond this value must correspond to subdirectories inside 'searchRootDir'. |
| outputDir                           | Project build dir | Directory to write POJO source code files to                                                                                        |
| rootPackage                         |                   | Root package path of output POJO classes and enums                                                                                  |
| pojoNameSuffix                      | "Dto"             | Suffix for POJO class and enum names                                                                                                |
| pojosAsRecords                      | true              | Whether to output Java records instead of Java classes                                                                              |
| addOpenApiSchemaAnnotations         | false             | Whether to generate model files with OpenAPI schema annotations                                                                     |
| addJsonPropertyAnnotations          | true              | Whether to generate model files with JSON property annotations                                                                      |
| addJakartaBeanValidationAnnotations | true              | Whether to generate model files with Jakarta Bean Validation annotations                                                            |
| useKotlinSyntax                     | false             | Whether to generate model files with Kotlin syntax                                                                                  |
| verbose                             | false             | Whether to log extra details                                                                                                        |

## Type mapping

JSON schema types and formats map to the following Java and Kotlin types in generated source code:

| Type                                         | Format            | Java type               | Kotlin type             |
|----------------------------------------------|-------------------|-------------------------|-------------------------|
| "array"                                      | N/A               | java.util.List          | java.util.List          |
| "array" with "uniqueItems" = true            | N/A               | java.util.Set           | java.util.Set           |
| "boolean"                                    | N/A               | Boolean                 | Boolean                 |
| "integer"                                    |                   | Integer                 | Int                     |
| "integer"                                    | "int32"           | Integer                 | Int                     |
| "integer"                                    | "int64"           | Long                    | Long                    |
| "number"                                     |                   | java.math.BigDecimal    | java.math.BigDecimal    |
| "number"                                     | "double"          | Double                  | Double                  |
| "number"                                     | "float"           | Float                   | Float                   |
| "object"                                     | N/A               | 1)                      | 1)                      |
| "object" with "additionalProperties" = {...} | N/A               | java.util.Map           | java.util.Map           |
| "string"                                     |                   | String                  | String                  |
| "string"                                     | "uri"             | java.net.URI            | java.net.URI            |
| "string"                                     | "uuid"            | java.util.UUID          | java.util.UUID          |
| "string"                                     | "duration" 2)     | java.time.Duration      | java.time.Duration      |
| "string"                                     | "date" 3)         | java.time.LocalDate     | java.time.LocalDate     |
| "string"                                     | "date-time" 4)    | java.time.LocalDateTime | java.time.LocalDateTime |
| "string"                                     | "binary"          | byte[]                  | ByteArray               |
| "string"                                     | All other formats | String                  | String                  |

### Footnotes

1. Inline objects not supported.
2. Expects string in the [ISO 8601](https://www.iso.org/iso-8601-date-and-time-format.html) duration format.
3. Expects string in the [ISO 8601](https://www.iso.org/iso-8601-date-and-time-format.html) local date format.
4. Expects string in the [ISO 8601](https://www.iso.org/iso-8601-date-and-time-format.html) local date time format (without milliseconds).

## Constraint mapping

JSON schema restriction properties map to the following Jakarta Bean Validation annotations (when enabled):

| Type      | Restriction                         | Annotation                |
|-----------|-------------------------------------|---------------------------|
| "array"   |                                     | @Valid                    |
| "array"   | Not nullable                        | @Valid @NotNull           |
| "array"   | "minItems": n                       | @Valid @Size(min = n)     |
| "array"   | "maxItems": n                       | @Valid @Size(max = n)     |
| "boolean" | Not nullable                        | @NotNull                  |
| "integer" | Not nullable                        | @NotNull                  |
| "integer" | "minimum": n                        | @Min(n)                   |
| "integer" | "maximum": n                        | @Max(n)                   |
| "number"  | Not nullable                        | @NotNull                  |
| "number"  | "minimum": n 1)                     | @Min(n)                   |
| "number"  | "maximum": n 1)                     | @Max(n)                   |
| "object"  |                                     | @Valid                    |
| "object"  | Not nullable                        | @Valid @NotNull           |
| "string"  | Not nullable                        | @NotBlank                 |
| "string"  | Not nullable and "format": "binary" | @NotEmpty                 |
| "string"  | "pattern": "expr"                   | @Pattern(regexp = "expr") |
| "string"  | "minLength": n                      | @Size(min = n)            |
| "string"  | "maxLength": n                      | @Size(max = n)            |
| "string"  | "format": "email"                   | @Email                    |

### Footnotes

1. When "format" is not specified only (i.e. BigDecimal).

## Guidelines

### General

Relaxed, abstract schemas are useful for validation, not so much for code generation. As a general rule, to produce meaningful POJOs, strict schemas are necessary. Hence, the "type" property is mandatory.

### Customization

The code generation can be customized per JSON Schema by using the following extension properties in the schema definition:

| Extension property      | Type    | Allowed where                        | Description                                                               |
|-------------------------|---------|--------------------------------------|---------------------------------------------------------------------------|
| x-json-serializer       | String  | In a property schema                 | Fully qualified classname of a JSON serializer class for the property     |
| x-validation-constraint | String  | In a property schema                 | Fully qualified classname of an annotation class to validate the property |
| x-nullable              | Boolean | In a property schema                 | If `true` the type of the property can be `null`                          |
| x-model-subdir          | String  | In an enum or object schema          | Subdirectory to place the generated DTO model classes                     |
| x-deprecation-message   | String  | Everywhere `deprecated` can be used  | Describing why something is deprecated, and what to use instead           |

### Inheritance

Inheritance is not supported, nor has JSON Schema such a construct. Inheritance can be "simulated" with composition using "allOf" on the root schema:

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://my-domain.com/my-api/schemas/vehicle",
  "type": "object",
  "properties": {
    "brand": {
      "type": "string"
    }
  },
  "required": [ "brand" ]
}
```
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://my-domain.com/my-api/schemas/car",
  "allOf" : [
    {
      "$ref": "https://my-domain.com/my-api/schemas/vehicle"
    },
    {
      "type": "object",
      "properties": {
        "doors": {
          "type": "integer"
        }
      },
      "required": [ "doors" ]
    }
  ]
}
```
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://my-domain.com/my-api/schemas/motor-cycle",
  "allOf" : [
    {
      "$ref": "https://my-domain.com/my-api/schemas/vehicle"
    },
    {
      "type": "object",
      "properties": {
        "sidekick": {
          "type": "boolean"
        }
      },
      "required": [ "sidekick" ]
    }
  ]
}
```

This produces the following Java records:

```java
public record VehicleDto (
    @NotBlank String brand
) {}
```
```java
public record CarDto (
    @NotBlank String brand,
    @NotNull Integer doors
) {}
```
```java
public record MotorCycleDto (
    @NotBlank String brand,
    @NotNull Boolean sidekick
) {}
```

## Limitations

The following JSON Schema constructs are currently not supported:

* Restrictions on the "number" type: "multipleOf".
* Properties with "const".
* "string" properties with: "contentMediaType", "contentEncoding", "contentSchema".
* Dynamic objects: "if", "then", "unevaluatedProperties".
* Nested inline objects.
* Extended schema validation features: "patternProperties", "propertyNames", "minProperties", "maxProperties".
* Restrictions on arrays: tuple validation with "prefixItems".
* Dynamic arrays: "unevaluatedItems", "contains", "minContains", "maxContains".
* Documentation: "readOnly", "writeOnly".
* Property schema composition: "allOf", "anyOf", "not". Only supports two subschemas for "oneOf", one of which must be {"type": "null"}.
* Conditional subschemas: "dependentRequired", "dependentSchemas", "if"-"then"-"else".
* Structuring: "$anchor", "$defs", recursion using "$ref".

## License

This project is licensed under the [Apache-2.0 License](LICENSE).