package com.mars_sim.core.structure;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.connection.BuildingConnectorManager;
import com.mars_sim.core.structure.construction.ConstructionManager;
import com.mars_sim.mapdata.location.Coordinates;

@SuppressWarnings("serial")
public class MockSettlement extends Settlement {

	/**
	 *
	 */
	public static final String DEFAULT_NAME = "Mock Settlement";

	/* default logger. */
	private static final Logger logger = Logger.getLogger(MockSettlement.class.getName());
	
	private Simulation sim = Simulation.instance();


	public MockSettlement()  {
		this(DEFAULT_NAME);
	}

	public MockSettlement(String name) {
		// Use Settlement constructor.
		super(name, new Coordinates(Math.PI / 2D, 0));
		
		if (sim == null)
			logger.severe("sim is null");
		
		if (sim.getUnitManager() == null)
			logger.severe("unitManager is null");
					
        // Set inventory total mass capacity.
		getEquipmentInventory().addCargoCapacity(Double.MAX_VALUE);

        // Initialize building manager
        buildingManager = new BuildingManager(this, DEFAULT_NAME);

        // Initialize building connector manager.
        buildingConnectorManager = new BuildingConnectorManager(this,
                new ArrayList<>());

        // Initialize construction manager.
        constructionManager = new ConstructionManager(this);

        // Initialize power grid
        powerGrid = new PowerGrid(this);
	}	
}