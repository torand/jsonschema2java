/*
 * Copyright (c) 2024-2026 Tore Eide Andersen
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
package io.github.torand.jsonschema2java.utils;

/**
 * A collection of Java package related utilities.
 */
public class PackageUtils {
    private PackageUtils() {}

    /**
     * Returns the class base name from specified fully qualified class name.
     * @param fqn the fully qualified class name.
     * @return the class base name.
     */
    public static String getClassNameFromFqn(String fqn) {
        int lastDot = fqn.lastIndexOf(".");
        if (lastDot == -1) {
            throw new IllegalArgumentException("Unexpected fully qualified class name: %s".formatted(fqn));
        }
        return fqn.substring(lastDot+1);
    }

    /**
     * Returns whether specified fully qualified class name refers to specified target package.
     * @param fqn the fully qualified class name
     * @param targetPackage the target package
     * @return true if specified fully qualified class name refers to specified target package; else false
     */
    public static boolean isFqnInPackage(String fqn, String targetPackage) {
        // Remove class name from fqn value
        int lastDotIdx = fqn.lastIndexOf(".");
        String typePackage = fqn.substring(0, lastDotIdx);

        return targetPackage.equals(typePackage);
    }

    /**
     * Returnss whether specified fully qualified class name refers to a Java package.
     * @param fqn the fully qualified class name
     * @return true if specified fully qualified class name refers to a Java package; else false
     */
    public static boolean isJavaPackage(String fqn) {
        return fqn.startsWith("java.");
    }
}
