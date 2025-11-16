/*
 * Mars Simulation Project
 * Battery.java
 * @date 2025-09-24
 * @author Manny Kung
 */

package com.mars_sim.core.equipment;

import java.io.Serializable;

import com.mars_sim.core.Unit;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.MathUtils;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * This class represents the modeling of an electrical battery.
 */
public class Battery implements Serializable {

    /** Default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Battery.class.getName());
	
	/** 
	 * The number of cells per module of the battery. 
	 * Note: 3.6 V * 104 = 374.4 V 
	 * 4.2 V * 104 = 436.8 V
	 * e.g. : Tesla Model S has 104 cells per module
	 */
	
	// The internal resistance of each cell in a Tesla Model 3 Long Range (LR) battery pack is 
	// estimated to be approximately 26.8 mΩ. This value is derived from the pack's total 
	// internal resistance of about 56 mΩ, which is based on a 96s46p cell configuration 
	// (96 * 46 = 4416 total cells), resulting in a per-brick resistance of 0.583 mΩ and a per-cell
	// tesistance of 26.8 mΩ (0.583 mΩ × 46).
	
	// 0.583 mΩ * 96 = 55.968 mΩ
	// 0.583 mΩ × 46 = 26.818 mΩ (the resistance of each individual cell)
	
	// For the Standard Range (SR) pack, which uses a 96s31p configuration (96 * 31 = 2975 total cells),
	// the estimated pack resistance is about 83 mΩ. 
	
	private static final int CELLS_IN_SERIES_PER_MODULE = 96;
	/** The internal resistance [in ohms] in each cell. */	
	private static final double R_CELL = 0.583 / 1000; 
	
	private static final double R_LOAD = 26.818 / 1000; // Assume a constant load resistance of the motor
	
	// The nominal voltage of the Tesla Model 3 battery pack is approximately 350 volts, 
	// derived from a 96-cell series configuration with a nominal cell voltage of 
	// around 3.65 volts ( 96×3.65 = 350.4 V).
	
	/** The nominal voltage per cell. */
	private static final double NOMINAL_CELL_VOLTAGE = 4.2;
	// Note: 104 * 4.2V = 403.8
    /** The standard voltage of this battery pack in volts. */
    public static final double HIGHEST_MAX_VOLTAGE = CELLS_IN_SERIES_PER_MODULE * NOMINAL_CELL_VOLTAGE; //403.8; //436.8; // 600
    
	
	/** The percent of the terminal voltage prior to cutoff */
//	public static final double PERCENT_TERMINAL_VOLTAGE = 66.67;
	
    /** The standard voltage of a drone battery pack in volts. */
    public static final double DRONE_VOLTAGE = HIGHEST_MAX_VOLTAGE / 8;
    
    /** The maximum current that can be safely drawn from this battery pack in Ampere. */
    // May add back: private static final double MAX_AMP_DRAW = 120
  
	/**
	 * The nominal capacity (Amp hours) of a lithium cell is about 250mAh at the 
	 * discharge current of 1C.
	 */
//	private static final double NOMINAL_AMP_HOURS = .25;
	/** The maximum continuous charge rate (within the safety limit) that this battery can handle. */
	private static final int MAX_C_RATING_CHARGING = 1;
	/** The maximum continuous discharge rate (within the safety limit) that this battery can handle. */
	private static final int MAX_C_RATING_DISCHARGING = 2;
	
	public static final double HOURS_PER_MILLISOL = 0.0247 ; //MarsTime.SECONDS_IN_MILLISOL / 3600D;
	/** The percent of health improvement after reconditioning. */
	public static final double PERCENT_BATTERY_RECONDITIONING = .1; // [in %]
	 
	public static final double UPPER_LIMIT_TEMPERATURE = 80;
	
	public static final double INITIAL_TEMPERATURE = 22;
	
	public static final double HEAT_TRANSFER_COEFF_HEATING = 6;
	
	public static final double HEAT_TRANSFER_COEFF_COOLING = 12;
	
