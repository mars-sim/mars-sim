/*
 * Mars Simulation Project
 * ThermionicNuclearPowerSource.java
 * @date 2023-06-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * A thermionic nuclear power source that gives a steady supply of electrical power.
 */
public class ThermionicNuclearPowerSource extends PowerSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
//	private static final Logger logger = Logger.getLogger(ThermionicNuclearSource.class.getName());

	private static final int MAX_LOAD_CAPACITY = 110;
	
	private static final int MIN_LOAD_CAPACITY = 5;
	
	private static final double MAINTENANCE_FACTOR = 2D;

	private static final double PERCENT_INCREMENT = .05;
	
	/** The number of modules. */
	private int numModules;

	/** The design electrical power in kW_e. */
	private final double designPowerElectrical;
	
	/** The design thermal power in kW_th. */
	private final double designPowerThermal;
	
	/** The design thermal-to-electrical power conversion efficiency in percent. */
	private final double designConversion;
	
	/** The design load capacity in percent. */
	private final double designPercentLoadCapacity;

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
		this.designConversion = conversion;
		this.designPercentLoadCapacity = percentLoadCapacity;

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
		return currentPowerElectrical * 0.707;
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

	@Override
	public void removeFromSettlement() {
		// Auto-generated method stub
	}
	
	@Override
	public void setTime(double time) {
		// Auto-generated method stub
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}
}
