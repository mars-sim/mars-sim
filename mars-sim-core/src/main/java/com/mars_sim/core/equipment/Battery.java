/*
 * Mars Simulation Project
 * Battery.java
 * @date 2025-09-24
 * @author Manny Kung
 */

package com.mars_sim.core.equipment;

import java.io.Serializable;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.RandomUtil;

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
	private static final int CELLS_PER_MODULE = 104;
	/** The internal resistance [in ohms] in each cell. */	
	private static final double R_CELL = 0.06; 

	private static final double R_LOAD = 1000D; // assume constant load resistance
	
	/** The percent of the terminal voltage prior to cutoff */
	public static final double PERCENT_TERMINAL_VOLTAGE = 66.67;
    /** The standard voltage of this battery pack in volts. */
    public static final double HIGHEST_MAX_VOLTAGE = 600; // 436.8;
    /** The standard voltage of a drone battery pack in volts. */
    public static final double DRONE_VOLTAGE = 48;
    
    /** The maximum current that can be safely drawn from this battery pack in Ampere. */
    // May add back: private static final double MAX_AMP_DRAW = 120
  
	/**
	 * The nominal capacity (Amp hours) of a lithium cell is about 250mAh at the 
	 * discharge current of 1C.
	 */
	private static final double NOMINAL_AMP_HOURS = .25;
	/** The maximum continuous charge rate (within the safety limit) that this battery can handle. */
	private static final int MAX_C_RATING_CHARGING = 4;

	public static final double HOURS_PER_MILLISOL = 0.0247 ; //MarsTime.SECONDS_IN_MILLISOL / 3600D;
	/** The percent of health improvement after reconditioning. */
	public static final double PERCENT_BATTERY_RECONDITIONING = .075; // [in %]
	 
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
	/** The number of times the battery has been fully discharged/depleted since last reconditioning. */
	private int timesFullyDepleted;
	/** The number of cycles of charging and discharge the battery. */
	private int numChargeCycles;
	/** The last number of cycles of charging and discharge the battery. */
	private int lastNumChargeCycles;
	
	
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
        performance = 1.0D;
        operable = true;
        
        lowPowerPercent = 5;
        standbyPower = 0.01;
        
        this.numModules = numModules;
		rTotal = R_CELL * numModules * CELLS_PER_MODULE;
		
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
	 * @param rLoad  the load resistance of the external circuit (power grid, vehicle, robot) 
	 * @param time    in millisols
	 * @return energy available to be delivered
	 */
	public double computeAvailableEnergy(double neededkWh, double rLoad, double time) {
		if (neededkWh <= 0)
			return 0;
		
		double stored = getkWattHourStored();
		
		double maxCap = getEnergyStorageCapacity();
		
		if (stored <= 0)
			return 0;

		double vTerminal = getTerminalVoltage();
		// Assume the internal resistance of the battery is constant
		double rInt = getTotalResistance();
		// Assume max stateOfCharge is 1
		double stateOfCharge = stored / maxCap;
		// Use fudge_factor (from 0.0 to 5.0) to improve the power delivery but decreases 
		// as the battery is getting depleted
		double fudgeFactor = 5 * stateOfCharge * stateOfCharge;
		// The output voltage
		double vOut = vTerminal * rLoad / (rLoad + rInt);

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
		
		double cRating = getMaxCRating();
		double nowAmpHr = ampHrRating * cRating * fudgeFactor * time;
		double possiblekWh = nowAmpHr / 1000D * vOut;

		double availablekWh = Math.min(stored, Math.min(possiblekWh, neededkWh));
		
		return availablekWh;
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

        return operable;
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
     * Gets the maximum power [in kW] that is allowed during charging.
     * 
     * @param time in hours
     * @return maximum power [in kW]
     */
    public double getMaxPowerCharging(double hours) {
    	// Note: Need to find the physical formula for max power charge
    	return MAX_C_RATING_CHARGING * ampHourStored * HIGHEST_MAX_VOLTAGE / hours / 1000;
    }
    
    /**
     * Gets the maximum energy [in kWh] that can be accepted during charging.
     * 
     * @param time in hours
     * @return maximum energy [in kWh]
     */
    public double getMaxEnergyCharging(double hours) {
    	// Note: Need to find the physical formula for max power charge
    	return getMaxPowerCharging(hours) * 3600;
    }

    /**
     * Requests energy from the battery.
     * 
     * @param consumekWh amount of energy to consume [in kWh]
     * @param time in hrs
     * @return energy to be delivered [in kWh]
     */
    public double requestEnergy(double consumekWh, double time) {
    	
		double available = computeAvailableEnergy(consumekWh, R_LOAD, time);
		// May add back for debugging: logger.info(robot, "kWh: " + kWhStored + "  available: " + available + "  consume: " + consumekWh)
       	
    	kWhStored -= available; 
    	
    	unit.fireUnitUpdate(UnitEventType.BATTERY_EVENT);

    	updateTerminalVoltage();
    	
        updateLowPowerMode();
            
        updateAmpHourStored();
        
    	cumulativeChargeDischarge += consumekWh;
    	
    	if (kWhStored <= 0) {
    		kWhStored = 0;
    		
    	    // Unlock the flag for reconditioning
    	    locked = false;
    	    
    	    degradeHealth();
    	    
    	    timesFullyDepleted++;

    		logger.warning(unit, 30_000L, "Battery out of power.");
    	}
    	
        return available;
    }

    /**
     * Estimates how much energy will be accepted given the maximum charging rate and an interval of time.
     * 
     * @param hours time in hrs
     * @return energy to be delivered [in kWh]
     */
    public double estimateChargeBattery(double hours) {
   
    	double percentStored = getBatteryPercent();
    	double energyAccepted = 0;
    	double percentAccepted = 0;
    	if (percentStored >= 100)
    		return 0;
    	
		percentAccepted = 100 - percentStored;
		energyAccepted = percentAccepted / 100.0 * energyStorageCapacity;

	 	// Consider the effect of the charging rate and the time parameter
    	double maxChargeEnergy = getMaxEnergyCharging(hours);
		
    	return Math.min(maxChargeEnergy, energyAccepted);
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
		
    	double maxChargeEnergy = estimateChargeBattery(hours);
		// Find the smallest amount of energy to be accepted
    	double kWhAccepted = Math.min(kWhPumpedIn, maxChargeEnergy);
		
    	kWhStored += kWhAccepted;

        updateAmpHourStored();

        updateLowPowerMode();
        
		unit.fireUnitUpdate(UnitEventType.BATTERY_EVENT);
    	
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
	 * Returns the current amount of energy in kWh. 
	 */
	public double getCurrentEnergy() {
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
     * Sets the performance factor.
     * 
     * @param newPerformance new performance (between 0 and 1).
     */
    private void setPerformanceFactor(double newPerformance) {
        if (newPerformance != performance) {
            performance = newPerformance;
			if (unit != null)
				unit.fireUnitUpdate(UnitEventType.PERFORMANCE_EVENT);
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
	 * Gets the current max storage capacity of the battery.
	 * 
	 * (Note : this accounts for the battery degradation over time)
	 * @return capacity (kWh).
	 */
    public double getEnergyStorageCapacity() {
        return energyStorageCapacity;
    }

	public double getMaxCRating() {
		return MAX_C_RATING_CHARGING;
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
    	if (energyStorageCapacity > maxCapNameplate)
    		energyStorageCapacity = maxCapNameplate;

    	updateAmpHourStored();
    	
		if (kWhStored > energyStorageCapacity) {
			kWhStored = energyStorageCapacity;		
		}
	}
	
	/**
	 * Degrades the health of the battery.
	 * Note: the degradation rate of the battery is % per 1000 milisols
	 */
	public void degradeHealth() {
    	health = health * (1 - percentBatteryDegrade/100/1000);		
	}

	/**
	 * Updates the number of charge and discharge cycles.
	 */
	public void updateNumCycles() {
		double value = cumulativeChargeDischarge / maxCapNameplate / 2;
		numChargeCycles = (int)value;
	}
	
	/**
	 * Gets the number of charge and discharge cycles.
	 * 
	 * @return
	 */
	public int getNumCycles() {
		return numChargeCycles;
	}
	
	/**
	 * Reconditions the battery.
	 * 
	 */
	public void reconditionBattery() {
		
		double kWh = kWhStored;
		
		if (!locked) {
			
			if (timesFullyDepleted > 10 || lastNumChargeCycles != numChargeCycles) {
	
				// Reset counter
				if (timesFullyDepleted > 10)
					timesFullyDepleted = 0;
				
				if (lastNumChargeCycles != numChargeCycles)
					lastNumChargeCycles = numChargeCycles;
				
				// Improve health
				health = health * (1 + PERCENT_BATTERY_RECONDITIONING/100D);
				if (health > 1)
					health = 1;
				logger.info(unit, 0, "The battery has just been reconditioned.");
			}
		}
		
		if (kWh > energyStorageCapacity) {
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
	 * Gets the building's stored energy.
	 * 
	 * @return energy (kW hr).
	 */
	public double getkWattHourStored() {
		return kWhStored;
	}
	
    /**
     * Prepares object for garbage collection.
     */
    public void destroy() {
        unit = null;
    }

}
