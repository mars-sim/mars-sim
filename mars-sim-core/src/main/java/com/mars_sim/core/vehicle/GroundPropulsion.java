/*
 * Mars Simulation Project
 * GroundPropulsion.java
 * @date 2024-08-06
 * @author Manny KUng
 */
package com.mars_sim.core.vehicle;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;

public class GroundPropulsion extends Propulsion implements Serializable {
	 
	/** Default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** Default logger. */
	private static SimLogger logger = SimLogger.getLogger(GroundPropulsion.class.getName());

	private static final double TRANSMISSION_EFFICIENCY = 0.85;
	 
	private static final String TWO_WHITESPACES = "  ";
	
    public static final DecimalFormat DECIMAL3_N = new DecimalFormat("#,##0.000 N");
    
	private Vehicle vehicle;
	
	
	/**
	 * Constructor.
	 * 
	 * @param vehicle The vehicle implementing this propulsion
	 * 
	 */
	public GroundPropulsion(Vehicle vehicle) {
		super(vehicle);
		
		this.vehicle = vehicle;
	}

	 /**
	  * Calculates overall power and forces acting on a rover.
	  * 
	  * @param weight
	  * @param vMS
	  * @param averageSpeed
	  * @param fGravity
	  * @param airDensity
	  * @return
	  */
	 public double calculateVehiclePower(double weight, double vMS , double averageSpeed, double fGravity, double airDensity) {
		 // Important for Ground rover in radians
		 double angle = vehicle.getTerrainGrade();
		 
		 // Assume road rolling resistance coeff of 0.075 on roads with pebbles/potholes on Mars (typically 0.015 on paved roads on Earth)
		 // See https://x-engineer.org/rolling-resistance/
		 // The ratio between distance and wheel radius is the rolling resistance coefficient
		 // Calculate the force on rolling resistance 
		 double fRolling = 0.075 * weight * Math.cos(angle);  
		 
		 // See https://x-engineer.org/road-slope-gradient-force/
		 // Calculate the gradient resistance or road slope force 
		 double fRoadSlope = weight * Math.sin(angle);
		 
		 double fInitialFriction = 7.0 / (0.5 + averageSpeed);  // [in N]
		 
		 double frontalArea = vehicle.getWidth() * vehicle.getWidth() * .9;
		 // https://x-engineer.org/aerodynamic-drag
		 // Note : Aerodynamic drag force = 0.5 * air drag coeff * air density * vehicle frontal area * vehicle speed ^2 
		 double fAeroDrag = 0.5 * 0.5 * airDensity * frontalArea * averageSpeed * averageSpeed;
		 // Gets the summation of all the forces acting against the forward motion of the vehicle
		 double totalForce = fInitialFriction + fAeroDrag + fGravity + fRolling + fRoadSlope;
		 
		 // if totalForce is +ve, then vehicle must generate that much force to overcome it to propel forward
		 // if totalForce is -ve, then vehicle may use regen mode to absorb the force.

		 logger.log(vehicle, Level.INFO, 10_000,  
				 "totalForce: " + DECIMAL3_N.format(totalForce) + TWO_WHITESPACES
				 + "fInitialFriction: " + DECIMAL3_N.format(fInitialFriction) + TWO_WHITESPACES
				 + "fAeroDrag: " + DECIMAL3_N.format(fAeroDrag) + TWO_WHITESPACES
				 + "airDensity: " + Math.round(airDensity * 1000.0)/1000.0 + " kg/m3  "
				 + "fRolling: " + DECIMAL3_N.format(fRolling) + TWO_WHITESPACES				 
				 + "angle: " + Math.round(angle * 1000.0)/1000.0 + " radians  "	 
				 + "fRoadSlope: " + DECIMAL3_N.format(fRoadSlope) + TWO_WHITESPACES);
		 
		 // Assume constant speed, P = F_T * v / η
		 // η, overall efficiency in transmission, normally ranging 0.85 (low gear) - 0.9 (direct drive)
		 // F_T, total forces acting on the car
		 // e.g. P = ((250 N) + (400 N)) (90 km/h) (1000 m/km) (1/3600 h/s) / 0.85 = 1.9118 kW
		 
		 // Calculate powerConstantSpeed 	 
		 return totalForce * vMS / TRANSMISSION_EFFICIENCY;
	 }
	
}
