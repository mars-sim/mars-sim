/*
 * Mars Simulation Project
 * Astro.java
 * @date 2021-06-20
 * @author Manny Kung
 * @note Original work by Osamu Ajiki and Ron Baalke (NASA/JPL)
 * http://www.astroarts.com/products/orbitviewer/
 * http://neo.jpl.nasa.gov/
 */

/**
 * Astronomical Constants
 */
package com.mars_sim.core.astroarts;

public class Astro {
	public static final double GAUSS  = 0.01720209895;
	public static final double JD2000 = 2451545.0;	// 2000.1.1 12h ET
	public static final double JD1900 = 2415021.0;	// 1900.1.1 12h ET

	private Astro() {
		// Prevent instantiation
	}
}
