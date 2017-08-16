/**
 * Mars Simulation Project
 * PowerGeneration.java
 * @version 3.07 2014-12-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
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

	/** default logger. */
	private static Logger logger = Logger.getLogger(PowerStorage.class.getName());
	
    private static String sourceName = logger.getName();
  
	// Building function name.
	private static final BuildingFunction FUNCTION = BuildingFunction.POWER_STORAGE;

	public static double HOURS_PER_MILLISOL = 0.0247 ; //MarsClock.SECONDS_IN_MILLISOL / 3600D;
	
	public static final double SECONDARY_LINE_VOLTAGE = 240D;

	public static double MAX_KW_HR_NAMEPLATE;
	
	public static final double BATTERY_MAX_VOLTAGE = 375D;
	
	public static double percentBatteryDegradationPerSol = .0005;
	
	public static final double PERCENT_BATTERY_RECONDITIONING_PER_CYCLE = .001;
	
	// Tesla Model S has 104 cells per module
	private static int cellsPerModule = 104; // 3.6 V * 104 = 374.4 V 
	
	// Data members.
	private int solCache = 1;

	private int numModules = 0;
	
	public double r_cell = 0.06; // [in ohms]

	/**
	 * The total internal resistance of the battery
	 * R_total = R of each cell * # of cells * # of modules 
	 */
	private double r_total;
	
	/**
	 * The C rating is the maximum safe continuous discharge rate of a pack
	 */
	private double C_rating = 1D;
	
	/** The health of the battery */
	private double batteryHealth = 1D; 	
	
	/** max energy storage capacity in kWh */
	private double currentMaxCapacity; // [in kilo watt-hour, not Watt-hour]
	
	/** energy last stored in the battery */
	private double kWattHoursCache;
	
	/** 
	 * The energy currently stored in the battery 
	 * The Watt-hour signifies that a battery can supply an amount of watts for an hour
	 * e.g. a 60 watt-hour battery can power a 60 watt light bulb for an hour
	 */
	private double kWattHoursStored; // [in kilo Watt-hour] 

	/** 
	 * The rating of the battery in terms of its charging/discharging ability at 
	 * a particular C-rating. An amp is a measure of electrical current. The hour 
	 * indicates the length of time that the battery can supply this current.
	 * e.g. a 2.2Ah battery can supply 2.2 amps for an hour
	 */
	private double ampHoursRating; // [in ampere-hour or Ah] 	
	
	/*
	 * The Terminal voltage is between the battery terminals with load applied. 
	 * It varies with SOC and discharge/charge current.
	 */
	private double terminalVoltage; 

	/*
	 * The minimum allowable voltage. It is this voltage that generally 
	 * defines the “empty” state of the battery
	 */
	//private double cutoffVoltage; 

	//private double selfDischargeRate;
	
	private double time;
	
	private static BuildingConfig config;
	
	private static MarsClock marsClock;
	
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
		
		currentMaxCapacity = MAX_KW_HR_NAMEPLATE;

		if (currentMaxCapacity == 20)
			numModules = 19;
		else if (currentMaxCapacity == 40)
			numModules = 38;
		else if (currentMaxCapacity == 80)
			numModules = 80;
		else if (currentMaxCapacity == 400)
			numModules = 380;

		r_total = r_cell * numModules * cellsPerModule;
		
		LogConsolidated.log(logger, Level.INFO, 1000, sourceName, 
				"r_total : " + r_total, null);
		
		ampHoursRating = 1000D * currentMaxCapacity/SECONDARY_LINE_VOLTAGE; 

		// 2017-01-03 at the start of sim, set to a random value		
		kWattHoursStored = RandomUtil.getRandomDouble(MAX_KW_HR_NAMEPLATE);		
		//logger.info("initial kWattHoursStored is " + kWattHoursStored);
		
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
			supply += store.currentMaxCapacity * wearModifier;
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
		
		kWattHoursCache = kWattHoursStored;
		kWattHoursStored = kWh;	
		
		boolean startReconditioning = true;
		
		if (kWh <= 0D) {
			kWh = 0D;
			startReconditioning = true;
		}
		
		else if (kWh < currentMaxCapacity / 5D) {
			int rand = RandomUtil.getRandomInt(3);		
			if (rand == 0)
				startReconditioning = true;
		}

		if (startReconditioning) {
			startReconditioning = false;
			reconditionBattery();
		}
		
		if (kWh > currentMaxCapacity) {
			kWh = currentMaxCapacity;			
		}
		
	
		updateVoltage();		
		
	}

	
	public void diagnoseBattery() {
		if (batteryHealth > 1)
			batteryHealth = 1;
		//System.out.println("battery_health is " + battery_health);
    	currentMaxCapacity = currentMaxCapacity * batteryHealth;
    	if (currentMaxCapacity > MAX_KW_HR_NAMEPLATE)
    		currentMaxCapacity = MAX_KW_HR_NAMEPLATE;
		ampHoursRating = 1000D * currentMaxCapacity/SECONDARY_LINE_VOLTAGE; 
		if (kWattHoursStored > currentMaxCapacity) {
			kWattHoursStored = currentMaxCapacity;		
			kWattHoursCache = kWattHoursStored; 
		}
	}
	
	public void updateVoltage() {
		//r_total = r_cell * cellsPerModule * numModules;
    	terminalVoltage = kWattHoursStored / ampHoursRating * 1000D;
    	if (terminalVoltage > BATTERY_MAX_VOLTAGE)
    		terminalVoltage = BATTERY_MAX_VOLTAGE;
	}
	

	public void updateHealth() {
    	batteryHealth = batteryHealth * (1 - percentBatteryDegradationPerSol/100D);		
	}

	public void reconditionBattery() {
		batteryHealth = batteryHealth * (1 + PERCENT_BATTERY_RECONDITIONING_PER_CYCLE/100D);
	}
	
	@Override
	public void timePassing(double time) {
		this.time = time;
        // check for the passing of each day
        int solElapsed = marsClock.getSolElapsedFromStart();
        
        if (solElapsed != solCache) {
        	solCache = solElapsed;
        	updateHealth();
    		diagnoseBattery();
    		updateVoltage();
        }
	}

	@Override
	public double getMaintenanceTime() {
		return currentMaxCapacity / 5D;
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
	 * Gets the building's current max storage capacity
	 * (Note : this accounts for the battery degradation over time)
	 * @return capacity (kW hr).
	 */
	public double getCurrentMaxCapacity() {
		return currentMaxCapacity;
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
	
	public double getTerminalVoltage() {
		return terminalVoltage;
	}
	
	public void setTerminalVoltage(double value) {
		terminalVoltage = value;
	}
	
	public double getBatteryHealth() {
		return batteryHealth;
	}
	
	public double geCRating() {
		return C_rating;
	}

	public double getResistance() {
		return r_total;
	}

}