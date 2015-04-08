/**
 * Mars Simulation Project
 * DailyWeather.java
 * @version 3.08 2015-04-08
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import java.io.Serializable;

import org.mars_sim.msp.core.time.MarsClock;

public class DailyWeather implements Serializable {

	private static final long serialVersionUID = 1L;

	private double temperature, dailyAverageT;
	private double pressure, dailyAverageP;
	private double airDensity, dailyAverageD;
	private double solarIrradiance, dailyAverageS;

	private MarsClock clock;
	//private Coordinates location;

	public DailyWeather(MarsClock clock, double temperature, double pressure, double airDensity, double solarIrradiance) {

		this.clock = clock; //(MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
		//this.location = location;
		this.temperature = temperature;
		this.pressure = pressure;
		this.airDensity = airDensity;
		this.solarIrradiance = solarIrradiance;
	}

	public void setDailyAverage(double t, double p, double d, double s) {
			dailyAverageT = t;
			dailyAverageP = p;
			dailyAverageD = d;
			dailyAverageS = s;
	}

	public double getPressure() {
		return pressure;
	}

	public int getSol() {
		return MarsClock.getTotalSol(clock);
	}
}
