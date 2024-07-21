/*
 * Mars Simulation Project
 * Battery.java
 * @date 2024-07-20
 * @author Manny Kung
 */

package com.mars_sim.core.equipment;

import java.io.Serializable;
import java.util.logging.Level;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.tools.util.RandomUtil;

/**
 * This class represents the modeling of an electrical battery.
 */
public class Battery implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(Battery.class.getName());
	
    /** The maximum current that can be safely drawn from this battery pack in Ampere. */
//    private static final double MAX_AMP_DRAW = 120;
    
	/**The maximum continuous discharge rate (within the safety limit) of this battery. */
	private static final int MAX_C_RATING = 4;
	
    /** The standard voltage of this battery pack in volts. */
    public static final double STANDARD_VOLTAGE = 600;
    
    /** The standard voltage of a drone battery pack in volts. */
    public static final double DRONE_VOLTAGE = 14.8;
    
    /** The maximum energy capacity of a standard battery module in kWh. */
//    public static final double ENERGY_PER_MODULE = 15.0;
    
    private static final String KWH__ = " kWh  ";
    private static final String KW__ = " kW  ";
    
    // Data members
    /** Is the unit operational ? */
    private boolean operable;
    /** Is the unit at low power mode ? */  
    private boolean isLowPower;
    /** Is the unit charging ? */  
    private boolean isCharging;
    
    /** The number of battery module. */
    public int numModule;
    
    /** The maximum energy capacity of a standard battery module in kWh. */
    public double energyPerModule;
    /** unit's stress level (0.0 - 100.0). */
    private double systemLoad;
    /** Performance factor. */
    private double performance;
	/** The percentage that triggers low power warning. */
    private double lowPowerPercent = 10;
	/** The current energy stored in kWh. */
	private double kWhStored;
    /** The standby power consumption in kW. */
    private double standbyPower = 0.01;
	/** The energy storage capacity [in kWh, not Wh] capacity. */
	private double energyStorageCapacity;
	/** 
	 * The rating [in ampere-hour or Ah] of the battery in terms of its charging/discharging ability at 
	 * a particular C-rating. An amp is a measure of electrical current. The hour 
	 * indicates the length of time that the battery can supply this current.
	 * e.g. a 2.2Ah battery can supply 2.2 amps for an hour
	 */
	private double ampHour;

	private Unit unit;
	
    /**
     * Constructor.
     * 
     * @param unit The unit requiring a battery.
	 * @param numModule
	 * @param energyPerModule
	 */
    public Battery(Unit unit, int numModule, double energyPerModule) {
    	this.unit = unit;
        performance = 1.0D;
        operable = true;
        this.numModule = numModule;
        this.energyPerModule = energyPerModule;
        energyStorageCapacity = energyPerModule * numModule;
        ampHour = energyStorageCapacity * 1000 / STANDARD_VOLTAGE;
		// At the start of sim, set to a random value
        kWhStored = energyStorageCapacity * (.5 + RandomUtil.getRandomDouble(.5));	
 
        updateLowPowerMode();
    }

    private void updateLowPowerMode() {
        isLowPower = getBatteryLevel() < lowPowerPercent;
    }

    /**
     * Updates the Amp Hour rating.
     */
    private void updateAmpHourRating() {
    	ampHour = 1000 * energyStorageCapacity / STANDARD_VOLTAGE; 
    	// maxAmpHour * kWhStored / energyStorageCapacity; 
    }
    
    /**
     * This timePassing method reflects a passing of time.
     * 
     * @param time amount of time passing (in millisols)
     * @param support life support system.
     * @param config unit configuration.
     */
    public boolean timePassing(double time) {

        // Consume a minute amount of energy even if a unit does not perform any tasks
//        if (!isCharging)
//        	; 

        return operable;
    }

    /**
     * Gets the maximum power [in kW] available when discharging or drawing power.
     * 
     * @param time in hours
     * @return
     */
    private double getMaxPowerDraw(double time) {
    	// Note: Need to find the physical formula for max power draw
    	return ampHour * STANDARD_VOLTAGE / time / 1000;
    }
    
    /**
     * Gets the maximum power [in kW] to be pumped in during charging.
     * 
     * @param time in hours
     * @return
     */
    public double getMaxPowerCharging(double hours) {
    	// Note: Need to find the physical formula for max power charge
    	return MAX_C_RATING * ampHour * STANDARD_VOLTAGE / hours / 1000;
//    	return Math.min((energyStorageCapacity - kWhStored) / time, ampHour * maxAmpHour * STANDARD_VOLTAGE / time);
    }
    
    /**
     * Gets a given amount of energy within an interval of time from the battery.
     * 
     * @param amount amount of energy to consume [in kWh]
     * @param time in hrs
     * @return energy to be delivered
     */
    public double requestEnergy(double kWh, double time) {
    	if (!isLowPower) {
    		double powerRequest = kWh / time;
    		
    		double powerMax = getMaxPowerDraw(time);
    				
    		double energyToDeliver = 0;
	    	double energyCanSupply = energyStorageCapacity - energyStorageCapacity * lowPowerPercent / 100;
	    	if (energyCanSupply <= 0)
	    		return 0;
	    	
//    		double powerAvailable = 0;
//    		if (powerRequest <= powerMax)
//    			powerAvailable = powerRequest;
//    		else
//    			powerAvailable = powerMax;
	    	
    		energyToDeliver = Math.min(kWhStored, Math.min(energyCanSupply, Math.min(powerRequest * time, Math.min(kWh, powerMax * time))));

          	logger.log(unit, Level.INFO, 20_000, 
          			"[Battery Status] "
          	       	+ "currentEnergy: " + Math.round(kWhStored * 1_000.0)/1_000.0 + KWH__
          			+ "energyCanSupply: " + Math.round(energyCanSupply * 1_000.0)/1_000.0 + KWH__
                	+ "kWh: " + + Math.round(kWh * 1_000.0)/1_000.0 + KWH__
                  	+ "energyToDeliver: " + + Math.round(energyToDeliver * 1_000.0)/1_000.0 + KWH__
                	+ "time: " + + Math.round(time * 1_000.0)/1_000.0 + " hrs  "
          			+ "powerRequest: " + + Math.round(powerRequest * 1_000.0)/1_000.0 + KW__
          			+ "powerMax: " + + Math.round(powerMax * 1_000.0)/1_000.0 + KW__
           	);
           	
	    	kWhStored -= energyToDeliver; 
	    	unit.fireUnitUpdate(UnitEventType.BATTERY_EVENT);

            updateLowPowerMode();
                
            updateAmpHourRating();
            
            return energyToDeliver;
    	}
    	
		return 0;
    }

    /**
     * Provides a given amount of energy to the battery within an interval of time.
     * 
     * @param amount amount of energy to consume [in kWh]
     * @param time in hrs
     * @return energy to be delivered
     */
    public double provideEnergy(double kWh, double time) {
    	// FUTURE: Consider the effect of the charging rate and the time parameter
    	
    	double percent = kWhStored / energyStorageCapacity * 100;
    	double energyAccepted = 0;
    	double percentAccepted = 0;
    	if (percent >= 100)
    		return 0;
    	
    	else {
    		percentAccepted = 100 - percent;
    		energyAccepted = percentAccepted / 100.0 * energyStorageCapacity;

    		if (kWh < energyAccepted) {
    			energyAccepted = kWh;
    		}
    		
        	kWhStored += energyAccepted;
    		unit.fireUnitUpdate(UnitEventType.BATTERY_EVENT);

            updateLowPowerMode();
            
            updateAmpHourRating();
            
        	return energyAccepted;
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
     * Gets the performance factor.
     * 
     * @return The value is between 0 -> 1.
     */
    public double getPerformanceFactor() {
        return performance;
    }

//    /**
//     * Sets the performance factor.
//     * 
//     * @param newPerformance new performance (between 0 and 1).
//     */
//    private void setPerformanceFactor(double newPerformance) {
//        if (newPerformance != performance) {
//            performance = newPerformance;
//			if (unit != null)
//				unit.fireUnitUpdate(UnitEventType.PERFORMANCE_EVENT);
//        }
//    }

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
     * Returns a percentage 0..100 of the battery energy level.
     * 
     * @return
     */
    public double getBatteryLevel() {
    	return kWhStored / energyStorageCapacity * 100;
    }
    
    /**
     * Gets the battery energy storage capacity in kWh.
     */
    public double getBatteryCapacity() {
        return energyStorageCapacity;
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
     * Delivers the energy to unit's battery.
     * 
     * @param kWh
     * @return the energy accepted
     */
    public double deliverEnergy(double kWh) {
    	double newEnergy = kWhStored + kWh;
        double targetEnergy = 0.95 * energyStorageCapacity;
    	if (newEnergy > targetEnergy) {
    		newEnergy = targetEnergy;
    		isCharging = false;
    	}
    	double diff = newEnergy - kWhStored;
    	kWhStored = newEnergy;
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
     * Gets the minimum battery power when charging.
     * 
     * @return Percentage (0..100)
     */
    public double getMinimumChargeBattery() {
        return 70D;
    }
    
    /**
     * Prepares object for garbage collection.
     */
    public void destroy() {
        unit = null;
    }

}
