/**
 * Mars Simulation Project
 * StandardPowerSource.java
 * @version 2.85 26.7.2008
 * @author Sebastien Venot
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.simulation.structure.building.Building;


public class FuelPowerSource extends PowerSource implements Serializable {

    private static final long serialVersionUID = 1L;
    private final static String TYPE = "Fuel Power Source";
    private boolean toggle = false;
    

    /**
     * @param type
     * @param maxPower
     */
    public FuelPowerSource(double maxPower) {
	super(TYPE, maxPower);
    }

    /* 
     * 
     */
    @Override
    public double getCurrentPower(Building building) {
	return 0;
    }
    
    public void toggleON() {
	toggle = true;
    }
    
    public void toggleOFF() {
	toggle = false;
    }
    
    public boolean isToggleON() {
	return toggle;
    }

}