	public static final double SURFACE_AREA_HEAT_DISSIPATION = 20;
	
    // Data members
    /** Is the unit at low power mode ? */  
    private boolean isLowPower;
    /** Is the unit charging ? */  
    private boolean isCharging;
	/** True if the battery reconditioning is prohibited. */
	private boolean locked;
    /** Is the unit operational ? */
    private boolean operable;
    
    /** The number of battery module. */
    public int numModules;
	
	private int cableSizeFactor;
	
	/** The number of times the battery has been discharged/depleted. */
	private double cyclesDepleted;
    /** The maximum energy capacity of a standard battery module in kWh. */
    public double energyPerModule;
    /** The standby power consumption in kW. */
    private double standbyPower;
    /** unit's stress level (0.0 - 100.0). */
    private double systemLoad;
    /** Performance factor. */
    private double performance;
	/** The percentage that triggers low power warning. */
    private double lowPowerPercent;
	/** 
	 * The energy [in kilo Watt-hour] currently stored in the battery. 
	 * The Watt-hour (Wh) signifies that a battery can supply an amount of power for an hour
	 * e.g. a 60 Wh battery can power a 60 W light bulb for an hour
	 */
	private double kWhStored;
	/** The energy storage capacity [in kWh, not Wh]. */
	private double energyStorageCapacity;
	/** The maximum nameplate kWh of this battery. */	
	public double maxCapNameplate;
	/** 
	 * The capacity rating [in ampere-hour or Ah] of the battery in terms of its 
	 * charging/discharging ability at a particular C-rating. 
	 * 
	 * An amp is a measure of electrical current. 
	 * 
	 * The hour indicates the length of time that the battery can supply this current.
	 * 
	 * e.g. a 2.2Ah battery can supply 2.2 amps for an hour.
	 * 
	 * Amp hour ratings are based on a standardized discharge rate. Typically, 
	 * this rate is 20 hours (C/20), but some manufacturers may use different 
	 * rates (e.g., C/5, C/10).
	 */
	private double ampHourStored;
	
	/** 
	 * The full capacity [in ampere-hour or Ah] of this battery in terms of its charging/discharging ability at 
	 * a particular C-rating.
	 */
	private double ampHourFullCapacity;

	/**  
	 * The total internal resistance of the battery.
	 * rTotal = rCell * # of cells * # of modules
	 */
	private double rTotal;
	
	/**
	 * The average individual cell voltage 
	 */
	private double cellVoltage;
	
	/*
	 * The Terminal voltage is between the battery terminals with load applied. 
	 * It varies with SOC and discharge/charge current.
	 * If the load increases, the terminal voltage lowers, due to the internal 
	 * series resistance of the battery.
	 */
	private double terminalVoltage; 
	/** The lifecycle of energy charging and discharging. For lifecycle analysis. */
	public double cumulativeChargeDischarge;
	/** The degradation rate of the battery in % per 1000 milisols. May be reduced via research. */
	public double percentBatteryDegrade = .05;
	/** The health of the battery. */
	private double health = 1D; 
	
	private double internalTemperature = INITIAL_TEMPERATURE;
			
	private Unit unit;
	
    /**
     * Constructor.
     * 
     * @param unit The unit requiring a battery.
	 * @param numModule
	 * @param energyPerModule
	 */
    public Battery(Unit unit, int numModules, double energyPerModule) {
    	this.unit = unit;
    	
    	if (unit instanceof Building) {
        	cableSizeFactor = 20; 
    	}
    	else if (unit instanceof Robot) {
        	cableSizeFactor = 80; 
    	}
    	else if (unit instanceof Vehicle) {
        	cableSizeFactor = 40; 
    	}
    	
        performance = 1.0D;
        operable = true;
        
        lowPowerPercent = 5;
        standbyPower = 0.01;
        
        this.numModules = numModules;
        // numModules * 0.583 mΩ * 96 = numModules * 55.968 mΩ
		rTotal = R_CELL * numModules * CELLS_IN_SERIES_PER_MODULE;
		cellVoltage = NOMINAL_CELL_VOLTAGE;
		
		// For now, energyPerModule is 15 kWh
        this.energyPerModule = energyPerModule; 
        energyStorageCapacity = energyPerModule * numModules;
        maxCapNameplate = energyStorageCapacity;
        
		// At the start of sim, set to a random value
        kWhStored = energyStorageCapacity * (.5 + RandomUtil.getRandomDouble(.5));	
 
		updateFullAmpHourCapacity();
		
        updateLowPowerMode();
        
    	updateTerminalVoltage();
    }
    
