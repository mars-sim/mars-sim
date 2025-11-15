package com.mars_sim.core.data;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RangeTest {

    @ParameterizedTest
    @CsvSource({
        "1, 10",
        "5, 15",
        "0, 100",
        "-10, 10"
        })
    void testGetRandomValue(int min, int max) {
        Range range = new Range(min, max);
        for(int i = 0; i < 10; i++) {
            double randomValue = range.getRandomValue();
            assertTrue(randomValue >= min && randomValue <= max);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "1, 10",
        "5, 15",
        "0, 100",
        "-10, 10"
        })
    void testIsBetween(int min, int max) {
        Range range = new Range(min, max);
        for(int i = min; i <= max; i++) {
            assertTrue(range.isBetween(i));
        }

        assertTrue(!range.isBetween(min-1));
        assertTrue(!range.isBetween(max+1));
    }
}
