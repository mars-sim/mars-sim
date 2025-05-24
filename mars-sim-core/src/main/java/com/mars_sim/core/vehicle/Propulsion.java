/*
 * Mars Simulation Project
 * Propulsion.java
 * @date 2024-08-06
 * @author Manny KUng
 */
package com.mars_sim.core.vehicle;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.ResourceUtil;

public abstract class Propulsion implements Serializable {
	 
	/** Default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** Default logger. */
	private static SimLogger logger = SimLogger.getLogger(Propulsion.class.getName());
	 
	/** Conversion factor : 1 Wh = 3.6 kilo Joules */
	private static final double JOULES_PER_WH = 3_600.0;
	/** Conversion factor : 1 m/s = 3.6 km/h (or kph) */
	protected static final double KPH_CONV = 3.6;
	 
	/** The ratio of the amount of oxidizer to methane fuel. */
	public static final double RATIO_OXIDIZER_METHANE = 1;
	/** The ratio of the amount of oxidizer to methanol fuel. */
	public static final double RATIO_OXIDIZER_METHANOL = 1.5;
	 
	// Water Ratio is 2 for direct methanol fuel cell (DMFC). 
	// Water Ratio varies for indirect methanol fuel cell (DMFC). say 1.125;
	/** The ratio of water produced for every methanol consumed. */
	private static final double RATIO_WATER_METHANOL = 2; 

	/** The ratio of water produced for every methane consumed. */
	private static final double RATIO_WATER_METHANE = 2.25;
	 
	public static final String TWO_WHITESPACES = "  ";
	
    public static final DecimalFormat DECIMAL2 = new DecimalFormat("#,##0.00");
	public static final DecimalFormat DECIMAL3_RAD = new DecimalFormat("#,##0.000 radians");
	public static final DecimalFormat DECIMAL3_KG_M3 = new DecimalFormat("#,##0.000 kg/m3");
    public static final DecimalFormat DECIMAL3_N = new DecimalFormat("#,##0.000 N");
    public static final DecimalFormat DECIMAL1_N = new DecimalFormat("#,##0.0 N"); 
    public static final DecimalFormat DECIMAL3_WH = new DecimalFormat("#,##0.00 Wh");
    public static final DecimalFormat DECIMAL3_KPH = new DecimalFormat("#,##0.00 kph");
    public static final DecimalFormat DECIMAL3_KM = new DecimalFormat("#,##0.000 km");
    public static final DecimalFormat DECIMAL3_M = new DecimalFormat("#,##0.000 m");
    public static final DecimalFormat DECIMAL2_M = new DecimalFormat("#,##0.00 m");
    public static final DecimalFormat DECIMAL3_S = new DecimalFormat("#,##0.000 secs");
    public static final DecimalFormat DECIMAL2_S = new DecimalFormat("#,##0.00 secs");
    public static final DecimalFormat DECIMAL3_M_S = new DecimalFormat("#,##0.000 m/s");
    public static final DecimalFormat DECIMAL3_W = new DecimalFormat("#,##0.000 W");
    public static final DecimalFormat DECIMAL2_W = new DecimalFormat("#,##0.00 W");
    public static final DecimalFormat DECIMAL1_W = new DecimalFormat("#,##0.0 W");
    public static final DecimalFormat DECIMAL3_J = new DecimalFormat("#,##0.000 J");
    public static final DecimalFormat DECIMAL1_J = new DecimalFormat("#,##0.0 J");
    
	private Vehicle vehicle;
	
	
	/**
	 * Constructor.
	 * 
	 * @param vehicle The vehicle implementing this propulsion
	 * 
	 */
	public Propulsion(Vehicle vehicle) {
		this.vehicle = vehicle;

	}
	