    /**
     * Initializes the power parameters with specific values.
     * 
     * @param lowPowerPercent
     * @param standbyPower
     */
    public void initPower(double lowPowerPercent, double standbyPower) {
    	 this.lowPowerPercent = lowPowerPercent;
    	 this.standbyPower = standbyPower;
    }
  		
    /**
	 * Computes how much stored energy can be delivered when discharging.
	 * 
	 * @param neededkWh  energy
	 * @param timeHr    in hours
	 * @return energy available to be delivered
	 */
	public double estimateEnergyToDeliver(double neededkWh, double timeHr) {
		if (neededkWh <= 0D && timeHr <= 0D)
			return 0D;
		
		double storedkWh = getkWhStored();
		
//		double maxCap = getEnergyStorageCapacity();
		
		if (storedkWh <= 0)
			return 0D;

		double vTerminal = getTerminalVoltage();
		// Assume the internal resistance of the battery is constant
		double rInt = getTotalResistance();
		// Assume max stateOfCharge is 1
//		double stateOfCharge = storedkWh / maxCap;

		// The output voltage
		// e.g. nominal voltage of Tesla Model 3 is 350 V
		// At 100% SoC, it's ~ 403 V
		
		// The actual nominal voltage is less than the terminal because some resistive loss is
		// expected due to internal resistance of the cell

		double powerNominal = storedkWh / timeHr / cableSizeFactor; // in W
		double currentNominal = powerNominal / vTerminal * 1000; // in Amp
		double vLoss = currentNominal * rInt; // in Volt
		double vOut = vTerminal - vLoss; // in Volt
		
		// Do NOT delete. May add back for debugging :
//		if (unit instanceof Building)
//			logger.info("vTerminal: " + Math.round(vTerminal * 100.0)/100.0
//				+ "  timeHr: " + Math.round(timeHr * 10_000.0)/10_000.0	
//				+ "  storedkWh: " + Math.round(storedkWh * 100.0)/100.0	
//				+ "  powerNominal: " + Math.round(powerNominal * 10000.0)/10000.0	
//				+ "  currentNominal: " + Math.round(currentNominal * 10000.0)/10000.0	
//				+ "  vLoss: " + Math.round(vLoss * 10000.0)/10000.0	
//				+ "  rInt: " + Math.round(rInt * 1000.0)/1000.0	
//				+ "  vOut: " + Math.round(vOut * 100.0)/100.0
//				+ " - " + unit);
				
//		cellVoltage = vTerminal / HIGHEST_MAX_VOLTAGE * NOMINAL_CELL_VOLTAGE;
//		double newVOut =  cellVoltage * CELLS_IN_SERIES_PER_MODULE; 
//		double newAmp = newVOut / rInt;
//		double newkWh = newAmp * newVOut / 1000 * time * HOURS_PER_MILLISOL;
		
		
		// In case of TEsla Model 3 battery pack, the actual voltage ranges from approximately 
		// 240–242 volts at the lowest state of charge (0% SoC) to a maximum of 403 volts 
		// during a full charge.
		
		// This variation occurs because the terminal voltage is influenced by the open-circuit 
		// voltage of the individual cells, which can be as low as 2.85 volts at 0% SoC and 
		// as high as 4.15 volts at 100% SoC, resulting in a pack voltage range of roughly 
		// 274 V to 399 V under resting conditions.
	
		if (vOut <= 0)
			return 0;

		double ampHr = getAmpHourStored();
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
		
		double cRatingDischarge = getMaxCRating();
		
		double nowAmpHr = ampHrRating * cRatingDischarge;
		
		double possiblekWh = nowAmpHr / 1000D * vOut;

		double readykWh = Math.min(storedkWh, Math.min(possiblekWh, neededkWh));
		
		// Do NOT delete. May add back for debugging :
//		if (unit instanceof Building)
//			logger.info("nowAmpHr: " + Math.round(nowAmpHr * 1000.00)/1000.00
//				+ "  ampHr: " + Math.round(ampHr * 1000.00)/1000.00		
//				+ "  cRatingDischarge: " + Math.round(cRatingDischarge * 1000.00)/1000.00
////				+ "  stateOfCharge: " + Math.round(stateOfCharge * 1000.00)/1000.00
////				+ "  maxCap: " + Math.round(maxCap * 1000.00)/1000.00
//				+ "  neededkWh: " + Math.round(neededkWh * 100_000.00)/100_000.00
//				+ "  possiblekWh: " + Math.round(possiblekWh * 1000.00)/1000.00
//				+ "  availablekWh: " + Math.round(readykWh * 100_000.00)/100_000.00
//				+ " - " + unit);
		
		return readykWh;
	}
	
