/**
 * Mars Simulation Project
 * PowerGeneration.java
 * @version 3.07 2014-12-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.PowerGrid;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The PowerStorage class is a building function for storing power.
 */
public class PowerStorage
extends Function
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Building function name.
	private static final BuildingFunction FUNCTION = BuildingFunction.POWER_STORAGE;

	public static double HOURS_PER_MILLISOL = 0.0247 ; //MarsClock.SECONDS_IN_MILLISOL / 3600D;
	
	public static final double SECONDARY_LINE_VOLTAGE = 480D;

	public static double MAX_KW_HR_NAMEPLATE;
	
	public static final double BATTERY_MAX_VOLTAGE = 450D;
	
	public static final double PERCENT_BATTERY_DEGRADATION_PER_SOL = .0005;
	
	public static final double PERCENT_BATTERY_RECONDITIONING_PER_CYCLE = .001;
	
	// Data members.
	private int solCache = 1;
	/** The health of the battery */
	private double batteryHealth = 1D; 	
	/** max energy storage capacity in kWh */
	private double maxCapacity; // Note: in kilo watt-hour, not Watt-hour
	/** energy last stored in the battery */
	private double kWattHoursCache;
	/** energy currently stored in the battery */
	private double kWattHoursStored; // in kilo watt hour 
	// Note: Watt-hours signifies that a battery can supply an amount of watts for an hour
	// e.g. a 60 watt-hour battery can power a 60 watt light bulb for an hour
	/** the rating of the battery in terms of its charging ability*/
	private double ampHoursRating; // [ampere-hour or Ah] 	
	// Note: An amp is a measure of electrical current, and the hour indicates the length of time that the battery can supply this current.
	// e.g. A 2.2Ah battery can supply 2.2 amps for an hour
	private double batteryVoltage;
	//private double currentAmpere;
		
	private boolean startCycle = false;
	
	private static BuildingConfig config;
	private static MarsClock marsClock;
	
	private double time;
	
	/**
	 * Constructor.
	 * @param building the building with the function.
	 * @throws BuildingException if error parsing configuration.
	 */
	public PowerStorage(Building building) {
		// Call Function constructor.
		super(FUNCTION, building);

		config = SimulationConfig.instance().getBuildingConfiguration();

		if (marsClock == null)
			marsClock = Simulation.instance().getMasterClock().getMarsClock();

		MAX_KW_HR_NAMEPLATE = config.getPowerStorageCapacity(building.getBuildingType());

		maxCapacity = MAX_KW_HR_NAMEPLATE;

		ampHoursRating = 1000D * maxCapacity/SECONDARY_LINE_VOLTAGE; 

		// 2017-01-03 at the start of sim, set to a random value		
		kWattHoursStored = RandomUtil.getRandomInt(1, (int)MAX_KW_HR_NAMEPLATE);		
		//System.out.println("kWattHoursStored is " + kWattHoursStored);
		
		// update batteryVoltage
		updateVoltage();
		
		//currentAmpere = wattHourStored / currentVoltage;

	}

	/**
	 * Gets the value of the function for a named building.
	 * @param buildingName the building name.
	 * @param newBuilding true if adding a new building.
	 * @param settlement the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding, Settlement settlement) {

		PowerGrid grid = settlement.getPowerGrid();

		double hrInSol = 1000D * PowerGrid.HOURS_PER_MILLISOL;//MarsClock.convertMillisolsToSeconds(1000D) / 60D / 60D;
		double demand = grid.getRequiredPower() * hrInSol;

		double supply = 0D;
		Iterator<Building> iStore = settlement.getBuildingManager().getBuildings(PowerStorage.FUNCTION).iterator();
		while (iStore.hasNext()) {
			Building building = iStore.next();
			PowerStorage store = (PowerStorage) building.getFunction(PowerStorage.FUNCTION);
			double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
			supply += store.maxCapacity * wearModifier;
		}

		double existingPowerStorageValue = demand / (supply + 1D);

		//BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		double powerStorage = config.getPowerStorageCapacity(buildingName);

		double value = powerStorage * existingPowerStorageValue / hrInSol;
		if (value > 10D) value = 10D;

		return value;
	}


	/**
	 * Sets the energy stored in the building.
	 * @param kWh the stored energy (kW hour).
	 */
	public void setEnergyStored(double kWh) {
		if (kWh < 0D) {
			kWh = 0D;
			startCycle = true;
		}
		
		else if (kWh < maxCapacity / 5D) {
		int rand = RandomUtil.getRandomInt(3);		
		if (rand == 0)
			startCycle = true;
		}

		else if (kWh > maxCapacity) {
			kWh = maxCapacity;			
		}
		
		kWattHoursCache = kWattHoursStored;
		kWattHoursStored = kWh;		
		updateVoltage();		
		
		if (startCycle) {
			startCycle = false;
			batteryHealth = batteryHealth * (1 + PERCENT_BATTERY_RECONDITIONING_PER_CYCLE/100D);
			diagnoseBattery();
		}
		
	}

	
	public void diagnoseBattery() {
		if (batteryHealth > 1)
			batteryHealth = 1;
		//System.out.println("battery_health is " + battery_health);
    	maxCapacity = maxCapacity * batteryHealth;
    	if (maxCapacity > MAX_KW_HR_NAMEPLATE)
    		maxCapacity = MAX_KW_HR_NAMEPLATE;
		ampHoursRating = 1000D * maxCapacity/SECONDARY_LINE_VOLTAGE; 
		if (kWattHoursStored > maxCapacity) {
			kWattHoursCache = kWattHoursStored;
			kWattHoursStored = maxCapacity;		
		}
		updateVoltage();

	}
	
	public void updateVoltage() {
    	batteryVoltage = kWattHoursStored / ampHoursRating * 1000D;
    	if (batteryVoltage > BATTERY_MAX_VOLTAGE)
    		batteryVoltage = BATTERY_MAX_VOLTAGE;
	}
	

	@Override
	public void timePassing(double time) {
		this.time = time;
        // check for the passing of each day
        int solElapsed = marsClock.getSolElapsedFromStart();
        
        if (solElapsed != solCache) {
        	solCache = solElapsed;
        	batteryHealth = batteryHealth * (1 - PowerStorage.PERCENT_BATTERY_DEGRADATION_PER_SOL/100D);
        	diagnoseBattery();
        }
	}

	@Override
	public double getMaintenanceTime() {
		return maxCapacity / 5D;
	}

	@Override
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public double getFullPowerRequired() {
		double delta = kWattHoursStored - kWattHoursCache;
		if (delta > 0 && time > 0) {
			kWattHoursCache = kWattHoursStored;
			return delta/time/HOURS_PER_MILLISOL; 
		}
		else
			return 0;
	}

	@Override
	public double getPoweredDownPowerRequired() {
		return 0;
	}

	/**
	 * Gets the building's energy storage capacity.
	 * @return capacity (kW hr).
	 */
	public double getEnergyStorageCapacity() {
		return maxCapacity;
	}

	/**
	 * Gets the building's stored energy.
	 * @return energy (kW hr).
	 */
	public double getkWattHourStored() {
		return kWattHoursStored;
	}
	
	
	public double getAmpHourRating() {
		return ampHoursRating;
	}
	
	public double getBatteryVoltage() {
		return batteryVoltage;
	}
	
	public void setBatteryVoltage(double value) {
		batteryVoltage = value;
	}
	
	public double getBatteryHealth() {
		return batteryHealth;
	}



	
}