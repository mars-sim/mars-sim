/**
 * Mars Simulation Project
 * TimeSpan.java
 * @version 3.1.1 2020-07-22
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

	/**
	 * Constructor
	 */
	public TimeSpan(int nYear, int nMonth, int nDay,
					int nHour, int nMin, double fSec) {
		this.nYear  = nYear;
		this.nMonth = nMonth;
		this.nDay   = nDay;
		this.nHour  = nHour;
		this.nMin   = nMin;
		this.fSec   = fSec;
	}
}
