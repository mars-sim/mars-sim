/*
 * Mars Simulation Project
 * VehicleController.java
 * @date 2023-11-22
 * @author Manny Kung
 */

 package com.mars_sim.core.vehicle;

 import java.io.Serializable;
import java.util.logging.Level;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.equipment.Battery;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.ResourceUtil;
 
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
 
	 
	 /** Need to provide oxygen as fuel oxidizer for the fuel cells. */
	 public static final int OXYGEN_ID = ResourceUtil.oxygenID;

	 public static final int WATER_ID = ResourceUtil.waterID;
	 /** The ratio of the amount of oxidizer to fuel. */
	 public static final double RATIO_OXIDIZER_FUEL = 1.5;
	 /** The ratio of water produced for every methanol consumed. */
	 private static final double RATIO_WATER_METHANOL = 1.125;
	 /** The ratio of water produced for every methane consumed. */
//	 private static final double RATIO_WATER_METHANE = 2.25;
	 /** The factor for estimating the adjusted fuel economy. */
	 public static final double FUEL_ECONOMY_FACTOR = .8;
	 /** The factor for estimating the adjusted fuel consumption. */
	 public static final double FUEL_CONSUMPTION_FACTOR = 1.2; 
	 /** The minimum fuel consumption. */
	 public static final double MIN_FUEL = 0.001;
	 
	 /** Mars surface gravity is 3.72 m/s2. */
	 private static final double GRAVITY = 3.72;
	 /** Conversion factor : 1 Wh = 3.6 kilo Joules */
	 private static final double JOULES_PER_WH = 3_600.0;
	 /** Conversion factor : 1 m/s = 3.6 km/h (or kph) */
	 private static final double KPH_CONV = 3.6;
		
	 /** Speed buffer in kph. */
	 public final double SPEED_BUFFER = .01;
	 /** The outside average air density. */
	 public double airDensity = 0.02;
	 
	 private static final String KG = " kg  ";
	 private static final String WH = " Wh  ";
	 private static final String KWH = " kWh  ";
	 private static final String KPH = " kph ";
	 private static final String KPH_ = " kph  "; 
	 private static final String W = " W  ";
	 private static final String KM = " km  ";
	 private static final String N = " N  ";
	 private static final String KW = " kW  ";
	 
	 /**
	  * Please do NOT delete any of the metric unit string below.
	  * They will be handy for testing. Thanks !
	  *

	 private static final String KM_KG = " km/kg  ";
	 private static final String WH_KM = " Wh/km  ";


	 private static final String KPH = " kph  ";
	  */
	 
	 // Data members
	 /** The fuel type id of this vehicle. */
	 private int fuelTypeID;
	 /**  Cache the time in hr. */ 
	 private double hrsTimeCache;
	 /** Cache the distance traveled in km. */ 
	 private double distanceCache;

	 /** The vehicle to operate. */ 
	 private Vehicle vehicle;
	 /** The battery of the vehicle. */ 
	 private Battery battery;
	 
	 private Simulation sim = Simulation.instance();
	 
	 /**
	  * Constructor.
	  * 
	  * @param vehicle The vehicle requiring a controller.
	  * 
	  */
	 public VehicleController(Vehicle vehicle) {
		 this.vehicle = vehicle;
		 
		 int numModule = vehicle.getVehicleSpec().getBatteryModule();
		 double energyPerModule = vehicle.getVehicleSpec().getEnergyPerModule();
		 battery = new Battery(vehicle, numModule, energyPerModule);
		 fuelTypeID = vehicle.getFuelTypeID();
	 }
 
	 /**
	  * Adjusts the speed of the vehicle (Accelerates or Decelerate) and possibly use the fuel or 
	  * battery reserve or both to speed up or slow down the vehicle.
	  *
	  * @param hrsTime
	  * @param distToCover the distance that can be covered [in km]
	  * @param vKPH
	  * @param remainingFuel
	  * @param remainingOxidizer
	  * @return remainingHrs
	  */
	 public double consumeFuelEnergy(double hrsTime, double distToCover, double vKPH, double remainingFuel, double remainingOxidizer) {
		 
		 /** Cache the energy recovered from regen braking in kWh. */ 
		 double regenEnergyBuffer = 0;	
		 
		 double remainingHrs = 0;
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
			 vMS = 0;
		 }
			
		 if (uKPH < 0 || uMS < 0) {
			 logger.log(vehicle, Level.INFO, 20_000, "Initial speed was negative (" 
					 +  Math.round(uKPH * 1000.0)/1000.0 + " kph). Reset back to zero.");
			 uKPH = 0;
			 uMS = 0;
		 }
		 
		 // distance in km
		 double distanceTravelled = distToCover; //vKPH * hrsTime;
			
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
		 
		 double fInitialFriction = 0;
		 
		 // 1 N = (1 kg) (1 m/s2)
		 
		 double angle = 0;
		 
		 double mg = mass * GRAVITY;
		 
		 double potentialEnergyDrone = 0;
		  
		 double powerDrone = 0;
			
		 airDensity = sim.getWeather().getAirDensity(vehicle.getCoordinates());
			
		 if (vehicle instanceof Rover) {
			 // Important for Ground rover
			 angle = vehicle.getTerrainGrade();
			 // Assume road rolling resistance coeff of 0.075 on roads with pebbles/potholes on Mars (typically 0.015 on paved roads on Earth)
			 // See https://x-engineer.org/rolling-resistance/
			 // The ratio between distance and wheel radius is the rolling resistance coefficient
			 fRolling = - 0.075 * mg * Math.cos(angle);
			 // https://x-engineer.org/road-slope-gradient-force/
			 fRoadSlope = - mg * Math.sin(angle);
			 
			 fInitialFriction = - 7.0 / (0.5 + averageSpeed);  // [in N]
		 }
		 
		 else if (vehicle instanceof Drone) {
			 // For drones, it needs energy to ascend into the air and hover in the air
			 // Note: Refine this equation for drones			 
			 // in km
			 double currentHeight = ((Drone) vehicle).getHoveringHeight();
			/*
			 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
			 * delete any of them. Needed for testing when new features are added in future. Thanks !
			 */ 
			  
			 // Airflow velocity (V1)  // V1 = V0 + v1
//			 double V1;
			 // UAV flight speed (V0) // 
//			 double V0;
			 // Propeller induced velocity (v1)
//			 double v1;
			 // Thrust coefficient CT = 2 * (V0 + v1) * v1
//			 double CT;
			 // propeller length in meter
//			 double radiusPropeller = .5;

			 // Rev per minutes
//			 double rotationalSpeedRPM = 0; 
//			 double dragForceD = 0;
//			 double weightForceW = 0;
//			 double liftForceL = 0;
//			 double thrustForceT = 0;
//			 double liftToDragRatio = 0;
			 
			 fInitialFriction = 0;
			 fGravity = - mg;		 
			 fRolling = 0;		 
			 fRoadSlope = 0;
			 
			  // FUTURE : How to simulate controlled descent to land at the destination ?
			  // Also need to account for the use of fuel or battery's power to ascend and descend 
			 
			 if (uMS < vMS) {
				 // If speeding up	
				 
				 // Case A : In the air
				 if (currentHeight >= Flyer.ELEVATION_ABOVE_GROUND) {

				 	// Case A1 : Hovering
					// Thus UAV flight speed (V0) is zero
					// V0 = 0;
					// Airflow velocity V1 = V0 + v1 = 0 + v1
					// V1 = v1;
					 
					// Do NOT ascent anymore. Hover at the this height and travel horizontally
					potentialEnergyDrone = 0;
					
					// Using Volume-based approach,
					// P = (m * g * h) / (η * ρ * v)
					// η, efficiency of the motor and propeller (typically 0.7-0.9) 
					// ρ = air density (kg/m³) 
					// v = air speed (m/s)

					// Power to maintain the height of the drone
					powerDrone = mg * currentHeight / 0.9 / airDensity / vMS;
					
					/*
					 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
					 * delete any of them. Needed for testing when new features are added in future. Thanks !
					 */
					// Hover airflow velocity V1 = v1 = sqrt (CT /2)
					// v1 = Math.sqrt(CT/2);	
					// Hover thrust coefficient: CT = 2 * v1^2
					// CT = 2 * v1 * v1;
					// Hover thrust: T = 2 * density * pi * r^2 * v1^2
					// thrustForceT =  2 * airDensity * Math.PI * radiusPropeller * radiusPropeller * v1 * v1;
					// liftToDragRatio = liftForceL / dragForceD;				 
					// thrustForceT = weightForceW / liftToDragRatio;
				 }
				 else {
					 // Case A2 : Ascent or Lifting Up
					 // Assume the height gained is the same as distanceTravelled
					 potentialEnergyDrone = mg * 1000 * distanceTravelled;				 	 

					 // Lifting up thrust T = 2 * density * pi * r^2 * (V0 + v1) * v1
			 
					 // Assume the total radius of the four propellers span the width of the drone
					 double width = vehicle.getWidth();
					 double radiusPropellerSquare = (width / 4) * (width / 4);
					 
					 // With four propeller motors, 
					 double thrustForceTotal = 4 * 2 * airDensity * Math.PI * radiusPropellerSquare * (uMS + vMS) * vMS;
					 
					 double efficiencyMotor = 0.9;
					 // Assume a constant voltage
					 double voltage = Battery.DRONE_VOLTAGE;
				
					 // the gain of potential energy of the drone require extra the power drain on the drone's fuel and battery system
					 powerDrone = thrustForceTotal * voltage / efficiencyMotor + potentialEnergyDrone / secs;
				 }
			 }
 
			 else {
				 // Case B : During controlled descent / going down
				 if (currentHeight <= 0) {
					 // Case B1 : landed
					 // Do NOT ascent anymore
					 potentialEnergyDrone = 0;
				 }
				 else {
					 // Case B2 : descending or preparing for landing
					 // Assume using about 25% of energy gain in potential energy to maintain optimal descent,
					 // avoid instability and perform a controlled descent
					   
					 // See detail strategies on https://aviation.stackexchange.com/questions/64055/how-much-energy-is-wasted-in-an-aeroplanes-descent
//					 if (currentHeight < distanceTravelled) {
						 // Assume the height lost is the same as distanceTravelled
						 distanceTravelled = currentHeight;
						  
						 // For descent, assume the height lost is the same as distanceTravelled
						 potentialEnergyDrone = mg * 1000 * distanceTravelled;
						 
						/*
						 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
						 * delete any of them. Needed for testing when new features are added in future. Thanks !
						 */
						 
						 double width = vehicle.getWidth();
						 double radiusPropellerSquare = (width / 4) * (width / 4);
						 // Landing airflow velocity V1 = v1 - abs(V0)
//						 double landingVel = vMS - Math.abs(uMS);					 
						 // Landing thrust coefficient: CT = −2(V0_bar + v1_bar)．v1_bar
						 // Landing induced velocity: v1_bar = −v0_bar / 2 - sqrt((v0_bar/2)^2 - CT/2))
						 // Landing thrust T = - 2 * density * pi * r^2 * ( V0 + v1) * v1
						 double thrustForceTotal = 4 * 2 * airDensity * Math.PI * radiusPropellerSquare * (vMS + vMS) * vMS;
						 
						 double efficiencyMotor = 0.9;
						 // Assume a constant voltage
						 double voltage = Battery.DRONE_VOLTAGE;
						 
						 // the loss of potential energy of the drone will reduce the power draing on the drone 
						 powerDrone = thrustForceTotal * voltage / efficiencyMotor - potentialEnergyDrone / secs;
//					 }
				 }
			 }
		 }

		 
		 double frontalArea = vehicle.getWidth() * vehicle.getWidth() * .9;
		 // https://x-engineer.org/aerodynamic-drag

		 // Note : Aerodynamic drag force = 0.5 * air drag coeff * air density * vehicle frontal area * vehicle speed ^2 
		 double fAeroDrag = - 0.5 * 0.4 * airDensity * frontalArea * averageSpeedSQ;
		 // Gets the summation of all the forces acting against the forward motion of the vehicle
		 double totalForce = fInitialFriction + fAeroDrag + fGravity + fRolling + fRoadSlope;
		 
		 double powerConstantSpeed = totalForce * vMS / 0.9;
		 
		 // If at constant speed, P = F_T * v / η
		 // η, overall efficiency in transmission, normally ranging 0.85 (low gear) - 0.9 (direct drive)
		 // F_T, total forces acting on the car
		 // e.g. P = ((250 N) + (400 N)) (90 km/h) (1000 m/km) (1/3600 h/s) / 0.85 = 1.9118 kW
		 
		 // Motor torque vs. power and rpm 
		 // T = P / (2 * π * n_rps) = 0.159 P / n_rps = P / (2π(n_rpm/60)) = 9.55 P / n_rpm
		 // The moment delivered by the motor in the car above with the engine running at speed 1500 rpm can be calculated as
		 // T = 9.55 (19118 W) / (1500 rpm) = 121 Nm
		 
		 
		 // Gets the natural deceleration due to these forces
