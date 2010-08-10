/**
 * Mars Simulation Project
 * SolarPowerSource.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.core.*;
import org.mars_sim.msp.core.mars.*;
import org.mars_sim.msp.core.structure.building.*;

/**
 * A power source that gives a supply of power proportional 
 * to the level of sunlight it receives.
 */
public class SolarPowerSource extends PowerSource implements Serializable {

	private final static String TYPE = "Solar Power Source";

    /**
     * Constructor
     * @param maxPower the maximum generated power (kW).
     */
	public SolarPowerSource(double maxPower) {
		// Call PowerSource constructor.
		super(TYPE, maxPower);
	}

	/**
	 * Gets the current power produced by the power source.
	 * @param building the building this power source is for.
	 * @return power (kW)
	 */
	public double getCurrentPower(Building building) {
		BuildingManager manager = building.getBuildingManager();
		Coordinates location = manager.getSettlement().getCoordinates();
		SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
		double sunlight = surface.getSurfaceSunlight(location);
		return sunlight * getMaxPower();
	}
}