	/**
	 * Consumes the fuel and the fuel oxidizer.
	 * 
	 * @param fuelNeeded
	 * @param fuelTypeID
	 */
	public void retrieveFuelNOxidizer(double fuelNeeded, int fuelTypeID) {
		 if (fuelNeeded > 0) {
			 
			 if (fuelTypeID == ResourceUtil.METHANOL_ID) {
				 // Retrieve the fuel needed for the distance traveled
				 vehicle.retrieveAmountResource(fuelTypeID, fuelNeeded);
				 // Assume oxygen as fuel oxidizer
				 vehicle.retrieveAmountResource(ResourceUtil.OXYGEN_ID, RATIO_OXIDIZER_METHANOL * fuelNeeded);
				 // Generate  water from the fuel cells
				 vehicle.storeAmountResource(ResourceUtil.WATER_ID, RATIO_WATER_METHANOL * fuelNeeded);
			 }

			 else if (fuelTypeID == ResourceUtil.METHANE_ID) {
				 // Retrieve the fuel needed for the distance traveled
				 vehicle.retrieveAmountResource(fuelTypeID, fuelNeeded);
				 // Assume oxygen as fuel oxidizer
				 vehicle.retrieveAmountResource(ResourceUtil.OXYGEN_ID, RATIO_OXIDIZER_METHANE * fuelNeeded);
				 // Generate the water from the fuel cells
				 vehicle.storeAmountResource(ResourceUtil.WATER_ID, RATIO_WATER_METHANE * fuelNeeded);
			 }				 
		 }
	}
	
