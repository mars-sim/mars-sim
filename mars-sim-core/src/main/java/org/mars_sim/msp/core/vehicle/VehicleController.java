/*
 * Mars Simulation Project
 * VehicleController.java
 * @date 2023-04-18
 * @author Manny Kung
 */

 package org.mars_sim.msp.core.vehicle;

 import java.io.Serializable;
 import java.util.logging.Level;
 
 import org.mars_sim.msp.core.equipment.Battery;
 import org.mars_sim.msp.core.logging.SimLogger;
 import org.mars_sim.msp.core.resource.ResourceUtil;
 
 /**
  * This class represents the modeling of a motor controller, which comprises power 
  * electronics and embedded micro-computing elements that directs the energy flow 
  * to the motor of an electric vehicle.
  */
 public class VehicleController implements Serializable {
 
	 /** default serial id. */
	 private static final long serialVersionUID = 1L;
 
	 /** default logger. */
	 private static SimLogger logger = SimLogger.getLogger(VehicleController.class.getName());
 
	 /** Speed in kph */
	 public static final double SPEED_BUFFER = .01;
	 
	 /** Need to provide oxygen as fuel oxidizer for the fuel cells. */
	 public static final int OXYGEN_ID = ResourceUtil.oxygenID;
	 /** The fuel cells will generate 2.25 kg of water per 1 kg of methane being used. */
	 public static final int WATER_ID = ResourceUtil.waterID;
	 /** The ratio of the amount of oxidizer to fuel. */
	 public static final double RATIO_OXIDIZER_FUEL = 1.5;
	 /** The ratio of water produced to methanol consumed. */
	 private static final double RATIO_WATER_METHANOL = 1.125;
	 /** The factor for estimating the adjusted fuel economy. */
	 public static final double FUEL_ECONOMY_FACTOR = 1.25;
	 
	 /** Mars surface gravity is 3.72 m/s2. */
	 private static final double GRAVITY = 3.72;
	 /** Conversion factor : 1 Wh = 3.6 kilo Joules */
	 private static final double JOULES_PER_WH = 3_600.0;
	 /** Conversion factor : 1 m/s = 3.6 km/h (or kph) */
	 private static final double KPH_CONV = 3.6;
		
	 private static final String KG = " kg  ";
	 private static final String N = " N  ";
	 private static final String KM_KG = " km/kg  ";
	 private static final String WH_KM = " Wh/km  ";
	 private static final String KM = " km  ";
	 private static final String KW = " kW  ";
	 private static final String KPH = " kph  ";
	 private static final String WH = " Wh  ";
	 private static final String KWH = " kWh  ";
	 private static final String W = " W  ";		
		 
	 // Data members
	 /** The fuel type of this vehicle. */
	 private int fuelType;
	 /**  Cache the time in hr. */ 
	 private double hrsTimeCache;
	 /** Cache the distance traveled in km. */ 
	 private double distanceCache;
	 /** Cache the fuel used in kg. */ 
	 private double fuelUsedCache;	
	 
	 /** The vehicle to operate. */ 
	 private Vehicle vehicle;
	 /** The battery of the vehicle. */ 
	 private Battery battery;
	 
	 /**
	  * Constructor.
	  * 
	  * @param vehicle The vehicle requiring a controller.
	  * 
	  */
	 public VehicleController(Vehicle vehicle) {
		 this.vehicle = vehicle;
		 battery = new Battery(vehicle);
		 fuelType = vehicle.getFuelType();
	 }
 
	 /**
	  * Adjust the speed of the vehicle (Accelerates or Decelerate) and possibly use the fuel or 
	  * battery reserve or both to speed up or slow down the vehicle.
	  *
	  * @param hrsTime
	  * @param distance
	  * @param vKPH
	  * @return
	  */
	 public double adjustSpeed(double hrsTime, double distance, double vKPH, double remainingFuel, double remainingOxidizer) {
		 
		 double remainingHrs = 0;
 
			double navpointDist = distance; // [in km]   
		 // Gets initial speed in kph
		 double uKPH = vehicle.getSpeed(); 
		 // Gets initial speed in m/s
		 double uMS = uKPH / KPH_CONV;
		 
		 // Works for cases when 
		 // 1. vKPH == 0 or
		 // 2. vKPH > 0 or
		 // 3. distance <= OperateVehicle.DESTINATION_BUFFER
 
		 double secs = 3600 * hrsTime;
		 // Note: 1 m/s = 3.6 km/hr (or kph)	     	
		 double vMS = vKPH / KPH_CONV; // [in m/s]
	  
			if (vKPH < 0 || vMS < 0) {
			 logger.log(vehicle, Level.INFO, 20_000, "Final speed was negative (" 
					 +  Math.round(vKPH * 1000.0)/1000.0 + " kph). Reset back to zero.");
			 vKPH = 0;
		 }
			
			if (uKPH < 0 || uMS < 0) {
			 logger.log(vehicle, Level.INFO, 20_000, "Initial speed was negative (" 
					 +  Math.round(uKPH * 1000.0)/1000.0 + " kph). Reset back to zero.");
			 uKPH = 0;
		 }
 
			double distanceTravelled = (uKPH + vKPH) / 2 * hrsTime;
			
		 double accelTarget = (vMS - uMS) / secs; // [in m/s2]
		 // Gets the current mass of the vehicle with payload
		 double mass = vehicle.getMass(); // [in kg]
	 
		 double averageSpeed = (vMS + uMS)/2.0;
		 double averageSpeedSQ = 0;
		 
		 if (averageSpeed > 0)
			 averageSpeedSQ =  averageSpeed * averageSpeed; // [in (m/s)^2]
		 
		 // Calculate force against Mars surface gravity
		 double fGravity = 0;      
		 // Calculate the force on rolling resistance 
		 double fRolling = 0;    
		 // Calculate the gradient resistance or road slope force 
		 double fRoadSlope = 0;
		 
		 double angle = 0;
		 
		 double mg = mass * GRAVITY;
		 
		 double potentialEnergyDrone = 0;
		 
		 if (vehicle instanceof Drone) {
			 // For drones, it needs energy to ascend into the air and hover in the air
			 // Note: Refine this equation for drones 
			 double currentHeight = ((Drone) vehicle).getHoveringHeight();
			 
			 fGravity = - mg;
			 
			 fRolling = 0;
			 
			 fRoadSlope = 0;
			  // FUTURE : How to simulate controlled descent to land at the destination ?
			  // Also need to account for the use of fuel or battery's power to ascend and descend 
			 
			 if (uMS < vMS) {
				 // Case A : During ascent
				   if (currentHeight >= Flyer.ELEVATION_ABOVE_GROUND) {
					   // Do NOT ascent anymore
					   potentialEnergyDrone = 0;
				 }
				   else {
					 // For ascent, assume the height gained is the same as distanceTravelled
					   potentialEnergyDrone = mg * distanceTravelled;
				   }
			 }
 
			 else {
				 // Case B : During controlled descent
				 if (currentHeight <= 0) {
					   // Do NOT ascent anymore
					 potentialEnergyDrone = 0;
				 }
				   else {
						// Assume using about 25% of energy gain in potenial energy to maintain optimal descent,
					   // avoid instability and perform a controlled descent
					   
					   // See detail strategies on https://aviation.stackexchange.com/questions/64055/how-much-energy-is-wasted-in-an-aeroplanes-descent
					   if (currentHeight < distanceTravelled) {
						   // Assume the height lost is the same as distanceTravelled
						   potentialEnergyDrone = .25 * mg * currentHeight;
					   }
					   else {
						 // For descent, assume the height lost is the same as distanceTravelled
						   potentialEnergyDrone = .25 * mg * distanceTravelled;
					   }
				   }
			 }
		 }
	 
		 else if (vehicle instanceof Rover) {
				// For Ground rover, it doesn't need as much
			 angle = vehicle.getTerrainGrade();
			 // Assume road rolling resistance coeff of 0.05 on roads with pebbles/potholes on Mars (typically 0.015 on paved roads on Earth)
			 // See https://x-engineer.org/rolling-resistance/
			 // The ratio between distance and wheel radius is the rolling resistance coefficient
			 fRolling = - 0.05 * mg * Math.cos(angle);
			 // https://x-engineer.org/road-slope-gradient-force/
			 fRoadSlope = - mg * Math.sin(angle);
		 }
		 
		 double fInitialFriction = - 5.0 / (0.5 + averageSpeed);  // [in N]
		 // Note : Aerodynamic drag force = 0.5 * air drag coeff * air density * vehicle frontal area * vehicle speed ^2 
		 // https://x-engineer.org/aerodynamic-drag
		 
		 double frontalArea = vehicle.getWidth() * vehicle.getWidth() * .9;
				 
		 double fAeroDrag = - 0.5 * 0.4 * 0.02 * frontalArea * averageSpeedSQ;
		 // Gets the summation of all the forces acting against the forward motion of the vehicle
		 double totalForce = fInitialFriction + fAeroDrag + fGravity + fRolling + fRoadSlope;
		 // Gets the natural deceleration due to these forces
		 double aForcesAgainst = totalForce / mass;
		 // Gets the acceleration of the motor
		 double aMotor = accelTarget - aForcesAgainst;
		 
		 if (aMotor >= 0) {
			 // Case 1: acceleration is needed to either maintain the speed or to go up to the top speed
  
			 // Set new vehicle acceleration
			 vehicle.setAccel(aMotor);
			 
			 double iPower = aMotor * mass * vMS + potentialEnergyDrone / secs; // [in W]
			 
			 if (uKPH - vKPH > SPEED_BUFFER || vKPH - uKPH < SPEED_BUFFER) {
				 logger.log(vehicle, Level.INFO, 20_000,  
					 "Need to exert power just to maintain the speed at "
					 + Math.round(vKPH * 1_000.0)/1_000.0 + " kph  "
					 + "aMotor: " + Math.round(aMotor * 1_000.0)/1_000.0 + " m/s2  "
					 + "accelTarget: " + Math.round(accelTarget * 1_000.0)/1_000.0 + " m/s2  "
					 + "aForcesAgainst: " + Math.round(aForcesAgainst * 1_000.0)/1_000.0 + " m/s2."
					 );
			 }
			 else {
				 logger.log(vehicle, Level.INFO, 20_000,  
					 "Need to accelerate and increase the speed from "
					 +  Math.round(uKPH * 1_000.0)/1_000.0 + " kph "
					 + "to " + Math.round(vKPH * 1_000.0)/1_000.0 + " kph  "
					 + "aMotor: " + Math.round(aMotor * 1_000.0)/1_000.0 + " m/s2  "
					 + "accelTarget: " + Math.round(accelTarget * 1_000.0)/1_000.0 + " m/s2  "
					 + "aForcesAgainst: " + Math.round(aForcesAgainst * 1_000.0)/1_000.0 + " m/s2."
					 );
			 }        
  
			 // Convert the total energy needed from J to Wh
			 double totalEnergyNeeded = iPower * secs / JOULES_PER_WH ; // [in Wh]
			 // Get energy from the battery
			 double energyByBattery = battery.requestEnergy(totalEnergyNeeded / 1000.0, hrsTime) * 1000.0;
			 // Get energy from the fuel
			 double energyByFuel = totalEnergyNeeded - energyByBattery;
			 
			 double fuelNeeded = 0;
			 
			 // Case A : Battery has enough juice for the acceleration
			  if (totalEnergyNeeded == energyByBattery) {
				 logger.log(vehicle, Level.INFO, 20_000,  
						 "Case A: Use on-board battery only. "
						 + "energyByBattery: " + Math.round(energyByBattery * 1000.0)/1000.0 + WH
						 + "totalEnergyNeeded: " + Math.round(totalEnergyNeeded * 1000.0)/1000.0 + WH				
						 + "Battery: " + Math.round(battery.getcurrentEnergy() * 1_000.0)/1_000.0 + KWH
						 );  
			  }
			  
			  else if (energyByFuel > 0.001) {
				  // Case B and C: If the battery is unable to meet the needed energy requirement
				 // Need to turn on fuel cells to supply more power
				  
				 // Derive the mass of fuel needed kg = Wh / Wh/kg
				 fuelNeeded = energyByFuel / vehicle.getFuelConv();
				 
				 if (fuelNeeded <= remainingFuel) {
					 // Case B: fuel is sufficient
					 logger.log(vehicle, Level.INFO, 20_000,  
						 "Case B: Partial battery with sufficient fuel.  " 
						 + "energyByBattery: " +  Math.round(energyByBattery * 1000.0)/1000.0 + WH
						 + "Battery: " + Math.round(battery.getcurrentEnergy() * 1_000.0)/1_000.0 + KWH 						
						 + "totalEnergyNeeded: " + Math.round(totalEnergyNeeded * 1000.0)/1000.0 + WH
						 + "fuelNeeded: " +  Math.round(fuelNeeded * 1000.0)/1000.0  + KG
						 + "distanceTravelled: " +  Math.round(distanceTravelled * 1000.0)/1000.0  + " km."
						 );
				  }
				 else {				
					 // Case C : fuel needed is less than available (just used up the last drop of fuel). Update fuelNeeded.
					 
					 // Limit the fuel to be used
					 fuelNeeded = remainingFuel;
 
					 energyByFuel = fuelNeeded * vehicle.getFuelConv();
		 
					 // FUTURE: need to consider the on-board vehicle power usage
					 iPower = energyByFuel / secs * JOULES_PER_WH;
 
					 // Find the new speed   
					 vKPH = iPower - potentialEnergyDrone / secs / aMotor / mass;
					 
					 // FUTURE : may need to find a way to optimize motor power usage 
					 // and slow down the vehicle to the minimal to conserve power	
					 
					 vMS = vKPH / KPH_CONV; // [in m/s^2]
 
					 accelTarget = (vMS - uMS) / secs; // [in m/s^2]
					 // Recompute the new distance it could travel
						distanceTravelled = (uKPH + vKPH) / 2 * hrsTime;
						
					 logger.log(vehicle, Level.INFO, 20_000,  
							 "Case C: Partial battery and insufficient fuel.  " 
							 + "energyByBattery: " +  Math.round(energyByBattery * 1000.0)/1000.0 + WH
							 + "Battery: " 			+ Math.round(battery.getcurrentEnergy() * 1_000.0)/1_000.0 + KWH
							 + "totalEnergyNeeded: " + Math.round(totalEnergyNeeded * 1000.0)/1000.0 + WH				        	
							 + "fuelNeeded: " +  Math.round(fuelNeeded * 1000.0)/1000.0  + KG
							 + "iPower: " 			+ Math.round(iPower * 1_000.0)/1_000.0 + W							
							 + "vKPH: " 				+ Math.round(vKPH * 1_000.0)/1_000.0 + KPH   							
							 + "navpointDist: " +  Math.round(navpointDist * 1000.0)/1000.0  + KM 
							 + "distanceTravelled: " +  Math.round(distanceTravelled * 1000.0)/1000.0  + KM
							 );
				 }
			  }
			  else {
				  logger.log(vehicle, Level.INFO, 20_000,  
						 "Case D: Unknown.  " 
						 + "energyByBattery: " +  Math.round(energyByBattery * 1000.0)/1000.0 + WH
						 + "Battery: " 			+ Math.round(battery.getcurrentEnergy() * 1_000.0)/1_000.0 + KWH
						 + "totalEnergyNeeded: " + Math.round(totalEnergyNeeded * 1000.0)/1000.0 + WH				        	
						 + "fuelNeeded: " +  Math.round(fuelNeeded * 1000.0)/1000.0  + KG
						 );
			  }
						
			 // Adjust the speed
			 vehicle.setSpeed(vKPH);
			 // Add distance traveled to vehicle's odometer.
			 vehicle.addOdometerMileage(distanceTravelled, fuelNeeded);
			 // Track maintenance due to distance traveled.
			 vehicle.addDistanceLastMaintenance(distanceTravelled);
			 // Derive the instantaneous fuel economy [in km/kg]
			 
			 double iFE = 0;
			 
			 if (fuelNeeded > 0) {
				 // Derive the instantaneous fuel economy [in km/kg]
				 iFE = distanceTravelled / fuelNeeded;	        
				 // Set the instantaneous fuel economy [in km/kg]
				 vehicle.setIFuelEconomy(iFE);
			 }
 
			 // Derive the instantaneous fuel consumption [Wh/km]
			 double iFC = 0;	
			 
			 if (distanceTravelled > 0) {
				 // Derive the instantaneous fuel consumption [Wh/km]
				 iFC = totalEnergyNeeded / distanceTravelled;	        
				 // Set the instantaneous fuel consumption [Wh/km]
				 vehicle.setIFuelConsumption(iFC);
			 }
			 
			 double bFC = vehicle.getBaseFuelConsumption();       
			 // Get the base fuel economy 
			 double bFE = vehicle.getBaseFuelEconomy();      	
			 // Calculate the average power for this time period [in kW]
			 double aveP = totalEnergyNeeded / 1000.0 / hrsTime;
			 
			 /*
			  * May comment off the block of codes below once debugging is done.
			  * 
			  * NOTE: DO NOT delete any of them. Needed for testing when new features are added in future.
			  */
			 logger.log(vehicle, Level.INFO, 20_000,  
					 vehicle.getSpecName()
					  + "mass: " 				+ Math.round(mass * 100.0)/100.0 + KG
					  + "odometer: " 			+ Math.round(vehicle.getOdometerMileage()* 1_000.0)/1_000.0 + KM
					  + "navpointDist: " 		+ Math.round(navpointDist * 1_000.0)/1_000.0 + KM
					  + "distanceTravelled: " + Math.round(distanceTravelled * 1_000.0)/1_000.0 + KM
					 + "time: "				+ Math.round(secs * 1_000.0)/1_000.0 + " secs  "
					 + "uKPH: "				+ Math.round(uKPH * 1_000.0)/1_000.0 + KPH
					 + "vKPH: " 				+ Math.round(vKPH * 1_000.0)/1_000.0 + KPH      	        
					 + "energyByBattery: " +  Math.round(energyByBattery * 1000.0)/1000.0 + WH
					 + "Battery: " 			+ Math.round(battery.getcurrentEnergy() * 1_000.0)/1_000.0 + KWH    
					 + "totalEnergyNeeded: " + Math.round(totalEnergyNeeded * 1000.0)/1000.0 + WH
					 
					 + "totalForce: " 		+ Math.round(totalForce * 10_000.0)/10_000.0 + N       	        
					 + "iPower: " 			+ Math.round(iPower * 1_000.0)/1_000.0 + W
					 
						+ "avePower: " 			+ Math.round(aveP * 1_000.0)/1_000.0 + KW
						
					 + "totalEnergyNeeded: " + Math.round(totalEnergyNeeded * 1_000.0)/1_000.0 + WH
					 + "energyByFuel: " 		+ Math.round(energyByFuel * 1_000.0)/1_000.0 + WH
					 + "energyByBattery: " 	+ Math.round(energyByBattery * 1_000.0)/1_000.0 + WH
					 + "Battery: " + Math.round(battery.getcurrentEnergy() * 1_000.0)/1_000.0 + KWH
					 
					 + "fuelUsed: " 			+ Math.round(fuelNeeded * 100_000.0)/100_000.0 + KG 
					 + "angle: "				+ Math.round(angle / Math.PI * 180.0 * 10.0)/10.0 + " deg  "
					 + "fInitialF: " 		+ Math.round(fInitialFriction * 1_000.0)/1_000.0 + N
					 + "fGravity: " 			+ Math.round(fGravity * 1_000.0)/1_000.0 + N
					 + "fAeroDrag: " 		+ Math.round(fAeroDrag * 1_000.0)/1_000.0 + N
					 + "fRolling: " 			+ Math.round(fRolling * 1_000.0)/1_000.0 + N
					 + "fRoadSlope: "		+ Math.round(fRoadSlope * 1_000.0)/1_000.0 + N
   
					 + "baseFE: " 			+ Math.round(bFE * 1_000.0)/1_000.0 + KM_KG  
						+ "estFE: " 			+ Math.round(vehicle.getEstimatedFuelEconomy() * 1_000.0)/1_000.0 + KM_KG
					 + "instantFE: " 		+ Math.round(iFE * 1_000.0)/1_000.0 + KM_KG  
						+ "cumFE: " 			+ Math.round(vehicle.getCumFuelEconomy() * 1_000.0)/1_000.0 + KM_KG  
					 + "baseFC: " 			+ Math.round(bFC * 1_000.0)/1_000.0 + WH_KM 
					 + "instantFC: " 		+ Math.round(iFC * 1_000.0)/1_000.0 + WH_KM    
						   + "cumFC: " 			+ Math.round(vehicle.getCumFuelConsumption() * 1_000.0)/1_000.0 + WH_KM  
			 );
					 
			 // Cache the new value of fuelUsed	
			 if (fuelNeeded > 0) {
				 // Retrieve the fuel needed for the distance traveled
				 vehicle.retrieveAmountResource(fuelType, fuelUsedCache);
				 // Assume double amount of oxygen as fuel oxidizer
				 vehicle.retrieveAmountResource(OXYGEN_ID, RATIO_OXIDIZER_FUEL * fuelUsedCache);
				 // Generate 1.75 times amount of the water from the fuel cells
				 vehicle.storeAmountResource(WATER_ID, RATIO_WATER_METHANOL * fuelUsedCache);
				 
				 fuelUsedCache = fuelNeeded;
			 }
		 }
		 
		 else {
				// Case 2: deceleration is needed
			 // Gets the deceleration using regenerative braking
			 double aRegen = aMotor;
			 // Set new vehicle acceleration
			 vehicle.setAccel(aRegen);
			 
			 double iPower = - aRegen * mass * vMS; // (vMS + uMS)/2.0; // [in W]
			 
			 logger.log(vehicle, Level.INFO, 20_000,  "Need to decelerate and reduce the speed from " 
					 +  Math.round(uKPH * 1_000.0)/1_000.0 + " kph "
					 + "to " + Math.round(vKPH * 1_000.0)/1_000.0
					 + " kph.  "
					 + "regen decel: " + Math.round(aRegen * 1_000.0)/1_000.0 
					 + " m/s2.  "
					 + "target decel: " + Math.round(accelTarget * 1_000.0)/1_000.0 
					 + " m/s2. "			
			 );
			 
			 // Convert the energyNeeded energy from J to Wh
			 double energyNeeded = iPower * secs / JOULES_PER_WH ; // [in Wh]
				 
			 double energyforCharging = battery.provideEnergy(energyNeeded / 1000, hrsTime) * 1000; 
			 
			 /*
			  * May comment off the block of codes below once debugging is done.
			  * 
			  * NOTE: DO NOT delete any of them. Needed for testing when new features are added in future.
			  */
			 logger.log(vehicle, Level.INFO, 20_000,  
			 vehicle.getSpecName()
			 		+ "mass: " 				+ Math.round(mass * 100.0)/100.0 + KG
					+ "odometer: " 			+ Math.round(vehicle.getOdometerMileage()* 1_000.0)/1_000.0 + KM
					+ "navpointDist: " 		+ Math.round(navpointDist * 1_000.0)/1_000.0 + KM
					+ "distanceTravelled: " + Math.round(distanceTravelled * 1_000.0)/1_000.0 + KM
					 + "time: "				+ Math.round(secs * 1_000.0)/1_000.0 + " secs  "
					 + "uKPH: "				+ Math.round(uKPH * 1_000.0)/1_000.0 + KPH
					 + "vKPH: " 				+ Math.round(vKPH * 1_000.0)/1_000.0 + KPH     
					 + "Battery: " 			+ Math.round(battery.getcurrentEnergy() * 1_000.0)/1_000.0 + KWH  
					 
					 + "totalForce: " 		+ Math.round(totalForce * 10_000.0)/10_000.0 + N       	        
					 + "iPower: " 			+ Math.round(iPower * 1_000.0)/1_000.0 + W
					 
					 + "energyNeeded: " 		+ Math.round(energyNeeded * 1_000.0)/1_000.0 + WH
					 + "energyforCharging: " + Math.round(energyforCharging * 1_000.0)/1_000.0 + WH
			 );
	  
			 // Derive the instantaneous fuel consumption [Wh/km]
			 double iFC = 0;	
			 
			 if (distanceTravelled > 0) {
				 // Derive the instantaneous fuel consumption [Wh/km]
				 iFC = energyforCharging / distanceTravelled;	        
				 // Set the instantaneous fuel consumption [Wh/km]
				 vehicle.setIFuelConsumption(iFC);
			 }
		 }
		 
		 // Set new vehicle speed
		 vehicle.setSpeed(vKPH);
		 // Determine new position
		 vehicle.setCoordinates(vehicle.getCoordinates().getNewLocation(vehicle.getDirection(), distanceTravelled));       
 
		 return remainingHrs;   
	 }
 
	 /**
	  * Gets the amount of fuel (kg) needed for a trip of a given distance (km).
	  *
	  * @param tripDistance   the distance (km) of the trip.
	  * @param fuelEconomy the vehicle's instantaneous fuel economy (km/kg).
	  * @param useMargin      Apply safety margin when loading resources before embarking if true.
	  * @return amount of fuel needed for trip (kg)
	  */
	 public double getFuelNeededForTrip(Vehicle vehicle, double tripDistance, double fuelEconomy, boolean useMargin) {
		 double result = tripDistance / fuelEconomy;
		 double factor = 1;
		 if (useMargin) {
			 if (tripDistance < 100) {
				 // Note: use formula below to add more extra fuel for short travel distance on top of the fuel margin
				 // in case of getting stranded locally
				 factor = - tripDistance / 50.0 + 3 ;
			 }	
			 factor *= vehicle.getFuelRangeErrorMargin();
			 result *= factor;
		 }
		 return result;
	 }
	 
	 /**
	  * Gets the HrsTime cache in hr.
	  * 
	  * @return
	  */
	 public double getHrsTimeCache() {
		 return hrsTimeCache;
	 }
	 
	 /** 
	  * Gets the distance cache in km.
	  * 
	  * @return
	  */
	 public double getDistanceCache() {
		 return distanceCache;
	 }
	 
	 /** 
	  * Gets the fuelUsed cache in kg.
	  * 
	  * @return
	  */
	 public double getFuelUsedCache() {
		 return fuelUsedCache;
	 }
	 
 }