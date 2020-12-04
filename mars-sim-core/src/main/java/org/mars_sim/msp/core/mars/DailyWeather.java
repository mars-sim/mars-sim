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
	
	private double temperature;
	
	private double airDensity;
	
	private double windSpeed;
	
	private double solarIrradiance;
	
	private double opticalDepth;

	public DailyWeather(double temperature, double pressure,
			double airDensity, double windSpeed,
			double solarIrradiance, double opticalDepth) {

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

	@Override
	public String toString() {
		return "DailyWeather [pressure=" + pressure + ", temperature=" + temperature + ", airDensity=" + airDensity
				+ ", windSpeed=" + windSpeed + ", solarIrradiance=" + solarIrradiance + ", opticalDepth=" + opticalDepth
				+ "]";
	}
	

}
