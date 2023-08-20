/*
 * Mars Simulation Project
 * SunData.java
 * @date 2022-06-24
 * @author Barry Evans
 */
package org.mars_sim.msp.core.environment;

import java.io.Serializable;

/**
 * Details about the Sun 
 */
public class SunData implements Serializable {
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private int zenith;
	private int maxSun;
	private int daylight;
	private int sunrise;
	private int sunset;

	public SunData(int sunrise, int sunset, int daylight, int zenith, int maxSun) {
		super();
		this.sunrise = sunrise;
		this.sunset = sunset;
		this.daylight = daylight;
		this.zenith = zenith;
		this.maxSun = maxSun;
	}

	public int getSunrise() {
		return sunrise;
	}

	public int getSunset() {
		return sunset;
	}

	public int getDaylight() {
		return daylight;
	}

	public int getZenith() {
		return zenith;
	}

	public int getMaxSun() {
		return maxSun;
	}
}
