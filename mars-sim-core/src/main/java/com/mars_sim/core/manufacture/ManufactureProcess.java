/*
 * Mars Simulation Project
 * ManufactureProcess.java
 * @date 2022-07-26
 * @author Scott Davis
 */

package com.mars_sim.core.manufacture;

import java.util.logging.Level;

import com.mars_sim.core.building.function.Manufacture;
import com.mars_sim.core.logging.SimLogger;

/**
 * A manufacturing process.
 */
public class ManufactureProcess extends WorkshopProcess {
	
	private static final SimLogger logger = SimLogger.getLogger(ManufactureProcess.class.getName());

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
		if (!getWorkshop().addManuProcess(this)) {
			return false;
		}
		var settlement = getBuilding().getSettlement();

		// Consume inputs.
		for (var item : getInfo().getInputList()) {
			switch(item.getType()) {
				case AMOUNT_RESOURCE:
					settlement.retrieveAmountResource(item.getId(), item.getAmount());
					break;
				case PART:
					settlement.retrieveItemResource(item.getId(), (int) item.getAmount());
					break;
				default:
					logger.log(getBuilding(), Level.SEVERE, 20_000,
							"Manufacture process input: " + item.getType() + " not a valid type.");
					return false;
			}
		}

		// Log manufacturing process starting.
		logger.log(getBuilding(), Level.FINEST, 20_000,
						"Starting manufacturing process: " + getName());
		
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
			// Log process ending.
			logger.log(b, Level.INFO, 10_000,
					"Finished the manu process '" + getName() + "'.");
		}
		else {
			returnInputs();
			// Log process ending.
			logger.log(b, Level.INFO, 10_000,
					"Unable to finish the manu process '" + getName() + "'.");
		}

		super.stopProcess(premature);
		
		// Record process finish
		b.getAssociatedSettlement().recordProcess(getInfo().getName(), "Manufacture", b);
    }
}
