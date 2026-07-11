/*
 * Mars Simulation Project
 * SystemCondition.java
 * @date 2025-09-24
 * @author Manny Kung
 */
package com.mars_sim.core.robot;

import java.io.Serializable;

import com.mars_sim.core.equipment.Battery;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;

/**
 * This class represents the System Condition of a robot.
 * It models a robot's health.
 */
public class SystemCondition implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/** default logger. */
    // May add back: private static SimLogger logger = SimLogger.getLogger(SystemCondition.class.getName())

    private static final int CABLE_GAUGE_SIZE = 80;
    		
	private static final int RECOMMENDED_LEVEL = 70;
	
	private static final double POWER_SAVE_CONSUMPTION = .1;

	private static final double ENERGY_PER_MODULE = 15;

    public static final String PERFORMANCE_EVENT = "robot performance";
	
    // Data members

    /** Is the robot on power save mode ? */  
    private boolean onPowerSave;

    /** The power consumed in the standby mode in kW. */
    private double standbyPower;
    /** Performance factor. */
    private double performance;
    private RobotPerfLevel performanceLevel;


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
        performanceLevel = RobotPerfLevel.fromValue(performance);
        
        double energyStorageCapacity = spec.getMaxCapacity();
        
        int numModules = (int)(Math.ceil(energyStorageCapacity/ENERGY_PER_MODULE));
        
        battery = new Battery(newRobot, CABLE_GAUGE_SIZE, numModules, ENERGY_PER_MODULE);
       
        standbyPower = spec.getStandbyPowerConsumption();
        		
        battery.initPower(spec.getLowPowerModePercent(), standbyPower);
    }
	
    /**
     * This method reflects a passing of time.
     * 
     * @param pulse amount of time in a clock pulse
     */
    public boolean timePassing(ClockPulse pulse) {
    	double time = pulse.getElapsed();
    	if (time == 0.0)
    		return false;
		
    	battery.timePassing(pulse);
    	
		// Degrade performance		
    	double aveFatigue = robot.getMalfunctionManager().findAverageWorstFatigue();
		performance = performance - Math.clamp(time * aveFatigue / 5000, 0, time / 500);
        var newLevel = RobotPerfLevel.fromValue(performance);
        if (newLevel != getPerformanceLevel()) {
        	robot.fireUnitUpdate(PERFORMANCE_EVENT);
            performanceLevel = newLevel;
        }
		
        // Avoid running this at half sol to relieve thread work load
    	if (!pulse.isNewHalfSol()) {
    		
    		int msol = pulse.getMarsTime().getMillisolInt();

    		// Note: Avoid checking at < 10 or 1000 millisols
    		//       due to high cpu util during the change of a sol
    		if (pulse.isNewIntMillisol() && msol >= 10 && msol < 995 && battery.getkWhStored() > 0) {
 
    	        // Consume a minute amount of energy even if a robot does not perform any tasks
    	    	if (onPowerSave) {
    	    		battery.consumeEnergy(POWER_SAVE_CONSUMPTION * standbyPower, 
    	    				time * MarsTime.HOURS_PER_MILLISOL);
    	    	}
    	    	else if (!battery.isCharging() && !robot.getTaskManager().hasTask()) {
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
     * Gets the performance level.
     */
    public RobotPerfLevel getPerformanceLevel() {
        return performanceLevel;
    }

    /**
     * Tunes up the performance.
     * 
     * @param reduction
     */
    public void tuneUpPerformance(double points) {
    	performance = Math.clamp(performance + points / 50, performance, 1D);
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
