package org.github.torand.jsonschema2java.utils;

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
