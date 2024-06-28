/**
 * Mars Simulation Project
 * PowerStorage.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function;

import java.util.Iterator;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.structure.PowerGrid;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingException;
import com.mars_sim.core.structure.building.FunctionSpec;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The PowerStorage class is a building function depicting the interworking of a grid battery for energy storage.
 */
public class PowerStorage extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(PowerStorage.class.getName());

	public static final double HOURS_PER_MILLISOL = 0.0247 ; //MarsTime.SECONDS_IN_MILLISOL / 3600D;
	public static final double SECONDARY_LINE_VOLTAGE = 240D;
	public static final double BATTERY_MAX_VOLTAGE = 374.4D;
	public static final double PERCENT_BATTERY_RECONDITIONING_PER_CYCLE = .1; // [in %]
	/**
	 * This is a building.xml property for the power storage function to control discharge.
	 */
	private static final String DISCHARGE_RATE = "discharge-rate";
	/** 
	 * The number of cells per module of the battery. 
	 * Note: 3.6 V * 104 = 374.4 V 
	 * e.g. : Tesla Model S has 104 cells per module
	 */
	private static final int CELLS_PER_MODULE = 104;
	
	// Data members.
	/**
	 * True if the battery reconditioning is prohibited.
	 */
	private boolean locked;
		
	/** The number of modules of the battery. */
	private int numModules = 0;
	
	/** The number of times the battery has been fully discharged/depleted since last reconditioning. */
	private int timesFullyDepleted = 0;
	
	/** The degradation rate of the battery in % per sol. May be reduced via research. */
	public double percentBatteryDegrade = .05;
	
	/** The maximum nameplate kWh of this battery. */	
	public double maxCapNameplate;
	
	/** The internal resistance [in ohms] in each cell. */	
	public double rCell = 0.06; 

	/**  
	 * The total internal resistance of the battery.
	 * rTotal = rCell * # of cells * # of modules
	 */
	private double rTotal;  
	
	/**The maximum continuous discharge rate (within the safety limit) of this battery. */
	private double maxCRating = 4D;
	// The capacity of a battery is generally rated and labeled at 3C rate(3C current) 
	// It means a fully charged battery with a capacity of 100Ah should be able to provide 
	// 3*100Amps current for one third hours. 
	// That same 100Ah battery being discharged at a C-rate of 1C will provide 100Amps 
	// for one hours, and if discharged at 0.5C rate it provide 50Amps for 2 hours.
	
	/** 
	 * The rating [in ampere-hour (Ah)] of the battery in terms of its charging/discharging ability at 
	 * a particular C-rating. An amp is a measure of electrical current. The hour 
	 * indicates the length of time that the battery can supply this current.
	 * e.g. a 2.2Ah battery can supply 2.2 amps for an hour
	 */
	private double ampHours;
	
	/** The health of the battery. */
	private double health = 1D; 	
	
	/** The maximum energy [in kWh, not Wh] storage capacity. */
	private double currentMaxCap; 
	
	/** 
	 * The energy [in kilo Watt-hour] currently stored in the battery. 
	 * The Watt-hour (Wh) signifies that a battery can supply an amount of power for an hour
	 * e.g. a 60 Wh battery can power a 60 W light bulb for an hour
	 */
	private double kWhStored;

	
	
	/*
	 * The Terminal voltage is between the battery terminals with load applied. 
	 * It varies with SOC and discharge/charge current.
	 */
	private double terminalVoltage; 
	
	
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
		
		maxCapNameplate = spec.getCapacity();		
		currentMaxCap = maxCapNameplate;
		numModules = (int)(Math.ceil(currentMaxCap/2));
		rTotal = rCell * numModules * CELLS_PER_MODULE;
		ampHours = 1000D * currentMaxCap/SECONDARY_LINE_VOLTAGE;
		maxCRating = spec.getDoubleProperty(DISCHARGE_RATE);	
		// At the start of sim, set to a random value		
		kWhStored = maxCapNameplate * (.5 + RandomUtil.getRandomDouble(.5));	
		
		// Update battery voltage
		updateVoltage();
		
