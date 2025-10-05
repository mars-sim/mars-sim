package com.mars_sim.core.test;

import org.junit.jupiter.api.BeforeEach;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.MockBuilding;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.VehicleMaintenance;
import com.mars_sim.core.environment.MarsSurface;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Rover;

/**
 * Abstract base class to support running Unit Tests for JUnit 4 and 5.
 * It uses the shared MarsSimContextImpl class to provide utility methods
 */
public abstract class MarsSimUnitTest {

    protected static final String EXPLORER_ROVER = "Explorer Rover";

    private MarsSimContextImpl context;

    @BeforeEach
    public void init() {
        // Initialize the MarsSimContextImpl for each test
        context = new MarsSimContextImpl();
    }

    protected SimulationConfig getConfig() {
        return context.getConfig();
    }

    protected MarsSimContextImpl getContext() {
        return context;
    }

    protected MarsSurface getMarsSurface() {
        return context.getSurface();
    }
    
    protected MockBuilding buildBuilding(BuildingManager buildingManager, LocalPosition pos) {
		return context.buildBuilding(buildingManager, "Mock", BuildingCategory.COMMAND, pos, 0D, true);
	}

    protected VehicleMaintenance buildGarage(BuildingManager buildingManager, LocalPosition pos) {
		var building0 = context.buildFunction(buildingManager, "Garage", BuildingCategory.VEHICLE,
									FunctionType.VEHICLE_MAINTENANCE,  pos, 0D, true);
	    
	    return building0.getVehicleParking();
	}

	protected Person buildPerson(String name, Settlement settlement) {
		return context.buildPerson(name, settlement);
	}

    /**
     * Build a rover and add it to the unit manager.
     * @param settlement
     * @param name
     * @param parked
     * @param spec If null a default spec will be used
     * @return
     */
    protected Rover buildRover(Settlement settlement, String name, LocalPosition parked, String spec) {
        return context.buildRover(settlement, name, parked, spec);
    }
    
    /**
     * Build a mock settlement at a default location with no goods manager
     * @param name
     * @return
     */
    protected Settlement buildSettlement(String name) {
        return context.buildSettlement(name, false,  MockSettlement.DEFAULT_COORDINATES);
    }
}
