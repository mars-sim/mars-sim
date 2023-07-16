/**
 * Mars Simulation Project
 * TimeSpan.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

/**
 * TimeSpan Class for ATime
 */
package org.mars_sim.msp.core.astroarts;

public class TimeSpan {
	public int    nYear, nMonth, nDay;
	public int    nHour, nMin;
	public double fSec;
	private String label;

	/**
	 * Constructor
	 */
	public TimeSpan(String label, int nYear, int nMonth, int nDay,
					int nHour, int nMin, double fSec) {
		this.label = label;
		this.nYear  = nYear;
		this.nMonth = nMonth;
		this.nDay   = nDay;
		this.nHour  = nHour;
		this.nMin   = nMin;
		this.fSec   = fSec;
	}

	@Override
	public String toString() {
		return label;
	}
}
