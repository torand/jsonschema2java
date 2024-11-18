package org.github.torand.jsonschema2java.utils;

public class KotlinTypeMapper {
    public static String toKotlinNative(String typeName) {
        return switch (typeName) {
            case "Integer" -> "Int";
            case "byte" -> "Byte";
            case "byte[]" -> "ByteArray";
            default -> typeName;
        };
    }
}
