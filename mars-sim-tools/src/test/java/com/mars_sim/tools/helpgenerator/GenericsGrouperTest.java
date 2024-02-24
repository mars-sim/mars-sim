package com.mars_sim.tools.helpgenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class GenericsGrouperTest {

    private record Item(String name, String description) {}

    @Test
    void testGroupsByFirstLetter() {
        List<Item> items = List.of(new Item("Aa", "a"), new Item("Ab", "b"),
                                new Item("Ba", "c"), new Item("CF", "f"));
                                
        var groups = GenericsGrouper.getGroups(items, v -> v.name().substring(0, 1));

        assertEquals(3, groups.size());
        assertEquals(2, groups.get(0).items().size());
        assertEquals(1, groups.get(1).items().size());

    }

    @Test
    void testGroupsByType() {
        List<Item> items = List.of(new Item("Aa", "a"), new Item("Ab", "a"),
                                new Item("Ba", "a"), new Item("CF", "f"));
                                
        var groups = GenericsGrouper.getGroups(items, v -> v.description());

        assertEquals(2, groups.size());
        assertEquals(3, groups.get(0).items().size());
        assertEquals(1, groups.get(1).items().size());
    }
}
