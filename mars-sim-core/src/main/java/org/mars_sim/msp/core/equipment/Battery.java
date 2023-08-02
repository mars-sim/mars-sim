/*
 * Mars Simulation Project
 * Battery.java
 * @date 2023-04-19
 * @author Manny Kung
 */

package org.mars_sim.msp.core.equipment;

import java.io.Serializable;
import java.util.logging.Level;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.logging.SimLogger;

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
    
    /** The standard voltage of this battery pack in kW. */
    private static final double STANDARD_VOLTAGE = 600;
    
    /** The maximum energy capacity of a standard battery module in kWh. */
//    public static final double ENERGY_PER_MODULE = 15.0;
    
    private static final String KWH = " kWH  ";
    private static final String KW = " kW  ";
    
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
	/** The current energy of the unit in kWh. */
	private double currentEnergy;
    /** The standby power consumption in kW. */
    private double standbyPower = 0.01;
	/** The maximum capacity of the battery in kWh. */	
	private double maxCapacity;
	/** The current ampere hour of the battery in Ah. */	
	private double ampHour;
	/** The max ampere hour of the battery in Ah. */	
	private final double maxAmpHour;

	
	private Unit unit;
	
    /**
     * Constructor.
     * 
     * @param unit The unit requiring a battery.
     * 
     */
    public Battery(Unit unit, int numModule, double energyPerModule) {
    	this.unit = unit;
        performance = 1.0D;
        operable = true;
        this.numModule = numModule;
        this.energyPerModule = energyPerModule;
        maxCapacity = energyPerModule * numModule;
        ampHour = maxCapacity * 1000 / STANDARD_VOLTAGE;
        // Save max Ah at the start
        maxAmpHour = ampHour;
        
        currentEnergy = maxCapacity; //RandomUtil.getRandomDouble(maxCapacity/2, maxCapacity);	
 
        updateLowPowerMode();
    }

    private void updateLowPowerMode() {
        isLowPower = getBatteryState() < lowPowerPercent;
    }

    private void updateCurrentAmpHour() {
    	ampHour = maxAmpHour * currentEnergy / maxCapacity; 
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
        if (!isCharging)
        	; 

        return operable;
    }

    /**
     * Gets the maximum power [in kW] available when discharging or drawing power.
     * 
     * @param time in hours
     * @return
     */
    private double getMaxPowerDraw(double time) {
    	return Math.min(currentEnergy / time, ampHour * STANDARD_VOLTAGE / time);
    }
    
    /**
     * Gets the maximum power [in kW] to be pumped in during charging.
     * 
     * @param time in hours
     * @return
     */
    public double getMaxPowerCharging(double time) {
    	return Math.min((maxCapacity - currentEnergy) / time, maxAmpHour * STANDARD_VOLTAGE / time);
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
	    	double energyCanSupply = maxCapacity - maxCapacity * lowPowerPercent / 100;
	    	if (energyCanSupply <= 0)
	    		return 0;
	    	
//    		double powerAvailable = 0;
//    		if (powerRequest <= powerMax)
//    			powerAvailable = powerRequest;
//    		else
//    			powerAvailable = powerMax;
	    	
    		energyToDeliver = Math.min(currentEnergy, Math.min(energyCanSupply, Math.min(powerRequest * time, Math.min(kWh, powerMax * time))));

          	logger.log(unit, Level.INFO, 20_000, 
          			"[Battery Status]  "
          	       	+ "currentEnergy: " + Math.round(currentEnergy * 1_000.0)/1_000.0 + KWH
          			+ "energyCanSupply: " + Math.round(energyCanSupply * 1_000.0)/1_000.0 + KWH
                	+ "kWh: " + + Math.round(kWh * 1_000.0)/1_000.0 + KWH
                  	+ "energyToDeliver: " + + Math.round(energyToDeliver * 1_000.0)/1_000.0 + KWH
                	+ "time: " + + Math.round(time * 1_000.0)/1_000.0 + " hrs  "
          			+ "powerRequest: " + + Math.round(powerRequest * 1_000.0)/1_000.0 + KW
          			+ "powerMax: " + + Math.round(powerMax * 1_000.0)/1_000.0 + KW
           	);
           	
	    	currentEnergy -= energyToDeliver; 
	    	unit.fireUnitUpdate(UnitEventType.BATTERY_EVENT);

            updateLowPowerMode();
                
            updateCurrentAmpHour();
            
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
    	
    	double percent = currentEnergy / maxCapacity;
    	double energyAccepted = 0;
    	double percentAccepted = 0;
    	if (percent >= 100)
    		return 0;
    	
    	else {
    		percentAccepted = 100 - percent;
    		energyAccepted = percentAccepted / 100.0 * maxCapacity;

    		if (kWh < energyAccepted) {
    			energyAccepted = kWh;
    		}
    		
        	currentEnergy += energyAccepted;
    		unit.fireUnitUpdate(UnitEventType.BATTERY_EVENT);

            updateLowPowerMode();
            
            updateCurrentAmpHour();
            
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
    	return (getBatteryState() > percent);
    }

	/** 
	 * Returns the current amount of energy in kWh. 
	 */
	public double getCurrentEnergy() {
		return currentEnergy;
	}
	
	/** 
	 * Charges up the battery in no time. 
	 */
	public void topUpBatteryEnergy() {
		currentEnergy = maxCapacity;
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
    public double getBatteryState() {
    	return currentEnergy * 100 / maxCapacity;
    }
    
    /**
     * Gets the maximum battery capacity in kWh.
     */
    public double getBatteryCapacity() {
        return maxCapacity;
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