	/**
     * This method reflects a passing of time.
     * 
     * @param pulse amount of time in a clock pulse
     * @param support life support system.
     * @param config robot configuration.
     */
    public boolean timePassing(ClockPulse pulse) {
    	double time = pulse.getElapsed();
    	if (time == 0.0)
    		return false;
		
    	if (pulse.isNewSol()) {
	        reconditionBattery();
    	}
    	else if (pulse.isNewHalfSol()) {
	        locked = false;
	    	diagnoseBattery();
		}

    	internalTemperature -= time;
    	
    	if (internalTemperature > UPPER_LIMIT_TEMPERATURE) {
			internalTemperature = UPPER_LIMIT_TEMPERATURE;
		}
    	
		if (internalTemperature > 0.5 * UPPER_LIMIT_TEMPERATURE && kWhStored > 0) {
			double deltaTemperature = applyForcedAirCooling(time 
    				* MathUtils.between(internalTemperature, INITIAL_TEMPERATURE, UPPER_LIMIT_TEMPERATURE) / UPPER_LIMIT_TEMPERATURE);
			internalTemperature -= deltaTemperature;
    	}
    	
    	else if (internalTemperature < INITIAL_TEMPERATURE) {
    		internalTemperature = INITIAL_TEMPERATURE;
    	}
    	
        return operable;
    }

    /**
     * Computes the rise of temperature due to current.
     * 
     * @param amp
     * @return
     */
    private double computeDeltaTemperature(double amp) {
    	double heatInternal = amp * amp * rTotal + amp * 0.0001;
    	return heatInternal / HEAT_TRANSFER_COEFF_HEATING / SURFACE_AREA_HEAT_DISSIPATION;
    	// May add back for debugging: logger.severe("heatInternal=" + heatInternal + "  deltaT=" + deltaT); return deltaT
    }
    
    /**
     * Turns on active cooling.
     * 
     * @param timeFactor
     * @return
     */
    private double applyForcedAirCooling(double timeFactor) {
    	double amp = timeFactor * 5;
    	double powerFlow = amp * amp * rTotal;
    	double cop = 3;
    	double powerAir = powerFlow * 0.75;
    	double powerAct = powerAir * cop;
    	double deltaTemperature = (powerAct - powerFlow) * HEAT_TRANSFER_COEFF_COOLING * SURFACE_AREA_HEAT_DISSIPATION;
    	// May add back for debugging: logger.severe(0, "delta power=" + (powerAct - powerFlow) + "  deltaTemperature=" + deltaTemperature)
    	// It will consume energy to cool the battery
    	kWhStored -= powerAir;
    	if (kWhStored < 0) {
    		kWhStored = 0;
    		ampHourStored = 0;	
    	}
 
    	return deltaTemperature;
    }
    
