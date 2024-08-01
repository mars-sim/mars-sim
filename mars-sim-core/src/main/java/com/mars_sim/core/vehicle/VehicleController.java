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
import com.mars_sim.tools.util.RandomUtil;
 
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
 
	 /** The oxygen as fuel oxidizer for the fuel cells. */
	 public static final int OXYGEN_ID = ResourceUtil.oxygenID;
	 /** The water as the by-product of the fuel cells */
	 public static final int WATER_ID = ResourceUtil.waterID;
	
	 /** Comparison to indicate a small but non-zero amount of fuel (methane) in kg that can still work on the fuel cell to propel the engine. */
	 private static final double LEAST_AMOUNT = GroundVehicle.LEAST_AMOUNT;
	 /** The ratio of the amount of oxidizer to fuel. */
	 public static final double RATIO_OXIDIZER_FUEL = 1.5;
	 /** The ratio of water produced for every methanol consumed. */
	 private static final double RATIO_WATER_METHANOL = 1.125;
	 /** The ratio of water produced for every methane consumed. */
//	 private static final double RATIO_WATER_METHANE = 2.25;
	 /** The factor for estimating the adjusted fuel economy [km/kg]. */
	 public static final double FUEL_ECONOMY_FACTOR = .85;
	 /** The factor for estimating the adjusted fuel consumption [Wh/km]. */
	 public static final double FUEL_CONSUMPTION_FACTOR = 1.2; 
	 /** Mars surface gravity is 3.72 m/s2. */
	 public static final double GRAVITY = 3.72;
	 /** Conversion factor : 1 Wh = 3.6 kilo Joules */
	 private static final double JOULES_PER_WH = 3_600.0;
	 /** Conversion factor : 1 m/s = 3.6 km/h (or kph) */
	 public static final double KPH_CONV = 3.6;
		
	 /** Speed buffer in kph. */
	 public final double SPEED_BUFFER = .01;
	 /** The outside average air density. */
	 public double airDensity = 0.02;
	 
	 private static final String SEC__ = " secs  ";
	 private static final String M_S2__ = " m/s2  ";
	 private static final String M_S__ = " m/s  ";
	 private static final String KG__ = " kg  ";
	 private static final String WH__ = " Wh  ";
	 private static final String KWH__ = " kWh  ";
	 private static final String KPH_ = " kph ";
	 private static final String KPH__ = " kph  "; 
	 private static final String KW__ = " kW  ";
	 private static final String W__ = " W  ";
	 private static final String KM__ = " km  ";
	 private static final String N__ = " N  ";
	 private static final String J__ = " J  ";
	 
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
	  * @param remainingFuel; if -1, it doesn't require methanol fuel to move
	  * @param remainingOxidizer; if -1, it doesn't require methanol fuel to move
	  * @return remainingHrs
	  */
	 public double consumeFuelEnergy(double hrsTime, double distToCover, double vKPH, double remainingFuel, double remainingOxidizer) {
		 
		 /** Cache the energy recovered from regen braking in kWh. */ 
//		 double regenEnergyBuffer = 0;	
		 
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
		 double fuelNeeded = 0;
		 
		 double accelSpeedUp = 0;
		 
		 double powerConstantSpeed  = 0;
		 
		 double energyByBattery = 0;	 
		 // [in Wh], not in kWh
		 double overallEnergyUsed = 0;	 
		 // In [W], not [kW]
		 double avePower = 0;
		 
		 double aveForce = 0;
		 // Weight = mass * g
		 double mg = mass * GRAVITY;
		 
		 double potentialEnergyDrone = 0;
		  
		 double powerThrustDrone = 0;
			
		 airDensity = sim.getWeather().getAirDensity(vehicle.getCoordinates());
			
		 if (vehicle instanceof Rover) {
			 // Important for Ground rover
			 double angle = vehicle.getTerrainGrade();
			 // Assume road rolling resistance coeff of 0.075 on roads with pebbles/potholes on Mars (typically 0.015 on paved roads on Earth)
			 // See https://x-engineer.org/rolling-resistance/
			 // The ratio between distance and wheel radius is the rolling resistance coefficient
			 fRolling = 0.075 * mg * Math.cos(angle);
			 // https://x-engineer.org/road-slope-gradient-force/
			 fRoadSlope = mg * Math.sin(angle);
			 
			 fInitialFriction = 7.0 / (0.5 + averageSpeed);  // [in N]
			 
			 double frontalArea = vehicle.getWidth() * vehicle.getWidth() * .9;
			 // https://x-engineer.org/aerodynamic-drag
			 // Note : Aerodynamic drag force = 0.5 * air drag coeff * air density * vehicle frontal area * vehicle speed ^2 
			 double fAeroDrag = 0.5 * 0.4 * airDensity * frontalArea * averageSpeedSQ;
			 // Gets the summation of all the forces acting against the forward motion of the vehicle
			 double totalForce = fInitialFriction + fAeroDrag + fGravity + fRolling + fRoadSlope;
			 
			 // if totalForce is +ve, then vehicle must generate that much force to overcome it to propel forward
	 		 // if totalForce is -ve, then vehicle may use regen mode to absorb the force.
	 
			 logger.log(vehicle, Level.INFO, 20_000,  
					 "totalForce: " + Math.round(totalForce * 1000.0)/1000.0 + N__
					 + "fInitialFriction: " + Math.round(fInitialFriction * 1000.0)/1000.0 + N__
					 + "fAeroDrag: " + Math.round(fAeroDrag * 1000.0)/1000.0 + N__
					 + "fGravity: " + Math.round(fGravity * 1000.0)/1000.0 + N__
					 + "fRolling: " + Math.round(fRolling * 1000.0)/1000.0 + N__				 
					 + "angle: " + Math.round(angle * 1000.0)/1000.0 + " deg  "	 
					 + "fRoadSlope: " + Math.round(fRoadSlope * 1000.0)/1000.0 + N__);
			 
			 // Assume constant speed, P = F_T * v / η
			 // η, overall efficiency in transmission, normally ranging 0.85 (low gear) - 0.9 (direct drive)
			 // F_T, total forces acting on the car
			 // e.g. P = ((250 N) + (400 N)) (90 km/h) (1000 m/km) (1/3600 h/s) / 0.85 = 1.9118 kW
			 
			 double overallEfficiency = 0.85;
			 
			 powerConstantSpeed = totalForce * vMS / overallEfficiency;

		 }
		 
		 else if (vehicle instanceof Drone drone) {
			 // For drones, it needs energy to ascend into the air and hover in the air
			 // Note: Refine this equation for drones			 
			 // in km
			 double currentHoveringHeight = drone.getHoveringHeight();

			 double fullMass = 777.7; 
			 // weight is mg
			 double weight = fullMass * VehicleController.GRAVITY;
			 
//			 double ascentHeight = 1000 * Flyer.ELEVATION_ABOVE_GROUND / 50;
//			 // Gain in potential energy
//			 double gainPotentialEnergy = weight * ascentHeight;
//			
//			 potentialEnergyDrone = potentialEnergyDrone + gainPotentialEnergy;	
			 //  g/m3 -> kg /m3
			 double airDensity = 14.76 / 1000; 
			 // 1 m/s = 3.6 km/h (or kph). KPH_CONV = 3.6;
//			 double vMS = 0; //60 /  VehicleController.KPH_CONV;

			 // Assume a constant voltage
			 double voltage = Battery.DRONE_VOLTAGE;	 
			 // For now, assume the propeller induced velocity is linearly proportional to the voltage of the battery 
			 double vPropeller = voltage * 60;	 
		
			 double efficiencyMotor = 0.9;
			 
			 // Assume the total radius of the four propellers span the width of the drone
			 double width = vehicle.getWidth();
			 
			 double radiusPropeller = width / 8;
		    	
			 double radiusPropellerSquare = radiusPropeller * radiusPropeller;	
			 
			 double thrustForceTotal = 0;
			 
			 double thrustToWeightRatio1 = 0;
			 
			 double gainPotentialEnergy = 0;
			 
			 double lostPotentialEnergy = 0;
			 
			 /*
			  * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
			  * delete any of them. Needed for testing when new features are added in future. Thanks !
			  */
			 // Vertical Thrust (Fz) is a function of the propeller’s thrust coefficient (Ct), 
			 // its diameter (D), and the air density (ρ). The equation is:
			 // T = 0.5 * ρ * Ct * D^2 * ω^2
			 
			 // ω is the propeller’s angular velocity.
			 
			 // Horizontal Thrust (Fx): The horizontal thrust force (Fx) is influenced by the 
			 // propeller’s thrust vectoring, tilt angle (θ), and the air density (ρ). 
			 // Fx = Fz * tan(θ)

			 // Combined Thrust: The total thrust force (F) is the vector sum of Fz and Fx. 
			 // For a quadcopter, this equation applies to each propeller:
			 // F = √(Fz^2 + Fx^2)
			 	 
			 // T = 2 * density * pi * r^2 * (V0 + v1) * v1
			 
			 // For our quadcopter design, we used Momentum Theory to relate thrust and power.
			 
			 // The Momentum Theory-based equation: T = ρ * A * v^2 * (1 + (v_w / v)^2))
			 // 3 stages of power:
			 // 1. Electrical power from fuel/battery
			 // 2. Mechanical power from motors
			 // 3. Lifting power by propellers

			 
			  // FUTURE : How to simulate controlled descent to land at the destination ?
			  // Also need to account for the use of fuel or battery's power to ascend and descend 
			 
			 if (uMS <= vMS) {
				 // If speeding up	
				 
				 double ascentHeight = 0;
				 
				 // Case A : In the air
				 if (vMS == 0.0) {
					 // Case A1 : Hovering	
					 vMS = 0;
					 
					 ascentHeight = 0;
					 // Gain in potential energy
					 gainPotentialEnergy = 0;
					 // Do NOT ascent anymore. Hover at the this height
					 potentialEnergyDrone = weight * currentHoveringHeight;
					
					 // If using Volume-based approach,
					 // P = (m * g * h) / (η * ρ * v)
					 // η efficiency of the motor and propeller (typically 0.7-0.9) 
					 // ρ = air density (kg/m³) 
					 // v = air speed (m/s)
					 // Power to maintain the height of the drone :
					 // Previously, powerDrone = mg * currentHoveringHeight / 0.9 / airDensity / vMS;
					 
					 thrustForceTotal = 2 * airDensity * Math.PI * radiusPropellerSquare * (vPropeller + vMS) * vPropeller;
					 // Double check with the ratio. Need to be at least 2:1
					 thrustToWeightRatio1 = thrustForceTotal / weight;					
					 // Zero gain of potential energy of the drone
					 powerThrustDrone = thrustForceTotal * voltage / efficiencyMotor + gainPotentialEnergy / secs;
						
					 /*
					  * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
					  * delete any of them. Needed for testing when new features are added in future. Thanks !
					  */
					 // Hover airflow velocity V1 = v1 = sqrt (CT /2)
					 // v1 = Math.sqrt(CT/2);	
					 // Hover thrust coefficient: CT = 2 * v1^2
					 // Hover thrust: T = 2 * density * pi * r^2 * v1^2
					 // thrustForceT =  2 * airDensity * Math.PI * radiusPropeller * radiusPropeller * v1 * v1;
					 // liftToDragRatio = liftForceL / dragForceD;				 
					 // thrustForceT = weightForceW / liftToDragRatio;
					
					 logger.log(vehicle, Level.INFO, 20_000, "uMS <= vMS Case A1: Hovering - " 
							 + "distance: " + Math.round(distanceTravelled * 10.0)/10.0 + KM__
							 + "powerDrone: " + Math.round(powerThrustDrone * 1000.0)/1000.0 + W__
							 + "thrust: " + Math.round(thrustForceTotal * 1000.0)/1000.0 + N__
							 + "PE: " + Math.round(potentialEnergyDrone * 1000.0)/1000.0 + J__
							 + "gainPE: " + Math.round(gainPotentialEnergy * 1000.0)/1000.0 + J__
							 + "lostPE: " + Math.round(lostPotentialEnergy * 1000.0)/1000.0 + J__				 
							 + "height: " + Math.round(currentHoveringHeight * 10.0)/10.0 + KM__);
				 }
				 
				 else if (currentHoveringHeight >= Flyer.ELEVATION_ABOVE_GROUND) {
					// Case A2 : Hovering at same height but tilted to move forward
 
					 ascentHeight = 0;
					 // Gain in potential energy
					 gainPotentialEnergy = 0;
					 // Do NOT ascent anymore. Hover at the this height
					 potentialEnergyDrone = weight * currentHoveringHeight;
					
					 // If using Volume-based approach,
					 // P = (m * g * h) / (η * ρ * v)
					 // η efficiency of the motor and propeller (typically 0.7-0.9) 
					 // ρ = air density (kg/m³) 
					 // v = air speed (m/s)
					 // Power to maintain the height of the drone :
					 // Previously, powerDrone = mg * currentHoveringHeight / 0.9 / airDensity / vMS;
					 
					 thrustForceTotal = 2 * airDensity * Math.PI * radiusPropellerSquare * (vPropeller + vMS) * vPropeller;
					 // Double check with the ratio. Need to be at least 2:1
					 thrustToWeightRatio1 = thrustForceTotal / weight;					
					 // Zero gain of potential energy of the drone
					 powerThrustDrone = thrustForceTotal * voltage / efficiencyMotor + gainPotentialEnergy / secs;
					
					 logger.log(vehicle, Level.INFO, 20_000, "uMS <= vMS Case A2: Hovering at same height. Tilted to move forward - " 
							 + "distance: " + Math.round(distanceTravelled * 10.0)/10.0 + KM__
							 + "powerDrone: " + Math.round(powerThrustDrone * 1000.0)/1000.0 + W__
							 + "thrust: " + Math.round(thrustForceTotal * 1000.0)/1000.0 + N__
							 + "PE: " + Math.round(potentialEnergyDrone * 1000.0)/1000.0 + J__
							 + "gainPE: " + Math.round(gainPotentialEnergy * 1000.0)/1000.0 + J__
							 + "lostPE: " + Math.round(lostPotentialEnergy * 1000.0)/1000.0 + J__				 
							 + "height: " + Math.round(currentHoveringHeight * 10.0)/10.0 + KM__);
				 }
				 else {
					 // Case A3 : Tilted forward and lifting up (or ascent)
				 
						ascentHeight = 1000 * Flyer.ELEVATION_ABOVE_GROUND / 50;
						 // Gain in potential energy
						gainPotentialEnergy = mg * ascentHeight;
						
						potentialEnergyDrone = 0 + gainPotentialEnergy;	
				    	
				    	// 1 m/s = 3.6 km/h (or kph). KPH_CONV = 3.6;
				    	vMS = 60 /  VehicleController.KPH_CONV;
						
						double alpha1 = Math.PI / 6;
						
						double alpha2 = Math.PI / 7;
						// 1000 RPM ~ 10.47 rad/s
						double radPerSec = 10;
						
						double vAirFlow =  (vMS * Math.sin(alpha2) + vPropeller * Math.cos(alpha1-alpha2)) * radiusPropeller * radPerSec; // vPropeller + vMS;
						
						thrustForceTotal = 2 * airDensity * Math.PI * radiusPropellerSquare * vAirFlow * vPropeller;
						// Double check with the ratio. Need to be at least 2:1
						thrustToWeightRatio1 = thrustForceTotal / weight;

						// The gain of potential energy of the drone require extra the power drain on the drone's fuel and battery system
						powerThrustDrone = thrustForceTotal * voltage / efficiencyMotor + gainPotentialEnergy / secs;
						
						 // Drone will hover over at around ELEVATION_ABOVE_GROUND km and no more
						 if (currentHoveringHeight <= Flyer.ELEVATION_ABOVE_GROUND) {
							 // Assume the height gained is the same as distanceTravelled
							 currentHoveringHeight = currentHoveringHeight + ascentHeight;
						 }
					 
						 logger.log(vehicle, Level.INFO, 20_000, "uMS <= vMS Case A3: Tilted forward and lifting up (or ascent) - " 
								 + "distance: " + Math.round(distanceTravelled * 10.0)/10.0 + KM__
								 + "powerDrone: " + Math.round(powerThrustDrone * 1000.0)/1000.0 + W__
								 + "thrust: " + Math.round(thrustForceTotal * 1000.0)/1000.0 + N__
								 + "T2W: " + Math.round(thrustToWeightRatio1 * 100.0)/100.0 + "  " 
								 + "PE: " + Math.round(potentialEnergyDrone * 1000.0)/1000.0 + J__
								 + "gainPE: " + Math.round(gainPotentialEnergy * 1000.0)/1000.0 + J__
								 + "lostPE: " + Math.round(lostPotentialEnergy * 1000.0)/1000.0 + J__
								 + "ascentHeight: " + Math.round(ascentHeight * 10.0)/10.0 + KM__
								 + "height: " + Math.round(currentHoveringHeight * 10.0)/10.0 + KM__);
				 }
			 }
 
			 else {
				 // uMS > vMS - Slowing down
				 
				 // Case B1 : During controlled descent / going down
				 if (currentHoveringHeight <= Flyer.ELEVATION_ABOVE_GROUND / 50) {
					 // Case B1 : landed
					 double descentHeight = currentHoveringHeight;
					 // Set currentHoveringHeight to zero
					 currentHoveringHeight = 0;
					 // Do NOT ascent anymore
					 potentialEnergyDrone = 0;
					 
					 // Landing airflow velocity V1 = v1 - abs(V0)
//							 double landingVel = vMS - Math.abs(uMS);					 
					 // Landing thrust coefficient: CT = −2(V0_bar + v1_bar)．v1_bar
					 // Landing induced velocity: v1_bar = −v0_bar / 2 - sqrt((v0_bar/2)^2 - CT/2))
					 // Landing thrust T = - 2 * density * pi * r^2 * ( V0 + v1) * v1

					 lostPotentialEnergy = mg * descentHeight;
					 
					 potentialEnergyDrone = potentialEnergyDrone - lostPotentialEnergy;

					 if (potentialEnergyDrone > 0 || potentialEnergyDrone < 0)
						 logger.severe(vehicle, 0, "potentialEnergyDrone: " + Math.round(potentialEnergyDrone * 10.0)/10.0 + J__);

					 double REDUCTION_FACTOR = .25;
						
					 thrustForceTotal = REDUCTION_FACTOR * 2 * airDensity * Math.PI * radiusPropellerSquare * (vPropeller + vMS) * vPropeller;
						
					 // Double check with the ratio. Need to be at least 2:1
					 thrustToWeightRatio1 = thrustForceTotal / weight;

					 // the gain of potential energy of the drone require extra the power drain on the drone's fuel and battery system
					 powerThrustDrone = thrustForceTotal * voltage / efficiencyMotor + gainPotentialEnergy / secs;
					 
					 logger.log(vehicle, Level.INFO, 20_000, "uMS > vMS Case B1: Controlled descent/landing - " 
							 + "distance: " + Math.round(distanceTravelled * 10.0)/10.0 + KM__
							 + "powerDrone: " + Math.round(powerThrustDrone * 1000.0)/1000.0 + W__
							 + "thrust: " + Math.round(thrustForceTotal * 1000.0)/1000.0 + N__
							 + "T2W: " + Math.round(thrustToWeightRatio1 * 100.0)/100.0 + "  " 
							 + "PE: " + Math.round(potentialEnergyDrone * 1000.0)/1000.0 + J__
							 + "gainPE: " + Math.round(gainPotentialEnergy * 1000.0)/1000.0 + J__
							 + "lostPE: " + Math.round(lostPotentialEnergy * 1000.0)/1000.0 + J__				 
							 + "height: " + Math.round(currentHoveringHeight * 10.0)/10.0 + KM__);
				 }
				 else {
					// Case B2 : descending or preparing for landing
					// Assume using about 25% of energy gain in potential energy to maintain optimal descent,
					// avoid instability and perform a controlled descent
					   
					// See detail strategies on https://aviation.stackexchange.com/questions/64055/how-much-energy-is-wasted-in-an-aeroplanes-descent

					/*
					 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
					 * delete any of them. Needed for testing when new features are added in future. Thanks !
					 */
		 			

					 // Landing airflow velocity V1 = v1 - abs(V0)
//							 double landingVel = vMS - Math.abs(uMS);					 
					 // Landing thrust coefficient: CT = −2(V0_bar + v1_bar)．v1_bar
					 // Landing induced velocity: v1_bar = −v0_bar / 2 - sqrt((v0_bar/2)^2 - CT/2))
					 // Landing thrust T = - 2 * density * pi * r^2 * ( V0 + v1) * v1
					 
					 double descentHeight = Flyer.ELEVATION_ABOVE_GROUND / 25;
		        		
					 lostPotentialEnergy = mg * descentHeight;
					 
					 potentialEnergyDrone = potentialEnergyDrone - lostPotentialEnergy;

					 currentHoveringHeight = currentHoveringHeight - descentHeight;

					 double REDUCTION_FACTOR = .5;
						
					 thrustForceTotal = REDUCTION_FACTOR * 2 * airDensity * Math.PI * radiusPropellerSquare * (vPropeller + vMS) * vPropeller;
						
					 // Double check with the ratio. Need to be at least 2:1
					 thrustToWeightRatio1 = thrustForceTotal / weight;

					 // the gain of potential energy of the drone require extra the power drain on the drone's fuel and battery system
					 powerThrustDrone = thrustForceTotal * voltage / efficiencyMotor + gainPotentialEnergy / secs;
	 
					 logger.log(vehicle, Level.INFO, 20_000, "uMS > vMS Case B2: Preparing to descent - " 
							 + "distance: " + Math.round(distanceTravelled * 10.0)/10.0 + KM__
							 + "powerDrone: " + Math.round(powerThrustDrone * 1000.0)/1000.0 + W__
							 + "thrust: " + Math.round(thrustForceTotal * 1000.0)/1000.0 + N__
							 + "T2W: " + Math.round(thrustToWeightRatio1 * 100.0)/100.0 + "  " 
							 + "PE: " + Math.round(potentialEnergyDrone * 1000.0)/1000.0 + J__
							 + "gainPE: " + Math.round(gainPotentialEnergy * 1000.0)/1000.0 + J__
							 + "lostPE: " + Math.round(lostPotentialEnergy * 1000.0)/1000.0 + J__
							 + "descentHeight: " + Math.round(descentHeight * 10.0)/10.0 + KM__
							 + "height: " + Math.round(currentHoveringHeight * 10.0)/10.0 + KM__);
				 }
			 }
		 
			 drone.setHoveringHeight(currentHoveringHeight);
		 }	 

		// Motor torque vs. power and rpm 
		 // T = P / (2 * π * n_rps) = 0.159 P / n_rps = P / (2π(n_rpm/60)) = 9.55 P / n_rpm
		 // The moment delivered by the motor in the car above with the engine running at speed 1500 rpm can be calculated as
		 // T = 9.55 (19118 W) / (1500 rpm) = 121 Nm
	 
 		 // Gets the acceleration of the motor
		 accelSpeedUp = (vMS - uMS) / secs;		 
		 // Note: if accelSpeedUp is +ve, speed up
		 //       if accelSpeedUp is -ve, powerSpeedUp is -ve
		 //  	  there's a chance regen can occur
		 
		 double powerSpeedUp = mass * accelSpeedUp * vMS;
		 
		 fuelNeeded = 0;
		 
		 energyByBattery = 0;	 
		 // [in Wh], not in kWh
		 overallEnergyUsed = 0;
		 
		 // In [W], not [kW]
		 avePower = powerConstantSpeed + powerSpeedUp + powerThrustDrone;
		 
		 aveForce = avePower / vMS;
		 
		 logger.log(vehicle, Level.INFO, 20_000,  
				 "aveForce: " + Math.round(aveForce * 1000.0)/1000.0 + N__
				 + "avePower: " + Math.round(avePower * 1000.0)/1000.0 + W__
				 + "powerConstantSpeed: " + Math.round(powerConstantSpeed * 1000.0)/1000.0 + W__
				 + "powerSpeedUp: " + Math.round(powerSpeedUp * 1000.0)/1000.0 + W__
				 + "powerDrone: " + Math.round(powerThrustDrone * 1000.0)/1000.0 + W__
				 + "accelSpeedUp: " + Math.round(accelSpeedUp * 1000.0)/1000.0 + M_S2__);	
		 
		 
		 if (avePower <= 0) {
			 // Scenario 0: regen mode
			 // Apply Regenerative Braking
			 double regen[] = startRegenMode(avePower, aveForce, secs, uMS, vMS, mass); 
	 
			 // Get the new avePower
			 double brakingPower = regen[0];
			 avePower = avePower - brakingPower;
			 
			 // Get the new vKPH
//			 double newVKPH = regen[1];
//			 vKPH = newVKPH;
			 
			 // Note: need to determine if new vKPH can be derived this way
			 
			 // Recompute the new distance it could travel
//			 distanceTravelled = newVKPH * hrsTime;
			 
			 logger.log(vehicle, Level.INFO, 20_000, "Scenario 0: regen mode - "
					 + "avePower: " + Math.round(aveForce * 1000.0)/1000.0 + N__
					 + "brakingPower: " + Math.round(brakingPower * 1000.0)/1000.0 + N__
					 + "seconds: " 				+ Math.round(secs * 100.0)/100.0 + SEC__
					 + "  uKPH: " + Math.round(uKPH * 100.0)/100.0 + M_S__					 
					 + "  vKPH: " + Math.round(vKPH * 100.0)/100.0 + M_S__
					 + "distanceTravelled: " + Math.round(distanceTravelled * 1000.0)/1000.0 + KM__
					 );
		 }
		 
		 else if (accelSpeedUp >= 0) {
			 // Scenario 1: acceleration is needed to either maintain the speed or to go up to the top speed
  
			 // Set new vehicle acceleration
			 vehicle.setAccel(accelSpeedUp);

			 // Convert the total energy [in Wh]. Need to convert from J to Wh
			 overallEnergyUsed = avePower * secs / JOULES_PER_WH; // [in Wh]
			 
			 int rand = RandomUtil.getRandomInt(4);
			 
			 if (rand == 0) {
				 // For now, set 20% of chance to use battery power only
				 remainingFuel = 0;
			 }
			 
			 // Get energy [in Wh] from the fuel
			 double energyNeededByFuel = 0;
		 
			 // Scenario 1: try using fuel first to fulfill the energy expenditure 

			 if (remainingFuel == -1) {
				 // Scenario 1A1: (Future) nuclear powered or solar powered

				 fuelNeeded = 0;
				 energyNeededByFuel = 0;
				 energyByBattery = overallEnergyUsed;
				 
				 // Future: Will need to continue to model how nuclear-powered engine would work 
				 
				 logger.log(vehicle, Level.INFO, 20_000,  "Scenario 1A1: accelMotor < 0. Requires no methanol fuel.  " 
						 + "energyByBattery: " + Math.round(energyByBattery * 100.0)/100.0 + WH__				
						 + "overallEnergyUsed: " + Math.round(overallEnergyUsed * 100.0)/100.0 + WH__
						 + "seconds: " + Math.round(secs * 100.0)/100.0 + SEC__);	
			 }
			 
			 else if (remainingFuel > 0 || fuelNeeded <= remainingFuel) {
				 // Scenario 1A2: fuel can fulfill all energy expenditure 

				 // if fuelNeeded is smaller than remainingFuel, then fuel is sufficient.
				 
				 // Get energy [in Wh] from the fuel
				 energyNeededByFuel = overallEnergyUsed;
				 		 
				 // Derive the mass of fuel needed kg = Wh / [Wh/kg]
				 fuelNeeded = energyNeededByFuel / vehicle.getFuelConv();
				 
				/*
				 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
				 * delete any of them. Needed for testing when new features are added in future. Thanks !
				 */
				 logger.log(vehicle, Level.INFO, 20_000,  
						 "Scenario 1A2: accelMotor > 0. Enough fuel.  " 
								 + "accelSpeedUp: " + Math.round(accelSpeedUp * 100.0)/100.0 + M_S2__
								 + "avePower: " + Math.round(avePower * 100.0)/100.0 + N__
								 + "energyNeededByFuel: " + Math.round(energyNeededByFuel * 100.0)/100.0 + WH__
								 + "fuelNeeded: " +  Math.round(fuelNeeded * 100.0)/100.0  + KG__					
								 + "overallEnergyUsed: " + Math.round(overallEnergyUsed * 100.0)/100.0 + WH__
								 + "seconds: " + Math.round(secs * 100.0)/100.0 + SEC__);	
			 }
			 
			 else {			
				 // Scenario 1B: fuel can fulfill some energy expenditure but not all

				 // Limit the fuel to be used
				 fuelNeeded = remainingFuel;
				 
				 // Calculate the new energy provided by the fuel [in Wh]
				 energyNeededByFuel = fuelNeeded * vehicle.getFuelConv();	
				 	 
				 // Calculate needed energy from battery [in Wh] 
				 energyByBattery = overallEnergyUsed - energyNeededByFuel;
				 
				 // Calculate energy that can be delivered by battery 
				 double energySuppliedByBattery = 0;
		 
				 // Test to see how much can be drawn from the battery
				 if (vehicle.getVehicleType() == VehicleType.DELIVERY_DRONE) {
					 // For drone, prioritize to use up fuel as power source first
					 // Get energy from the battery [in Wh]			  
					 energySuppliedByBattery = battery.requestEnergy(energyByBattery / 1000, hrsTime) * 1000;
				 }
				 else {
					 // For ground vehicles
					 // Get energy from the battery [in Wh]
					 energySuppliedByBattery = battery.requestEnergy(energyByBattery / 1000, hrsTime) * 1000;
				 }
				 
				 double batteryEnergyDeficit = energyByBattery - energySuppliedByBattery;
				 
				 if (remainingFuel <= LEAST_AMOUNT) {
					 // Scenario 1B1: Ran out of fuel. Need battery to provide for the rest
					 
					 // Recalculate the new ave power W
					 // W = Wh / s * 3600 J / Wh
					 avePower = energySuppliedByBattery / secs * JOULES_PER_WH;
					 
					 // Recalculate the new overall energy expenditure [in Wh]
					 overallEnergyUsed = energySuppliedByBattery;
					 
					 // Recalculate the new speed 
					 // FUTURE: will consider the on-board accessory vehicle power usage
					 // m/s = W / (kg * m/s2)
					 vMS = avePower / aveForce;
					 	 
					 // FUTURE : may need to find a way to optimize motor power usage 
					 // and slow down the vehicle to the minimal to conserve power	
					 
					 vKPH = vMS * KPH_CONV;
					 
					 // Find new acceleration
					 accelSpeedUp = (vMS - uMS) / secs; // [in m/s^2]
					 
					// Set new vehicle acceleration
					 vehicle.setAccel(accelSpeedUp);
					 
					 // Recompute the new distance it could travel
					 distanceTravelled = vKPH * hrsTime;
							 
					 /*
					  * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
					  * delete any of them. Needed for testing when new features are added in future. Thanks !
					  */			 
					logger.log(vehicle, Level.INFO, 20_000,  
								 "Scenario 1B1: accelMotor < 0. Out of fuel. Use only battery.  " 
								 + "energyNeededByFuel: " + Math.round(energyNeededByFuel * 100.0)/100.0 + WH__
								 + "fuelNeeded: " +  Math.round(fuelNeeded * 100.0)/100.0  + KG__					
								 + "energyByBattery: " +  Math.round(energyByBattery * 100.0)/100.0 + WH__
								 + "Battery: " 			+ Math.round(battery.getCurrentEnergy() * 100.0)/100.0 + KWH__	
								 + "overallEnergyUsed: " + Math.round(overallEnergyUsed * 100.0)/100.0 + WH__							 
								 + "avePower: " 			+ Math.round(avePower * 100.0)/100.0 + W__							
								 + "vKPH: " 				+ Math.round(vKPH * 100.0)/100.0 + KPH_   	
								 + "seconds: " 				+ Math.round(secs * 100.0)/100.0 + " secs  "
								 + "distanceTravelled: " +  Math.round(distanceTravelled * 100.0)/100.0  + KM__);
					
					 // Equate energyByBattery to energySuppliedByBattery in order to add to odometer easily
					 energyByBattery = energySuppliedByBattery;
				 }	
				 
				 else if (batteryEnergyDeficit <= 0) {
					 // Energy expenditure is met. It's done.
					 // Scenario 1B1: fuel can fulfill some energy expenditure but not all. Battery provides the rest
					 
					 // Recalculate the new ave power W
					 // W = Wh / s * 3600 J / Wh
					 avePower = energySuppliedByBattery / secs * JOULES_PER_WH;	 
					 // Recalculate the new overall energy expenditure [in Wh]
					 overallEnergyUsed = energySuppliedByBattery + energyNeededByFuel;
					 // Recompute the new distance it could travel
					 distanceTravelled = vKPH * hrsTime;
					 
					 /*
						 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
						 * delete any of them. Needed for testing when new features are added in future. Thanks !
						 */
					logger.log(vehicle, Level.INFO, 20_000,  
								 "Scenario 1B2: accelMotor > 0. Some fuel. The rest is battery. " 
								 + "energyNeededByFuel: " + Math.round(energyNeededByFuel * 100.0)/100.0 + WH__
								 + "fuelNeeded: " +  Math.round(fuelNeeded * 100.0)/100.0  + KG__					
								 + "energyByBattery: " +  Math.round(energyByBattery * 100.0)/100.0 + WH__
								 + "Battery: " 			+ Math.round(battery.getCurrentEnergy() * 100.0)/100.0 + KWH__	
								 + "overallEnergyUsed: " + Math.round(overallEnergyUsed * 100.0)/100.0 + WH__							 
								 + "avePower: " 			+ Math.round(avePower * 100.0)/100.0 + W__							
								 + "vKPH: " 				+ Math.round(vKPH * 100.0)/100.0 + KPH__   	
								 + "distanceTravelled: " +  Math.round(distanceTravelled * 100.0)/100.0  + KM__
								 + "seconds: " + Math.round(secs * 100.0)/100.0 + SEC__);
						 
					// Equate energyByBattery to energySuppliedByBattery in order to add to odometer easily
					energyByBattery = energySuppliedByBattery;
						 
				 }
				 else {
					 // Energy expenditure is NOT met. Need to cut down the speed and power. 
					 // Scenario 1B2: fuel can fulfill some energy expenditure but not all. Battery cannot provide the rest
					 
					 // Previously, it was overallEnergyUsed = avePower * secs / JOULES_PER_WH ; // [in Wh]				
					 
		 			 // Recalculate the new power
					 // 1 Wh = 3.6 kJ 
					 // W = J / s  / [3.6 kJ / Wh]
					 // W = Wh / 3.6k
		 
					 // Recalculate the new ave power W
					 // W = Wh / s * 3600 J / Wh
					 avePower = energySuppliedByBattery / secs * JOULES_PER_WH;
					 
					 // Recalculate the new overall energy expenditure [in Wh]
					 overallEnergyUsed = energySuppliedByBattery + energyNeededByFuel;
					 
					 // Recalculate the new speed 
					 // FUTURE: will consider the on-board accessory vehicle power usage
					 // m/s = W / (kg * m/s2)
					 vMS = avePower / aveForce;
					 	 
					 // FUTURE : may need to find a way to optimize motor power usage 
					 // and slow down the vehicle to the minimal to conserve power	
					 
					 vKPH = vMS * KPH_CONV;
					 
					 // Find new acceleration
					 accelSpeedUp = (vMS - uMS) / secs; // [in m/s^2]
					 
					// Set new vehicle acceleration
					 vehicle.setAccel(accelSpeedUp);
					 
					 // Recompute the new distance it could travel
					 distanceTravelled = vKPH * hrsTime;
							 
					/*
					 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
					 * delete any of them. Needed for testing when new features are added in future. Thanks !
					 */
					 logger.log(vehicle, Level.INFO, 20_000,  
							 "Scenario 1B3: accelMotor < 0. Fuel and/or energy deficit. Slowing down.  " 
							 + "energyNeededByFuel: " + Math.round(energyNeededByFuel * 100.0)/100.0 + WH__
							 + "fuelNeeded: " +  Math.round(fuelNeeded * 100.0)/100.0  + KG__					
							 + "energyByBattery: " +  Math.round(energyByBattery * 100.0)/100.0 + WH__
							 + "energySuppliedByBattery: " +  Math.round(energySuppliedByBattery * 100.0)/100.0 + WH__
							 + "Battery: " 			+ Math.round(battery.getCurrentEnergy() * 100.0)/100.0 + KWH__	
							 + "overallEnergyUsed: " + Math.round(overallEnergyUsed * 100.0)/100.0 + WH__							 
							 + "avePower: " 			+ Math.round(avePower * 100.0)/100.0 + W__							
							 + "vKPH: " 				+ Math.round(vKPH * 100.0)/100.0 + KPH_   							
							 + "distanceTravelled: " +  Math.round(distanceTravelled * 100.0)/100.0  + KM__);	
					 
					 // Equate energyByBattery to energySuppliedByBattery in order to add to odometer easily
					 energyByBattery = energySuppliedByBattery;
				 } // end of Scenario 1B2
			 } // end of Scenario 1B
			 
			 double iFE = 0;

			 double iFC = 0;	
			 
			 if (distanceTravelled > .1 && energyNeededByFuel > .1) {
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
			 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
			 * delete any of them. Needed for testing when new features are added in future. Thanks !
			 */
			 
			 /*
			 double bFC = vehicle.getBaseFuelConsumption();       
			 // Get the base fuel economy 
			 double bFE = vehicle.getBaseFuelEconomy();      	

			 
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
			 */
			 
			 // Cache the new value of fuelUsed	
			 if (fuelNeeded > 0 && remainingFuel != -1) {
//				 logger.log(vehicle, Level.INFO, 20_000, "fuelNeeded: " 
//						 +  Math.round(fuelNeeded * 1000.0)/1000.0 + " kg");
				 // Retrieve the fuel needed for the distance traveled
				 vehicle.retrieveAmountResource(fuelTypeID, fuelNeeded);
				 // Assume double amount of oxygen as fuel oxidizer
				 vehicle.retrieveAmountResource(OXYGEN_ID, RATIO_OXIDIZER_FUEL * fuelNeeded);
				 // Generate 1.75 times amount of the water from the fuel cells
				 vehicle.storeAmountResource(WATER_ID, RATIO_WATER_METHANOL * fuelNeeded);
			 }
		 }
		 
//		 else if (accelSpeedUp == 0D) {
//			// accelMotor < 0
//			 // Scenario 2: Coasting. Maintaining at a constant speed only
//			 		 
//			 // Gets the deceleration using regenerative braking
//			 double regenDecel = 0;
//			 // Set new vehicle acceleration
//			 vehicle.setAccel(0);
//			 
//			 iPower = 0; // (vMS + uMS)/2.0; // [in W]
//				
//			 /*
//			  * NOTE: May comment off the logging codes below once debugging is done.
//			  * But DO NOT delete any of them. Needed for testing when 
//			  * new features are added in future. Thanks !
//			  */ 
//			 logger.log(vehicle, Level.INFO, 20_000, 
//					 "Scenario 2: No change of speed. uKPH: " 
//					 +  Math.round(uKPH * 100.0)/100.0 + KPH__
//					 + "vKPH: " + Math.round(vKPH * 100.0)/100.0 + KPH__
//					 + "regen decel: " + Math.round(regenDecel * 100.0)/100.0 
//					 + " m/s2.  "
//					 + "target accel: " + Math.round(accelTarget * 100.0)/100.0 
//					 + " m/s2."			
//			 );
//		 }
		 
		 else { 
			 // Scenario 2: accelMotor < 0. Set to decelerate
			 
			 // Set new vehicle acceleration
			 vehicle.setAccel(accelSpeedUp);

			 // Convert the total energy [in Wh]. Need to convert from J to Wh
			 overallEnergyUsed = avePower * secs / JOULES_PER_WH; // [in Wh]
			 
			 int rand = RandomUtil.getRandomInt(4);
			 
			 if (rand == 0) {
				 // For now, set 20% of chance to use battery power only
				 remainingFuel = 0;
			 }
			 
			 // Get energy [in Wh] from the fuel
			 double energyNeededByFuel = 0;
	 
			 // Scenario 2: try using fuel first to fulfill the energy expenditure 
 
			 if (remainingFuel == -1) {
				 // Scenario 2A1: (Future) nuclear powered or solar powered
				 
				 fuelNeeded = 0;
				 energyNeededByFuel = 0;
				 energyByBattery = overallEnergyUsed;
				 
				 // Future: Will need to continue to model how nuclear-powered engine would work 
				 
				 logger.log(vehicle, Level.INFO, 20_000,  "Scenario 2A1: accelMotor < 0. Requires no methanol fuel.  " 
						 + "energyByBattery: " + Math.round(energyByBattery * 100.0)/100.0 + WH__				
						 + "overallEnergyUsed: " + Math.round(overallEnergyUsed * 100.0)/100.0 + WH__);	
			 }
			 
			 else if (remainingFuel > 0 || fuelNeeded <= remainingFuel) {
				 // Scenario 2A2: fuel can fulfill all energy expenditure 

				 // if fuelNeeded is smaller than remainingFuel, then fuel is sufficient.
				 
				 // Convert the total energy [in Wh]. Need to convert from J to Wh
				 overallEnergyUsed = avePower * secs / JOULES_PER_WH ; // [in Wh]
				 
				 // Get energy [in Wh] from the fuel
				 energyNeededByFuel = overallEnergyUsed;
				 
				 // Scenario 2: try using fuel first to fulfill the energy expenditure 
				 // Derive the mass of fuel needed kg = Wh / [Wh/kg]
				 fuelNeeded = energyNeededByFuel / vehicle.getFuelConv();
				 
				/*
				 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
				 * delete any of them. Needed for testing when new features are added in future. Thanks !
				 */
				 logger.log(vehicle, Level.INFO, 20_000,  
						 "Scenario 2A2: accelMotor < 0. Enough fuel.  " 
								 + "energyNeededByFuel: " + Math.round(energyNeededByFuel * 100.0)/100.0 + WH__
								 + "fuelNeeded: " +  Math.round(fuelNeeded * 100.0)/100.0  + KG__					
								 + "overallEnergyUsed: " + Math.round(overallEnergyUsed * 100.0)/100.0 + WH__);	
			 }
			 
			 else {			
				 // Scenario 2B: fuel can fulfill some energy expenditure but not all

				 // Limit the fuel to be used
				 fuelNeeded = remainingFuel;
				 
				 // Calculate the new energy provided by the fuel [in Wh]
				 energyNeededByFuel = fuelNeeded * vehicle.getFuelConv();	
				 			 
				 // Calculate needed energy from battery [in Wh] 
				 energyByBattery = overallEnergyUsed - energyNeededByFuel;
				 
				 // Calculate energy that can be delivered by battery 
				 double energySuppliedByBattery = 0;
		 
				 // Test to see how much can be drawn from the battery
				 if (vehicle.getVehicleType() == VehicleType.DELIVERY_DRONE) {
					 // For drone, prioritize to use up fuel as power source first
					 // Get energy from the battery [in Wh]			  
					 energySuppliedByBattery = battery.requestEnergy(energyByBattery / 1000, hrsTime) * 1000;
				 }
				 else {
					 // For ground vehicles
					 // Get energy from the battery [in Wh]
					 energySuppliedByBattery = battery.requestEnergy(energyByBattery / 1000, hrsTime) * 1000;
				 }
				 
				 double deficit = energyByBattery - energySuppliedByBattery;
				 
				 if (remainingFuel <= 0.01) {
					 // Scenario 2B1: Ran out of fuel. Need battery to provide for the rest
					 
					 // Recalculate the new ave power W
					 // W = Wh / s * 3600 J / Wh
					 avePower = energySuppliedByBattery / secs * JOULES_PER_WH;
					 
					 // Recalculate the new overall energy expenditure [in Wh]
					 overallEnergyUsed = energySuppliedByBattery;
					 
					 // Recalculate the new speed 
					 // FUTURE: will consider the on-board accessory vehicle power usage
					 // m/s = W / (kg * m/s2)
					 vMS = avePower / aveForce;
					 	 
					 // FUTURE : may need to find a way to optimize motor power usage 
					 // and slow down the vehicle to the minimal to conserve power	
					 
					 vKPH = vMS * KPH_CONV;
					 
					 // Find new acceleration
					 accelSpeedUp = (vMS - uMS) / secs; // [in m/s^2]
					 
					 // Set new vehicle acceleration
					 vehicle.setAccel(accelSpeedUp);
					 
					 // Recompute the new distance it could travel
					 distanceTravelled = vKPH * hrsTime;
					 
					 /*
						 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
						 * delete any of them. Needed for testing when new features are added in future. Thanks !
						 */
					 logger.log(vehicle, Level.INFO, 20_000,  
								 "Scenario 2B1: accelMotor < 0. Out of fuel. Use battery.  " 
								 + "energyNeededByFuel: " + Math.round(energyNeededByFuel * 100.0)/100.0 + WH__
								 + "fuelNeeded: " +  Math.round(fuelNeeded * 100.0)/100.0  + KG__					
								 + "energyByBattery: " +  Math.round(energyByBattery * 100.0)/100.0 + WH__
								 + "Battery: " 			+ Math.round(battery.getCurrentEnergy() * 100.0)/100.0 + KWH__	
								 + "overallEnergyUsed: " + Math.round(overallEnergyUsed * 100.0)/100.0 + WH__							 
								 + "avePower: " 			+ Math.round(avePower * 100.0)/100.0 + W__							
								 + "vKPH: " 				+ Math.round(vKPH * 100.0)/100.0 + KPH_   							
								 + "distanceTravelled: " +  Math.round(distanceTravelled * 100.0)/100.0  + KM__);
						 
					 // Equate energyByBattery to energySuppliedByBattery in order to add to odometer easily
					 energyByBattery = energySuppliedByBattery;	 
				 }
				 
				 else if (deficit <= 0) {
					 // Energy expenditure is met. It's done.
					 // Scenario 2B1: fuel can fulfill some energy expenditure but not all. Battery provides the rest
					 
					 // Recalculate the new ave power W
					 // W = Wh / s * 3600 J / Wh
					 avePower = energySuppliedByBattery / secs * JOULES_PER_WH;	 
					 // Recalculate the new overall energy expenditure [in Wh]
					 overallEnergyUsed = energySuppliedByBattery + energyNeededByFuel;
					 // Recompute the new distance it could travel
					 distanceTravelled = vKPH * hrsTime;
					 
					 /*
						 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
						 * delete any of them. Needed for testing when new features are added in future. Thanks !
						 */
						 logger.log(vehicle, Level.INFO, 20_000,  
								 "Scenario 2B2: accelMotor < 0. Some fuel. Rest is battery.  " 
								 + "energyNeededByFuel: " + Math.round(energyNeededByFuel * 100.0)/100.0 + WH__
								 + "fuelNeeded: " +  Math.round(fuelNeeded * 100.0)/100.0  + KG__					
								 + "energyByBattery: " +  Math.round(energyByBattery * 100.0)/100.0 + WH__
								 + "Battery: " 			+ Math.round(battery.getCurrentEnergy() * 100.0)/100.0 + KWH__	
								 + "overallEnergyUsed: " + Math.round(overallEnergyUsed * 100.0)/100.0 + WH__							 
								 + "avePower: " 			+ Math.round(avePower * 100.0)/100.0 + W__							
								 + "vKPH: " 				+ Math.round(vKPH * 100.0)/100.0 + KPH_   							
								 + "distanceTravelled: " +  Math.round(distanceTravelled * 100.0)/100.0  + KM__);
						 
					 // Equate energyByBattery to energySuppliedByBattery in order to add to odometer easily
					 energyByBattery = energySuppliedByBattery;
						 
				 }
				 else {
					 // Energy expenditure is NOT met. Need to cut down the speed and power. 
					 // Scenario 2B3: fuel can fulfill some energy expenditure but not all. Battery cannot provide the rest
					 
					 // Previously, it was overallEnergyUsed = avePower * secs / JOULES_PER_WH ; // [in Wh]				
					 
		 			 // Recalculate the new power
					 // 1 Wh = 3.6 kJ 
					 // W = J / s  / [3.6 kJ / Wh]
					 // W = Wh / 3.6k
		 
					 // Recalculate the new ave power W
					 // W = Wh / s * 3600 J / Wh
					 avePower = energySuppliedByBattery / secs * JOULES_PER_WH;
					 
					 // Recalculate the new overall energy expenditure [in Wh]
					 overallEnergyUsed = energySuppliedByBattery + energyNeededByFuel;
					 
					 // Recalculate the new speed 
					 // FUTURE: will consider the on-board accessory vehicle power usage
					 // m/s = W / (kg * m/s2)
					 vMS = avePower / aveForce;
					 	 
					 // FUTURE : may need to find a way to optimize motor power usage 
					 // and slow down the vehicle to the minimal to conserve power	
					 
					 vKPH = vMS * KPH_CONV;
					 
					 // Find new acceleration
					 accelSpeedUp = (vMS - uMS) / secs; // [in m/s^2]
					 
					// Set new vehicle acceleration
					 vehicle.setAccel(accelSpeedUp);
					 
					 // Recompute the new distance it could travel
					 distanceTravelled = vKPH * hrsTime;
							 
					/*
					 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
					 * delete any of them. Needed for testing when new features are added in future. Thanks !
					 */
					 logger.log(vehicle, Level.INFO, 20_000,  
							 "Scenario 2B3: accelMotor < 0. Fuel and/or energy deficit. Slowing down.  " 
							 + "energyNeededByFuel: " + Math.round(energyNeededByFuel * 100.0)/100.0 + WH__
							 + "fuelNeeded: " +  Math.round(fuelNeeded * 100.0)/100.0  + KG__					
							 + "energyByBattery: " +  Math.round(energyByBattery * 100.0)/100.0 + WH__
							 + "energySuppliedByBattery: " +  Math.round(energySuppliedByBattery * 100.0)/100.0 + WH__
							 + "Battery: " 			+ Math.round(battery.getCurrentEnergy() * 100.0)/100.0 + KWH__	
							 + "overallEnergyUsed: " + Math.round(overallEnergyUsed * 100.0)/100.0 + WH__							 
							 + "avePower: " 			+ Math.round(avePower * 100.0)/100.0 + W__							
							 + "vKPH: " 				+ Math.round(vKPH * 100.0)/100.0 + KPH_   							
							 + "distanceTravelled: " +  Math.round(distanceTravelled * 100.0)/100.0  + KM__);	
					 
					 // Equate energyByBattery to energySuppliedByBattery in order to add to odometer easily
					 energyByBattery = energySuppliedByBattery;
				 }  // end of Scenario 2B2
			 } // end of Scenario 2B
			 
			 // Future: Apply Regen Braking
			
//			 totalEnergyUsed = (0.1 + vehicle.getVehicleSpec().getOtherEnergyUsagePercent() / 100) 
//					 * (overallEnergyUsed + regenEnergyBuffer);
//			 
			 /*
			  * NOTE: May comment off the logging codes below once debugging is done.
			  * But DO NOT delete any of them. Needed for testing when 
			  * new features are added in future. Thanks !
			  
			 logger.log(vehicle, Level.INFO, 20_000, 
					 "Scenario 3: accelMotor < 0. Decelerate from " 
					 +  Math.round(uKPH * 100.0)/100.0 + KPH_
					 + "to " + Math.round(vKPH * 100.0)/100.0 + KPH__
					 + "iPower: " + Math.round(iPower * 1_000.0)/1_000.0 + W__	
					 + "iEnergyKWH: " + Math.round(iEnergyKWH * 1_000.0)/1_000.0 + KWH__
					 + "regen decel: " + Math.round(regenDecel * 100.0)/100.0 
					 + " m/s2.  "
					 + "target accel: " + Math.round(accelTarget * 100.0)/100.0 
					 + " m/s2.");
			  */ 
			 
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
			 // avePower is in W, not kW. Divide by 1000 to get kW
			 vehicle.setAverageRoadLoadPower((int)Math.round(avePower/1000));
		 }

		 // Determine new position
		 vehicle.setCoordinates(vehicle.getCoordinates().getNewLocation(vehicle.getDirection(), distanceTravelled)); 
		 

		/*
		 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
		 * delete any of them. Needed for testing when new features are added in future. Thanks !

		 logger.info(vehicle, 20_000L, "dist: " + Math.round(distanceTravelled * 100.0)/100.0 
				 + "  totalEnergyUsed: "  + Math.round(totalEnergyUsed* 100.0)/100.0
				 + "  totalEnergyUsed / dist: "  + Math.round(totalEnergyUsed/distanceTravelled * 100.0)/100.0
				 + "  averageRoadLoadPower: " + Math.round(averageRoadLoadPower * 100.0)/100.0);
		 */
		 
		 
		 // Note: in regen mode, both energyByBattery and fuelNeeded are zero
		 
		 // Add distance traveled to vehicle's odometer.
		 vehicle.addOdometerMileage(distanceTravelled, energyByBattery, fuelNeeded);
		 // Track maintenance due to distance traveled.
		 vehicle.addDistanceLastMaintenance(distanceTravelled);
		 // Derive the instantaneous fuel economy [in km/kg]
		 
		 return remainingHrs;   
	 }
 
	 /**
	  * Starts the regenerative braking to take back some power.
	  * 
	  * @param avePower
	  * @param aveForce
	  * @param secs
	  * @param uMS
	  * @param vMS
	  * @param mass
	  * @return
	  */
	 private double [] startRegenMode(double avePower, double aveForce, double secs, 
			 double uMS, double vMS, double mass) {
		 	 	 
		 // Gets the deceleration using regenerative braking
		 double regenDecel = aveForce / mass;
		 double oldAccel = vehicle.getAccel();
		 double newAccel = oldAccel + regenDecel;

		 // Since regenDecel is -ve,
		 double oppositeForce = mass * - regenDecel;
		 double brakingPower = oppositeForce * vMS;
		 double brakingEnergy = brakingPower * secs;
		 
		 if (newAccel < 0)
			 // Avoid a -ve newAccel
			 newAccel = 0;
		 // Set new vehicle acceleration
		 vehicle.setAccel(newAccel);
		 // J = W * s / 3600 / [J/Wh] 
		 // J = W * h / 
		 
		 // Gets energy in Wh
		 double energyWH = brakingEnergy / JOULES_PER_WH;  	 
		 // Gets energy in kWh
		 double energyKWH = energyWH / 1000;  
		 // Gets hrsTime
		 double hrsTime = secs / 3600;
		// Get the energy stored [kWh] into the battery
		 double energyAcceptedKWH = battery.chargeBattery(-energyKWH, hrsTime); 
		 // Get the power absorbed in W
		 double powerAbsorbed = energyAcceptedKWH / secs * JOULES_PER_WH * 1000;
		 // Recompute the new distance it could travel
//		 double distanceTravelled = vKPH * hrsTime;
		 // Record this regen energy [Wh] as cache
//		 regenEnergyBuffer = energyforCharging;	 
		 logger.log(vehicle, Level.INFO, 20_000,  
				 "Scenario 0: regen mode - "
				 + "oldAccel: " + Math.round(oldAccel * 1000.0)/1000.0 + M_S2__
				 + "regenDecel: " + Math.round(regenDecel * 1000.0)/1000.0 + M_S2__
				 + "newAccel: " + Math.round(newAccel * 1000.0)/1000.0 + M_S2__
				 + "oppositeForce: " + Math.round(oppositeForce * 1000.0)/1000.0 + N__
				 + "brakingPower: " + Math.round(brakingPower * 1000.0)/1000.0 + W__
				 + "brakingEnergy: " + Math.round(brakingEnergy * 1000.0)/1000.0 + J__
				 + "energyKWH: " + Math.round(energyKWH * 1000.0)/1000.0 + KWH__
				 + "energyAcceptedKWH: " + Math.round(energyAcceptedKWH * 1000.0)/1000.0 + KWH__
				 + "powerAbsorbed: " + Math.round(powerAbsorbed * 1000.0)/1000.0 + W__
				 );
	 
		 // Note that avePower is -ve
		 return new double[] {brakingPower, 0};
	 }
	 
	 
	 /**
	  * Gets the amount of fuel (kg) needed for a trip of a given distance (km).
	  *
	  * @param tripDistance   the distance (km) of the trip.
	  * @param fuelEconomy the vehicle's fuel economy (km/kg).
	  * @param useMargin      Apply safety margin when loading resources before embarking if true.
	  * @return amount of fuel needed for trip (kg)
	  */
	 public double getFuelNeededForTrip(Vehicle vehicle, double tripDistance, double fuelEconomy, boolean useMargin) {
		 // The amount of "fuel" covered by the energy in the battery 
		 double batterydistance = vehicle.getController().getBattery().getCurrentEnergy() 
				 / vehicle.getEstimatedFuelConsumption();
		 
		 double amountFuel = (tripDistance - batterydistance) / fuelEconomy;
		 
		 double factor = 1;
		 if (useMargin) {
			 if (tripDistance < 100) {
				 // Note: use formula below to add more extra fuel for short travel distance on top of the fuel margin
				 // in case of getting stranded due to difficult local terrain
				 factor = 3 - tripDistance / 50.0;
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