/*
 * Mars Simulation Project
 * SolarPowerSource.java
 * @date 2022-06-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * A power source that gives a supply of power proportional
 * to the level of sunlight it receives.
 */
public class SolarPowerSource
extends PowerSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
//	private static final Logger logger = Logger.getLogger(SolarPowerSource.class.getName());
	
	private static final double MAINTENANCE_FACTOR = 2.5D;	
	/** NASA MER has an observable solar cell degradation rate of 0.14% per sol, 
	 	Here we tentatively set to 0.04% per sol instead of 0.14%, since that in 10 earth years,
	 	the efficiency will	drop down to 23.21% of the initial 100%
	 	100*(1-.04/100)^(365*10) = 23.21% */
	public static double DEGRADATION_RATE_PER_SOL = .0004; // assuming it is a constant through its mission
	/*
	 * The number of layers/panels that can be mechanically steered 
	 * toward the sun to maximum the solar irradiance
	 */
//	public static double NUM_LAYERS = 1.5D;
//	public static double STEERABLE_ARRAY_AREA = 50D;		// in square feet
//	public static double AUXILLARY_PANEL_AREA = 15D;	// in square feet
		
//	public static double PI = Math.PI;
//	public static double HALF_PI = PI / 2D;

//	private static final String SOLAR_PHOTOVOLTAIC_ARRAY = "Solar Photovoltaic Array";

	// Notes :
	// 1. The solar Panel is made of triple-junction solar cells with theoretical max eff of 68%  
	// 2. the flat-plate single junction has max theoretical efficiency at 29%
	// 3. The modern Shockley and Queisser (SQ) Limit calculation is a maximum efficiency of 33.16% for any 
	// type of single junction solar cell. 
	// see http://www.solarcellcentral.com/limits_page.html

	/*
	 * The theoretical max efficiency of the triple-junction solar cells 
	 */
	private double efficiency_solar_panel = .55;

	/**
	 * The dust deposition rates is proportional to the dust loading. Here we use MER program's extended the analysis 
	 * to account for variations in the atmospheric columnar dust amount.
	 */
//	private double dust_deposition_rate = 0;
	
	// As of Sol 4786 (July 11, 2017), the solar array energy production was 352 watt-hours with 
	// an atmospheric opacity (Tau) of 0.748 and a solar array dust factor of 0.549.

	/**
	 * Constructor.
	 * @param maxPower the maximum generated power (kW).
	 */
	public SolarPowerSource(double maxPower) {
		// Call PowerSource constructor.
		super(PowerSourceType.SOLAR_POWER, maxPower);

	}

//	/**
//	 * Computes and updates the dust deposition rate for a settlement
//	 * @param the rate
//	 */
//	public void computeDustDeposition(Settlement settlement) {
//
//		if (location == null)
//			location = settlement.getCoordinates();
//
//		double tau = surface.getOpticalDepth(location);		
//	
//		// e.g. The Material Adherence Experiement (MAE) on Pathfinder indicate steady dust accumulation on the Martian 
//		// surface at a rate of ~ 0.28% of the surface area per day (Landis and Jenkins, 1999)
//		dust_deposition_rate = .0018 * tau /.5;
//		
//		if (dust_deposition_rate > 0.001)
//			logger.info(building.getNickName() + " dust_deposition_rate: " + Math.round(dust_deposition_rate* 10000.0)/10000.0);

//		// during relatively periods of clear sky, typical values for tau (optical depth) were between 0.2 and 0.5
//	}
	
	/**
	 * Gets the current power produced by the power source.
	 * @param building the building this power source is for.
	 * @return power (kW)
	 */
	@Override
	public double getCurrentPower(Building building) {
		double I = surface.getSolarIrradiance(building.getCoordinates());

		if (I <= 0)
			return 0;
		
		return I / SurfaceFeatures.MEAN_SOLAR_IRRADIANCE * getMaxPower();
	}

	@Override
	public double getAveragePower(Settlement settlement) {
		return getMaxPower() * 0.707;
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxPower() * MAINTENANCE_FACTOR;
	}

	public void setEfficiency(double value) {
		 efficiency_solar_panel = value;
	}

	public double getEfficiency() {
		return efficiency_solar_panel;
	}

	@Override
	public void removeFromSettlement() {
	}
	
	@Override
	public void setTime(double time) {
	}
	
	@Override
	public void destroy() {
		super.destroy();	
	}


}
