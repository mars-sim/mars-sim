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
	private static final double POWER_SAVE_CONSUMPTION = .01;

	private static final double R_LOAD = 1000D; // assume constant load resistance
	public static final double HOURS_PER_MILLISOL = 0.0247 ; //MarsTime.SECONDS_IN_MILLISOL / 3600D;
	public static final double SECONDARY_LINE_VOLTAGE = 240D;
	public static final double BATTERY_MAX_VOLTAGE = 374.4D;
	public static final double PERCENT_BATTERY_RECONDITIONING_PER_CYCLE = .1; // [in %]
	/** 
	 * The number of cells per module of the battery. 
	 * Note: 3.6 V * 104 = 374.4 V 
	 * e.g. : Tesla Model S has 104 cells per module
	 */
	private static final int CELLS_PER_MODULE = 104;
	
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
	private int timesFullyDepleted = 0;
	
	/** The degradation rate of the battery in % per sol. May be reduced via research. */
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
	/** The internal resistance [in ohms] in each cell. */	
	public double rCell = 0.06; 

	/**  
	 * The total internal resistance of the battery.
	 * rTotal = rCell * # of cells * # of modules
	 */
	private double rTotal;  
	
	/**The maximum continuous discharge rate (within the safety limit) of this battery. */
	private double maxCRating = 4D;

	/** 
	 * The rating [in ampere-hour or Ah]  of the battery in terms of its charging/discharging ability at 
	 * a particular C-rating. An amp is a measure of electrical current. The hour 
	 * indicates the length of time that the battery can supply this current.
	 * e.g. a 2.2Ah battery can supply 2.2 amps for an hour
	 */
	private double ampHours;	
	
	/** The maximum energy [in kWh, not Wh] storage capacity. */
	private double currentMaxCap; 
	
	/*
	 * The Terminal voltage is between the battery terminals with load applied. 
	 * It varies with SOC and discharge/charge current.
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
        currentMaxCap = maxCapNameplate;
        
        numModules = (int)(Math.ceil(currentMaxCap/2));
		rTotal = rCell * numModules * CELLS_PER_MODULE;
		ampHours = 1000D * currentMaxCap/SECONDARY_LINE_VOLTAGE;	
		
		// At the start of sim, set to a random value		
    	kWhStored = lowPowerModePercent * (.5 + RandomUtil.getRandomDouble(.5));	
		   
        updateVoltage();
        
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
    	
        // Consume a minute amount of energy even if a robot does not perform any tasks
    	if (onPowerSave) {
    		consumeEnergy(time * MarsTime.HOURS_PER_MILLISOL * powerSavekW, time);
    	}
    	else if (!isCharging && !robot.getTaskManager().hasTask()) {
        	consumeEnergy(time * MarsTime.HOURS_PER_MILLISOL * standbykW, time);	
		}
		
    	if (pulse.isNewSol()) {
	        locked = false;
	        updateHealth();
	    	diagnoseBattery();
	    	updateVoltage();
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
    		
    		double newkWhStored = kWhStored - available;
    		
		    reconditionBattery(newkWhStored);
		    
	    	robot.fireUnitUpdate(UnitEventType.BATTERY_EVENT);

	    	updateVoltage();
	    	
	    	if (kWhStored <= 0) {
	    		logger.warning(robot, 30_000L, "Out of power.");
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
    	return (getBatteryState() > percent);
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
	 * Returns the current amount of energy in kWh. 
	 */
	public double getCurrentEnergy() {
		return kWhStored;
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
    public double getBatteryState() {
    	return (kWhStored * 100D) / currentMaxCap;
    }
    
    /**
     * Gets the maximum battery capacity in kWh.
     */
    public double getBatteryCapacity() {
        return currentMaxCap;
    }

	public double getMaxCRating() {
		return maxCRating;
	}
	
    /**
     * Is the robot on low power mode ?
     * 
     * @return
     */
    public boolean isLowPower() {
    	return getBatteryState() < lowPowerModePercent;
    }
    
    /**
     * Stores the energy to robot's battery.
     * 
     * @param storekWh
     * @return the energy unable to accepted
     */
    public double storeEnergy(double storekWh) {
    	double newEnergy = kWhStored + storekWh;
        double targetEnergy = currentMaxCap;
        double unable2Accept = newEnergy - targetEnergy;
    	if (newEnergy >= targetEnergy) {
    		newEnergy = targetEnergy;   		
    		// Target reached and quit charging
    		isCharging = false;
    	}
    	
    	kWhStored = newEnergy;
   	
		robot.fireUnitUpdate(UnitEventType.BATTERY_EVENT);
    	
		updateVoltage();
		
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
    
	/**
	 * Updates the terminal voltage of the battery.
	 */
	private void updateVoltage() {
    	terminalVoltage = kWhStored / ampHours * 1000D;
    	if (terminalVoltage > BATTERY_MAX_VOLTAGE)
    		terminalVoltage = BATTERY_MAX_VOLTAGE;
	}
	
	public double getAmpHourRating() {
		return ampHours;
	}
	
	public double getTerminalVoltage() {
		return terminalVoltage;
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
				logger.info(robot, "The battery has just been reconditioned.");
			}
		}
		
		if (kWh > currentMaxCap) {
			kWh = currentMaxCap;			
		}	
	
		kWhStored = kWh;	
	
//		updateVoltage();
	}
	
	public double getTotalResistance() {
		return rTotal;
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
	
    /**
     * Prepares object for garbage collection.
     */
    public void destroy() {
        robot = null;
    }
}
