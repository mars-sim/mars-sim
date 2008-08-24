/**
 * Mars Simulation Project
 * SolarThermalPowerSource.java
 * @version 2.85 2008-08-23
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.mars.SurfaceFeatures;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;

/**
 * A solar thermal power source.
 */
public class SolarThermalPowerSource extends PowerSource implements
        Serializable {

    private final static String TYPE = "Solar Thermal Power Source";
    
    /**
     * Constructor
     * @param maxPower the maximum generated power.
     */
    public SolarThermalPowerSource(double maxPower) {
        // Call PowerSource constructor.
        super(TYPE, maxPower);
    }
    
    @Override
    public double getCurrentPower(Building building) {
        BuildingManager manager = building.getBuildingManager();
        Coordinates location = manager.getSettlement().getCoordinates();
        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        double sunlight = surface.getSurfaceSunlight(location);
        
        // Solar thermal mirror only works in direct sunlight.
        if (sunlight == 1D) return getMaxPower();
        else return 0D;
    }
}