    /**
     * Updates the Amp Hour stored capacity [in Ah].
     */
    private void updateAmpHourStored() {
    	ampHourStored = 1000 * kWhStored / HIGHEST_MAX_VOLTAGE; 
    }
    
    /**
     * Updates the full Amp Hour capacity [in Ah].
     * NOTE: DO NOT DELTE. RETAIN THIS METHOD FOR FUTURE USE.
     */
    private void updateFullAmpHourCapacity() {
    	ampHourFullCapacity = 1000 * energyStorageCapacity / HIGHEST_MAX_VOLTAGE; 
    }
    
    /**
     * Gets the maximum power [in kW] available when discharging or drawing power.
     * 
     * @param time in hours
     * @return
     */
    private double getMaxPowerDraw(double time) {
    	// Note: Need to find the physical formula for max power draw
    	return ampHourStored * HIGHEST_MAX_VOLTAGE / time / 1000;
    }

    
    /**
     * Consumes energy from the battery. This will discharge the battery.
     * 
     * @param consumekWh amount of energy to consume [in kWh]
     * @param time in hrs
     * @return energy to be delivered [in kWh]
     */
    public double consumeEnergy(double consumekWh, double time) {
    	
		double available = estimateEnergyToDeliver(consumekWh, time);
		// May add back for debugging : logger.info(unit, "kWh: " + Math.round(kWhStored * 100.0/100.0) + "  available: " + Math.round(available * 10000.0/10000.0) + "  consume: " + Math.round(consumekWh * 10000.0/1000.0))
       	
		double previouskWhStored = kWhStored;
		
    	kWhStored -= available;
    	
    	internalTemperature += computeDeltaTemperature(1000 * available / HIGHEST_MAX_VOLTAGE / time / MarsTime.HOURS_PER_MILLISOL);
    	  	
    	if ((previouskWhStored - kWhStored) / previouskWhStored > .2) {
    	    // If drawing too much energy at a time, it hurts the battery and degrade health
    	    degradeHealth();
    	}
    	
	    cyclesDepleted += available / 3 / energyStorageCapacity;

    	unit.fireUnitUpdate(EntityEventType.BATTERY_EVENT);

    	updateTerminalVoltage();
    	
        updateLowPowerMode();
            
        updateAmpHourStored();
        
    	cumulativeChargeDischarge += consumekWh;
    	
    	if (kWhStored / maxCapNameplate < .02) {
    	    // Unlock the flag for reconditioning
    	    locked = false;
   
    	    changeDegradation();
    	    
    		logger.warning(unit, 10_000L, "Battery almost out of power.");
    	}
    	
        return available;
    }

    /**
     * Estimates how much energy can be accepted and received given the maximum charging rate and an interval of time.
     * 
     * @param hours time in hrs
     * @return energy to be delivered [in kWh]
     */
    public double estimateEnergyToReceive(double hours) {
   
    	double percentStored = getBatteryPercent();
    	double energyAccepted = 0;
    	double percentAccepted = 0;
    	if (percentStored >= 100)
    		return 0;
    	
		percentAccepted = 100 - percentStored;
		energyAccepted = percentAccepted / 100.0 * energyStorageCapacity;

	 	// Consider the effect of the charging rate and the time parameter
    	double maxChargeEnergy = getMaxPowerCharging() * hours;
		
    	return Math.min(maxChargeEnergy, energyAccepted);
    }
    
    /**
     * Gets the maximum power [in kW] that is allowed during charging.
     * 
     * @return maximum power [in kW]
     */
    public double getMaxPowerCharging() {
    	// Note: Need to find the physical formula for max power charge
    	double power = MAX_C_RATING_CHARGING * ampHourStored * HIGHEST_MAX_VOLTAGE / 1000;
    	// May add back for debugging: logger.info("getMaxPowerCharging: " + Math.round(power * 10.0)/10.0 + "  ampHourStored: " + Math.round(ampHourStored* 10.0)/10.0)
    	return power;
    }
    
