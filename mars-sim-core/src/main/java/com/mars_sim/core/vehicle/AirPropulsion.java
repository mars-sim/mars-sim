/*
 * Mars Simulation Project
 * AirPropulsion.java
 * @date 2024-08-06
 * @author Manny KUng
 */
package com.mars_sim.core.vehicle;

import java.io.Serializable;

import com.mars_sim.core.logging.SimLogger;

public class AirPropulsion extends Propulsion implements Serializable {
	 
	/** Default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** Default logger. */
	private static SimLogger logger = SimLogger.getLogger(AirPropulsion.class.getName());

	private Vehicle vehicle;
	
	
	/**
	 * Constructor.
	 * 
	 * @param vehicle The vehicle implementing this propulsion
	 * 
	 */
	public AirPropulsion(Vehicle vehicle) {
		super(vehicle);
		
		this.vehicle = vehicle;
	}
	
	 /**
	  * Calculates overall power and forces acting on a flyer.
	  * 
	  * @param weight
	  * @param vMS
	  * @param averageSpeed
	  * @param fGravity
	  * @param airDensity
	  * @return
	  */
	 public double calculateVehiclePower(double weight, double vMS , double averageSpeed, double fGravity, double airDensity) {
		 return 0;
	 }
	
}
