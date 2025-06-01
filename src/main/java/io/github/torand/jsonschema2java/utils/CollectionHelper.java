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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class CollectionHelper {
    private CollectionHelper() {}

    public static <T> boolean isEmpty(Collection<T> collection) {
        return isNull(collection) || collection.isEmpty();
    }

    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return isNull(map) || map.isEmpty();
    }

    public static <T> boolean nonEmpty(Collection<T> collection) {
        return nonNull(collection) && !collection.isEmpty();
    }

    public static <K, V> boolean nonEmpty(Map<K, V> map) {
        return nonNull(map) && !map.isEmpty();
    }

    public static <T> Stream<T> streamSafely(Collection<T> collection) {
        return isNull(collection) ? Stream.empty() : collection.stream();
    }

    public static <T> Stream<T> streamConcat(Collection<T> collection1, Collection<T> collection2) {
        return Stream.concat(streamSafely(collection1), streamSafely(collection2));
    }

    public static <T> boolean containsOneOf(Collection<T> collection, T... values) {
        if (isNull(values) || values.length == 0) {
            throw new IllegalArgumentException("No values specified");
        }
        if (isEmpty(collection)) {
            return false;
        }
        return Stream.of(values).anyMatch(collection::contains);
    }

    public static <T> Stream<T> asStream(Iterator<T> iterator) {
        Iterable<T> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
