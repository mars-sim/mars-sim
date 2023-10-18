package com.mars_sim.core.structure;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.structure.PowerGrid;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.connection.BuildingConnectorManager;
import com.mars_sim.core.structure.construction.ConstructionManager;
import com.mars_sim.mapdata.location.Coordinates;

@SuppressWarnings("serial")
public class MockSettlement extends Settlement {

	/* default logger. */
	private static final Logger logger = Logger.getLogger(MockSettlement.class.getName());
	
	private Simulation sim = Simulation.instance();

	/**
	 * Constructor
	 */
	public MockSettlement()  {
		// Use Settlement constructor.
		super("Mock Settlement", 0, new Coordinates(Math.PI / 2D, 0));
		
		if (sim == null)
			logger.severe("sim is null");
		
		if (sim.getUnitManager() == null)
			logger.severe("unitManager is null");
					
        // Set inventory total mass capacity.
		getEquipmentInventory().addCargoCapacity(Double.MAX_VALUE);

        // Initialize building manager
        buildingManager = new BuildingManager(this, "Mock Settlement");

        // Initialize building connector manager.
        buildingConnectorManager = new BuildingConnectorManager(this,
                new ArrayList<>());

        // Initialize construction manager.
        constructionManager = new ConstructionManager(this);

        // Initialize power grid
        powerGrid = new PowerGrid(this);
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
}