//		logger.info(building, "maxCapNameplate: " + maxCapNameplate 
//							+ "  numModules: " + numModules
//							+ "  rCell: " + rCell
//							+ "  rTotal: " + rTotal
//							+ "  ampHours: " + ampHours
//							+ "  maxCRating: " + maxCRating
//							+ "  Vt: " + terminalVoltage							
//							+ "  kWhStored: " + kWhStored);
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
			supply += store.currentMaxCap * wearModifier;
		}

		double existingPowerStorageValue = demand / (supply + 1D);

		double powerStorage = buildingConfig.getFunctionSpec(type, FunctionType.POWER_STORAGE).getCapacity();

		double value = powerStorage * existingPowerStorageValue / hrInSol;
		if (value > 10D) value = 10D;

		return value;
	}

	/**
	 * Computes how much stored energy can be delivered when discharging.
	 * 
	 * @param needed  energy
	 * @param rLoad  the load resistance of the external circuit (power grid, vehicle, robot) 
	 * @param time    in millisols
	 * @return energy available to be delivered
	 */
	public double computeAvailableEnergy(double needed, double rLoad, double time) {
		if (needed <= 0)
			return 0;
		
		double stored = getkWattHourStored();
		double maxCap = getCurrentMaxCapacity();
		
		if (stored <= 0)
			return 0;

		double vTerminal = getTerminalVoltage();
		// Assume the internal resistance of the battery is constant
		double rInt = getTotalResistance();
		// Assume max stateOfCharge is 1
		double stateOfCharge = stored / maxCap;
		// Use fudge_factor to improve the power delivery but decreases 
		// as the battery is getting depleted
		double fudgeFactor = 5 * stateOfCharge;
		// The output voltage
		double vOut = vTerminal * rLoad / (rLoad + rInt);

		if (vOut <= 0)
			return 0;

		double ampHr = getAmpHourRating();
//		double hr = time * HOURS_PER_MILLISOL;	
		
		// Use Peukert's Law for lithium ion battery to dampen the power delivery when 
		// battery is getting depleted
		// Set k to 1.10
		double ampHrRating = ampHr; // * Math.pow(hr, -1.1);

		// The capacity of a battery is generally rated and labeled at 3C rate(3C current) 
		// It means a fully charged battery with a capacity of 100Ah should be able to provide 
		// 3*100Amps current for one third hours. 
		// That same 100Ah battery being discharged at a C-rate of 1C will provide 100Amps 
		// for one hours, and if discharged at 0.5C rate it provide 50Amps for 2 hours.
		
		double cRating = getMaxCRating();
		double nowAmpHr = cRating * ampHrRating * fudgeFactor * stateOfCharge;
		double possiblekWh = nowAmpHr / 1000D * vOut ;

		double availablekWh = Math.min(stored, Math.min(possiblekWh, needed));

//		logger.info(building, "kWh: " + Math.round(stored * 100.0)/100.0
//				+ "  available: " + Math.round(availablekWh * 10000.0)/10000.0 
//				+ "  needed: " + Math.round(needed * 10000.0)/10000.0 
//				+ "  possiblekWh: " + Math.round(possiblekWh * 10000.0)/10000.0
//				+ "  ampHrRating: " + Math.round(ampHrRating * 100.0)/100.0
//				+ "  nowAmpHr: " + Math.round(nowAmpHr * 100.0)/100.0);

		return availablekWh;
	}
	
	/**
	 * Reconditions the battery.
	 * 
	 * @param kWh the new value of stored energy.
	 */
	public void reconditionBattery(double kWh) {
		
		if (!locked) {
			
			boolean needRecondition = false;
			
			if (kWh <= 0D) {
				kWh = 0D;
				needRecondition = true;
		        // recondition once and lock it for the rest of the sol
		        locked = true;
		        timesFullyDepleted++;
			}
			
			else if (kWh < currentMaxCap / 5D) {
				
				int rand = RandomUtil.getRandomInt((int)kWh);		
				if (rand == 0) {
					needRecondition = true;
			        // recondition once and lock it for the rest of the sol
			        locked = true;
				}
			}
	
			if (needRecondition && timesFullyDepleted > 20) {
				needRecondition = false;
				timesFullyDepleted = 0;
				
				health = health * (1 + PERCENT_BATTERY_RECONDITIONING_PER_CYCLE/100D);
				logger.info(building, "The battery has just been reconditioned.");
			}
		}
		
		if (kWh > currentMaxCap) {
			kWh = currentMaxCap;			
		}	
	
		kWhStored = kWh;	
	
		updateVoltage();
	}

	/**
	 * Updates the terminal voltage of the battery.
	 */
	private void updateVoltage() {
    	terminalVoltage = Math.round(kWhStored / ampHours * 1000D * 1_000)/1_000;
    	if (terminalVoltage > BATTERY_MAX_VOLTAGE)
    		terminalVoltage = BATTERY_MAX_VOLTAGE;
	}
	
	/**
	 * Diagnoses health and update the status of the battery.
	 */
	private void diagnoseBattery() {
		if (health > 1)
			health = 1;
    	currentMaxCap = currentMaxCap * health;
    	if (currentMaxCap > maxCapNameplate)
    		currentMaxCap = maxCapNameplate;
		ampHours = 1000D * currentMaxCap/SECONDARY_LINE_VOLTAGE; 
		if (kWhStored > currentMaxCap) {
			kWhStored = currentMaxCap;		
		}
	}
	
	/**
	 * Updates the health of the battery.
	 */
	private void updateHealth() {
    	health = health * (1 - percentBatteryDegrade/100D);		
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid && pulse.isNewSol()) {
	        locked = false;
	        updateHealth();
	    	diagnoseBattery();
	    	updateVoltage();
		}
        return valid;
	}

	@Override
	public double getMaintenanceTime() {
		return currentMaxCap / 5D;
	}

	
	@Override
	public double getFullPowerRequired() {
			return 0;
	}

	/**
	 * Gets the current max storage capacity of the battery.
	 * 
	 * (Note : this accounts for the battery degradation over time)
	 * @return capacity (kWh).
	 */
	public double getCurrentMaxCapacity() {
		return currentMaxCap;
	}

	/**
	 * Gets the building's stored energy.
	 * 
	 * @return energy (kW hr).
	 */
	public double getkWattHourStored() {
		return kWhStored;
	}
	
	public double getAmpHourRating() {
		return ampHours;
	}
	
	public double getTerminalVoltage() {
		return terminalVoltage;
	}
	
	public void setTerminalVoltage(double value) {
		terminalVoltage = value;
	}
	
	public double getBatteryHealth() {
		return health;
	}
	
	public double getMaxCRating() {
		return maxCRating;
	}

	public double getTotalResistance() {
		return rTotal;
	}
	
	@Override
	public void destroy() {
//		super.destroy();
	}
}