    /**
     * Charges the battery, namely storing the energy to robot's battery.
     * @Note: For calculating charging time: To estimate charging time, divide 
     * the battery capacity (in Ah) by the charging current (in A), and add 
     * 0.5-1 hour to account for the slower charging rate at the end of the cycle.
     * 
     * @param kWhPumpedIn amount of energy to come in [in kWh]
     * @param hours time in hrs
     * @return energy accepted during charging [in kWh]
     */
    public double chargeBattery(double kWhPumpedIn, double hours) {
		
    	double maxChargeEnergy = estimateEnergyToReceive(hours);
		// Find the smallest amount of energy to be accepted
    	double kWhAccepted = Math.min(kWhPumpedIn, maxChargeEnergy);
		
    	kWhStored += kWhAccepted;

    	internalTemperature += computeDeltaTemperature(1000 * kWhAccepted / HIGHEST_MAX_VOLTAGE / hours);
    	
        updateAmpHourStored();

        updateLowPowerMode();
        
		unit.fireUnitUpdate(EntityEventType.BATTERY_EVENT);
    	
		updateTerminalVoltage();
		
		cumulativeChargeDischarge += kWhPumpedIn;
		
    	return kWhAccepted;
    }
    
    /**
     * Is the battery level at above this prescribed percentage ?
     * 
     * @percent 
     * @return
     */
    public boolean isBatteryAbove(double percent) {
    	return (getBatteryPercent() > percent);
    }

	/** 
	 * Returns the current amount of stored energy in kWh. 
	 */
	public double getCurrentStoredEnergy() {
		return kWhStored;
	}
	
	/** 
	 * Charges up the battery in no time. 
	 */
	public void topUpBatteryEnergy() {
		kWhStored = energyStorageCapacity;
	}
	
	/** 
	 * Gets the percentage that triggers the low power mode for this robot model.
	 */
	public double getLowPowerPercent() {
		return lowPowerPercent;
	}
	
	/**
	 * Is this battery charging ?
	 * 
	 * @return
	 */
	public boolean isCharging() {
		return isCharging;
	}
	
	/**
	 * Sets the charging status.
	 * 
	 * @param value
	 */
	public void setCharging(boolean value) {
		isCharging = value;
	}

    /**
     * Gets the performance factor.
     * 
     * @return The value is between 0 -> 1.
     */
    public double getPerformanceFactor() {
        return performance;
    }

    /**
     * Sets the battery performance factor.
     * 
     * @param newPerformance new performance (between 0 and 1).
     */
    private void setPerformanceFactor(double newPerformance) {
        if (newPerformance <= 1.0 && newPerformance >= 0.0 && performance != newPerformance) {
            performance = newPerformance;
			unit.fireUnitUpdate(EntityEventType.PERFORMANCE_EVENT);
        }
    }

    /**
     * Gets the unit system stress level.
     * 
     * @return stress (0.0 to 100.0)
     */
    public double getStress() {
        return systemLoad;
    }


    /**
     * Checks if the unit is inoperable.
     *
     * @return true if inoperable
     */
    public boolean isInoperable() {
        return !operable;
    }

    /**
     * Returns the percentage of the battery energy level.
     * 
     * @return
     */
    public double getBatteryPercent() {
    	return kWhStored / energyStorageCapacity * 100;
    }

    /**
     * Gets the internal temperature.
     * 
     * @return
     */
    public double getInternalTemperature() {
    	return internalTemperature;    
    }
    
    /**
	 * Gets the current max storage capacity of the battery.
	 * 
	 * (Note : this accounts for the battery degradation over time)
	 * @return capacity (kWh).
	 */
    public double getEnergyStorageCapacity() {
        return energyStorageCapacity;
    }

	public double getMaxCRating() {
		return MAX_C_RATING_DISCHARGING;
	}

