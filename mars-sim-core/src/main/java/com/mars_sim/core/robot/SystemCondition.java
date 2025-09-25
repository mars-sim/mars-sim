/*
 * Mars Simulation Project
 * SystemCondition.java
 * @date 2025-09-24
 * @author Manny Kung
 */

package com.mars_sim.core.robot;

import java.io.Serializable;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;

/**
 * This class represents the System Condition of a robot.
 * It models a robot's health.
 */
public class SystemCondition implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(SystemCondition.class.getName());

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
	public static final double HIGHEST_MAX_VOLTAGE = 436.8;
	
	/** The maximum continuous discharge rate (within the safety limit) of this battery. */
	private static final int MAX_C_RATING_CHARGING = 4;
	/**
	 * The nominal capacity (Amp hours) of a lithium cell is about 250mAh at the 
	 * discharge current of 1C.
	 */
	private static final double NOMINAL_AMP_HOURS = .25;
	
	private static final int RECOMMENDED_LEVEL = 70;
	
	private static final double POWER_SAVE_CONSUMPTION = .1;

	public static final double HOURS_PER_MILLISOL = 0.0247 ; //MarsTime.SECONDS_IN_MILLISOL / 3600D;

	/** The percent of health improvement after reconditioning. */
	public static final double PERCENT_BATTERY_RECONDITIONING = .075; // [in %]
	
    // Data members
	   /** Is the unit at low power mode ? */  
    private boolean isLowPower;
    /** Is the robot charging ? */  
    private boolean isCharging;
	/** True if the battery reconditioning is prohibited. */
	private boolean locked;
    /** Is the robot on power save mode ? */  
    private boolean onPowerSave;

	/** The number of modules of the battery. */
	private int numModules;
	/** The number of times the battery has been fully discharged/depleted since last reconditioning. */
	private int timesFullyDepleted;
	/** The number of cycles of charging and discharge the battery. */
	private int numChargeCycles;
	/** The last number of cycles of charging and discharge the battery. */
	private int lastNumChargeCycles;
	
	
	
    /** The power consumed in the standby mode in kW. */
    private double standbyPower;
    /** The power consumed in the power save mode in kW. */
    private double powerSavekW;
    /** Robot's stress level (0.0 - 100.0). */
    private double systemLoad;
    /** Performance factor. */
    private double performance;
	/** The percentage that triggers low power warning. */
    private double lowPowerPercent = 5;
	
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
	
    private Robot robot;

    /**
     * Constructor.
     * 
     * @param robot The robot requiring a physical presence.
     */
    public SystemCondition(Robot newRobot, RobotSpec spec) {
        robot = newRobot;
        performance = 1.0D;
   
        lowPowerPercent = spec.getLowPowerModePercent();
        standbyPower = spec.getStandbyPowerConsumption();
        powerSavekW = POWER_SAVE_CONSUMPTION * standbyPower; 
        
        maxCapNameplate = spec.getMaxCapacity();
        energyStorageCapacity = maxCapNameplate;
        
        numModules = (int)(Math.ceil(energyStorageCapacity/2));
		rTotal = R_CELL * numModules * CELLS_PER_MODULE;
		
		// At the start of sim, set to a random value		
    	kWhStored = energyStorageCapacity * (.5 + RandomUtil.getRandomDouble(.5));	

		updateFullAmpHourCapacity();
		
		updateAmpHourStored();
		
    	updateTerminalVoltage();
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

//		logger.info(robot, "kWh: " + Math.round(stored * 100.0)/100.0
//				+ "  available: " + Math.round(availablekWh * 10000.0)/10000.0 
//				+ "  needed: " + Math.round(needed * 10000.0)/10000.0
//				+ "  possiblekWh: " + Math.round(possiblekWh * 10000.0)/10000.0
//				+ "  ampHrRating: " + Math.round(ampHrRating * 100.0)/100.0
//				+ "  nowAmpHr: " + Math.round(nowAmpHr * 100.0)/100.0);
		
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
//	        reconditionBattery();
	    	diagnoseBattery();
		}
    	else {
    		
    		int msol = pulse.getMarsTime().getMillisolInt();

    		// Note: Avoid checking at < 10 or 1000 millisols
    		//       due to high cpu util during the change of a sol
    		if (pulse.isNewIntMillisol() && msol >= 10 && msol < 995) {
 
    	        // Consume a minute amount of energy even if a robot does not perform any tasks
    	    	if (onPowerSave && kWhStored > 0) {
    	    		requestEnergy(MarsTime.HOURS_PER_MILLISOL * powerSavekW, MarsTime.HOURS_PER_MILLISOL);
    	    	}
    	    	else if (!isCharging && !robot.getTaskManager().hasTask() && kWhStored > 0) {
    	        	requestEnergy(MarsTime.HOURS_PER_MILLISOL * standbyPower, MarsTime.HOURS_PER_MILLISOL);	
    			}
    	    	
    	    	int remainder = msol % 10;
    			if (remainder == 1) {
    				degradeHealth();
    		    	updateNumCycles();
    			}
    		}
		}
    	
        return true;
    }

    /**
     * Updates the Amp Hour stored capacity [in Ah].
     */
    private void updateAmpHourStored() {
    	ampHourStored = 1000 * kWhStored / HIGHEST_MAX_VOLTAGE; 
    }
    
    /**
     * Updates the Amp Hour rating.
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
     * @param consumekWh amount of energy to consume [in kWh].
    * @param time in hrs
     * @return energy to be delivered [in kWh]
     */
    public void requestEnergy(double consumekWh, double time) {
    	if (!isCharging) {
	    	
    		double available = computeAvailableEnergy(consumekWh, R_LOAD, time);
    		// May add back for debugging: logger.info(robot, "kWh: " + kWhStored + "  available: " + available + "  consume: " + consumekWh)
    		
    		kWhStored -= available;
    		  
	    	robot.fireUnitUpdate(UnitEventType.BATTERY_EVENT);

	    	updateTerminalVoltage();
	    	
	    	updateAmpHourStored();
	    	
	    	cumulativeChargeDischarge += consumekWh;
	    	
	    	if (kWhStored <= 0) {
	    		kWhStored = 0;
	    		
	    	    // Unlock the flag for reconditioning
	    	    locked = false;
	    	    
	    	    degradeHealth();
	    	    
	    	    timesFullyDepleted++;

	    		logger.warning(robot, 30_000L, "Battery out of power.");
	    	}
    	}
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
     * Gets the recommended battery charging threshold.
     * 
     * @return
     */
    public double getRecommendedThreshold() {
        return RECOMMENDED_LEVEL;
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
		
		if (isCharging) {
			robot.addSecondaryStatus(BotMode.CHARGING);
		}
		else {
			robot.removeSecondaryStatus(BotMode.CHARGING);
		}
	}
	
	/**
	 * Sets the maintenance status of the robot.
	 *  
	 * @param value
	 */
	public void setMaintenance(boolean value) {
		if (value) {
			setPowerSave(true);
			robot.setPrimaryStatus(BotMode.POWER_DOWN);
			robot.addSecondaryStatus(BotMode.MAINTENANCE);
			robot.setInoperable(true);
		}
		else {
			setPowerSave(false);
			robot.setPrimaryStatus(BotMode.NOMINAL);
			robot.removeSecondaryStatus(BotMode.MAINTENANCE);
			robot.setInoperable(false);
		}
	}

	/**
	 * Is the robot in maintenance status.
	 * 
	 * @return
	 */
	public boolean isInMaintenance() {
		return robot.haveStatusType(BotMode.MAINTENANCE);
	}
	
    /**
     * Gets the performance factor that effect Person with the complaint.
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
    void setPerformanceFactor(double newPerformance) {
        if (newPerformance != performance) {
            performance = newPerformance;
			if (robot != null)
				robot.fireUnitUpdate(UnitEventType.PERFORMANCE_EVENT);
        }
    }

    /**
     * Gets the robot system stress level.
     * 
     * @return stress (0.0 to 100.0)
     */
    public double getStress() {
        return systemLoad;
    }

    /**
     * Estimates how much energy will be accepted given the maximum charging rate and an interval of time.
     * 
     * @param hours time in hrs
     * @return energy to be delivered [in kWh]
     */
    public double estimateChargeBattery(double hours) {
   
    	double percentStored = kWhStored / energyStorageCapacity * 100;
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
//    	double newEnergy = kWhStored + kWhPumpedIn;
//        double targetEnergy = energyStorageCapacity;
//        double excessEnergy = newEnergy - targetEnergy;
//        
//    	if (newEnergy >= targetEnergy) {
//    		newEnergy = targetEnergy;   		
//    		// Target reached and quit charging
//    		isCharging = false;
//    	}
//    	
//    	kWhStored = newEnergy;
    	
    	double maxChargeEnergy = estimateChargeBattery(hours);
		// Find the smallest amount of energy to be accepted
    	double kWhAccepted = Math.min(kWhPumpedIn, maxChargeEnergy);
		
    	kWhStored += kWhAccepted;

        updateAmpHourStored();

        updateLowPowerMode();
        
		robot.fireUnitUpdate(UnitEventType.BATTERY_EVENT);
    	
		updateTerminalVoltage();
		
		cumulativeChargeDischarge += kWhPumpedIn;
		
		return kWhAccepted;
    }

 
    /** 
     * Is the robot on power save mode ? 
     */ 
    public boolean isPowerSave() {  
    	return onPowerSave;
    }
    
    /** 
     * Sets the robot power save mode. 
     * 
     * @param value
     */
    public void setPowerSave(boolean value) {  
    	onPowerSave = value;
    	
    	if (value && isCharging) {
    		// Turns off charging
    		isCharging = false;
    	}
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

//    /**
//     * Is the robot on low power mode ?
//     * 
//     * @return
//     */
//    public boolean isLowPower() {
//    	return getBatteryLevel() < lowPowerModePercent;
//    }
    
	/**
	 * Is the battery on low power mode ?
	 * 
	 * @return
	 */
    public boolean isLowPower() {
    	return isLowPower;
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
	private void diagnoseBattery() {
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
	private void degradeHealth() {
    	health = health * (1 - percentBatteryDegrade/100/1000);		
	}

	/**
	 * Updates the number of charge and discharge cycles.
	 */
	private void updateNumCycles() {
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
				logger.info(robot, 0, "The battery has just been reconditioned.");
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
        robot = null;
    }
}
