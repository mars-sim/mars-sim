/*
 * Mars Simulation Project
 * ShiftPattern.java
 * @date 2022-11-18
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure;

import java.util.List;

/**
 * This class represents the definition of a Shift Pattern template. It consistsof a number of Shits.
 */
public class ShiftPattern {

    private List<ShiftSpec> shifts;
    private String name;

    public ShiftPattern(String name, List<ShiftSpec> shifts) {
        this.name = name;
        this.shifts = shifts;
    }

    public List<ShiftSpec> getShifts() {
        return shifts;
    }

    public String getName() {
        return name;
    }

}