    private void updateLowPowerMode() {
        isLowPower = getBatteryPercent() < lowPowerPercent;
    }

	/**
	 * Is the battery on low power mode ?
	 * 
	 * @return
	 */
    public boolean isLowPower() {
    	return isLowPower;
	}
    
    /**
     * Gets the minimum battery power when charging.
     * 
     * @return Percentage (0..100)
     */
    public double getMinimumChargeBattery() {
        return 70D;
    }
    
    /**
     * Gets the standby power consumption rate.
     * 
     * @return power consumed (kW)
     * @throws Exception if error in configuration.
     */
    public double getStandbyPowerConsumption() {
        return standbyPower;
    }

	
	public double getAmpHourStored() {
		return ampHourStored;
	}
	
	public double getMaxCapNameplate() {
		return maxCapNameplate;
	}
	
	public double getPercentDegrade() {
		return percentBatteryDegrade;
	}
	
	public double getHealth() {
		return health;
	}
	
	public double getTerminalVoltage() {
		return terminalVoltage;
	}
	
	/**
	 * Updates the terminal voltage of the battery.
	 */
	private void updateTerminalVoltage() {
		if (energyStorageCapacity > 0) {
			terminalVoltage = kWhStored / energyStorageCapacity * HIGHEST_MAX_VOLTAGE - ampHourStored * rTotal / 3600;
		}
		else {
			terminalVoltage = 0;
		}
    	if (terminalVoltage > HIGHEST_MAX_VOLTAGE) {
			// terminalVoltage should not be greater than HIGHEST_MAX_VOLTAGE
    		terminalVoltage = HIGHEST_MAX_VOLTAGE;
		}
	}
	
	/**
	 * Diagnoses health and update the status of the battery.
	 */
	public void diagnoseBattery() {
		if (health > 1)
			health = 1;
		
    	energyStorageCapacity = energyStorageCapacity * health;
    	
    	if (energyStorageCapacity > maxCapNameplate) {
			// energyStorageCapacity should not be greater than maxCapNameplate
    		energyStorageCapacity = maxCapNameplate;
    		
    	}

    	updateAmpHourStored();
    	
		if (kWhStored > energyStorageCapacity) {
			// kWhStored should not be greater than energyStorageCapacity
			kWhStored = energyStorageCapacity;		
		}
	}
	
	/**
	 * Degrades the health of the battery.
	 * Note: the degradation rate of the battery is % per 1000 milisols.
	 */
	public void degradeHealth() {
    	health = health * (1 - percentBatteryDegrade/100);		
	}
	
	/**
	 * Changes the degradation rate.
	 */
	public void changeDegradation() {
		percentBatteryDegrade = percentBatteryDegrade 
				* (1 + MathUtils.between(cyclesDepleted, 0, 300) / 100);
	}
	
	/**
	 * Gets the number of charge and discharge cycles.
	 * 
	 * @return
	 */
	public double getNumCycles() {
		return cyclesDepleted;
	}
	
	/**
	 * Re-conditions the battery.
	 * 
	 */
	public void reconditionBattery() {
		
		double kWh = kWhStored;
		
		if (!locked) {		
			// Improve health
			health = health * (1 + PERCENT_BATTERY_RECONDITIONING / 100);
			if (health > 1)
				health = 1;
			logger.info(unit, 0, "The battery has just been reconditioned.");
		}
		
		if (kWh > energyStorageCapacity) {
			// kWh should not be greater than energyStorageCapacity but
			kWh = energyStorageCapacity;			
		}	
	
		kWhStored = kWh;
	}
	
	/**
	 * Gets the total resistance.
	 * 
	 * @return
	 */
	public double getTotalResistance() {
		return rTotal;
	}
	
	/**
	 * Gets the stored energy.
	 * 
	 * @return energy (kWh).
	 */
	public double getkWhStored() {
		return kWhStored;
	}
	
    /**
     * Prepares object for garbage collection.
     */
    public void destroy() {
        unit = null;
    }

}
