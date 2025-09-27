/*
 * Mars Simulation Project
 * ThermionicNuclearPowerSource.java
 * @date 2024-08-03
 * @author Manny Kung
 */
package com.mars_sim.core.building.utility.power;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.structure.Settlement;

/**
 * A thermionic nuclear power source that gives a steady supply of electrical power.
 */
public class ThermionicNuclearPowerSource extends PowerSource
		implements AdjustablePowerSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final int MAX_LOAD_CAPACITY = 110;
	
	private static final int MIN_LOAD_CAPACITY = 5;
	
	private static final double MAINTENANCE_FACTOR = 2D;
	// Increment in percentage %
	private static final double PERCENT_INCREMENT = 5;
	
	/** The number of modules. */
	private int numModules;

	/** The design electrical power in kW_e. */
	private final double designPowerElectrical;
	
	/** The design thermal power in kW_th. */
	private final double designPowerThermal;

	/** The current conversion efficiency in percent. */
	private double currentConversion;
	
	/** The current load capacity in percent. */
	private double currentLoadCapacity;
	
	/** The current electrical power in kWe. */
	private double currentPowerElectrical;

	private double maintenanceTime;
	
	public ThermionicNuclearPowerSource(int numModules, double thermalPower, double conversion, double percentLoadCapacity) {
		// Call PowerSource constructor.
		super(PowerSourceType.THERMIONIC_NUCLEAR_POWER, thermalPower);
		
		this.numModules = numModules;
		this.designPowerThermal = thermalPower;

		designPowerElectrical = numModules * thermalPower * percentLoadCapacity * conversion / 10_000;

		currentConversion = conversion;
		currentLoadCapacity = percentLoadCapacity;
		currentPowerElectrical = designPowerElectrical;
		
		maintenanceTime = currentPowerElectrical * MAINTENANCE_FACTOR;
	}

	/**
	 * Gets the current power produced.
	 * 
	 * @param building the building this power source is for.
	 * @return power (kW)
	 */
	public double getCurrentPower(Building building) {
		return currentPowerElectrical;
	}

	/**
	 * Gets the average power produced.
	 * 
	 * @param building the building this power source is for.
	 * @return power (kW)
	 */
	public double getAveragePower(Settlement settlement) {
		return currentPowerElectrical;
	}

	/**
	 * Gets the current load capacity.
	 * 
	 * @return percent
	 */
	public double getCurrentLoadCapacity() {
		return currentLoadCapacity;
	}

	
	/**
	 * Increases the power load capacity.
	 */
	public void increaseLoadCapacity() {
		currentLoadCapacity = currentLoadCapacity + PERCENT_INCREMENT;
		
		if (currentLoadCapacity > MAX_LOAD_CAPACITY)
			currentLoadCapacity = MAX_LOAD_CAPACITY;
		
		// Recalculate the current kW_e
		recalculatekWe();
	}
	
	/**
	 * Increases the power load capacity.
	 */
	public void decreaseLoadCapacity() {
		currentLoadCapacity = currentLoadCapacity - PERCENT_INCREMENT;
		
		if (currentLoadCapacity < MIN_LOAD_CAPACITY)
			currentLoadCapacity = MIN_LOAD_CAPACITY;
		
		// Recalculate the current kW_e
		recalculatekWe();
	}
	
	/**
	 * Recalculates the current electrical power.
	 */
	public void recalculatekWe() {
		// Recalculates current kWh_e
		currentPowerElectrical = numModules * designPowerThermal * currentLoadCapacity * currentConversion / 10_000;
		// Recalculate maintenance time
		maintenanceTime = currentPowerElectrical * MAINTENANCE_FACTOR;
	}
	
	@Override
	public double getMaintenanceTime() {
	    return maintenanceTime;
	}
	
	/**
	 * Measures or estimates the power produced by this power source.
	 * 
	 * @param percent The percentage of capacity of this power source
	 * @return power (kWe)
	 */
	@Override
	public double measurePower(double percent) {
		return currentPowerElectrical;
	}
}
