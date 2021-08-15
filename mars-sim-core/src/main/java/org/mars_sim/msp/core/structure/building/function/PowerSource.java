/**
 * Mars Simulation Project
 * PowerSource.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.environment.Environment;
import org.mars_sim.msp.core.environment.OrbitInfo;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.environment.Weather;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * The PowerSource class represents a power generator for a building.
 */
public abstract class PowerSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	//private static final Logger logger = Logger.getLogger(HeatSource.class.getName());

	// Data members
	private double maxPower;
	
	private PowerSourceType type;

	protected static SurfaceFeatures surface ;
	protected static Environment mars;
	protected static OrbitInfo orbitInfo;
	protected static Weather weather;
	
	
	/**
	 * Constructor.
	 * @param type the type of power source.
	 * @param maxPower the max power generated.
	 */
	public PowerSource(PowerSourceType type, double maxPower) {
		this.type = type;
		this.maxPower = maxPower;

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
	 * Gets the type of power source.
	 * @return type
	 */
	public PowerSourceType getType() {
		return type;
	}

	/**
	 * Gets the max power generated.
	 * @return power (kW)
	 */
	public double getMaxPower() {
		return maxPower;
	}

	/**
	 * Gets the current power produced by the power source.
	 * @param building the building this power source is for.
	 * @return power (kW)
	 */
	public abstract double getCurrentPower(Building building);

	/**
	 * Gets the average power produced by the power source.
	 * @param settlement the settlement this power source is at.
	 * @return power(kW)
	 */
	public abstract double getAveragePower(Settlement settlement);

	/**
     * Gets the maintenance time for this power source.
     * @return maintenance work time (millisols).
     */
	public abstract double getMaintenanceTime();

	// 2015-09-28 Added removeFromSettlement() to return the fuel cell stacks to the inventory
	public abstract void removeFromSettlement();
	
	public abstract void setTime(double time);
	
	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param {@link Environment}
	 * @param {@link SurfaceFeatures}
	 * @param {@link OrbitInfo}
	 * @param {@link Weather}
	 */
	public static void initializeInstances(Environment m, SurfaceFeatures s, OrbitInfo o, Weather w) {
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
