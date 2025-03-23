/*
 * Mars Simulation Project
 * SolarPowerSource.java
 * @date 2024-08-03
 * @author Scott Davis
 */
package com.mars_sim.core.building.utility.power;


import com.mars_sim.core.building.Building;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.structure.Settlement;

/**
 * A power source that gives a supply of power proportional
 * to the level of sunlight it receives.
 */
public class SolarPowerSource extends PowerSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	
	private static final double MAINTENANCE_FACTOR = 2.5D;	
	/** NASA MER has an observable solar cell degradation rate of 0.14% per sol, 
	 	Here we tentatively set to 0.04% per sol instead of 0.14%, since that in 10 earth years,
	 	the efficiency will	drop down to 23.21% of the initial 100%
	 	100*(1-.04/100)^(365*10) = 23.21% */
	public static final double DEGRADATION_RATE_PER_SOL = .0004; // assuming it is a constant through its mission
	
	/** The rated efficiency of converting to electricity. */
	private static final double RATED_ELECTRIC_EFFICIENCY = .55;
	
	// Notes :
	// 1. The solar Panel is made of triple-junction solar cells with theoretical max eff of 68%  
	// 2. the flat-plate single junction has max theoretical efficiency at 29%
	// 3. The modern Shockley and Queisser (SQ) Limit calculation is a maximum efficiency of 33.16% for any 
	// type of single junction solar cell. 
	// see http://www.solarcellcentral.com/limits_page.html

	/*
	 * The theoretical max efficiency of the triple-junction solar cells 
	 */
	private double electricEfficiency = .55;
	
	// As of Sol 4786 (July 11, 2017), the solar array energy production was 352 watt-hours with 
	// an atmospheric opacity (Tau) of 0.748 and a solar array dust factor of 0.549.

	private Building building;
	
	/**
	 * Constructor.
	 * 
	 * @param maxPower the maximum generated power (kW).
	 */
	public SolarPowerSource(Building building, double maxPower) {
		// Call PowerSource constructor.
		super(PowerSourceType.SOLAR_POWER, maxPower);
		
		this.building = building;
	}

	/**
	 * Gets the current power produced by the power source.
	 * 
	 * @param building the building this power source is for.
	 * @return power (kW)
	 */
	@Override
	public double getCurrentPower(Building building) {
		double I = surface.getSolarIrradiance(building.getCoordinates());

		if (I <= 0)
			return 0;
		
		return I / SurfaceFeatures.MEAN_SOLAR_IRRADIANCE * getMaxPower() 
				* electricEfficiency / RATED_ELECTRIC_EFFICIENCY;
	}

	@Override
	public double getAveragePower(Settlement settlement) {
		return getMaxPower();
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxPower() * MAINTENANCE_FACTOR;
	}

	public void setElectricEfficiency(double value) {
		 electricEfficiency = value;
	}

	public double getElectricEfficiency() {
		return electricEfficiency;
	}
	
	 /**
	   * Requests an estimate of the power produced by this power source.
	   * 
	   * @param percent The percentage of capacity of this power source
	   * @return power (kWe)
	   */
	 @Override
	 public double requestPower(double percent) {
		 return getCurrentPower(building);
	 }
}
