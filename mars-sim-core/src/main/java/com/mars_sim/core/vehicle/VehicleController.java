/*
 * Mars Simulation Project
 * VehicleController.java
 * @date 2023-11-22
 * @author Manny Kung
 */

 package com.mars_sim.core.vehicle;

 import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.logging.Level;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.equipment.Battery;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.mission.AbstractVehicleMission;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.NavPoint;
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
	 /** The standard hovering height for a drone. */
	 public static final int STANDARD_HOVERING_HEIGHT = (int) (Flyer.ELEVATION_ABOVE_GROUND * 1000);
	 /** The standard stepping up height for a drone. */
	 public static final double STEP_UP_HEIGHT = STANDARD_HOVERING_HEIGHT / 64D;
	 /** The standard stepping down height for a drone. */
	 public static final double STEP_DOWN_HEIGHT = STANDARD_HOVERING_HEIGHT / 32D;	 
	 /** Comparison to indicate a small but non-zero amount of fuel (methane) in kg that can still work on the fuel cell to propel the engine. */
	 private static final double LEAST_AMOUNT = GroundVehicle.LEAST_AMOUNT;
	 /** The ratio of the amount of oxidizer to methane fuel. */
	 public static final double RATIO_OXIDIZER_METHANE = 1;
	 /** The ratio of the amount of oxidizer to methanol fuel. */
	 public static final double RATIO_OXIDIZER_METHANOL = 1.5;
	 
	 // Water Ratio is 2 for direct methanol fuel cell (DMFC). 
	 // Water Ratio varies for indirect methanol fuel cell (DMFC). say 1.125;
	 /** The ratio of water produced for every methanol consumed. */
