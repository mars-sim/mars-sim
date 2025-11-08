package com.mars_sim.core.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

class ConversionTest {
    @Test
    void testCapitalize() {
        var seed = "One two THREE";
        var changed = Conversion.capitalize(seed);
        assertEquals(changed, "One Two Three", "Changed");
    }

    @Test
    void testSplit() {
        var seed = "OneTwoThree";
        var changed = Conversion.split(seed);
        assertEquals(changed, "One Two Three", "Changed");
    }


    @Test
    void testTrimShort() {
        var seed = "small";
        var trimmed = Conversion.trim(seed, 10);
        assertEquals(seed, trimmed, "Trimmed");
    }

    @Test
    void testTrimLong() {
        var seed = "Long text forever";
        var trimmed = Conversion.trim(seed, 10);
        assertEquals(10, trimmed.length(), "Trimmed length");
        assertTrue(trimmed.endsWith("..."), "Has ending");
    }
}
