package org.mars_sim.msp.core.structure;

import java.util.ArrayList;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.MockBuilding;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnectorManager;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;

public class MockSettlement extends Settlement {

	/**
	 * Constructor
	 */
	public MockSettlement()  {
		// Use Settlement constructor.
		super("Mock Settlement", 0, new Coordinates(0, 0));

        // Set inventory total mass capacity.
		getInventory().addGeneralCapacity(Double.MAX_VALUE);

        // Initialize building manager
        buildingManager = new BuildingManager(this, true);
        buildingManager.addBuilding(new MockBuilding(buildingManager), false);

        // Initialize building connector manager.
        buildingConnectorManager = new BuildingConnectorManager(this,
                new ArrayList<BuildingTemplate>());

        // Initialize construction manager.
        constructionManager = new ConstructionManager(this);

        // Initialize power grid
        powerGrid = new PowerGrid(this);
/*
        Settlement settlement = Settlement.createMockSettlement("Mock Settlement", 0, new Coordinates(0, 0));

        // Set inventory total mass capacity.
		settlement.getInventory().addGeneralCapacity(Double.MAX_VALUE);

        // Initialize building manager
		BuildingManager mgr = new BuildingManager(settlement, true);
        mgr.addBuilding(new MockBuilding(mgr), false);

        // Initialize building connector manager.
        BuildingConnectorManager buildingConnectorManager = new BuildingConnectorManager(settlement,
                new ArrayList<BuildingTemplate>());

        // Initialize construction manager.
        ConstructionManager constructionManager = new ConstructionManager(settlement);

        // Initialize power grid
        PowerGrid powerGrid = new PowerGrid(settlement);

*/

	}
}