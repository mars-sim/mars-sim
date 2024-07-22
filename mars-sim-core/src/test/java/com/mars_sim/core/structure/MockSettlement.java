package com.mars_sim.core.structure;

import java.util.ArrayList;

import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.connection.BuildingConnectorManager;
import com.mars_sim.core.structure.building.utility.power.PowerGrid;
import com.mars_sim.core.structure.construction.ConstructionManager;
import com.mars_sim.mapdata.location.Coordinates;

@SuppressWarnings("serial")
public class MockSettlement extends Settlement {

	/**
	 *
	 */
	public static final String DEFAULT_NAME = "Mock Settlement";
	public static final String SETTLEMENT_TEMPLATE = "Alpha Base";

	public MockSettlement()  {
		this(DEFAULT_NAME, false);
	}

	public MockSettlement(String name, boolean needGoods) {
		// Use Settlement constructor.
		super(name, new Coordinates(Math.PI / 2D, 0));
					
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

		if (needGoods)
			goodsManager = new GoodsManager(this);
	}	

	@Override
	public String getTemplate() {
		return SETTLEMENT_TEMPLATE;
	}
}