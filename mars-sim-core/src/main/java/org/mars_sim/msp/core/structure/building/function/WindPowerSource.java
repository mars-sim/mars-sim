/**
 * Mars Simulation Project
 * WindPowerSource.java
 * @version 3.1.0 2017-08-30
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * This class explains the power source behind the Wind Turbine Farm. 
 * Assume each Vertical Axis Wind Turbine (VAWT) is capable of provides 
 * up to 2 kW of power if the wind speed is as much as 20 m/s.	
 */
public class WindPowerSource
extends PowerSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final double MAINTENANCE_FACTOR = .5D;

	// NOTE: Parameters below are based on the design of a 500 Watt VAWT scaled up to 2kW  
	// - http://spectrum.library.concordia.ca/36176/1/low_reynolds_number.doc
	// - https://www.researchgate.net/publication/245526020_Low_Reynolds_Number_Vertical_Axis_Wind_Turbine_for_Mars

	/** Assume the turbine is designed to capture up to 20 m/s wind speed. */
	private static final double WIND_SPEED_THRESHOLD = 20D; // [in m/s]
	//private static final double AVERAGE_WIND_SPEED = 7D; // [in m/s]
	private static final double AIR_DENSITY_MARTIAN_ATM = 0.0156D; // [in kg / m^3]
	
	private static final double SWEPT_AREA = 8.4345;
	
	private static final double POWER_COEFFICIENT = 3.8D;
    /** The height factor accounts for the average increase of power output due to higher wind above the surface. */
	private static final double HEIGHT_FACTOR = 1.2D;
	// Note : for the height of 0.5–10 m from the surface of Mars the wind speed vary from 15–26.5 m/s.
	private int numModules = 0;

	/**
	 * Constructor.
	 * @param maxPower the maximum generated power.
	 */
	public WindPowerSource(double maxPower) {
		// Call PowerSource constructor.
		super(PowerSourceType.WIND_POWER, maxPower);
				
		if (maxPower == 18)
			numModules = 9;
		else
			numModules = 4;
	}

	@Override
	public double getCurrentPower(Building building) {
		// TODO: Make power generated to be based on current wind speed at location.
		double speed = Math.min(HEIGHT_FACTOR * weather.getWindSpeed(building.getCoordinates()), WIND_SPEED_THRESHOLD); 

		return Math.min(getMaxPower(), numModules * getPowerOutput(speed));			
	}

	public double getPowerOutput(double velocity) {
		return .5 * AIR_DENSITY_MARTIAN_ATM * SWEPT_AREA * POWER_COEFFICIENT  * velocity;		
	}
	
	@Override
	public double getAveragePower(Settlement settlement) {
		return getMaxPower() * 0.707;
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxPower() * MAINTENANCE_FACTOR;
	}

	@Override
	public void removeFromSettlement() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void setTime(double time) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}
}