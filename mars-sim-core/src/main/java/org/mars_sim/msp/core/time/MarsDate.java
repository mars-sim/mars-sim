/*
 * Mars Simulation Project
 * MarsDate.java
 * @date 2023-07-11
 * @author Barry Evans
 */

package org.mars_sim.msp.core.time;

import java.io.Serializable;

/**
 * The class represent a date on Mars
 */
public class MarsDate implements Serializable {

	private static final long serialVersionUID = 65894231L;
	
	// Data members
	/** The Martian year. Note: the year of landing is considered to be the first year/orbit */
	private final int orbit;
	/** The Martian month. */
	private final int month;
	/** The Martian day. */
	private final int solOfMonth;
	/** The total Millisols */
	private final double totalMillisols;

	private transient String dateString = null;

	MarsDate(int orbit, int month, int sol) {
		// Set date/time to given parameters.
		this.orbit = orbit;
		this.month = month;
		this.solOfMonth = sol;
		this.totalMillisols = MarsTime.calculateTotalMillisols(orbit, month, sol, 0);
	}

	/**
	 * Returns formatted time stamp string in the format of e.g. "03-Adir-05:056.349".
	 *
	 * @return formatted time stamp string
	 */
	public String getDateStamp() {
		if (dateString == null) {
			dateString = MarsTimeFormat.getDateStamp(this);
		}

		return dateString;
	}

	/**
	 * Return the total millisols.
	 * 
	 * @return Total
	 */
	public double getTotalMillisols() {
		return totalMillisols;
	}

	/**
	 * Returns the orbit.
	 *
	 * @return the orbit as an integer
	 */
	public int getOrbit() {
		return orbit;
	}

	/**
	 * Returns the month (1 - 24).
	 *
	 * @return the month as an integer
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * Returns the sol of month (1 - 28).
	 *
	 * @return the sol of month as an integer
	 */
	public int getSolOfMonth() {
		return solOfMonth;
	}


	/**
	 * Displays the string version of the clock.
	 *
	 * @return time stamp string.
	 */
	public String toString() {
		return getDateStamp();
	}

	/**
	 * Checks if another object is equal to this one.
	 *
	 * @param object for comparison
	 * @return true if equal
	 */
	public boolean equals(Object object) {
		boolean result = false;
		if (object instanceof MarsDate) {
			MarsDate otherClock = (MarsDate) object;
			result = (totalMillisols == otherClock.totalMillisols);
		}
		return result;
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	public int hashCode() {
		return orbit * month;
	}
}
