/**
 * Mars Simulation Project
 * SolarHeatSource.java
 * @version 3.1.0 2017-08-14
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * This class accounts for the effect of temperature via passive solar water heating or passive solar heat collector system.
 */
public class SolarHeatSource
extends HeatSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static Logger logger = Logger.getLogger(SolarHeatSource.class.getName());

	// Tentatively set to 0.14% or (.0014) efficiency degradation per sol as reported by NASA MER
	public static double DEGRADATION_RATE_PER_SOL = .0014;
	
	public static double MEAN_SOLAR_IRRADIANCE = SurfaceFeatures.MEAN_SOLAR_IRRADIANCE;
	/**
	 * The dust deposition rates is proportional to the dust loading. Here we use MER program's extended the analysis 
	 * to account for variations in the atmospheric columnar dust amount.
	 */
	private double dust_deposition_rate = 0;
	
	private double efficiency_solar_to_heat = .68;
	
	private double efficiency_solar_to_electricity = .55;

	private double maxHeat = 0;
	
	private double factor = 1;

	private Coordinates location ;

	/**
	 * Constructor.
	 * @param maxHeat the maximum generated power.
	 */
	public SolarHeatSource(double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.SOLAR_HEATING, maxHeat);
		this.maxHeat = maxHeat;
		
	}
	
	/***
	 * Computes and updates the dust deposition rate for a settlement
	 * @param the rate
	 */
	public void computeDustDeposition(Settlement settlement) {

		if (location == null)
			location = settlement.getCoordinates();

		double tau = surface.getOpticalDepth(location);		
	
		// e.g. The Material Adherence Experiement (MAE) on Pathfinder indicate steady dust accumulation on the Martian 
		// surface at a rate of ~ 0.28% of the surface area per day (Landis and Jenkins, 1999)
		dust_deposition_rate = .0018 * tau /.5;
		
		// during relatively periods of clear sky, typical values for optical depth were between 0.2 and 0.5
	}
	
	public double getCollected(Building building) {
		return surface.getSolarIrradiance(building.getCoordinates()) 
				* building.getFloorArea() / 1000D;
	}

	public double getEfficiencySolarHeat() {
		return efficiency_solar_to_heat;
	}

	public double getEfficiencyElectricHeat() {
		return efficiency_solar_to_electricity;
	}

	public void setEfficiencyToHeat(double value) {
		efficiency_solar_to_heat = value;
	}

	public void setEfficiencyToElectricity(double value) {
		efficiency_solar_to_electricity = value;
	}

	@Override
	public double getCurrentHeat(Building building) {
		double available = getCollected(building) * efficiency_solar_to_heat;
		double col = maxHeat * factor * efficiency_solar_to_heat;
//		logger.info(building.getNickName() + " getCurrentHeat(): " + Math.round(maxHeat * collected * factor * 100.0)/100.0 + " kW");	
		if (available > col)
			return col;
		
		return available;
	}

	@Override
	public double getCurrentPower(Building building) {
		double available = getCollected(building) * efficiency_solar_to_electricity;
		double col = maxHeat * factor * efficiency_solar_to_electricity;
//		logger.info(building.getNickName() + "'s maxHeat is " + maxHeat 
//				+ " collected is " + collected
//				+ " getCurrentPower(): " + Math.round(maxHeat * collected * factor * 100.0)/100.0 + " kW");		
		if (available > col)
			return col;
		
		return available;
	}
	
	@Override
	public double getAverageHeat(Settlement settlement) {
		return getMaxHeat() *.707;
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxHeat() * 1D;
	}

	@Override
	public double getEfficiency() {
		return getEfficiencySolarHeat();
	}
	
	@Override
	public void setTime(double time) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void switch2Half() {
		factor = 1/2D;
	}
	
	@Override
	public void switch2OneQuarter() {
		factor = 1/4D;
	}
	
	@Override
	public void switch2Full() {
		factor = 1D;
	}
	
	@Override
	public void switch2ThreeQuarters() {
		factor = .75;
	}
	
	@Override
	public void destroy() {
		super.destroy();
		location = null;
	}

}
