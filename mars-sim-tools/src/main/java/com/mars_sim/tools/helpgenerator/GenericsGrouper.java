/*
 * Mars Simulation Project
 * GenericsGrouper.java
 * @date 2024-02-24
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A utility class to group items into named groups.
 */
public final class GenericsGrouper {

    /**
     * A named group of items
     */
    public static record NamedGroup<T> (String id, String name, List<T> items) {}

    /**
     * Group the items in the collection by the grouper function into named groups.
     * @param source The collection of items to group
     * @param grouper The function to group the items by
     * @return The list of named groups
     */
    public static <T> List<NamedGroup<T>> getGroups(Collection<T> source, Function<T,String> grouper) {
        var groups = source.stream().collect(Collectors.groupingBy(grouper));

        // Assign the grouped items into a single named group
        return groups.entrySet().stream()
                    .map(e -> new NamedGroup<>(generateId(e.getKey()), e.getKey(), e.getValue()))
                    .sorted((a,b) -> a.name.compareTo(b.name)).toList();

    }

    private static String generateId(String key) {
        return key.toLowerCase().replaceAll("\\W", "-");
    }

    private GenericsGrouper() {
        // Static utility class
    }
}