/*
 * Mars Simulation Project
 * SettlementCodeTest.java
 * @date 2023-11-11
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import com.mars_sim.core.AbstractMarsSimUnitTest;

/**
 * Test the internals of the ShiftMansger
 */
public class SettlementCodeTest extends AbstractMarsSimUnitTest {

    /**
     * Test the code logic
     */
    public void testSingleSettlement() {
        Settlement settlement = buildSettlement("First Second");
        assertEquals("Single Settlement code", "FS", settlement.getSettlementCode());
    }

    /**
     * Test the code logic based on words
     */
    public void testWordSettlement() {
        buildSettlement("First Second");
        Settlement settlement2 = buildSettlement("First Second Forth");

        assertEquals("Second Settlement code", "FF", settlement2.getSettlementCode());
    }

    /**
     * Test the code logic based on words
     */
    public void testLettersSettlement() {
        buildSettlement("First Second");
        Settlement settlement2 = buildSettlement("First Sx");

        assertEquals("Second Settlement code", "FI", settlement2.getSettlementCode());
    }

    /**
     * Test the code logic based on letters with puncutation
     */
    public void testLettersPuncSettlement() {
        buildSettlement("First Second");
        Settlement settlement2 = buildSettlement("F#-rst Sx");

        assertEquals("Second Settlement code", "FR", settlement2.getSettlementCode());
    }
}