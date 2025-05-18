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
		super(info.getName(), workshop, info);
	}
	
	/**
	 * Start the process by adding to the Workshop active list and claim input resources
	 * @return Was the process started
	 */
	@Override
	public boolean startProcess() {
		if (!getWorkshop().addProcess(this)) {
			return false;
		}
		var settlement = getBuilding().getSettlement();

		getInfo().retrieveInputs(settlement);

		return true;
	}

    /**
     * Stop this salvage process.
     * @param premature is the process being stopped prematurely?
     */
    @Override
    public void stopProcess(boolean premature) {	
		
		var b = getBuilding();	
		if (!premature) {
			depositOutputs();
		}
		else {
			returnInputs();
		}

		super.stopProcess(premature);
		
		// Record process finish
		b.getAssociatedSettlement().recordProcess(getInfo().getName(), "Manufacture", b);
    }
}
