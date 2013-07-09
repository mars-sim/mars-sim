package org.mars_sim.msp.core.structure;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.MockBuilding;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;

public class MockSettlement extends Settlement {

	/**
	 * Constructor
	 */
	public MockSettlement()  {
		// Use Settlement constructor.
		super("Mock Settlement", new Coordinates(0, 0));
		
        // Set inventory total mass capacity.
		getInventory().addGeneralCapacity(Double.MAX_VALUE);
		
        // Initialize building manager
        buildingManager = new BuildingManager(this, null);
        buildingManager.addBuilding(new MockBuilding(buildingManager));
        
        // Initialize construction manager.
        constructionManager = new ConstructionManager(this);
        
        // Initialize power grid
        powerGrid = new PowerGrid(this);
	}
}