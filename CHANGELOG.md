Changelog
=========

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- ...

### Changed
- ...

### Deprecated
- ...

### Removed
- ...

### Fixed
- Escape string values that may contain Java special characters

## [1.2.0] - 2026-03-01

### Added
- New schema extension 'x-json-deserializer' to specify a custom Jackson deserializer class.
- New config parameter 'durationClassName' to specify a custom class to represent string schemas with format "duration" in generated code.
- New config parameter 'dateClassName' to specify a custom class to represent string schemas with format "date" in generated code.
- New config parameter 'dateTimeClassName' to specify a custom class to represent string schemas with format "date-time" in generated code.
- Support 'minLength' and 'maxLength' for Schema annotations on schemas of type 'string'.
- 
### Changed
- Bump dependency com.networknt:json-schema-validator to v3.0.0.
- JsonFormat annotation on strings with format "date" or "date-time" made optional and customizable through a new schema extension 'x-json-format'.

### Fixed
- Generate valid Java and Kotlin code for schema components of type 'object' with no properties.
- Detect correct type when resolving $ref to schema component of type 'array'.
- Skip import of classes in same package as generated code.
 
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
