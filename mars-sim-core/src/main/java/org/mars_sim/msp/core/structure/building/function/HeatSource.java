/**
 * Mars Simulation Project
 * HeatSource.java
 * @version 3.1.0 2017-09-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.mars.OrbitInfo;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * The HeatSource class represents a heat generator for a building.
 */
public abstract class HeatSource implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	// private static Logger logger = Logger.getLogger(HeatSource.class.getName());

	// Data members
	private double maxHeat;

	private HeatSourceType type;

	protected static SurfaceFeatures surface;
	protected static Mars mars;
	protected static OrbitInfo orbitInfo;
	protected static Weather weather;

	/**
	 * Constructor.
	 * 
	 * @param type    the type of Heat source.
	 * @param maxHeat the max heat generated.
	 */
	public HeatSource(HeatSourceType type, double maxHeat) {
		this.type = type;
		this.maxHeat = maxHeat;

		if (mars == null)
			mars = Simulation.instance().getMars();
		if (surface == null)
			surface = mars.getSurfaceFeatures();
		if (orbitInfo == null)
			orbitInfo = mars.getOrbitInfo();
		if (weather == null)
			weather = mars.getWeather();

	}

	/**
	 * Gets the type of Heat source.
	 * 
	 * @return type
	 */
	public HeatSourceType getType() {
		return type;
	}

	/**
	 * Gets the max heat generated.
	 * 
	 * @return Heat
	 */
	public double getMaxHeat() {
		return maxHeat;
	}

	/**
	 * Gets the current Heat produced by the heat source.
	 * 
	 * @param building the building this heat source is for.
	 * @return Heat (kW)
	 */
	public abstract double getCurrentHeat(Building building);

	/**
	 * Gets the average Heat produced by the heat source.
	 * 
	 * @param settlement the settlement this heat source is at.
	 * @return heat(kW)
	 */
	public abstract double getAverageHeat(Settlement settlement);

	/**
	 * Gets the efficiency by the heat source.
	 * 
	 * @return efficiency (max is 1)
	 */
	public abstract double getEfficiency();

	/**
	 * Gets the maintenance time for this heat source.
	 * 
	 * @return maintenance work time (millisols).
	 */
	public abstract double getMaintenanceTime();

	/**
	 * Gets the current Power produced by the heat source.
	 * 
	 * @param building the building this heat source is for.
	 * @return power (kW)
	 */
	public abstract double getCurrentPower(Building building);

	/**
	 * Sets the time for burning the fuel
	 * 
	 * @param time
	 */
	public abstract void setTime(double time);

	/**
	 * Switch to producing the full output
	 */
	public abstract void switch2Full();

	/**
	 * Switch to producing only half of the output
	 */
	public abstract void switch2Half();

	/**
	 * Switch to producing only a quarter of the output
	 */
	public abstract void switch2OneQuarter();

	/**
	 * Switch to producing only three quarters of the output
	 */
	public abstract void switch2ThreeQuarters();
	
	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param {@link Mars}
	 * @param {@link SurfaceFeatures}
	 * @param {@link OrbitInfo}
	 * @param {@link Weather}
	 */
	public static void initializeInstances(Mars m, SurfaceFeatures s, OrbitInfo o, Weather w) {
		mars = m;
		surface = s;
		orbitInfo = o;
		weather = w;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		type = null;
	}

}