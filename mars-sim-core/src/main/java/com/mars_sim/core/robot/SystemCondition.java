/*
 * Mars Simulation Project
 * SystemCondition.java
 * @date 2025-09-24
 * @author Manny Kung
 */

package com.mars_sim.core.robot;

import java.io.Serializable;

import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.equipment.Battery;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.MathUtils;

/**
 * This class represents the System Condition of a robot.
 * It models a robot's health.
 */
public class SystemCondition implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/** default logger. */
    // May add back: private static SimLogger logger = SimLogger.getLogger(SystemCondition.class.getName())

	private static final int RECOMMENDED_LEVEL = 70;
	
	private static final double POWER_SAVE_CONSUMPTION = .1;

	private static final double ENERGY_PER_MODULE = 15;
	
    // Data members

    /** Is the robot on power save mode ? */  
    private boolean onPowerSave;

    /** The power consumed in the standby mode in kW. */
    private double standbyPower;
    /** Robot's stress level (0.0 - 100.0). */
    private double systemLoad;
    /** Performance factor. */
    private double performance;

	/** The robot that owns this class. */ 
    private Robot robot;
    
	/** The battery of the vehicle. */ 
	private Battery battery;

    /**
     * Constructor.
     * 
     * @param robot The robot requiring a physical presence.
     */
    public SystemCondition(Robot newRobot, RobotSpec spec) {
        robot = newRobot;

        performance = 1.0;
        
        double energyStorageCapacity = spec.getMaxCapacity();
        
        int numModules = (int)(Math.ceil(energyStorageCapacity/ENERGY_PER_MODULE));
        
        battery = new Battery(newRobot, numModules, ENERGY_PER_MODULE);
       
        battery.initPower(spec.getLowPowerModePercent(), spec.getStandbyPowerConsumption());
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
		
    	battery.timePassing(pulse);
    	
		// Degrade performance;				
    	double aveFatigue = robot.getMalfunctionManager().findAverageWorstFatigue();

		setPerformanceFactor(performance - MathUtils.between(time * aveFatigue / 5000, 0, time / 500));	
		
    	if (!pulse.isNewHalfSol()) {
    		
    		int msol = pulse.getMarsTime().getMillisolInt();

    		// Note: Avoid checking at < 10 or 1000 millisols
    		//       due to high cpu util during the change of a sol
    		if (pulse.isNewIntMillisol() && msol >= 10 && msol < 995) {
 
    	        // Consume a minute amount of energy even if a robot does not perform any tasks
    	    	if (onPowerSave && battery.getkWhStored() > 0) {
    	    		battery.consumeEnergy(POWER_SAVE_CONSUMPTION * standbyPower, 
    	    				time * MarsTime.HOURS_PER_MILLISOL);
    	    	}
    	    	else if (!battery.isCharging() && !robot.getTaskManager().hasTask() && battery.getkWhStored() > 0) {
    	    		battery.consumeEnergy(standbyPower, 
    	    				time * MarsTime.HOURS_PER_MILLISOL);	
    			}
    		}
		}
    	
        return true;
    }

    /**
     * Gets the battery instance.
     * 
     * @return
     */
    public Battery getBattery() {
    	return battery;
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
	 * Sets the charging status.
	 * 
	 * @param value
	 */
	public void setCharging(boolean value) {
		battery.setCharging(value);
		
		if (value) {
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
        if (newPerformance <= 1.0 && newPerformance >= 0.0 && performance != newPerformance) {
            performance = newPerformance;
			robot.fireUnitUpdate(EntityEventType.PERFORMANCE_EVENT);
        }
    }

    /**
     * Tunes up the performance.
     * 
     * @param reduction
     */
    public void tuneUpPerformance(double points) {
    	double newPerformance = MathUtils.between(performance + points / 50, performance, .99);
    	setPerformanceFactor(newPerformance);
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
    	
    	if (value && battery.isCharging()) {
    		// Turns off charging
    		battery.setCharging(false);
    	}
    }
    
	/**
	 * Is the battery on low power mode ?
	 * 
	 * @return
	 */
    public boolean isLowPower() {
    	return battery.isLowPower();
	}

    /**
     * Prepares object for garbage collection.
     */
    public void destroy() {
        robot = null;
    }
}
