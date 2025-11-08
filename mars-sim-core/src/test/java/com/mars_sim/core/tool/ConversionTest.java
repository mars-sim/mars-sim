package com.mars_sim.core.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

class ConversionTest {
    @Test
    void testCapitalize() {
        var seed = "One two THREE";
        var changed = Conversion.capitalize(seed);
        assertEquals("Changed", "One Two Three", changed);
    }

    @Test
    void testSplit() {
        var seed = "OneTwoThree";
        var changed = Conversion.split(seed);
        assertEquals("Changed", "One Two Three", changed);
    }


    @Test
    void testTrimShort() {
        var seed = "small";
        var trimmed = Conversion.trim(seed, 10);
        assertEquals("Trimmed", seed, trimmed);
    }

    @Test
    void testTrimLong() {
        var seed = "Long text forever";
        var trimmed = Conversion.trim(seed, 10);
        assertEquals("Trimmed length", 10, trimmed.length());
        assertTrue("Has ending", trimmed.endsWith("..."));
    }
}
