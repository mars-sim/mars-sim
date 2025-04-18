/*
 * Mars Simulation Project
 * ManufactureProcess.java
 * @date 2022-07-26
 * @author Scott Davis
 */

package com.mars_sim.core.manufacture;

import com.mars_sim.core.building.function.Manufacture;

/**
 * A manufacturing process.
 */
public class ManufactureProcess extends WorkshopProcess {
	
	/**
	 * Constructor.
	 * 
	 * @param info     information about the process.
	 * @param workshop the manufacturing workshop where the process is taking place.
	 */
	public ManufactureProcess(ManufactureProcessInfo info, Manufacture workshop) {
		super(workshop, info);
	}
	
    /**
     * Stop this salvage process.
     * @param premature is the process being stopped prematurely?
     */
    @Override
    public void stopProcess(boolean premature) {
        getWorkshop().endManufacturingProcess(this, premature);
    }
}
