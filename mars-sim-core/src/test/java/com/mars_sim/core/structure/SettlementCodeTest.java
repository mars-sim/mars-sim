/*
 * Mars Simulation Project
 * SettlementCodeTest.java
 * @date 2023-11-11
 * @author Barry Evans
 */
package com.mars_sim.core.structure;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;

/**
 * Tests the internals of the ShiftManager.
 */
public class SettlementCodeTest extends MarsSimUnitTest {

    /**
     * Test the code logic
     */
    @Test
    public void testSingleSettlement() {
        Settlement settlement = buildSettlement("First Second");
        assertEquals("FS", settlement.getSettlementCode(), "Single Settlement code");
    }

    /**
     * Tests the code logic based on words.
     */
    @Test
    public void testWordSettlement() {
        buildSettlement("First Second");
        Settlement settlement2 = buildSettlement("First Second Forth");

        assertEquals("FF", settlement2.getSettlementCode(), "Second Settlement code");
    }

    /**
     * Tests the code logic based on words.
     */
    @Test
    public void testLettersSettlement() {
        buildSettlement("First Second");
        Settlement settlement2 = buildSettlement("First Sx");

        assertEquals("FI", settlement2.getSettlementCode(), "Second Settlement code");
    }

    /**
     * Tests the code logic based on letters with punctuation.
     */
    @Test
    public void testLettersPuncSettlement() {
        buildSettlement("First Second");
        Settlement settlement2 = buildSettlement("F#-rst Sx");

        assertEquals("FR", settlement2.getSettlementCode(), "Second Settlement code");
    }
}
