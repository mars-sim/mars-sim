/*
 * Mars Simulation Project
 * PowerStorage.java
 * @date 2025-09-25
 * @author Scott Davis
 */
package com.mars_sim.core.building.utility.power;

import java.util.Iterator;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.config.FunctionSpec;
import com.mars_sim.core.building.function.Function;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.equipment.Battery;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;

/**
 * The PowerStorage class is a building function hosting a grid battery for energy storage.
 */
public class PowerStorage extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	// May add back : private static final SimLogger logger = SimLogger.getLogger(PowerStorage.class.getName())

	public static final double HOURS_PER_MILLISOL = 0.0247 ; //MarsTime.SECONDS_IN_MILLISOL / 3600D;
	public static final double SECONDARY_LINE_VOLTAGE = 240D;
	public static final double BATTERY_MAX_VOLTAGE = 374.4D;
	public static final double PERCENT_BATTERY_RECONDITIONING_PER_CYCLE = .1; // [in %]
	/**
	 * This is a building.xml property for the power storage function to control discharge.
	 */
	private static final String DISCHARGE_RATE = "discharge-rate";

	// Data members.
	/**  
	 * The total internal resistance of the battery.
	 * rTotal = rCell * # of cells * # of modules
	 */
	private double rTotal;  
	
	private double dischargeRate;

	/** The battery of the vehicle. */ 
	private Battery battery;
	
	/**
	 * Constructor.
	 * 
	 * @param building the building with the function.
	 * @param spec Specification of Function
	 * @throws BuildingException if error parsing configuration.
	 */
	public PowerStorage(Building building, FunctionSpec spec) {
		// Call Function constructor.
		super(FunctionType.POWER_STORAGE, spec, building);

		dischargeRate = spec.getDoubleProperty(DISCHARGE_RATE);	
		
        double energyStorageCapacity = spec.getCapacity();

        int numModules = (int)(Math.ceil(energyStorageCapacity/2));
        
        battery = new Battery(building, numModules, energyStorageCapacity / numModules);
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param type the building type.
	 * @param newBuilding true if adding a new building.
	 * @param settlement the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String type, boolean newBuilding, Settlement settlement) {

		PowerGrid grid = settlement.getPowerGrid();

		double hrInSol = 1000D * PowerGrid.HOURS_PER_MILLISOL;
		double demand = grid.getRequiredPower() * hrInSol;

		double supply = 0D;
		Iterator<Building> iStore = settlement.getBuildingManager().getBuildingSet(FunctionType.POWER_STORAGE).iterator();
		while (iStore.hasNext()) {
			Building building = iStore.next();
			PowerStorage store = building.getPowerStorage();
			double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
			supply += store.getBattery().getEnergyStorageCapacity() * wearModifier;
		}

		double existingPowerStorageValue = demand / (supply + 1D);

		double powerStorage = buildingConfig.getFunctionSpec(type, FunctionType.POWER_STORAGE).getCapacity();

		double value = powerStorage * existingPowerStorageValue / hrInSol;
		if (value > 10D) value = 10D;

		return value;
	}

	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
	    	battery.timePassing(pulse);
		}
        return valid;
	}
	
    /**
     * Gets the battery instance.
     * 
     * @return
     */
    public Battery getBattery() {
    	return battery;
    }

	@Override
	public double getMaintenanceTime() {
		return battery.getEnergyStorageCapacity() / 5D;
	}
	
	@Override
	public double getCombinedPowerLoad() {
			return 0;
	}


	public double getTotalResistance() {
		return rTotal;
	}
	
	@Override
	public void destroy() {
//		super.destroy();
	}
}
