/*
 * Mars Simulation Project
 * PowerMonitor.java
 * @date 2026-07-08
 * @author Manny Kung
 */

package com.mars_sim.core.building.utility.power;

import java.io.Serializable;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.Function;

/**
 * This class models a building power's infrastructure.
 */
public class PowerMonitor implements Serializable {

    /** Default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Default logger. */
	// May add back: private static final SimLogger logger = SimLogger.getLogger(PowerMonitor.class.getName())
	
    public static final int NORMAL_LINE_VOLTAGE = 480;
    
    public static final String POWER_MODE_EVENT = "power mode";

	/** The power priority number for this building. */
	private int powerPriority;
	
	private double baseFullPowerLoad;
	private double baseLowPowerLoad;
	private double powerNeededForEVAHeater;

	private double lineVoltage = NORMAL_LINE_VOLTAGE;
	private double loadVoltage = NORMAL_LINE_VOLTAGE;

	private PowerMode powerModeCache;
	
	private Building building;
	
	/**
     * Constructor.
     * 
     * @param buiding.
	 */
    public PowerMonitor(Building building, int powerPriority, double baseFullPowerLoad, double baseLowPowerLoad) {
    	this.building = building;
    	this.powerPriority = powerPriority;
    	this.baseFullPowerLoad = baseFullPowerLoad;
    	this.baseLowPowerLoad = baseLowPowerLoad;
    	
		this.powerModeCache = PowerMode.FULL_POWER;
    }
		
	public int getPowerPriority() {
		return powerPriority;
	}
	
	/**
	 * Gets the base full power load.
	 *
	 * @return power in kW.
	 */
	public double getBaseFullPowerLoad() {
		return baseFullPowerLoad;
	}
	
	/**
	 * Gets the base low power load.
	 *
	 * @return power in kW.
	 */
	public double getBaseLowPowerLoad() {
		return baseLowPowerLoad;
	}
	
	/**
	 * Gets the power needed for EVA heater.
	 *
	 * @return power in kW.
	 */
	public double getPowerNeededForEVAHeater() {
		return powerNeededForEVAHeater;
	}
	
	/**
	 * Gets the power needed for EVA heater.
	 *
	 * @return power in kW.
	 */
	public void setPowerNeededForEVAHeater(double value) {
		powerNeededForEVAHeater = value;
	}
	
	/**
	 * Gets the building's power mode.
	 */
	public PowerMode getPowerMode() {
		return powerModeCache;
	}

	/**
	 * Sets the building's power mode.
	 */
	public void setPowerMode(PowerMode powerMode) {
		if (powerModeCache != powerMode) {
			powerModeCache = powerMode;
			building.fireUnitUpdate(POWER_MODE_EVENT, this);
		}
	}
	
	/**
	 * Gets the line voltage.
	 *
	 * @return in Volt
	 */
	public double getLineVoltage() {
		return lineVoltage;
	}
	
	/**
	 * Gets the load voltage.
	 *
	 * @return in Volt
	 */
	public double getLoadVoltage() {
		return loadVoltage;
	}
	
	/**
	 * Gets the load current.
	 *
	 * @return in Amperage.
	 */
	public double getLoadCurrent() {
		return getFullPowerLoad() / loadVoltage;
	}
	
	/**
	 * Gets the total power load for full-power mode on all functions.
	 *
	 * @return power in kW.
	 */
	public double getFullPowerLoad() {
		double result = getBaseFullPowerLoad();

		// Determine power required for each function.
		for (Function function : building.getFunctions()) {
			double power = function.getFullPowerLoad();
			if (power > 0) {
				result += power;
			}
		}

		return result + getPowerNeededForEVAHeater();
	}

	/**
	 * Gets the total low power load.
	 *
	 * @return power in kW.
	 */
	public double getLowPowerLoad() {
		double result = getBaseLowPowerLoad();

		// Determine power required for each function.
		for (Function function : building.getFunctions()) {
			double power = function.getLowPowerLoad();
			if (power > 0) {
				result += power;
			}
		}

		return result;
	}
	
	/**
	 * Gets the total power load for current mode on all functions.
	 *
	 * @return power in kW.
	 */
	public double getPowerLoad() {
		double result = 0;
		
		PowerMode mode = getPowerMode();
		
		if (PowerMode.FULL_POWER == mode) {
			result = getFullPowerLoad();
		}
		else if (PowerMode.LOW_POWER == mode) {
			result = getLowPowerLoad();
		}

		return result;
	}
	
	
    /**
     * Prepares object for garbage collection.
     */
    public void destroy() {
        building = null;
    }
}
