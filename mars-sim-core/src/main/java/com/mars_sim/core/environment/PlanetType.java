/*
 * Mars Simulation Project
 * PlanetType.java
 * @date 2024-08-10
 * @author Manny Kung
 */

package com.mars_sim.core.environment;

import com.mars_sim.core.tool.Msg;

public enum PlanetType {

	EARTH			(Msg.getString("PlanetType.earth.mass"), Msg.getString("PlanetType.earth.radius")), //$NON-NLS-1$
	MARS			(Msg.getString("PlanetType.mars.mass"), Msg.getString("PlanetType.mars.radius")) //$NON-NLS-1$
	;

	private final double mass;
	private final double radius;
	private final double surfaceGravity;
	private static final double G = 6.67300E-11; 
	
	/** hidden constructor. */
	private PlanetType(String mass, String radius) {
		this.mass = Double.parseDouble(mass);
		this.radius = Double.parseDouble(radius);
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