//		 double accelForcesAgainst = totalForce / mass;
		 
		 // Gets the acceleration of the motor
//		 double accelMotor = accelTarget - accelForcesAgainst;
		 
		// Gets the acceleration of the motor
		 double accelSpeedUp = (vMS - uMS) / secs;
		 
		 double powerSpeedUp = mass * accelSpeedUp * vMS;
		 
		 double fuelNeeded = 0;
		 
		 double energyByBattery = 0;
		 
		 // [in Wh], not in kWh
		 double overallEnergyUsed = 0;

		 double iPower = 0;
		 
		 // In [W], not [kW]
		 double avePower = Math.abs(powerConstantSpeed) +  Math.abs(powerSpeedUp) +  Math.abs(powerDrone);
		 
		 if (accelSpeedUp >= 0) {
			 // Scenario 1: acceleration is needed to either maintain the speed or to go up to the top speed
  
			 // Set new vehicle acceleration
			 vehicle.setAccel(accelSpeedUp);
			 // W = [m/s2] [kg] [m/s] + [J] / [s]
			 iPower = accelSpeedUp * mass * vMS + potentialEnergyDrone / secs; // [in W]
			  
//			 if (uKPH - vKPH > SPEED_BUFFER || vKPH - uKPH < SPEED_BUFFER) {
				/*
				 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
				 * delete any of them. Needed for testing when new features are added in future. Thanks !

				 logger.log(vehicle, Level.INFO, 20_000,  
					 "Scenario 1A: Need to exert power just to maintain the speed at "
					 + Math.round(vKPH * 100.0)/100.0 + KPH
					 + "accelMotor: " + Math.round(accelMotor * 100.0)/100.0 + " m/s2  "
					 + "accelTarget: " + Math.round(accelTarget * 100.0)/100.0 + " m/s2  "
					 + "accelForcesAgainst: " + Math.round(accelForcesAgainst * 100.0)/100.0 + " m/s2."
					 );
				 */
//			 }
//			 else {
				/*
				 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
				 * delete any of them. Needed for testing when new features are added in future. Thanks !

				 logger.log(vehicle, Level.INFO, 20_000,  
					 "Scenario 1B: Need to accelerate and increase the speed from "
					 +  Math.round(uKPH * 100.0)/100.0 + KPH_
					 + "to " + Math.round(vKPH * 100.0)/100.0 + KPH_
					 + "accelMotor: " + Math.round(accelMotor * 100.0)/100.0 + " m/s2  "
					 + "accelTarget: " + Math.round(accelTarget * 100.0)/100.0 + " m/s2  "
					 + "accelForcesAgainst: " + Math.round(accelForcesAgainst * 1_0.0)/100.0 + " m/s2.");
				 */
//			 }        
  
			 // Convert the total energy [in Wh]. Need to convert from J to Wh
			 overallEnergyUsed = iPower * secs / JOULES_PER_WH ; // [in Wh]
			// Get energy [in Wh] from the battery
//			 double energyByBattery = 0;
			 // Get energy [in Wh] from the fuel
			 double energyNeededByFuel = 0;
			 
			 if (vehicle.getVehicleType() == VehicleType.DELIVERY_DRONE) {
				 // For drone, prioritize to use up fuel as power source first
				 // Get energy from the battery				 
				 energyByBattery = battery.requestEnergy(overallEnergyUsed / 1000, hrsTime) * 1000.0;
			 }
			 else {
				 // For ground vehicles
				 // Get energy from the battery
				 energyByBattery = battery.requestEnergy(overallEnergyUsed / 1000, hrsTime) * 1000.0;
			 }
			 
			 // Get energy from the fuel
			 energyNeededByFuel = overallEnergyUsed - energyByBattery;
			 
//			 overallEnergyUsed = totalEnergyNeeded;
			 
			 if (Math.round(overallEnergyUsed * 1000.0)/1000.0 == Math.round(energyByBattery * 1000.0)/1000.0) {
				 // Scenario 2A : Battery has enough juice for the acceleration
				 
				/*
				 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
				 * delete any of them. Needed for testing when new features are added in future. Thanks !
				 */

				 logger.log(vehicle, Level.INFO, 20_000,  
						 "Scenario 2A: Use on-board battery only. "
						 + "energyByBattery: " + Math.round(energyByBattery * 100.0)/100.0 + WH
//						 + "totalEnergyNeeded: " + Math.round(totalEnergyNeeded * 100.0)/100.0 + WH	
						 + "overallEnergyUsed: " + Math.round(overallEnergyUsed * 100.0)/100.0 + WH 						 
						 + "Battery: " + Math.round(battery.getCurrentEnergy() * 100.0)/100.0 + KWH); 		 

			 }
			  
			 else if (energyNeededByFuel > MIN_FUEL) {
				  // Scenario 2B and 2C: If the battery is unable to meet the needed energy requirement
				 // Need to turn on fuel cells to supply more power
				  
				 // Derive the mass of fuel needed kg = Wh / [Wh/kg]
				 fuelNeeded = energyNeededByFuel / vehicle.getFuelConv();
				 
				 // Note that if remainingFuel == -1, it's either nuclear powered or solar powered
				 if (remainingFuel == -1 || fuelNeeded <= remainingFuel) {
					// Scenario 2B: if fuelNeeded is smaller than remainingFuel, 
					// then fuel is sufficient.
					 
					/*
					 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
					 * delete any of them. Needed for testing when new features are added in future. Thanks !
					 */
					 logger.log(vehicle, Level.INFO, 20_000,  
						 "Scenario 2B: Partial battery with sufficient fuel.  " 
						 + "energyByBattery: " +  Math.round(energyByBattery * 100.0)/100.0 + WH
						 + "Battery: " + Math.round(battery.getCurrentEnergy() * 100.0)/100.0 + KWH 						
//						 + "totalEnergyNeeded: " + Math.round(totalEnergyNeeded * 100.0)/100.0 + WH
						 + "overallEnergyUsed: " + Math.round(overallEnergyUsed * 100.0)/100.0 + WH  						 
						 + "fuelNeeded: " +  Math.round(fuelNeeded * 100.0)/100.0  + KG
						 + "distanceTravelled: " +  Math.round(distanceTravelled * 100.0)/100.0  + " km.");

				 }
				 else {				
					 // Scenario 2C : fuel needed is less than available (just used up the last drop of fuel). Update fuelNeeded.
					 
					 // Limit the fuel to be used
					 fuelNeeded = remainingFuel;
					 // Calculate the new energy provided by the fuel
					 energyNeededByFuel = fuelNeeded * vehicle.getFuelConv();	
					 
					 logger.log(vehicle, Level.INFO, 20_000, "energyNeededByFuel: " 
							 +  Math.round(energyNeededByFuel * 1000.0)/1000.0 + " Wh");					 
					 
					 // Recompute overallEnergyUsed
					 overallEnergyUsed = energyNeededByFuel + energyByBattery;
							
					 // Calculate the new instantaneous power
					 // 1 Wh = 3.6 kJ 
					 // W = J / s  / [3.6 kJ / Wh]
					 // W = Wh / 3.6k
					 iPower = overallEnergyUsed / secs / JOULES_PER_WH;
//					 avePower = iPower;
					 
					 // FUTURE: will consider the on-board accessory vehicle power usage
		
					 // Previously, double iPower = accelMotor * mass * vMS + potentialEnergyDrone / secs; // [in W]
					 // iPower - potentialEnergyDrone / secs = accelMotor * mass * vMS ; 
					 // vMS = (iPower - potentialEnergyDrone / secs) / accelMotor / mass
					 
					 vMS = (iPower - potentialEnergyDrone / secs) / accelSpeedUp / mass;
					 
					 // Find the new speed   
//?					 vKPH = iPower - potentialEnergyDrone / secs / accelMotor / mass;
					 
					 // FUTURE : may need to find a way to optimize motor power usage 
					 // and slow down the vehicle to the minimal to conserve power	
					 
					 vKPH = vMS * KPH_CONV;
					 
//					 vMS = vKPH / KPH_CONV; // [in m/s^2]
 
					 accelTarget = (vMS - uMS) / secs; // [in m/s^2]
					 // Recompute the new distance it could travel
					 distanceTravelled = vKPH * hrsTime;
							 
					/*
					 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
					 * delete any of them. Needed for testing when new features are added in future. Thanks !
					 */
					 logger.log(vehicle, Level.INFO, 20_000,  
							 "Scenario 2C: Partial battery and insufficient fuel.  " 
							 + "energyByBattery: " +  Math.round(energyByBattery * 100.0)/100.0 + WH
							 + "Battery: " 			+ Math.round(battery.getCurrentEnergy() * 100.0)/100.0 + KWH
//							 + "totalEnergyNeeded: " + Math.round(totalEnergyNeeded * 100.0)/100.0 + WH
							 + "overallEnergyUsed: " + Math.round(overallEnergyUsed * 100.0)/100.0 + WH							 
							 + "fuelNeeded: " +  Math.round(fuelNeeded * 100.0)/100.0  + KG
							 + "iPower: " 			+ Math.round(iPower * 100.0)/100.0 + W							
							 + "vKPH: " 				+ Math.round(vKPH * 100.0)/100.0 + KPH   							
//							 + "navpointDist: " +  Math.round(navpointDist * 1000.0)/1000.0  + KM 
							 + "distanceTravelled: " +  Math.round(distanceTravelled * 100.0)/100.0  + KM);
					 
				 }
			 }
			 else { 
				// Scenario 2D : battery power is not sufficient and not using any fuel
				// Diagnostic : 
				/*
				 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
				 * delete any of them. Needed for testing when new features are added in future. Thanks !
				 */
				  logger.log(vehicle, Level.INFO, 20_000,  
							 "Scenario 2D: " 
							 + "energyByBattery: " +  Math.round(energyByBattery * 100.0)/100.0 + WH
							 + "Battery: " 			+ Math.round(battery.getCurrentEnergy() * 100.0)/100.0 + KWH
//							 + "totalEnergyNeeded: " + Math.round(totalEnergyNeeded * 100.0)/100.0 + WH
							 + "energyNeededByFuel: " +  Math.round(energyNeededByFuel * 100.0)/100.0 + WH
							 + "overallEnergyUsed: " + Math.round(overallEnergyUsed * 100.0)/100.0 + WH							 
							 + "fuelNeeded: " +  Math.round(fuelNeeded * 100.0)/100.0  + KG);
			 }
			 
			 double iFE = 0;

			 double iFC = 0;	
			 
			 if (distanceTravelled > 1 && energyNeededByFuel > 1) {
				 // Derive the instantaneous fuel consumption [Wh/km]
				 iFC = energyNeededByFuel / distanceTravelled;	        
				 // Set the instantaneous fuel consumption [Wh/km]
				 vehicle.setIFuelConsumption(iFC);

				 // Derive the instantaneous fuel economy [km/kg]
				 // [km/kg] = [km] / [Wh] *  [Wh/kg]
				 //  energyNeededByFuel = fuelNeeded * vehicle.getFuelConv();
				 iFE = distanceTravelled / energyNeededByFuel * vehicle.getFuelConv();	        
				 // Set the instantaneous fuel economy [km/kg]
				 vehicle.setIFuelEconomy(iFE);
			 }
 
			 /*
			 double bFC = vehicle.getBaseFuelConsumption();       
			 // Get the base fuel economy 
			 double bFE = vehicle.getBaseFuelEconomy();      	
			 */
			 // Calculate the average power for this time period [in kW]
//			 avePower = overallEnergyUsed / 1000.0 / hrsTime;
			 
			/*
			 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
			 * delete any of them. Needed for testing when new features are added in future. Thanks !
			 */
			  
			 logger.log(vehicle, Level.INFO, 20_000,  
					vehicle.getSpecName()
					  + "  mass: " 				+ Math.round(mass * 100.0)/100.0 + KG
					  + "odometer: " 			+ Math.round(vehicle.getOdometerMileage()* 1_000.0)/1_000.0 + KM
//					  + "navpointDist: " 		+ Math.round(navpointDist * 1_000.0)/1_000.0 + KM
					  + "distanceTravelled: " + Math.round(distanceTravelled * 1_000.0)/1_000.0 + KM
					 + "time: "				+ Math.round(secs * 10.0)/10.0 + " secs  "
					 + "uKPH: "				+ Math.round(uKPH * 100.0)/100.0 + KPH
					 + "vKPH: " 				+ Math.round(vKPH * 100.0)/100.0 + KPH);
			 
			 logger.log(vehicle, Level.INFO, 20_000,
					vehicle.getSpecName()	
					 + "  energyByBattery: " +  Math.round(energyByBattery * 1000.0)/1000.0 + WH
					 + "avePower: " 			+ Math.round(avePower * 1_000.0)/1_000.0 + W
					 + "Battery: " 			+ Math.round(battery.getCurrentEnergy() * 1_000.0)/1_000.0 + KWH    
//					 + "totalEnergyNeeded: " + Math.round(totalEnergyNeeded * 1000.0)/1000.0 + WH   	        				
					 + "energyNeededByFuel: " 		+ Math.round(energyNeededByFuel * 1_000.0)/1_000.0 + WH
					 + "overallEnergyUsed: " + Math.round(overallEnergyUsed * 1000.0)/1000.0 + WH   					 
					 + "fuelUsed: " 			+ Math.round(fuelNeeded * 100_000.0)/100_000.0 + KG
					 + "iPower: " 			+ Math.round(iPower * 1_000.0)/1_000.0 + W			  
					 );

			logger.log(vehicle, Level.INFO, 20_000,
					vehicle.getSpecName()					 
					 + "  angle: "				+ Math.round(angle / Math.PI * 180.0 * 10.0)/10.0 + " deg  "
					 + "totalForce: " 		+ Math.round(totalForce * 100.0)/100.0 + N    					 
					 + "fInitialF: " 		+ Math.round(fInitialFriction * 1_000.0)/1_000.0 + N
					 + "fGravity: " 			+ Math.round(fGravity * 1_000.0)/1_000.0 + N
					 + "fAeroDrag: " 		+ Math.round(fAeroDrag * 1_000.0)/1_000.0 + N
					 + "fRolling: " 			+ Math.round(fRolling * 1_000.0)/1_000.0 + N
					 + "fRoadSlope: "		+ Math.round(fRoadSlope * 1_000.0)/1_000.0 + N);
   
//			 logger.log(vehicle, Level.INFO, 20_000,
//					vehicle.getSpecName()
//					+ "  baseFE: " 			+ Math.round(bFE * 100.0)/100.0 + KM_KG  
//					+ "initFE: " 			+ Math.round(vehicle.getInitialFuelEconomy() * 100.0)/100.0 + KM_KG
//					+ "instantFE: " 		+ Math.round(iFE * 100.0)/100.0 + KM_KG
//					+ "estFE: " 			+ Math.round(vehicle.getEstimatedFuelEconomy() * 100.0)/100.0 + KM_KG
//					+ "cumFE: " 			+ Math.round(vehicle.getCumFuelEconomy() * 100.0)/100.0 + KM_KG);
			 
//			 logger.log(vehicle, Level.INFO, 20_000,
//					vehicle.getSpecName()			 
//					+ "  baseFC: " 			+ Math.round(bFC * 100.0)/100.0 + WH_KM 
//					+ "initFC: " 			+ Math.round(vehicle.getInitialFuelConsumption() * 100.0)/100.0 + WH_KM  					
//					+ "instantFC: " 		+ Math.round(iFC * 100.0)/100.0 + WH_KM
//					+ "estFC: " 			+ Math.round(vehicle.getEstimatedFuelConsumption() * 100.0)/100.0 + WH_KM  
//					+ "cumFC: " 			+ Math.round(vehicle.getCumFuelConsumption() * 100.0)/100.0 + WH_KM);
			 
			 // Cache the new value of fuelUsed	
			 if (fuelNeeded > 0 && remainingFuel != -1) {
				 logger.log(vehicle, Level.INFO, 20_000, "fuelNeeded: " 
						 +  Math.round(fuelNeeded * 1000.0)/1000.0 + " kg");
				 // Retrieve the fuel needed for the distance traveled
				 vehicle.retrieveAmountResource(fuelTypeID, fuelNeeded);
				 // Assume double amount of oxygen as fuel oxidizer
				 vehicle.retrieveAmountResource(OXYGEN_ID, RATIO_OXIDIZER_FUEL * fuelNeeded);
				 // Generate 1.75 times amount of the water from the fuel cells
				 vehicle.storeAmountResource(WATER_ID, RATIO_WATER_METHANOL * fuelNeeded);
			 }
		 }
		 
		 else if (accelSpeedUp == 0D) {
			// accelMotor < 0
			 // Scenario 2: deceleration is needed
			 
			 // Gets the deceleration using regenerative braking
			 double regenDecel = 0;
			 // Set new vehicle acceleration
			 vehicle.setAccel(0);
			 
			 iPower = 0; // (vMS + uMS)/2.0; // [in W]
				
			 /*
			  * NOTE: May comment off the logging codes below once debugging is done.
			  * But DO NOT delete any of them. Needed for testing when 
			  * new features are added in future. Thanks !
			  */ 
			 logger.log(vehicle, Level.INFO, 20_000, 
					 "Scenario 2a: No change of speed. uKPH: " 
					 +  Math.round(uKPH * 100.0)/100.0 + KPH_
					 + "vKPH: " + Math.round(vKPH * 100.0)/100.0 + KPH_
					 + "regen decel: " + Math.round(regenDecel * 100.0)/100.0 
					 + " m/s2.  "
					 + "target accel: " + Math.round(accelTarget * 100.0)/100.0 
					 + " m/s2."			
			 );
		 }
		 
		 else { 
			 // accelMotor < 0
			 // Scenario 2: deceleration is needed
			 
			 // Gets the deceleration using regenerative braking
			 double regenDecel = accelSpeedUp;
			 // Set new vehicle acceleration
			 vehicle.setAccel(regenDecel);
			 
			 iPower = - regenDecel * mass * vMS; // (vMS + uMS)/2.0; // [in W]
			 
			 avePower = 0;
			 
			 /*
			  * NOTE: May comment off the logging codes below once debugging is done.
			  * But DO NOT delete any of them. Needed for testing when 
			  * new features are added in future. Thanks !
			  */ 
			 logger.log(vehicle, Level.INFO, 20_000, 
					 "Scenario 2b: Decelerate and reduce the speed from " 
					 +  Math.round(uKPH * 100.0)/100.0 + KPH
					 + "to " + Math.round(vKPH * 100.0)/100.0 + KPH_
					 + "regen decel: " + Math.round(regenDecel * 100.0)/100.0 
					 + " m/s2.  "
					 + "target accel: " + Math.round(accelTarget * 100.0)/100.0 
					 + " m/s2."			
			 );

 
			 // Convert the energyNeeded energy from J to Wh
			 double energyNeeded = iPower * secs / JOULES_PER_WH ; // [in Wh]
			 // Get the energy stored [Wh] into the battery
			 double energyforCharging = battery.provideEnergy(energyNeeded / 1000, hrsTime) * 1000; 
			 
			 // Record this regen energy [Wh] as cache
			 regenEnergyBuffer = energyforCharging;
			 
//			 double iFE = 0;
//
//			 double iFC = 0;	
//			 
//			 if (distanceTravelled > 0 && regenEnergyBuffer > 0) {
//				 // Derive the instantaneous fuel consumption [Wh/km]
//				 iFC = regenEnergyBuffer / distanceTravelled;	        
//				 // Set the instantaneous fuel consumption [Wh/km]
//				 vehicle.setIFuelConsumption(iFC);
//
//				 // Derive the instantaneous fuel economy [in km/kg]
//				 iFE = distanceTravelled / regenEnergyBuffer * vehicle.getFuelConv();	        
//				 // Set the instantaneous fuel economy [in km/kg]
//				 vehicle.setIFuelEconomy(iFE);
//			 }
			 
			/*
			 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
			 * delete any of them. Needed for testing when new features are added in future. Thanks !

			 logger.log(vehicle, Level.INFO, 20_000,  
			 vehicle.getSpecName()
			 		+ "  mass: " 				+ Math.round(mass * 100.0)/100.0 + KG
					+ "odometer: " 			+ Math.round(vehicle.getOdometerMileage()* 1_000.0)/1_000.0 + KM
//					+ "navpointDist: " 		+ Math.round(navpointDist * 1_000.0)/1_000.0 + KM
					+ "distanceTravelled: " + Math.round(distanceTravelled * 1_000.0)/1_000.0 + KM
					 + "time: "				+ Math.round(secs * 1_000.0)/1_000.0 + " secs  "
					 + "uKPH: "				+ Math.round(uKPH * 100.0)/100.0 + KPH
					 + "vKPH: " 				+ Math.round(vKPH * 100.0)/100.0 + KPH);     

			 logger.log(vehicle, Level.INFO, 20_000,
						vehicle.getSpecName()				 
					 + "  Battery: " 			+ Math.round(battery.getCurrentEnergy() * 1_000.0)/1_000.0 + KWH  
					 + "energyNeeded: " 		+ Math.round(energyNeeded * 100.0)/100.0 + WH
					 + "regenEnergyBuffer: " + Math.round(regenEnergyBuffer * 1_000.0)/1_000.0 + WH
					 + "totalForce: " 		+ Math.round(totalForce * 10_000.0)/10_000.0 + N       	        
					 + "iPower: " 			+ Math.round(iPower * 1_000.0)/1_000.0 + W
			 );
			 */
		 }
		 
		 // Set new vehicle speed
		 vehicle.setSpeed(vKPH);
		 // overallEnergyUsed [in Wh], not in kWh
		 // Calculate the average road load power in kW = Wh / s * 3.6
