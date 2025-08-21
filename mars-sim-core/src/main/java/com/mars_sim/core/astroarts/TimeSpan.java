/*
 * Mars Simulation Project
 * TimeSpan.java
 * @date 2021-06-20
 * @author Manny Kung
 */

/**
 * TimeSpan Class for ATime.
 */
package com.mars_sim.core.astroarts;

/**
 * Constructor.
 */
public record TimeSpan(String label, int nYear, int nMonth, int nDay,
				int nHour, int nMin, double fSec) {

	@Override
	public String toString() {
		return label;
	}
}