	/**
	 * Cuts down the speed and power due to energy expenditure NOT being met.
	 * 
	 * @param caseText
	 * @param eBattery
	 * @param secs
	 * @param energyByFuel
	 * @param uMS
	 * @param mass
	 * @return
	 */
	public double[] propelBatteryOnly(String caseText, double aveForce, double eBattery, 
			double secs, double uMS, double mass) {
		 // Scenario 2B1: Ran out of fuel. Need battery to provide for the rest
		 
		 // Recalculate the new ave power W
		 // W = Wh / s * 3600 J / Wh
		 double powerSuppliedByBattery = eBattery / secs * JOULES_PER_WH;
		 	 
		 double newAvePower = powerSuppliedByBattery;
	 
		 // Recalculate the new overall energy expenditure [in Wh]
		 double eUsed = eBattery;
		 
		 // Recalculate the kinetic energy
		 double kineticEnergy = eUsed;
		 
		 // Recalculate the new speed 
		 double vMSKE = Math.sqrt(kineticEnergy / .5 / mass);
		 	 
		 // Recalculate the new aveForce 
//		 Not working: double aveForce = avePower / (vMS - uMS);
		 
		 // Recalculate the new speed 
		 // FUTURE: will consider the on-board accessory vehicle power usage
		 // m/s = W / (kg * m/s2)
	 
		 // FUTURE : may need to find a way to optimize motor power usage 
		 // and slow down the vehicle to the minimal to conserve power	
		 
		 double newVMS = newAvePower / aveForce;
				 
		 double newVKPH = newVMS * KPH_CONV;
		 
		 double uKPH = uMS * KPH_CONV;
		 
		 // Find new acceleration
		 double accelSpeedUp = (newVMS - uMS) / secs; // [in m/s^2]
		 
		 // Set new vehicle acceleration
		 vehicle.setAccel(accelSpeedUp);
		 
		 // Recompute the new distance it could travel
		 double distanceTravelled = newVKPH * secs / 3600;
		 
		 /*
		  * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
		  * delete any of them. Needed for testing when new features are added in future. Thanks !
		  */
		 logger.log(vehicle, Level.INFO, 10_000, caseText
				 + "eBattery: " +  DECIMAL3_WH.format(eBattery) + TWO_WHITESPACES
				 + "eUsed: " + DECIMAL3_WH.format(eUsed) + TWO_WHITESPACES							 
				 + "newAvePower: " 			+ DECIMAL2_W.format(newAvePower) + TWO_WHITESPACES							
				 + "vMSKE: " 			+ DECIMAL3_KPH.format(vMSKE) + TWO_WHITESPACES 
				 + "u -> newVKPH: " 			+ DECIMAL3_KPH.format(uKPH) + " -> "
				 						+ DECIMAL3_KPH.format(newVKPH) + TWO_WHITESPACES   
				 + "secs: " 			+ DECIMAL2_S.format(secs) + TWO_WHITESPACES   						
				 + "d: " +  DECIMAL3_KM.format(distanceTravelled));			 
		 
		 return new double[] {newAvePower, newVKPH, distanceTravelled};
	}
	
	
	/**
	 * Cuts down the speed and power due to energy expenditure NOT being met.
	 * 
	 * @param caseText
	 * @param eBattery
	 * @param secs
	 * @param eFuel
	 * @param uMS
	 * @param mass
	 * @return
	 */
	public double[] cutDownSpeedNPower(String caseText, double aveForce, double eBattery, 
			double secs, double eFuel, double uMS, double mass) {
		// Energy expenditure is NOT met. Need to cut down the speed and power. 
	
		// Scenario 2B3: fuel can fulfill some energy expenditure but not all. Battery cannot provide the rest
		 
		// Previously, it was overallEnergyUsed = avePower * secs / JOULES_PER_WH ; // [in Wh]				
		 
		 // Recalculate the new power
		 // 1 Wh = 3.6 kJ 
		 // W = J / s  / [3.6 kJ / Wh]
		 // W = Wh / 3.6k

		// Recalculate the new ave power W
		// W = Wh / s * 3600 J / Wh
		double newAvePower = eBattery / secs * JOULES_PER_WH;
		 
		// Recalculate the new overall energy expenditure [in Wh]
		double eUsed = eBattery + eFuel;
		 
		// Recalculate the kinetic energy
		double kineticEnergy = eUsed;
		 
		 // Recalculate the new speed 
		double vMSKE = Math.sqrt(kineticEnergy / .5 / mass);
		 
		// Recalculate the new aveForce 
//		Not working double aveForce = avePower / (vMS - uMS);
		 
		 // Recalculate the new force 
		 // FUTURE: will consider the on-board accessory vehicle power usage
		 // m/s = W / (kg * m/s2)

		 // FUTURE : may need to find a way to optimize motor power usage 
		 // and slow down the vehicle to the minimal to conserve power	
		 
		 double newVMS = newAvePower / aveForce;

		 double newVKPH = newVMS * KPH_CONV;
		 
		 double uKPH = uMS * KPH_CONV;
		 
		 // Find new acceleration
		 double accelSpeedUp = (newVMS - uMS) / secs; // [in m/s^2]
		 
		 // Set new vehicle acceleration
		 vehicle.setAccel(accelSpeedUp);
		 
		 // Recompute the new distance it could travel
		 double distanceTravelled = newVKPH * secs / 3600;
			 
		/*
		 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
		 * delete any of them. Needed for testing when new features are added in future. Thanks !
		 */
		logger.log(vehicle, Level.INFO, 10_000, caseText 
				 + "eFuel: " + DECIMAL3_WH.format(eFuel) + TWO_WHITESPACES
				 + "eBattery: " +  DECIMAL3_WH.format(eBattery) + TWO_WHITESPACES
				 + "eUsed: " + DECIMAL3_WH.format(eUsed) + TWO_WHITESPACES			
				 + "KE: " 			+ DECIMAL3_J.format(kineticEnergy) + TWO_WHITESPACES
				 + "newAvePower: " 			+ DECIMAL2_W.format(newAvePower) + TWO_WHITESPACES							
				 + "vMSKE: " 			+ DECIMAL3_KPH.format(vMSKE) + TWO_WHITESPACES 
				 + "u -> newVKPH: " 			+ DECIMAL3_KPH.format(uKPH) + " -> "
				 						+ DECIMAL3_KPH.format(newVKPH) + TWO_WHITESPACES 				 
				 + "d: " +  DECIMAL3_KM.format(distanceTravelled));	
		 
		 // Equate energyByBattery to energySuppliedByBattery in order to add to odometer easily
//		 energyByBattery = energySuppliedByBattery;
		 
		 return new double[] {newAvePower, newVKPH, distanceTravelled};
	}
	