//	 private static final double RATIO_WATER_METHANOL = 2; 

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

	 public static final String TWO_WHITESPACES = "  ";
		
	 public static final DecimalFormat DECIMAL3_KPH = new DecimalFormat("#,##0.000 kph");
	 public static final DecimalFormat DECIMAL3_SEC = new DecimalFormat("#,##0.000 secs");
	 public static final DecimalFormat DECIMAL3_N = new DecimalFormat("#,##0.000 N");
	 public static final DecimalFormat DECIMAL2_N = new DecimalFormat("#,##0.00 N");
	 public static final DecimalFormat DECIMAL2_W = new DecimalFormat("#,##0.00 W");
	 public static final DecimalFormat DECIMAL2_J = new DecimalFormat("#,##0.00 J");
	 public static final DecimalFormat DECIMAL3_M_S = new DecimalFormat("#,##0.000 m/s");
	 public static final DecimalFormat DECIMAL3_M_S2 = new DecimalFormat("#,##0.000 m/s2");
	 public static final DecimalFormat DECIMAL3_WH = new DecimalFormat("#,##0.000 Wh");
	 public static final DecimalFormat DECIMAL3_KWH = new DecimalFormat("#,##0.000 kWh");
	 public static final DecimalFormat DECIMAL3_KG = new DecimalFormat("#,##0.000 kg");
	 public static final DecimalFormat DECIMAL3_KM = new DecimalFormat("#,##0.000 km");
	 
	 
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
	 
	 private Propulsion propulsion;
	 
	 private Simulation sim = Simulation.instance();
	 
	 /**
	  * Constructor.
	  * 
	  * @param vehicle The vehicle requiring a controller.
	  * 
	  */
	 public VehicleController(Vehicle vehicle) {
		 this.vehicle = vehicle;
		 
		 if (VehicleType.isRover(vehicle.getVehicleType())) {
			 propulsion = new GroundPropulsion(vehicle);
		 }
		 else if (VehicleType.isDrone(vehicle.getVehicleType())) {
			 propulsion = new AirPropulsion(vehicle);
		 }
		 
		 int numModule = vehicle.getVehicleSpec().getBatteryModule();
		 double energyPerModule = vehicle.getVehicleSpec().getEnergyPerModule();
		 
		 battery = new Battery(vehicle, numModule, energyPerModule);
		 fuelTypeID = vehicle.getFuelTypeID();
	 }
 

	 /**
	  * Consumes the fuel and/or battery power to propel the vehicle.
	  * Adjusts the speed and distance travelled.
	  *
	  * @param hrsTime
	  * @param distToCover the distance that can be covered [in km]
	  * @param vKPH
	  * @param remainingFuel; if -1, it doesn't require methanol fuel to move
	  * @return remainingHrs
	  */
	 public double consumeFuelEnergy(double hrsTime, double distToCover, double vKPH, double remainingFuel) {
		 	 
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
		 double vMS = vKPH / KPH_CONV;
	  
		 if (vKPH < 0 || vMS < 0) {
			 logger.log(vehicle, Level.INFO, 10_000, "Final speed -ve (" 
					 +  Math.round(vKPH * 1000.0)/1000.0 + " kph). Reset back to zero.");
			 vKPH = 0;
			 vMS = 0;
		 }
			
		 if (uKPH < 0 || uMS < 0) {
			 logger.log(vehicle, Level.INFO, 10_000, "Initial speed -ve (" 
					 +  Math.round(uKPH * 1000.0)/1000.0 + " kph). Reset back to zero.");
			 uKPH = 0;
			 uMS = 0;
		 }
		 
		 // distance in km
		 double distanceTravelled = distToCover; //vKPH * hrsTime;
		 // Gets the current mass of the vehicle with payload
		 double mass = vehicle.getMass(); // [in kg]
		 // weight is mg
		 double weight = mass * GRAVITY;	
		 
		 double averageSpeed = (vMS + uMS) / 2;
		 // Calculate force against Mars surface gravity
		 double fGravity = 0;      
		 // 1 N = (1 kg) (1 m/s2)	 
		 double accelSpeedUp = 0;
		 
		 double powerConstantSpeed  = 0;
		 
		 double fuelNeeded = 0;
		 // Get energy [in Wh] from the fuel
		 double energyByFuel = 0;
		 
		 double energyByBattery = 0;	 
		 // [in Wh], not in kWh
		 double overallEnergyUsed = 0;	 
		 // In [W], not [kW]
		 double avePower = 0;
		 
		 double aveForce = 0;
		  
		 double powerThrustDrone = 0;
		 //  g/m3 -> kg /m3;  14.76 g/m3 / 1000 -> kg /m3; 
		 double airDensity = sim.getWeather().getAirDensity(vehicle.getCoordinates()) / 1000;
			
		 if (VehicleType.isRover(vehicle.getVehicleType())) {
			 // Calculates forces and power
			 
			 double drive[] = propulsion.driveOnGround(weight, vMS, averageSpeed, fGravity, airDensity);
			 
//			 double forceConstantSpeed = drive[0];
			 powerConstantSpeed = drive[1];
		 }
		 
		 else if (vehicle instanceof Drone drone) {
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
			 
			 // Future: will remain thrustToWeightRatio1 = thrustForceTotal / weight to be around 2 and optimize vKPH
			 // 		Thus adjusting vKPH according to the weight to save power
			 		 
			 // Gets the terrain angle in radians
			 double angle = vehicle.getTerrainGrade();
			 double dH = distanceTravelled * Math.tan(angle);
			 double oldHeight = drone.getHoveringHeight();
			 
			 // Find the new hovering height for the drone
			 double newHeight = oldHeight - dH;
			 
			 if (newHeight < 0) {
				 // Give the drone a little bit of height
				 newHeight = STEP_UP_HEIGHT;
			 }
			 // Set the new hovering height for the drone due to terrain change
			 drone.setHoveringHeight(newHeight);
			 
			 double multiplier = 1 + (int)(angle / Math.PI / 32); 

			 double height = 0;
			 // if (angle == 0)
			 String caseText = "Case A: Tilt forward at same height - ";
			 if (angle > 0) {
				 // Case C: tilt forward ascent
				 height = STEP_UP_HEIGHT * multiplier;
				 caseText = "Case C: Tilt forward & ascent - ";
			 } else if (angle < 0) {
				 // Case B: tilt forward descent
				 height = - STEP_DOWN_HEIGHT * multiplier;
				 
				 caseText = "Case B: Tilt forward & descent - ";
			 }

			 powerThrustDrone = propulsion.flyInAir(caseText,
						 angle, height, weight,
						 airDensity, vMS, secs);
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
		 
		 if (vMS > 0)
			 aveForce = avePower / vMS;
		 
		 logger.log(vehicle, Level.INFO, 10_000,  
				 "accelSpeedUp: " + DECIMAL3_M_S2.format(accelSpeedUp) + TWO_WHITESPACES
				 + "aveForce: " + DECIMAL2_N.format(aveForce) + TWO_WHITESPACES
				 + "avePower: " + DECIMAL2_W.format(avePower) + TWO_WHITESPACES
				 + "powerConstantSpeed: " + DECIMAL2_W.format(powerConstantSpeed) + TWO_WHITESPACES
				 + "powerSpeedUp: " + DECIMAL2_W.format(powerSpeedUp) + TWO_WHITESPACES
				 + "powerDrone: " + DECIMAL2_W.format(powerThrustDrone));	
		 
		 if (avePower < 0) {
			 // Scenario 0: regen mode
			 // Apply Regenerative Braking
			 double regen[] = startRegenMode(avePower, aveForce, secs, uMS, vMS, mass); 
	 
			 // Get the new avePower
			 double newAvePower = regen[0];
			 double newVMS = regen[1];
			 double newVKPH = newVMS * KPH_CONV;		 
			 double newDistanceTravelled = uMS + newVMS * secs;
			 
			 logger.log(vehicle, Level.INFO, 10_000, "Scenario 0: Regen mode - "
					 + "avePower: " 
					 	+ DECIMAL3_N.format(avePower) + " -> " + DECIMAL3_N.format(newAvePower) + TWO_WHITESPACES
					 + "seconds: " 	+ DECIMAL3_SEC.format(secs) + TWO_WHITESPACES
					 + "oldVKPH: " 	+ DECIMAL3_KPH.format(vKPH) + TWO_WHITESPACES
					 + "u -> v: " 	+ DECIMAL3_KPH.format(uKPH) 
					 	+ " -> " + DECIMAL3_KPH.format(newVKPH) + TWO_WHITESPACES  
					 + "d: " + DECIMAL3_KM.format(distanceTravelled) 
					 	+ " -> " + DECIMAL3_KM.format(newDistanceTravelled) 
					 );
			 
			 vKPH = newVKPH;
			 avePower = newAvePower;
			 distanceTravelled = newDistanceTravelled;
		 }
		 
		 else if (avePower > 0.0 || aveForce > 0.0) { 
			 // Scenario 1
			 
			 // if u == v, u < v, u > v, or accelSpeedUp > 0 or accelSpeedUp < 0

			 // Note: some acceleration may still needed to either maintain the speed 
			 // or to go up to the specified speed vMS
  
			 // Set new vehicle acceleration
			 vehicle.setAccel(accelSpeedUp);
			 // Convert the total energy [in Wh]. Need to convert from J to Wh
			 overallEnergyUsed = avePower * secs / JOULES_PER_WH; // [in Wh]
			 
			 boolean byBatteryOnly = false;
			 
			 double percent = battery.getBatteryLevel();
			 
			 if (percent == 100.0) {
				 byBatteryOnly = true;
			 }
			 else {
				 int rand = RandomUtil.getRandomInt(100);
				 
				 if (rand < percent / 8) {
					 // Set the chance to 1/8 of the battery percent level
					 byBatteryOnly = true;
				 }
			 }
		 
			 // Scenario 1: try using fuel first to fulfill the energy expenditure 

			 if (remainingFuel == -1) {
				 // Scenario 1A1: (Future) nuclear powered or solar powered

				 fuelNeeded = 0;
				 energyByFuel = 0;
				 energyByBattery = overallEnergyUsed;
				 
				 // Future: Will need to continue to model how nuclear-powered engine would work 
				 
				 logger.log(vehicle, Level.INFO, 10_000,  "Scenario 1A1: Nuclear powered or solar powered - " 
						 + "u -> v: " 			+ DECIMAL3_KPH.format(uKPH) + " -> "
	 						+ DECIMAL3_KPH.format(vKPH) + TWO_WHITESPACES   
						 + "energyByBattery: " + DECIMAL3_WH.format(energyByBattery) + TWO_WHITESPACES				
						 + "overallEnergyUsed: " + DECIMAL3_WH.format(overallEnergyUsed) + TWO_WHITESPACES
						 + "seconds: " + DECIMAL3_SEC.format(secs) + TWO_WHITESPACES);	
			 }
			 
			 else if (!byBatteryOnly && remainingFuel > 0 && fuelNeeded <= remainingFuel) {
				 // Scenario 1A2: fuel can fulfill all energy expenditure 

				 // if fuelNeeded is smaller than remainingFuel, then fuel is sufficient.
				// Convert the total energy [in Wh]. Need to convert from J to Wh
				 overallEnergyUsed = avePower * secs / JOULES_PER_WH ; // [in Wh]
				 // Get energy [in Wh] from the fuel
				 energyByFuel = overallEnergyUsed;			 		 
				 // Derive the mass of fuel needed kg = Wh / [Wh/kg]
				 fuelNeeded = energyByFuel / vehicle.getFuelConv();
				 
				/*
				 * NOTE: May comment off the logging codes below once debugging is done. But DO NOT 
				 * delete any of them. Needed for testing when new features are added in future. Thanks !
				 */
				 logger.log(vehicle, Level.INFO, 10_000,  
						 "Scenario 1A2: Enough fuel - " 
								 + "fuelNeeded: " +  DECIMAL3_KG.format(fuelNeeded)  + TWO_WHITESPACES	
								 + "u -> v: " 			+ DECIMAL3_KPH.format(uKPH) + " -> "
			 						+ DECIMAL3_KPH.format(vKPH) + TWO_WHITESPACES   
								 + "accelSpeedUp: " + DECIMAL3_M_S2.format(accelSpeedUp) + TWO_WHITESPACES
								 + "avePower: " + DECIMAL2_W.format(avePower) + TWO_WHITESPACES
								 + "energyByFuel: " + DECIMAL3_WH.format(energyByFuel) + TWO_WHITESPACES
								 + "overallEnergyUsed: " + DECIMAL3_WH.format(overallEnergyUsed) + TWO_WHITESPACES
								 + "seconds: " + DECIMAL3_SEC.format(secs) + TWO_WHITESPACES);	
			 }
			 
			 else {			
				 // Scenario 1B: fuel can fulfill some energy expenditure but not all

				 if (byBatteryOnly) {
					 // Limit the fuel to be used
					 fuelNeeded = 0;
					 // Calculate the new energy provided by the fuel [in Wh]
					 energyByFuel = 0;					 			 
					 // Calculate needed energy from battery [in Wh] 
					 energyByBattery = overallEnergyUsed;
				 }
				 else {
					 // Limit the fuel to be used
					 fuelNeeded = remainingFuel;				 
					 // Calculate the new energy provided by the fuel [in Wh]
					 energyByFuel = fuelNeeded * vehicle.getFuelConv();			 			 
					 // Calculate needed energy from battery [in Wh] 
					 energyByBattery = overallEnergyUsed - energyByFuel;
				 }
				 
				 // Calculate energy that can be delivered by battery 
				 double energySuppliedByBattery = battery.requestEnergy(energyByBattery / 1000, hrsTime) * 1000;
		 
//				 // Test to see how much can be drawn from the battery
//				 if (VehicleType.isDrone(vehicle.getVehicleType())) {
//					 // For drone, prioritize to use up fuel as power source first
//					 // Get energy from the battery [in Wh]			  
//					 energySuppliedByBattery = battery.requestEnergy(energyByBattery / 1000, hrsTime) * 1000;
//				 }
//				 else {
//					 // For ground vehicles
//					 // Get energy from the battery [in Wh]
//					 energySuppliedByBattery = battery.requestEnergy(energyByBattery / 1000, hrsTime) * 1000;
//				 }
				 
				 double batteryEnergyDeficit = energyByBattery - energySuppliedByBattery;
				 
				 if (byBatteryOnly || remainingFuel <= LEAST_AMOUNT) {
					 // Scenario 1B1: Ran out of fuel or switch to battery power.
					 
						logger.log(vehicle, Level.INFO, 10_000,  
								"Scenario 1B1: No fuel. Battery only - " 				
								+ "energyByBattery -> energySuppliedByBattery: " 
									+ DECIMAL3_WH.format(energyByBattery) + " -> "
									+ DECIMAL3_WH.format(energySuppliedByBattery) + TWO_WHITESPACES
								+ "Battery: " + DECIMAL3_KWH.format(battery.getCurrentEnergy()));
						
					 double[] result = propulsion.propelBatteryOnly("Scenario 1B1: Battery only.  ",
							 aveForce, energySuppliedByBattery, secs, uMS, mass);
					 
					 avePower = result[0]; 
					 vKPH = result[1]; 
					 distanceTravelled = result[2]; 
					 
					 energyByBattery = energySuppliedByBattery;				
				 }	
				 
				 else if (batteryEnergyDeficit <= 0 && aveForce > 0) {
					 // Energy expenditure is met. It's done.
					 // Scenario 1B2: fuel can fulfill some energy expenditure but not all. Battery provides the rest
					 
					 double[] result = propulsion.propelWithBothFuelNBattery(
							 "Scenario 1B2: Some fuel some battery - ",
							 aveForce, avePower, energySuppliedByBattery, secs, 
							 energyByFuel, uMS, vMS, mass);
				
					 avePower = result[0]; 
					 vKPH = result[1]; 
					 distanceTravelled = result[2];
					 
					 energyByBattery = energySuppliedByBattery;
				 }
				 else {
					 // Energy expenditure is NOT met. Need to cut down the speed and power. 
					 // Scenario 1B3: fuel can fulfill some energy expenditure but not all. Battery cannot provide the rest
					 
					 double[] result = propulsion.cutDownSpeedNPower(
							 "Scenario 1B3: Fuel and/or energy deficit. Slowing down - ",
							 aveForce, energySuppliedByBattery, secs, energyByFuel, uMS, mass);
					 
					 avePower = result[0]; 
					 vKPH = result[1]; 
					 distanceTravelled = result[2];
					 
					 energyByBattery = energySuppliedByBattery;	
		 
				 } // end of Scenario 1B2
			 } // end of Scenario 1B
			 
			 // Save the instantaneous fuel consumption and economy
			 recordIFEFC(distanceTravelled, energyByFuel, energyByBattery); 

			 // Retrieve fuelNeeded	
			 if (remainingFuel != -1 && fuelNeeded > 0) {
				 propulsion.retrieveFuelNOxidizer(fuelNeeded, fuelTypeID);
			 } 
	
		 }  // end of Scenario 1
		 
		 return updateMetrics(remainingHrs, vKPH, avePower, 
				 distanceTravelled, energyByBattery, fuelNeeded);
	 }
	 
	 /**
	  * Records the instantaneous FE and FC.
	  * 
	  * @param distanceTravelled
	  * @param energyByFuel
	  * @param energyByBattery
	  */
	 private void recordIFEFC(double distanceTravelled, double energyByFuel, double energyByBattery) {
	
		 double iFE = 0;
		 double iFC = 0;	
		 
		 if (distanceTravelled > 0 && (energyByFuel > 0 || energyByBattery > 0)) {
			 // Wh  / km 
			 // Derive the instantaneous fuel consumption [Wh/km]
			 iFC = (energyByFuel + energyByBattery) / distanceTravelled;	        
			 // Set the instantaneous fuel consumption [Wh/km]
			 vehicle.setIFuelConsumption(iFC);

			 // Derive the instantaneous fuel economy [km/kg]
			 // [km/kg] = [km] / [Wh] *  [Wh/kg]
			 //  energyByFuel = fuelNeeded * vehicle.getFuelConv();
			 // getFuelConv() is [Wh/kg]
			 iFE = distanceTravelled / (energyByFuel * vehicle.getFuelConv() + energyByBattery / 1000);	        
			 // Set the instantaneous fuel economy [km/kg]
			 vehicle.setIFuelEconomy(iFE);
		 }
	 }
	 
	 /**
	  * Updates the vehicle parameters.
	  * 
	  * @param remainingHrs
	  * @param vKPH
	  * @param avePower
	  * @param distanceTravelled
	  * @param energyByBattery
	  * @param fuelNeeded
	  * @return
	  */
	 private double updateMetrics(double remainingHrs, double vKPH, double avePower, 
			 double distanceTravelled, double energyByBattery, double fuelNeeded) {
	 
		 // Update new vehicle speed
		 vehicle.setSpeed(vKPH);
		 // overallEnergyUsed [in Wh], not in kWh
		 // Calculate the average road load power in kW = Wh / s * 3.6
//		 double averageRoadLoadPower = overallEnergyUsed / secs * 3.6;
//		 double averageRoadLoadPower = accelMotor * mass * vMS / 1000;
	 
		 if (vKPH > 1 && avePower > 0) {
			 // update average road load speed in kph
			 vehicle.setAverageRoadLoadSpeed((int)Math.round(vKPH));
			 // update average road load power
			 // avePower is in W, not kW. Divide by 1000 to get kW
			 vehicle.setAverageRoadLoadPower((int)Math.round(avePower/1000));
		 }

		 // Determine new position
		 vehicle.setCoordinates(vehicle.getCoordinates().getNewLocation(vehicle.getDirection(), distanceTravelled)); 

		 // Note: in regen mode, both energyByBattery and fuelNeeded are zero
		 
		 // Add distance traveled to vehicle's odometer.
		 vehicle.addOdometerMileage(distanceTravelled, energyByBattery, fuelNeeded);
		 // Track maintenance due to distance traveled.
		 vehicle.addDistanceLastMaintenance(distanceTravelled);
		 // Derive the instantaneous fuel economy [in km/kg]
		 	 
		 Mission mission = vehicle.getMission();
		    
		 if (mission instanceof AbstractVehicleMission vm) {
			 NavPoint np = vm.getNextNavpoint();
			 // Record the distance travelled to the next navpoint
			 np.addActualTravelled(distanceTravelled);
		 }
		 
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
//		 if (newAccel < 0)
//		 // Avoid a -ve newAccel
//		 newAccel = 0;
		 // Set new vehicle acceleration
		 vehicle.setAccel(newAccel);
	 
		 double newVMS = uMS + newAccel * secs;
		 if (newVMS < 0)
			 newVMS = 0;
		 double deltaVMS = uMS - newVMS;
		 if (deltaVMS < 0)
			 deltaVMS = 0;
		 
		 // oppositeForce is + ve since regenDecel is -ve,
		 double oppositeForce = mass * - regenDecel;
		 // brakingPower is + ve 
		 double brakingPower = oppositeForce * deltaVMS;
		 // brakingEnergy is + ve 
		 double brakingEnergy = brakingPower * secs;
		 
		 // J = W * s / 3600 / [J/Wh] 
		 // J = W * h / 
		 
		 // Gets energy in Wh. energyWH is + ve 
		 double energyWH = brakingEnergy / JOULES_PER_WH;  	 
		 // Gets energy in kWh. energyKWH is + ve 
		 double energyKWH = energyWH / 1000;  
		 // Gets hrsTime
		 double hrsTime = secs / 3600;
		// Get the energy stored [kWh] into the battery. energyAcceptedKWH is + ve 
		 double energyAcceptedKWH = battery.chargeBattery(energyKWH, hrsTime); 
		 // Get the power absorbed in W. powerAbsorbed is + ve 
		 double powerAbsorbed = energyAcceptedKWH / secs * JOULES_PER_WH * 1000;
		 
		 // Question: how to get new vKPH so as to get new distance travelled ?
		 
		 // ? = -ve + +ve
		 double newAvePower = avePower + brakingPower;
		// -ve = -ve + +ve
		 double newForce = aveForce + oppositeForce;

		 // Recompute the new distance it could travel
//		 double distanceTravelled = vKPH * hrsTime;
		 // Record this regen energy [Wh] as cache
//		 regenEnergyBuffer = energyforCharging;	 
		 logger.log(vehicle, Level.INFO, 10_000,  
				 "Scenario 0: regen mode - "
				 + "oldAccel: " + DECIMAL3_M_S2.format(oldAccel) + TWO_WHITESPACES
				 + "regenDecel: " + DECIMAL3_M_S2.format(regenDecel) + TWO_WHITESPACES
				 + "newAccel: " + DECIMAL3_M_S2.format(newAccel) + TWO_WHITESPACES
				 + "vMS: " + DECIMAL3_M_S.format(vMS) + TWO_WHITESPACES
				 + "newVMS: " + DECIMAL3_M_S.format(newVMS) + TWO_WHITESPACES
				 + "deltaVMS: " + DECIMAL3_M_S.format(deltaVMS) + TWO_WHITESPACES
				 + "oppositeForce: " + DECIMAL2_N.format(oppositeForce) + TWO_WHITESPACES
				 + "newForce: " + DECIMAL2_N.format(newForce) + TWO_WHITESPACES
				 + "brakingPower: " + DECIMAL2_W.format(brakingPower) + TWO_WHITESPACES
				 + "brakingEnergy: " + DECIMAL2_J.format(brakingEnergy) + TWO_WHITESPACES
				 + "energyKWH: " + DECIMAL3_KWH.format(energyKWH) + TWO_WHITESPACES
				 + "energyAcceptedKWH: " + DECIMAL3_KWH.format(energyAcceptedKWH) + TWO_WHITESPACES
				 + "powerAbsorbed: " + DECIMAL2_W.format(powerAbsorbed)
				 );
	 
		 // avePower is -ve
		 // newVMS is +ve
		 return new double[] {newAvePower, newVMS};
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