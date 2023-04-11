/*
 * Mars Simulation Project
 * Battery.java
 * @date 2023-04-11
 * @author Manny Kung
 */

package org.mars_sim.msp.core.equipment;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * This class represents the modeling of an electrical battery
 */
public class Battery implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Battery.class.getName());


    // Data members
    /** Is the unit operational ? */
    private boolean operable;
    /** Is the unit at low power mode ? */  
    private boolean isLowPower;
    /** Is the unit charging ? */  
    private boolean isCharging;
    
    /** unit's stress level (0.0 - 100.0). */
    private double systemLoad;
    /** Performance factor. */
    private double performance;
	/** The percentage that triggers low power warning. */
    private double lowPowerPercent = 10;
	/** The current energy of the unit in kWh. */
	private double currentEnergy;
    /** The standby power consumption in kW. */
    private double standbyPower = 0.01;
	/** The maximum capacity of the battery in kWh. */	
	private double maxCapacity = 100;

	private Unit unit;
	
    /**
     * Constructor.
     * 
     * @param unit The unit requiring a physical presence.
     * 
     */
    public Battery(Unit unit) {
    	this.unit = unit;
        performance = 1.0D;
        operable = true;

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
     * @param config unit configuration.
     */
    public boolean timePassing(double time) {

        // 3. Consume a minute amount of energy even if a unit does not perform any tasks
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
	    		unit.fireUnitUpdate(UnitEventType.BATTERY_EVENT);

                updateLowPowerMode();
	    	}
	    	else
	    		logger.warning(unit, 30_000L, "Out of power.");
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
			if (unit != null)
				unit.fireUnitUpdate(UnitEventType.PERFORMANCE_EVENT);
        }
    }

    /**
     * Gets the unit system stress level
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
     * Checks if the unit is inoperable.
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
     * Is the unit on low power mode ?
     * 
     * @return
     */
    public boolean isLowPower() {
    	return isLowPower;
    }
    
    /**
     * Delivers the energy to unit's battery.
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
		unit.fireUnitUpdate(UnitEventType.BATTERY_EVENT);

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
        unit = null;
    }

    /**
     * Get the minimum battery power when charging.
     * @return Percentage (0..100)
     */
    public double getMinimumChargeBattery() {
        return 70D;
    }
}
