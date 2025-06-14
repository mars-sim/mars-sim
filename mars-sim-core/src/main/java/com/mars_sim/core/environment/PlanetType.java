/*
 * Mars Simulation Project
 * PlanetType.java
 * @date 2024-08-10
 * @author Manny Kung
 */

package com.mars_sim.core.environment;

public enum PlanetType {

	EARTH			(5.975e+24, 6.378e6),
	MARS			(6.419e+23, 3.393e6)
	;

	private final double mass;
	private final double radius;
	private final double surfaceGravity;
	private static final double G = 6.67300E-11; 
	
	/** hidden constructor. */
	private PlanetType(double mass, double radius) {
		this.mass = mass;
		this.radius = radius;
		surfaceGravity = G * this.mass / (this.radius * this.radius);
		
	}

	public final String getData() {
		// will return the uppercase string of EARTH or MARS
		return toString(); 
	}
	
	public double getMass() {
		return mass;
	}

	public double getRadius() {
		return radius;
	}
	
	public double getSurfaceGravity() {
		return surfaceGravity;
	}
}
