/*
 * Mars Simulation Project
 * SystemCondition.java
 * @date 2023-12-04
 * @author Manny Kung
 */

package com.mars_sim.core.robot;

import java.io.Serializable;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.logging.SimLogger;
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
	
    // Data members
    /** Is the robot operational ? */
    private boolean operable;
    /** Is the robot at low power mode ? */  
    private boolean isLowPower;
    /** Is the robot charging ? */  
    private boolean isCharging;
    /** Is the robot on power save mode ? */  
    private boolean onPowerSave;
    
    /** The power consumed in the standby mode in kW. */
    private double standbykW;
    /** The power consumed in the power save mode in kW. */
    private double powerSavekW;
    /** Robot's stress level (0.0 - 100.0). */
    private double systemLoad;
    /** Performance factor. */
    private double performance;
	/** The percentage that triggers low power warning. */
    private double lowPowerPercent;
	/** The current energy of the robot in kWh. */
	private double currentEnergy;
	/** The maximum capacity of the battery in kWh. */	
	private double maxCapacity;

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

        lowPowerPercent = spec.getLowPowerModePercent();
        standbykW = spec.getStandbyPowerConsumption();
        powerSavekW = POWER_SAVE_CONSUMPTION * standbykW; 
        maxCapacity = spec.getMaxCapacity();
        currentEnergy = RandomUtil.getRandomDouble(maxCapacity * (lowPowerPercent/100 * 2), maxCapacity);
        updateLowPowerMode();
    }

    private void updateLowPowerMode() {
        isLowPower = getBatteryState() < lowPowerPercent;
    }

    /**
     * This method reflects a passing of time.
     * 
     * @param time amount of time passing (in millisols)
     * @param support life support system.
     * @param config robot configuration.
     */
    public boolean timePassing(double time) {

        // Consume a minute amount of energy even if a robot does not perform any tasks
    	if (onPowerSave) {
    		consumeEnergy(time * MarsTime.HOURS_PER_MILLISOL * powerSavekW);
    	}
    	else if (!isCharging)
        	consumeEnergy(time * MarsTime.HOURS_PER_MILLISOL * standbykW);

        return operable;
    }

    /**
     * Consumes a given amount of energy.
     * 
     * @param amount amount of energy to consume [in kWh].
     * @param container unit to get power from
     * @throws Exception if error consuming power.
     */
    public void consumeEnergy(double kWh) {
    	if (!isCharging) {
	    	double diff = currentEnergy - kWh;
	    	if (diff >= 0) {
	    		currentEnergy = diff; 
	    		robot.fireUnitUpdate(UnitEventType.BATTERY_EVENT);

                updateLowPowerMode();
	    	}
	    	else
	    		logger.warning(robot, 30_000L, "Out of power.");
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
     * Gets the recommended battery charing threshold.
     * 
     * @return
     */
    public double getRecommendedThreshold() {
        return RECOMMENDED_LEVEL;
    }
    
	/** 
	 * Returns the current amount of energy in kWh. 
	 */
	public double getcurrentEnergy() {
		return currentEnergy;
	}
	
	public double getLowPowerPercent() {
		return lowPowerPercent;
	}
	
	public boolean isCharging() {
		return isCharging;
	}
	
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
    	return (currentEnergy * 100D) / maxCapacity;
    }
    
    /**
     * Gets the maximum battery capacity in kWh.
     */
    public double getBatteryCapacity() {
        return maxCapacity;
    }

    /**
     * Is the robot on low power mode ?
     * 
     * @return
     */
    public boolean isLowPower() {
    	return isLowPower;
    }
    
    /**
     * Delivers the energy to robot's battery.
     * 
     * @param kWh
     * @return the energy accepted
     */
    public double deliverEnergy(double kWh) {
    	double newEnergy = currentEnergy + kWh;
        double targetEnergy = 0.95 * maxCapacity;
    	if (newEnergy > targetEnergy) {
    		newEnergy = targetEnergy;
    		// Target reached and quit charging
    		isCharging = false;
    	}
    	double diff = newEnergy - currentEnergy;
    	currentEnergy = newEnergy;
		robot.fireUnitUpdate(UnitEventType.BATTERY_EVENT);

        updateLowPowerMode();

    	return diff;
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
     * Prepares object for garbage collection.
     */
    public void destroy() {
        robot = null;
    }
}