//		 double averageRoadLoadPower = overallEnergyUsed / secs * 3.6;
//		 double averageRoadLoadPower = accelMotor * mass * vMS / 1000;
	 
		 if (vKPH > 1 && avePower > 1) {
			 // update average road load speed
			 vehicle.setAverageRoadLoadSpeed((int)Math.round(vKPH));
			 // update average road load power
			 // avePower is in W, not kW
			 vehicle.setAverageRoadLoadPower((int)Math.round(avePower/1000));
		 }

		 // Determine new position
		 vehicle.setCoordinates(vehicle.getCoordinates().getNewLocation(vehicle.getDirection(), distanceTravelled)); 
		 
		 // Apply regen
//		 totalEnergyUsed = (0.1 + vehicle.getVehicleSpec().getOtherEnergyUsagePercent() / 100) 
//				 * (overallEnergyUsed + regenEnergyBuffer);

		/*
		 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
		 * delete any of them. Needed for testing when new features are added in future. Thanks !

		 logger.info(vehicle, 20_000L, "dist: " + Math.round(distanceTravelled * 100.0)/100.0 
				 + "  totalEnergyUsed: "  + Math.round(totalEnergyUsed* 100.0)/100.0
				 + "  totalEnergyUsed / dist: "  + Math.round(totalEnergyUsed/distanceTravelled * 100.0)/100.0
				 + "  averageRoadLoadPower: " + Math.round(averageRoadLoadPower * 100.0)/100.0);
		 */
		 
		 // Add distance traveled to vehicle's odometer.
		 vehicle.addOdometerMileage(distanceTravelled, energyByBattery, fuelNeeded);
		 // Track maintenance due to distance traveled.
		 vehicle.addDistanceLastMaintenance(distanceTravelled);
		 // Derive the instantaneous fuel economy [in km/kg]
		 
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
		 // The amount of "fuel" covered by the energy in the battery 
		 double batteryFuel = vehicle.getController().getBattery().getCurrentEnergy() / vehicle.getFuelConv();
		 
		 double amountFuel = tripDistance * 
				 	.5 * (1.0 / fuelEconomy + vehicle.getEstimatedFuelConsumption() / vehicle.getFuelConv())
				 	- batteryFuel;

		 double factor = 1;
		 if (useMargin) {
			 if (tripDistance < 100) {
				 // Note: use formula below to add more extra fuel for short travel distance on top of the fuel margin
				 // in case of getting stranded locally
				 factor = - tripDistance / 50.0 + 3;
			 }	
			 factor *= Vehicle.getFuelRangeErrorMargin();
			 amountFuel *= factor;
			 
		 }

		 return amountFuel;
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
	 * Charges up the battery in no time. 
	 */
	public void topUpBatteryEnergy() {
		battery.topUpBatteryEnergy();
	}
		
	/** 
	 * Charges up the battery in no time. 
	 */
	public Battery getBattery() {
		return battery;
	}

 }