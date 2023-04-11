/*
 * Mars Simulation Project
 * SystemCondition.java
 * @date 2022-07-19
 * @author Manny Kung
 */

package org.mars_sim.msp.core.robot;

import java.io.Serializable;

import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * This class represents the System Condition of a robot.
 * It models a robot's health.
 */
public class SystemCondition implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(SystemCondition.class.getName());

    // Data members
    /** Is the robot operational ? */
    private boolean operable;
    /** Is the robot at low power mode ? */  
    private boolean isLowPower;
    /** Is the robot charging ? */  
    private boolean isCharging;
    
    /** The standby power consumption in kW. */
    private double standbyPower;
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
        standbyPower = spec.getStandbyPowerConsumption();
        maxCapacity = spec.getMaxCapacity();
        currentEnergy = RandomUtil.getRandomDouble(1, maxCapacity);
        updateLowPowerMode();
    }

    private void updateLowPowerMode() {
        isLowPower = getBatteryState() < lowPowerPercent;
    }

    /**
     * This timePassing method 2 reflect a passing of time.
     * 
     * @param time amount of time passing (in millisols)
     * @param support life support system.
     * @param config robot configuration.
     */
    public boolean timePassing(double time) {

        // 3. Consume a minute amount of energy even if a robot does not perform any tasks
        if (!isCharging)
        	consumeEnergy(time * MarsClock.HOURS_PER_MILLISOL * standbyPower);

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
     * Get the performance factor that effect Person with the complaint.
     * @return The value is between 0 -> 1.
     */
    public double getPerformanceFactor() {
        return performance;
    }

    /**
     * Sets the performance factor.
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
     * Gets the robot system stress level
     * @return stress (0.0 to 100.0)
     */
    public double getStress() {
        return systemLoad;
    }

    /**
     * This Person is now dead.
     * @param illness The compliant that makes person dead.
     */
    public void setInoperable(HealthProblem illness) {
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
     * Get teh maximum battery capacity in kWh.
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
        return standbyPower;
    }

    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        robot = null;
    }

    /**
     * Get the minimum battery power when charging.
     * @return Percentage (0..100)
     */
    public double getMinimumChargeBattery() {
        return 70D;
    }
}
