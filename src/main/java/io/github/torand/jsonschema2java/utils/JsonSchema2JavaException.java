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
package io.github.torand.jsonschema2java.utils;

/**
 * Generic runtime exception thrown by this plugin.
 */
public class JsonSchema2JavaException extends RuntimeException {

    /**
     * Creates a runtime exception.
     * @param message the message.
     */
    public JsonSchema2JavaException(String message) {
        super(message);
    }

    /**
     * Creates a runtime exception.
     * @param message the message.
     * @param cause the inner cause.
     */
    public JsonSchema2JavaException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a runtime exception.
     * @param cause the inner cause.
     */
    public JsonSchema2JavaException(Throwable cause) {
        super(cause);
    }
}
