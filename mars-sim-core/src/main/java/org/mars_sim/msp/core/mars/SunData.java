package org.mars_sim.msp.core.mars;

/**
 * Details about the Sun 
 */
public class SunData {

	private int zenith;
	private int maxSun;
	private int daylight;
	private int sunrise;
	private int sunset;

	public SunData(int sunrise, int sunset, int daylight, int zenith, int maxSun) {
		super();
		this.zenith = zenith;
		this.maxSun = maxSun;
		this.daylight = daylight;
		this.sunrise = sunrise;
		this.sunset = sunset;
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

	public int getMaxSun() {
		return maxSun;
	}

	public int getZenith() {
		return zenith;
	}

}