	/**
	 * Propels a vehicle forward with both fuel and battery power.
	 * 
	 * @param caseText
	 * @param aveForce
	 * @param avePower
	 * @param eBattery
	 * @param secs
	 * @param eFuel
	 * @param uMS
	 * @param vMS
	 * @param mass
	 * @return
	 */
	public double[] propelWithBothFuelNBattery(String caseText, double aveForce, 
			double avePower, double eBattery, 
			double secs, double eFuel, double uMS, double vMS, double mass) {
		// Scenario 1B2: fuel can fulfill some energy expenditure but not all. Battery provide the rest

		// Recalculate the new ave power W
		// W = Wh / s * 3600 J / Wh
		double newAvePower = eBattery / secs * JOULES_PER_WH;
		 
		// Recalculate the new overall energy expenditure [in Wh]
		double eUsed = eBattery + eFuel;
		 
		// Recalculate the kinetic energy
		double kineticEnergy = eUsed;
		 
		 // Recalculate the new speed 
		double vMSKE = Math.sqrt(kineticEnergy / .5 / mass);
		 
		 // Assume newAvePower < avePower
		 // Reduction in power causes reduction in speed
		 double newVMS = vMS - (avePower - newAvePower) / aveForce;
		 
		 double newVKPH = newVMS * KPH_CONV;
		 
		 double uKPH = uMS * KPH_CONV;
		 
		 // Find new acceleration
		 double accelSpeedUp = (newVMS - uMS) / secs; // [in m/s^2]
		 
		 // Set new vehicle acceleration
		 vehicle.setAccel(accelSpeedUp);
		 
		 // Recompute the new distance it could travel
		 double distanceTravelled = newVKPH * secs / 3600;
			 
		/*
		 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
		 * delete any of them. Needed for testing when new features are added in future. Thanks !
		 */
		logger.log(vehicle, Level.INFO, 10_000, caseText 
				 + "eFuel: " + DECIMAL3_WH.format(eFuel) + TWO_WHITESPACES
				 + "eBattery: " +  DECIMAL3_WH.format(eBattery) + TWO_WHITESPACES
				 + "eUsed: " + DECIMAL3_WH.format(eUsed) + TWO_WHITESPACES	
				 + "KE: " 			+ DECIMAL3_J.format(kineticEnergy) + TWO_WHITESPACES	
				 + "newAvePower: " 			+ DECIMAL2_W.format(newAvePower) + TWO_WHITESPACES							
				 + "vMSKE: " 			+ DECIMAL3_KPH.format(vMSKE) + TWO_WHITESPACES 
				 + "u -> newVKPH: " 			+ DECIMAL3_KPH.format(uKPH) + " -> "
				 						+ DECIMAL3_KPH.format(newVKPH) + TWO_WHITESPACES 				 
				 + "d: " +  DECIMAL3_KM.format(distanceTravelled));	
		 
		 // Equate energyByBattery to energySuppliedByBattery in order to add to odometer easily
//		 energyByBattery = energySuppliedByBattery;
		 
		 return new double[] {newAvePower, newVKPH, distanceTravelled};
	}
	
	 /**
	  * Drives the rover and calculates overall power and forces acting on vehicle.
	  * 
	  * @param weight
	  * @param vMS
	  * @param averageSpeed
	  * @param fGravity
	  * @param airDensity
	  * @return
	  */
	 public abstract double[] driveOnGround(double weight, double vMS, double averageSpeed, double fGravity, double airDensity);
	 
	 /**
	  * Flies in the air and calculate overall power and forces acting on the flyer.
	  * 
	  * @param caseText
	  * @param angle
	  * @param ascentHeight
	  * @param weight
	  * @param airDensity
	  * @param vMS
	  * @param secs
	  * @return
	  */
	 public abstract double flyInAir(String caseText, double angle, double ascentHeight, double weight,
			 double airDensity, double vMS, double secs);
	 
}
