jsonschema2java
===============

[![CI](https://github.com/torand/jsonschema2java/actions/workflows/continuous-integration.yml/badge.svg)](https://github.com/torand/jsonschema2java/actions/workflows/continuous-integration.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.torand/jsonschema2java.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aio.github.torand%20a%3Ajsonschema2java)
[![Javadocs](http://www.javadoc.io/badge/io.github.torand/jsonschema2java.svg)](https://www.javadoc.io/doc/io.github.torand/jsonschema2java)

A Maven plugin to generate Java models (POJOs) from [JSON Schema](https://json-schema.org/) files.

## Table of contents

- [Overview](#Overview)
- [Usage](#Usage)
- [Configuration](#Configuration)
- [JSON Schema extensions](#JSON_Schema_extensions)
- [Limitations](#Limitations)
- [Guidelines](#Guidelines)
- [License](#License)

## Overview

Include this Maven plugin in any Java project processing JSON payloads to enable a [Contract First](https://dzone.com/articles/designing-rest-api-what-is-contract-first) build workflow.
The current version supports the JSON Schema specification version "2020-12".

The JSON Schema files are read, parsed and validated using the [networknt/json-schema-validator](https://github.com/networknt/json-schema-validator) library.
For each JSON Schema file a Java class or enum definition is written to a specified output directory.
The generated source code is compatible with Java 17+ and optionally adds annotations from the following libraries:

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
| searchRootDir                       | Project root dir  | Root directory to search for schema files from                                                                                      |
| searchFilePattern                   |                   | Schema file path search pattern. Supports 'glob' patterns.                                                                          |
| schemaIdRootUri                     |                   | Root URI of $id property in schema files. Path elements beyond this value must correspond to subdirectories inside 'searchRootDir'. |
| outputDir                           | Project build dir | Directory to write Java/Kotllin code files to                                                                                       |
| rootPackage                         |                   | Root package path of output Java/Kotlin classes                                                                                     |
| pojoNameSuffix                      | "Dto"             | Suffix for POJO (model) class and enum names                                                                                        |
| pojosAsRecords                      | true              | Whether to output Java records instead of Java classes                                                                              |
| addOpenApiSchemaAnnotations         | false             | Whether to generate model files with OpenAPI schema annotations                                                                     |
| addJsonPropertyAnnotations          | true              | Whether to generate model files with JSON property annotations                                                                      |
| addJakartaBeanValidationAnnotations | true              | Whether to generate model files with Jakarta Bean Validation annotations                                                            |
| useKotlinSyntax                     | false             | Whether to generate model files with Kotlin syntax                                                                                  |
| verbose                             | false             | Whether to log extra details                                                                                                        |

## JSON Schema extensions

The JSON Schema specification is augmented with the following extension properties:

| Extension               | Type    | Allowed where                        | Description                                                               |
|-------------------------|---------|--------------------------------------|---------------------------------------------------------------------------|
| x-json-serializer       | String  | In a property schema                 | Fully qualified classname of a JSON serializer class for the property     |
| x-validation-constraint | String  | In a property schema                 | Fully qualified classname of an annotation class to validate the property |
| x-nullable              | Boolean | In a property schema                 | If `true` the type of the property can be `null`                          |
| x-model-subdir          | String  | In an enum or object schema          | Subdirectory to place the generated DTO model classes                     |
| x-deprecation-message   | String  | Everywhere `deprecated` can be used  | Describing why something is deprecated, and what to use instead           |

## Limitations

Relaxed, abstract schemas are useful for validation, not so much for code generation. As a general rule, to produce meaningful POJOs, strict schemas are necessary. Hence, the "type" property is mandatory.  

The following JSON Schema constructs are currently not supported:

* Values for the "format" property: "idn-email", "hostname", "idn-hostname", "ipv4", "ipv6", "uri-reference", "iri", "iri-reference", "uri-template", "json-pointer", "relative-json-pointer", "regex". These formats are currently mapped to the Java "String" type.
* Restrictions on the "number" type: "multipleOf".
* Properties with "const".
* "string" properties with: "contentMediaType", "contentEncoding", "contentSchema". 
* Dynamic objects: "if", "then", "unevaluatedProperties".
* Nested objects.
* Extended schema validation features: "patternProperties", "propertyNames", "minProperties", "maxProperties".
* Restrictions on arrays: "uniqueItems", tuple validation with "prefixItems".
* Dynamic arrays: "unevaluatedItems", "contains", "minContains", "maxContains".
* Documentation: "readOnly", "writeOnly".
* Property schema composition: "allOf", "anyOf", "not". Only supports two subschemas for "oneOf", one of which must be {"type": "null"}.
* Conditional subschemas: "dependentRequired", "dependentSchemas", "if"-"then"-"else".
* Structuring: "$anchor", "$defs", recursion using "$ref".

## Guidelines

### Inheritance

Inheritance is not supported, nor has JSON Schema such a construct. Inheritance can be "simulated" with composition using "allOf" on the root schema:


```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://my-domain.com/my-api/schema/vehicle",
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
  "$id": "https://my-domain.com/my-api/schema/car",
  "allOf" : [
    {
      "$ref": "https://my-domain.com/my-api/schema/vehicle"
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
  "$id": "https://my-domain.com/my-api/schema/motor-cycle",
  "allOf" : [
    {
      "$ref": "https://my-domain.com/my-api/schema/vehicle"
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

## License

This project is licensed under the [Apache-2.0 License](LICENSE).