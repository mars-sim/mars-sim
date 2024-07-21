/*
 * Mars Simulation Project
 * SystemCondition.java
 * @date 2024-06-27
 * @author Manny Kung
 */

package com.mars_sim.core.robot;

import java.io.Serializable;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.tools.util.RandomUtil;

/**
 * This class represents the System Condition of a robot.
 * It models a robot's health.
 */
public class SystemCondition implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(SystemCondition.class.getName());

	private static final int RECOMMENDED_LEVEL = 70;
	private static final double POWER_SAVE_CONSUMPTION = .1;

	private static final double R_LOAD = 1000D; // assume constant load resistance
	public static final double HOURS_PER_MILLISOL = 0.0247 ; //MarsTime.SECONDS_IN_MILLISOL / 3600D;
	
//	public static final double SECONDARY_LINE_VOLTAGE = 240D;
	
	public static final double HIGHEST_MAX_VOLTAGE = 436.8;
	/** The percent of health improvement after reconditioning. */
	public static final double PERCENT_BATTERY_RECONDITIONING = .075; // [in %]
	/** The percent of the terminal voltage prior to cutoff */
	public static final double PERCENT_TERMINAL_VOLTAGE = 66.67;
	/** 
	 * The number of cells per module of the battery. 
	 * Note: 3.6 V * 104 = 374.4 V 
	 * 4.2 V * 104 = 436.8 V
	 * e.g. : Tesla Model S has 104 cells per module
	 */
	private static final int CELLS_PER_MODULE = 104;
	/**The maximum continuous discharge rate (within the safety limit) of this battery. */
	private static final int MAX_C_RATING = 4;
	/**
	 * The nominal capacity (Amp hours) of a lithium cell is about 250mAh at the 
	 * discharge current of 1C.
	 */
	private static final double NOMINAL_AMP_HOURS = .25;
	/** The internal resistance [in ohms] in each cell. */	
	private static final double R_CELL = 0.06; 
	
    // Data members
    /** Is the robot operational ? */
    private boolean operable;
    /** Is the robot charging ? */  
    private boolean isCharging;
    /** Is the robot on power save mode ? */  
    private boolean onPowerSave;
	/**
	 * True if the battery reconditioning is prohibited.
	 */
	private boolean locked;
	
	/** The number of modules of the battery. */
	private int numModules = 0;
	/** The number of times the battery has been fully discharged/depleted since last reconditioning. */
	private int timesFullyDepleted;
	/** The number of cycles of charging and discharge the battery. */
	private int numChargeCycles;
	/** The last number of cycles of charging and discharge the battery. */
	private int lastNumChargeCycles;
	
	/** The lifecycle of energy charging and discharging. For lifecycle analysis. */
	public double cumulativeChargeDischarge;
	/** The degradation rate of the battery in % per 1000 milisols. May be reduced via research. */
	public double percentBatteryDegrade = .05;
	/** The health of the battery. */
	private double health = 1D; 
    /** The power consumed in the standby mode in kW. */
    private double standbykW;
    /** The power consumed in the power save mode in kW. */
    private double powerSavekW;
    /** Robot's stress level (0.0 - 100.0). */
    private double systemLoad;
    /** Performance factor. */
    private double performance;
	/** The percentage that triggers low power warning. */
    private double lowPowerModePercent;
	/** 
	 * The energy [in kilo Watt-hour] currently stored in the battery. 
	 * The Watt-hour (Wh) signifies that a battery can supply an amount of power for an hour
	 * e.g. a 60 Wh battery can power a 60 W light bulb for an hour
	 */
	private double kWhStored;
	/** The maximum nameplate kWh of this battery. */	
	public double maxCapNameplate;

	/**  
	 * The total internal resistance of the battery.
	 * rTotal = rCell * # of cells * # of modules
	 */
	private double rTotal;

	/** 
	 * The rating [in ampere-hour or Ah] of the battery in terms of its 
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
	private double ampHour;	
	
	/** The energy storage capacity [in kWh, not Wh] capacity. */
	private double energyStorageCapacity; 
	
	/*
	 * The Terminal voltage is between the battery terminals with load applied. 
	 * It varies with SOC and discharge/charge current.
	 * If the load increases, the terminal voltage lowers, due to the internal 
	 * series resistance of the battery.
	 */
	private double terminalVoltage; 

    private Robot robot;

    /**
     * Constructor.
     * 
     * @param robot The robot requiring a physical presence.
     */
    public SystemCondition(Robot newRobot, RobotSpec spec) {
        robot = newRobot;
        performance = 1.0D;
        operable = true;

        lowPowerModePercent = spec.getLowPowerModePercent();
        standbykW = spec.getStandbyPowerConsumption();
        powerSavekW = POWER_SAVE_CONSUMPTION * standbykW; 
        maxCapNameplate = spec.getMaxCapacity();
        energyStorageCapacity = maxCapNameplate;
        
        numModules = (int)(Math.ceil(energyStorageCapacity/2));
		rTotal = R_CELL * numModules * CELLS_PER_MODULE;
		
		// At the start of sim, set to a random value		
    	kWhStored = energyStorageCapacity * (.5 + RandomUtil.getRandomDouble(.5));	

		updateAmpHourRating();

    	updateTerminalVoltage();

//		logger.info(spec.getRobotType().getName() + " - maxCapNameplate: " + maxCapNameplate 
//				+ "  numModules: " + numModules
//				+ "  rCell: " + rCell
//				+ "  rTotal: " + rTotal
//				+ "  ampHours: " + ampHours
//				+ "  maxCRating: " + maxCRating
//				+ "  Vt: " + terminalVoltage
//				+ "  kWhStored: " + kWhStored);
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

		double ampHr = getAmpHour();
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

		double availablekWh = Math.min(stored, Math.min(possiblekWh, needed));

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
     * @param time amount of time passing (in millisols)
     * @param support life support system.
     * @param config robot configuration.
     */
    public boolean timePassing(ClockPulse pulse) {
    	double time = pulse.getElapsed();
    	if (time < 0.001)
    		return false;
		
    	if (pulse.isNewSol()) {
	        reconditionBattery();
    	}
    	else if (pulse.isNewHalfSol()) {
	        locked = false;
	        reconditionBattery();
	    	diagnoseBattery();
		}
    	else {
    		
    		int msol = pulse.getMarsTime().getMillisolInt();

    		// Note: Avoid checking at < 10 or 1000 millisols
    		//       due to high cpu util during the change of a sol
    		if (pulse.isNewIntMillisol() && msol >= 10 && msol < 995) {
 
    	        // Consume a minute amount of energy even if a robot does not perform any tasks
    	    	if (onPowerSave && kWhStored > 0) {
    	    		consumeEnergy(MarsTime.HOURS_PER_MILLISOL * powerSavekW, 1);
    	    	}
    	    	else if (!isCharging && !robot.getTaskManager().hasTask() && kWhStored > 0) {
    	        	consumeEnergy(MarsTime.HOURS_PER_MILLISOL * standbykW, 1);	
    			}
    	    	
    	    	int remainder = msol % 10;
    			if (remainder == 1) {
    				degradeHealth();
    		    	updateNumCycles();
    			}
    		}
		}
    	
        return operable;
    }

    /**
     * Consumes a given amount of energy.
     * 
     * @param amount amount of energy to consume [in kWh].
     * @param container unit to get power from
     * @throws Exception if error consuming power.
     */
    public void consumeEnergy(double consumekWh, double time) {
    	if (!isCharging) {
	    	
    		double available = computeAvailableEnergy(consumekWh, R_LOAD, time);
//    		logger.info(robot, "kWh: " + kWhStored + "  available: " + available + "  consume: " + consumekWh );
    		
    		kWhStored -= available;
    		  
	    	robot.fireUnitUpdate(UnitEventType.BATTERY_EVENT);

	    	updateTerminalVoltage();
	    	
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
    	return (getBatteryLevel() > percent);
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
	public double getLowPowerModePercent() {
		return lowPowerModePercent;
	}
	
	/**
	 * Is this robot charging ?
	 * 
	 * @return
	 */
	public boolean isCharging() {
		return isCharging;
	}
	
	/**
	 * Sets the robot charging status.
	 * 
	 * @param value
	 */
	public void setCharging(boolean value) {
		isCharging = value;
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
    private void setPerformanceFactor(double newPerformance) {
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
     * Sets this robot to inoperable.
     */
    public void setInoperable() {
        setPerformanceFactor(0D);
        operable = false;
    }

    /**
     * Checks if the robot is inoperable.
     *
     * @return true if inoperable
     */
    public boolean isInoperable() {
        return !operable;
    }

    /**
     * Returns a percentage 0..100 of the battery energy level.
     * 
     * @return
     */
    public double getBatteryLevel() {
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
		return MAX_C_RATING;
	}
	
    /**
     * Is the robot on low power mode ?
     * 
     * @return
     */
    public boolean isLowPower() {
    	return getBatteryLevel() < lowPowerModePercent;
    }
    
    /**
     * Charges the battery, namely storing the energy to robot's battery.
     * Note: For calculating charging time: To estimate charging time, divide 
     * the battery capacity (in Ah) by the charging current (in A), and add 
     * 0.5-1 hour to account for the slower charging rate at the end of the cycle.
     * 
     * @param storekWh
     * @return the energy unable to accepted
     */
    public double storeEnergy(double storekWh) {
    	double newEnergy = kWhStored + storekWh;
        double targetEnergy = energyStorageCapacity;
        double unable2Accept = newEnergy - targetEnergy;
    	if (newEnergy >= targetEnergy) {
    		newEnergy = targetEnergy;   		
    		// Target reached and quit charging
    		isCharging = false;
    	}
    	
    	kWhStored = newEnergy;
   	
		robot.fireUnitUpdate(UnitEventType.BATTERY_EVENT);
    	
		updateTerminalVoltage();
		
		cumulativeChargeDischarge += storekWh;
		
		if (unable2Accept > 0)
			return unable2Accept;
		
		return 0;
    }

    /**
     * Gets the standby power consumption rate.
     * 
     * @return power consumed (kW)
     * @throws Exception if error in configuration.
     */
    public double getStandbyPowerConsumption() {
        return standbykW;
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
	
	public double getAmpHour() {
		return ampHour;
	}
	
	public double getTerminalVoltage() {
		return terminalVoltage;
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
	
    /**
     * Updates the Amp Hour rating.
     */
    private void updateAmpHourRating() {
    	ampHour = 1000 * energyStorageCapacity / HIGHEST_MAX_VOLTAGE; 
    }

	/**
	 * Updates the terminal voltage of the battery.
	 */
	private void updateTerminalVoltage() {
		if (energyStorageCapacity > 0) {
			terminalVoltage = kWhStored / energyStorageCapacity * HIGHEST_MAX_VOLTAGE - ampHour * rTotal / 3600;
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

    	updateAmpHourRating();
    	
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
				logger.info(robot, 10_000, "The battery has just been reconditioned.");
			}
		}
		
		if (kWh > energyStorageCapacity) {
			kWh = energyStorageCapacity;			
		}	
	
		kWhStored = kWh;
	}
	
	
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
