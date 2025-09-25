/*
 * Mars Simulation Project
 * Battery.java
 * @date 2025-09-24
 * @author Manny Kung
 */

package com.mars_sim.core.equipment;

import java.io.Serializable;
import java.util.logging.Level;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.tool.RandomUtil;

/**
 * This class represents the modeling of an electrical battery.
 */
public class Battery implements Serializable {

    /** Default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Battery.class.getName());
	
    /** The maximum current that can be safely drawn from this battery pack in Ampere. */
//    private static final double MAX_AMP_DRAW = 120;
    
	/** The maximum continuous charge rate (within the safety limit) that this battery can handle. */
	private static final int MAX_C_RATING_CHARGING = 4;
	/** 
	 * The number of cells per module of the battery. 
	 * Note: 3.6 V * 104 = 374.4 V 
	 * 4.2 V * 104 = 436.8 V
	 * e.g. : Tesla Model S has 104 cells per module
	 */
	private static final int CELLS_PER_MODULE = 104;
	/** The internal resistance [in ohms] in each cell. */	
	private static final double R_CELL = 0.06; 
	
    /** The standard voltage of this battery pack in volts. */
    public static final double HIGHEST_MAX_VOLTAGE = 600;
    
    /** The standard voltage of a drone battery pack in volts. */
    public static final double DRONE_VOLTAGE = 48;
    
    /** The maximum energy capacity of a standard battery module in kWh. */
    // ENERGY_PER_MODULE = 15.0;
    
    private static final String KWH = " kWh  ";
    private static final String KW = " kW  ";
    
    // Data members
    /** Is the unit operational ? */
    private boolean operable;
    /** Is the unit at low power mode ? */  
    private boolean isLowPower;
    /** Is the unit charging ? */  
    private boolean isCharging;
    
    /** The number of battery module. */
    public int numModules;
     
    /** The maximum energy capacity of a standard battery module in kWh. */
    public double energyPerModule;
    /** The standby power consumption in kW. */
    private double standbyPower = 0.01;
    /** unit's stress level (0.0 - 100.0). */
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
	
	private Unit unit;
	
    /**
     * Constructor.
     * 
     * @param unit The unit requiring a battery.
	 * @param numModule
	 * @param energyPerModule
	 */
    public Battery(Unit unit, int numModules, double energyPerModule) {
    	this.unit = unit;
        performance = 1.0D;
        operable = true;
        
        this.numModules = numModules;
		rTotal = R_CELL * numModules * CELLS_PER_MODULE;
		
        this.energyPerModule = energyPerModule;
        energyStorageCapacity = energyPerModule * numModules;

		// At the start of sim, set to a random value
        kWhStored = energyStorageCapacity * (.5 + RandomUtil.getRandomDouble(.5));	
 
		updateFullAmpHourCapacity();
		
        updateLowPowerMode();
    }

    private void updateLowPowerMode() {
        isLowPower = getBatteryLevel() < lowPowerPercent;
    }

    /**
     * Updates the Amp Hour stored capacity [in Ah].
     */
    private void updateAmpHourStored() {
    	ampHourStored = 1000 * kWhStored / HIGHEST_MAX_VOLTAGE; 
    }
    
    /**
     * Updates the full Amp Hour capacity [in Ah].
     * NOTE: DO NOT DELTE. RETAIN THIS METHOD FOR FUTURE USE.
     */
    private void updateFullAmpHourCapacity() {
    	ampHourFullCapacity = 1000 * energyStorageCapacity / HIGHEST_MAX_VOLTAGE; 
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
     * Delivers the energy to unit's battery.
     * Note: Not in use
     * 
     * @param kWh
     * @return the diff in kWh energy
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
     * Gets a given amount of energy within an interval of time from the battery.
     * 
     * @param amount amount of energy to consume [in kWh]
     * @param time in hrs
     * @return energy to be delivered [in kWh]
     */
    public double requestEnergy(double kWh, double time) {
    	
		double powerRequest = kWh / time;
		
		double powerMax = getMaxPowerDraw(time);
				
		double energyToDeliver = 0;
		
		boolean lowBatteryAlarm = false;
				
    	double energyCanSupply = energyStorageCapacity;
    	
    	if (energyCanSupply <= 0.001)
    		logger.log(unit, Level.INFO, 20_000, 
          			"No more battery. energyStorageCapacity: " + Math.round(energyStorageCapacity * 1_000.0)/1_000.0 + KWH);
    		
		if (kWhStored < energyStorageCapacity * lowPowerPercent / 100)
			lowBatteryAlarm = true;

		energyToDeliver = Math.min(kWhStored, Math.min(energyCanSupply, 
						Math.min(powerRequest * time, Math.min(kWh, powerMax * time))));

    	if (lowBatteryAlarm) {
			logger.log(unit, Level.WARNING, 20_000, 
      			"[Low Battery Alarm] "
                + "kWh: " + + Math.round(kWh * 1_000.0)/1_000.0 + KWH
      	       	+ "kWhStored: " + Math.round(kWhStored * 1_000.0)/1_000.0 + KWH
      			+ "energyCanSupply: " + Math.round(energyCanSupply * 1_000.0)/1_000.0 + KWH
              	+ "energyToDeliver: " + + Math.round(energyToDeliver * 1_000.0)/1_000.0 + KWH
            	+ "time: " + + Math.round(time * 1_000.0)/1_000.0 + " hrs  "
      			+ "powerRequest: " + + Math.round(powerRequest * 1_000.0)/1_000.0 + KW
      			+ "powerMax: " + + Math.round(powerMax * 1_000.0)/1_000.0 + KW);
    	}
       	
       	
    	kWhStored -= energyToDeliver; 
    	unit.fireUnitUpdate(UnitEventType.BATTERY_EVENT);

        updateLowPowerMode();
            
        updateAmpHourStored();
        
        return energyToDeliver;
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
     * Provides a given amount of energy to the battery within an interval of time.
     * 
     * @param kWhPumpedIn amount of energy to consume [in kWh]
     * @param hours time in hrs
     * @return energy accepted during charging [in kWh]
     */
    public double chargeBattery(double kWhPumpedIn, double hours) {
		
    	double maxChargeEnergy = estimateChargeBattery(hours);
		// Find the smallest amount of energy to be accepted
    	double kWhAccepted = Math.min(kWhPumpedIn, maxChargeEnergy);
		
    	kWhStored += kWhAccepted;

        updateAmpHourStored();

        updateLowPowerMode();
        
		unit.fireUnitUpdate(UnitEventType.BATTERY_EVENT);

    	return kWhAccepted;
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

    /**
     * Sets the performance factor.
     * 
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
     * Prepares object for garbage collection.
     */
    public void destroy() {
        unit = null;
    }

}
