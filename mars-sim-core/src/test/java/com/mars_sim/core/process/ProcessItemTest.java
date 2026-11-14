package com.mars_sim.core.process;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.resource.ItemType;

class ProcessItemTest {

    @Test
    void testConsumerablePropertyDefaultsFalse() {
        ProcessItem item = new ProcessItem(1, "water", ItemType.AMOUNT_RESOURCE, 1.0D);

        assertFalse(item.isConsumable());
    }

    @Test
    void testConsumerablePropertyCanBeSet() {
        ProcessItem item = new ProcessItem(2, "water", ItemType.AMOUNT_RESOURCE, 2.0D, true);

        assertTrue(item.isConsumable());
    }
}
