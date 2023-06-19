/*
 * Mars Simulation Project
 * ShiftSpec.java
 * @date 2022-11-18
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure;

/**
 * A specification of a single Shift that belongs to a ShiftPattern.
 */
public class ShiftSpec {

    private String name;
    private int start;
    private int end;
    private int popPercentage;

    public ShiftSpec(String name, int start, int end, int popPerc) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.popPercentage = popPerc;
    }

    public String getName() {
        return name;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getPopPercentage() {
        return popPercentage;
    }

}
