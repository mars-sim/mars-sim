/*
 * Mars Simulation Project
 * WorkshopProcess.java
 * @date 2025-04-17
 * @author Barry Evans
 */
package com.mars_sim.core.manufacture;

import java.io.Serializable;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.Manufacture;
import com.mars_sim.core.equipment.BinFactory;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleFactory;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * A process that is executed in a Workshop.
 */
public abstract class WorkshopProcess implements Serializable {
	private static final SimLogger logger = SimLogger.getLogger(WorkshopProcess.class.getName());

	private String name;
	private double workTimeRemaining;
	private double processTimeRemaining;
    private double averageSkillLevel = 0D;

	private ProcessInfo info;
	private boolean active;
    private Manufacture workshop;

	/**
	 * Process to run on a Workshop
	 * @param workshop
	 * @param info
	 */
    protected WorkshopProcess(String name, Manufacture workshop, ProcessInfo info) {
        this.workshop = workshop;
        this.workTimeRemaining = info.getWorkTimeRequired();
		this.processTimeRemaining = info.getProcessTimeRequired();
		this.info = info;
		this.name = name;
		this.active = true;
    }

    /**
	 * Gets the remaining work time.
	 * 
	 * @return work time (millisols)
	 */
	public double getWorkTimeRemaining() {
		return workTimeRemaining;
	}

	/**
	 * Is the process active?
	 * @return
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Adds work time to the process.
	 * 
	 * @param workTime work time (millisols)
	 * @rteturn Is the process still active
	 */
	public boolean addWorkTime(double workTime, int skill) {
		workTimeRemaining -= workTime;
		if (workTimeRemaining < 0D)
			workTimeRemaining = 0D;
        
        // Add skill for work time for average skill level.
        averageSkillLevel += workTime / info.getWorkTimeRequired() * skill;

		checkStillActive();
		return active;
	}

    /**
     * Gets the average material science skill level
     * used during the salvage process.
     * 
     * @return skill level.
     */
    public double getAverageSkillLevel() {
        return averageSkillLevel;
    }

	/**
	 * Gets the remaining process time.
	 * 
	 * @return process time (millisols)
	 */
	public double getProcessTimeRemaining() {
		return processTimeRemaining;
	}

	/**
	 * Adds process time to the process.
	 * 
	 * @param processTime process time (millisols)
	 * @return True iof the process is still active
	 */
	public boolean addProcessTime(double processTime) {
		processTimeRemaining -= processTime;
		if (processTimeRemaining < 0D)
			processTimeRemaining = 0D;

		checkStillActive();
		return active;
	}

	private void checkStillActive() {
		if (!active)
			return;

		active = (processTimeRemaining > 0D) || (workTimeRemaining > 0D);
		if (!active) {
			stopProcess(false);
		}
	}

    /**
	 * Gets the workshop running htis process
	 * 
	 * @return workshop building function.
	 */
	public Manufacture getWorkshop() {
		return workshop;
	}

	/**
	 * Gets the process information.
	 * 
	 * @return process information.
	 */
	public ProcessInfo getInfo() {
		return info;
	}

	/**
	 * Get the descriptive name of the process
	 */
	public String getName() {
		return name;
	}

	/**
	 * Where is this process running?
	 * @return
	 */
	protected Building getBuilding() {
		return workshop.getBuilding();
	}

	/**
	 * Deposits the outputs.
	 * 
	 * @param process
	 */
	protected void depositOutputs() {
		var host = getWorkshop().getBuilding();
		Settlement settlement = host.getAssociatedSettlement();

		// Produce outputs.
		for (var item : info.getOutputList()) {
			depositItem(item, settlement, host, true);
		}
	}

	/**
	 * Returns the used inputs.
	 * 
	 * @param process
	 */
	protected void returnInputs() {
		var host = getWorkshop().getBuilding();
		Settlement settlement = host.getAssociatedSettlement();

		// Produce outputs.
		for (var item : info.getInputList()) {
			depositItem(item, settlement, host, false);
		}
	}

	private void depositItem(ProcessItem item, Settlement settlement, Building host, boolean updateGoods) {
		int outputId = -1;
		double outputAmount = item.getAmount();
		switch(item.getType()) {
			case AMOUNT_RESOURCE: {

				// Produce amount resources.
				outputId = item.getId();
				double capacity = settlement.getAmountResourceRemainingCapacity(outputId);
				if (outputAmount> capacity) {
					double overAmount = item.getAmount() - capacity;
					logger.severe(host, "Not enough storage capacity to store " 
						+ Math.round(overAmount * 10.0)/10.0 + " kg " + item.getName()
							+ " from '" + name + "'.");
					outputAmount = capacity;
				}
				settlement.storeAmountResource(outputId, outputAmount);
			} break;

			case PART: {
				// Produce parts.
				outputId = item.getId();
				Part part = ItemResourceUtil.findItemResource(outputId);
				int num = (int)outputAmount;
				double mass = num * part.getMassPerItem();
				double capacity = settlement.getCargoCapacity();
				if (mass <= capacity) {
					settlement.storeItemResource(outputId, num);
				}
				else {
					outputId = -1;
				}
			} break;

			case EQUIPMENT: {
				// Produce equipment.
				var equipmentType = EquipmentType.convertName2Enum(item.getName());
				outputId = EquipmentType.getResourceID(equipmentType);
				int number = (int) outputAmount;
				for (int x = 0; x < number; x++) {
					EquipmentFactory.createEquipment(equipmentType, settlement);
				}
			} break;

			case BIN: {
				// Produce bins.
				outputAmount = item.getAmount();
				int number = (int) outputAmount;
				for (int x = 0; x < number; x++) {
					BinFactory.createBins(item.getName(), settlement);
				}
			} break;
		
			case VEHICLE: {
				// Produce vehicles.
				int number = (int) outputAmount;
				var unitMgr = Simulation.instance().getUnitManager();// Don't like this
				for (int x = 0; x < number; x++) {
					Vehicle v = VehicleFactory.createVehicle(unitMgr, settlement, item.getName());

					outputId = VehicleType.getVehicleID(v.getVehicleType());
				}
			} break;
		}

		// Record goods benefit
		if (updateGoods) {
			if (outputId >= 0) {
				settlement.addOutput(outputId, outputAmount, info.getWorkTimeRequired());
			}

			Good good = GoodsUtil.getGood(item.getName());
			if (good == null) {
				logger.severe(item.getName() + " is not a good.");
			}
			else
				// Recalculate settlement good value for the output item.
				settlement.getGoodsManager().determineGoodValue(good);
		}
	}

	/**
	 * Start the process running
	 */
	public abstract boolean startProcess();

	/**
	 * Stops the process.
	 * @param premature Was it stopped prematurely?
	 */
	public void stopProcess(boolean premature) {
		workshop.removeProcess(this);
		active = false;
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		workshop = null;
	}
}
