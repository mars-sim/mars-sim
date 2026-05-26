/*
 * Mars Simulation Project
 * Flyer.java
 * @date 2023-06-05
 * @author Manny
 */

package com.mars_sim.core.vehicle;

import com.mars_sim.core.environment.TerrainElevation;
import com.mars_sim.core.map.location.Direction;
import com.mars_sim.core.structure.Settlement;

/**
 * The Flyer class represents an airborne.
 */
public abstract class Flyer extends Vehicle {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// default logger.
	// May add back SimLogger logger = SimLogger.getLogger(Flyer.class.getName())
	
	/** Comparison to indicate a small but non-zero amount of fuel (methane) in kg that can still work on the fuel cell to propel the engine. */
    public static final double LEAST_AMOUNT = .001D;
    
    /** Ideal hovering elevation. */
	public final static double ELEVATION_ABOVE_GROUND = .05; // in km
	
	/** The overall system scope. */
	public final static String DRONE = "drone";
	
	// Data members
	/** Current hovering height in km. */
	private double hoveringHeight = 0;

	/** Current Angle of Attack in degree. */
//	private double AoA;

// NASA Space Shuttle Fuel Cell Power Plant 7.6 kg/kW
// The targeted space systems feature power outputs of 1 to 10 kW systems 
// (eventually scalable up to 100 kW), compact sizing (250 to 350 watts per kg),
// and high reliability for long lives (10,000 hours).
	
	/**
	 * Constructs a {@link Flyer} object at a given settlement.
	 * 
	 * @param name                name of the airborne vehicle
	 * @param spec         the configuration description of the vehicle.
	 * @param settlement          settlement the airborne vehicle is parked at
	 * @param maintenanceWorkTime the work time required for maintenance (millisols)
	 */
	protected Flyer(String name, VehicleSpec spec, Settlement settlement, double maintenanceWorkTime) {
		// use Vehicle constructor
		super(name, spec, settlement, maintenanceWorkTime);
	}

	/**
	 * Returns the hovering height of the vehicle above ground [in meter], not km.
	 * 
	 * @return height
	 */
	public double getHoveringHeight() {
		return hoveringHeight;
	}

	/**
	 * Sets the hovering height of the vehicle above ground [in meter], not km.
	 * 
	 * @param height 
	 */
	public void setHoveringHeight(double height) {
		this.hoveringHeight = height;
	}

	/**
	 * Gets the average angle of attack over over a sample distance in direction
	 * vehicle is traveling.
	 * 
	 * @return airborne vehicle's current angle of attack in radians from horizontal plane
	 */
	public double getAngleOfAttack() {
		return getTerrainGrade();
	}
}
