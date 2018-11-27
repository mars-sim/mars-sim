/**
 * Mars Simulation Project
 * SolarHeatSource.java
 * @version 3.1.0 2017-08-14
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.Mars;
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
	// Tentatively set to 0.14% or (.0014) efficiency degradation per sol as reported by NASA MER
	public static double DEGRADATION_RATE_PER_SOL = .0014;

	private double efficiency_solar_to_heat = .58;
	
	private double efficiency_solar_to_electricity = .58;

	private double maxHeat;
	
	private double factor = 1;

	private Coordinates location ;
	
	private static SurfaceFeatures surface ;
	private static Mars mars;

	/**
	 * The dust deposition rates is proportional to the dust loading. Here we use MER program's extended the analysis 
	 * to account for variations in the atmospheric columnar dust amount.
	 */
	private double dust_deposition_rate = 0;
	
	
	/**
	 * Constructor.
	 * @param maxHeat the maximum generated power.
	 */
	public SolarHeatSource(double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.SOLAR_HEATING, maxHeat);
		this.maxHeat = maxHeat;
		
        if (mars == null)
        	mars = Simulation.instance().getMars();
		if (surface == null)
			surface = mars.getSurfaceFeatures();
	}
	
	/***
	 * Computes and updates the dust deposition rate for a settlement
	 * @param the rate
	 */
	public void computeDustDeposition(Settlement settlement) {

		if (location == null)
			location = settlement.getCoordinates();
//        if (mars == null)
//        	mars = Simulation.instance().getMars();
//		if (surface == null)
//			surface = mars.getSurfaceFeatures();
		double tau = surface.getOpticalDepth(location);		
	
		// e.g. The Material Adherence Experiement (MAE) on Pathfinder indicate steady dust accumulation on the Martian 
		// surface at a rate of ~ 0.28% of the surface area per day (Landis and Jenkins, 1999)
		dust_deposition_rate = .0018 * tau /.5;
		
		// during relatively periods of clear sky, typical values for optical depth were between 0.2 and 0.5
	}
	
	public double getCollected(Building building) {
//		if (surface == null)
//			surface = Simulation.instance().getMars().getSurfaceFeatures();
		return surface.getSolarIrradiance(building.getCoordinates()) / SurfaceFeatures.MEAN_SOLAR_IRRADIANCE * building.getFloorArea() / 1000D ;
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
		double collected = getCollected(building);// * efficiency_solar_to_heat;
		return maxHeat * collected * factor;
	}

	@Override
	public double getCurrentPower(Building building) {
		double collected = getCollected(building);// * efficiency_solar_to_electricity;
		//System.out.println(building.getNickName() + "'s maxHeat is " + maxHeat + " collected is " + collected);
		return maxHeat * collected * factor;
	}
	
	@Override
	public double getAverageHeat(Settlement settlement) {
		// NOTE: why divide by 2 ? why settlement ?		
		return getMaxHeat() / 2D;
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
	public void switch2Quarter() {
		factor = 1/4D;
	}
	
	@Override
	public void switch2Full() {
		factor = 1D;
	}
	
	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param clock
	 * @param s
	 */
	public static void justReloaded(Mars m, SurfaceFeatures s) {
		mars = m;
		surface = s;
	}
	
	@Override
	public void destroy() {
		super.destroy();
		surface = null;
		location = null;
		mars = null;
//		orbitInfo = null;
	}


	 
}
