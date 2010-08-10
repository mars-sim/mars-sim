/**
 * Mars Simulation Project
 * PartSalvage.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.manufacture;

import java.io.Serializable;

/**
 * A part that can be returned in a salvage process.
 */
public class PartSalvage implements Serializable {

    // Data members
    private String name;
    private int number;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getNumber() {
        return number;
    }
    
    public void setNumber(int number) {
        this.number = number;
    }
    
    @Override
    public String toString() {
        return name;
    }
}