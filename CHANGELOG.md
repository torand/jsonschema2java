Changelog
=========

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- New schema extension 'x-json-deserializer' to specify a custom Jackson deserializer class.
- New config parameter 'dateClassName' to specify a custom class to represent string schemas with format "date" in generated code.
- New config parameter 'dateTimeClassName' to specify a custom class to represent string schemas with format "date-time" in generated code.

### Changed
- Bump dependency com.networknt:json-schema-validator to v3.0.0.
- JsonFormat annotation on strings with format "date" or "date-time" made optional and customizable through a new schema extension 'x-json-format'.

### Deprecated
- ...

### Removed
- ...

### Fixed
- Generate valid Java and Kotlin code for POJOs with no properties.
- Detect correct type when resolving $ref to component of type 'array'.

## [1.1.3] - 2025-10-10

### Changed
- Bump dependency com.networknt:json-schema-validator to v1.5.9

### Fixed
- Bean validation annotations on primitive subtypes of compound pojo property types now generated.

## [1.1.2] - 2025-06-01

### Changed
- Bump dependency com.networknt:json-schema-validator to v1.5.7

## [1.1.1] - 2025-02-05

### Changed
- Bump dependency com.networknt:json-schema-validator to v1.5.5

## [1.1.0] - 2024-12-06

### Added
- Customizable indentation with new config params 'indentWithTab' and 'indentSize'
 
### Changed
- Array with uniqueItems = true now maps to java.util.Set, not java.util.List

## [1.0.0] - 2024-12-01

### Added
- Initial version
