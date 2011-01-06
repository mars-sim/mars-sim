/**
 * Mars Simulation Project
 * SolarThermalPowerSource.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;

import java.io.Serializable;

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