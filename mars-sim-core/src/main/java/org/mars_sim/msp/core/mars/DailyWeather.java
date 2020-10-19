/**
 * Mars Simulation Project
 * DailyWeather.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import java.io.Serializable;

public class DailyWeather implements Serializable {

	private static final long serialVersionUID = 1L;

	private double pressure;
	private double dailyAverageP;
	
	private double temperature;
	private double dailyAverageT;
	
	private double airDensity;
	private double dailyAverageD;
	
	private double windSpeed;
	private double dailyAverageW;
	
	private double solarIrradiance;
	private double dailyAverageS;
	
	private double opticalDepth;
	private double dailyAverageO;

	// The integer form of millisol 
	private int msol;

	public DailyWeather(int msol, double temperature, double pressure,
			double airDensity, double windSpeed,
			double solarIrradiance, double opticalDepth) {

		this.msol = msol;
		this.temperature = temperature;
		this.pressure = pressure;
		this.airDensity = airDensity;
		this.windSpeed = windSpeed;
		this.solarIrradiance = solarIrradiance;
		this.opticalDepth = opticalDepth;
	}

	public double getSolarIrradiance() {
		return solarIrradiance;
	}
	
	public int getMarsClockInt() {
		return msol;
	}
	
	
//	public void setDailyAverage(double t, double p, double d, double s, double o, double w) {
//			dailyAverageT = t;
//			dailyAverageP = p;
//			dailyAverageD = d;
//			dailyAverageS = s;
//			dailyAverageO = o;
//			dailyAverageW = w;
//	}

//	public double getPressure() {
//		return pressure;
//	}

}
