/*
 * Mars Simulation Project
 * SalvageProcess.java
 * @date 2024-08-10
 * @author Scott Davis
 */
package com.mars_sim.core.manufacture;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.building.function.Manufacture;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * A process for salvaging a piece of equipment or vehicle.
 */
public class SalvageProcess extends WorkshopProcess {
	
    private static final SimLogger logger = SimLogger.getLogger(SalvageProcess.class.getName());

	// Data members.    
    private Salvagable salvagedUnit;
        
    /**
     * Constructor.
     * 
     * @param info information about the salvage process.
     * @param workshop the manufacturing workshop where the salvage is taking place.
     */
    public SalvageProcess(SalvageProcessInfo info, Manufacture workshop, Salvagable salvagedUnit) {
        super("Salvage " + salvagedUnit.getName(), workshop, info);
        this.salvagedUnit = salvagedUnit;
    }

    /**
     * Gets the salvaged unit.
     * 
     * @return salvaged unit.
     */
    public Salvagable getSalvagedUnit() {
        return salvagedUnit;
    }
    
    /**
     * Prepares object for garbage collection.
     */
    @Override
    public void destroy() {
        super.destroy();
        salvagedUnit = null;
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

		// Retrieve salvaged unit and remove from unit manager.
		switch(salvagedUnit) {
			case Equipment e: {
				settlement.removeEquipment(e);
			} break;
			case Robot r: {
				settlement.removeOwnedRobot(r);
			} break;
			case Vehicle v: {
				settlement.removeOwnedVehicle(v);
				settlement.removeVicinityParkedVehicle(v);
			} break;
			default: throw new IllegalStateException("Salvage process can not remote target");
		}

		// Set the salvage process info for the salvaged unit.
		salvagedUnit.startSalvage((SalvageProcessInfo)getInfo(), settlement.getIdentifier());

		// Log salvage process starting.
		return true;
	}

    /**
     * Stop this salvage process.
     * @param premature is the process being stopped prematurely?
     */
    @Override
    public void stopProcess(boolean premature) {
        var building = getBuilding();
		var settlement = building.getSettlement();

		Map<Integer, Integer> partsSalvaged = new HashMap<>();

		if (premature) {
			returnInputs();
			// Log salvage process ending.
			logger.log(building, Level.INFO, 10_000,
							"Unable to finish the process '" + getName() + "'.");	
		}
		
		else {
			// Produce salvaged parts.

			// Determine the salvage chance based on the wear condition of the item.
			double salvageChance = 50D;
			if (salvagedUnit instanceof Malfunctionable malfunctionable) {
				double wearCondition = malfunctionable.getMalfunctionManager().getWearCondition();
				salvageChance = (wearCondition * .25D) + 25D;
			}

			// Add the average material science skill of the salvagers.
			salvageChance += getAverageSkillLevel() * 5D;

			// Salvage parts.
			for (var partSalvage : getInfo().getOutputList()) {
                int id = partSalvage.getId();
				Part part = ItemResourceUtil.findItemResource(id);

				int totalNumber = 0;
				for (int x = 0; x < (int)partSalvage.getAmount(); x++) {
					if (RandomUtil.lessThanRandPercent(salvageChance))
						totalNumber++;
				}
				totalNumber = Math.max(totalNumber, 1); // Salvage at least one part.

				if (totalNumber > 0) {
					partsSalvaged.put(id, totalNumber);

					double mass = totalNumber * part.getMassPerItem();
					double capacity = settlement.getCargoCapacity();
					if (mass <= capacity)
						settlement.storeItemResource(id, totalNumber);

					Good good = GoodsUtil.getGood(part.getName());
					if (good == null) {
						logger.severe(getBuilding(), part.getName() + " is not a good.");
					}
					else
						// Recalculate settlement good value for salvaged part.
						settlement.getGoodsManager().determineGoodValue(good);
				}
			}

			settlement.recordProcess(getName(), "Salvage", building);
		}

		// Finish the salvage.
        var masterClock = Simulation.instance().getMasterClock();
		salvagedUnit.getSalvageInfo().finishSalvage(partsSalvaged, masterClock.getMarsTime());

		super.stopProcess(premature);
    }
}
