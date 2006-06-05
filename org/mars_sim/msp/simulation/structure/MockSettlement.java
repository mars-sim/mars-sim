package org.mars_sim.msp.simulation.structure;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;
import org.mars_sim.msp.simulation.structure.building.MockBuilding;

public class MockSettlement extends Settlement {

	/**
	 * Constructor
	 */
	public MockSettlement() throws Exception {
		// Use Settlement constructor.
		super("Mock Settlement", new Coordinates(0, 0));
		
        // Set inventory total mass capacity.
		inventory.addGeneralCapacity(Double.MAX_VALUE);
		
        // Initialize building manager
        buildingManager = new BuildingManager(this, null);
        buildingManager.addBuilding(new MockBuilding(buildingManager));
        
        // Initialize power grid
        powerGrid = new PowerGrid(this);
	}
}