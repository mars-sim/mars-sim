/*
 * Mars Simulation Project
 * ClockUtils.java
 * @date 2021-12-22
 * @author Manny Kung
 */
package com.mars_sim.core.time;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * The ClockUtils class stores methods for clock related computation
 */
public class ClockUtils implements Serializable {

	private static final long serialVersionUID = 65465354252L;

	private static final int SEC_PER_MIN = 60, SEC_PER_HR = 3600, SEC_PER_DAY = 86400;

	/** the sim time label string */
	private static final String SIM_TIME = " (Sim-Time)";

	private static final String DAYS = "d ";
	private static final String HOURS = "h ";
	private static final String MINUTES = "m ";
	private static final String ZERO_MINUTES = "00m ";
	private static final String SECONDS = "s";

	private static Map<Double, String> mapping = new HashMap<>();

	/**
	 * Returns the real time clock (RTC) string in HHh MMm SS.SSs format
	 *
	 * @param tr The time ratio
	 * @return a date time string
	 */
	public static String getRTCString(double tr) {
		double ratio = Math.round(tr * 10.0)/10.0;
		
		if (mapping.containsKey(ratio)) {
			return mapping.get(ratio);
		}

		int days = (int) (ratio / SEC_PER_DAY);
		int hours = (int) ((ratio % SEC_PER_DAY) / SEC_PER_HR);
		int minutes = (int) ((ratio % SEC_PER_HR) / SEC_PER_MIN);
		double secs = ratio % SEC_PER_MIN;

		StringBuilder b = new StringBuilder();

		if (days > 0) {
			b.append(String.format("%01d", days)).append(DAYS);
		}
		
		if (hours > 0) {
			b.append(String.format("%02d", hours)).append(HOURS);
		}

		if (minutes > 0) {
			b.append(String.format("%02d", minutes)).append(MINUTES);
		} else {
			b.append(ZERO_MINUTES);
		}

		b.append(String.format("%02.1f", secs) + SECONDS);
//			.append(SIM_TIME);
		
		String s = b.toString();

		mapping.put(ratio, s);

		return s;
	}
}
