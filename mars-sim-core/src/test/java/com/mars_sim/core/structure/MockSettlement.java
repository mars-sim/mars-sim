package com.mars_sim.core.structure;

import java.util.ArrayList;

import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.connection.BuildingConnectorManager;
import com.mars_sim.core.building.construction.ConstructionManager;
import com.mars_sim.core.building.utility.power.PowerGrid;
import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.manufacture.ManufacturingManager;
import com.mars_sim.core.map.location.Coordinates;

@SuppressWarnings("serial")
public class MockSettlement extends Settlement {

	/**
	 *
	 */
	public static final String DEFAULT_NAME = "Mock Settlement";
	public static final String SETTLEMENT_TEMPLATE = "Alpha Base";
	public static final Coordinates DEFAULT_COORDINATES = new Coordinates(Math.PI / 2D, 0);

	private ManufacturingManager manuManager;

	public MockSettlement()  {
		this(DEFAULT_NAME, false, DEFAULT_COORDINATES);
	}

	public MockSettlement(String name, boolean needGoods, Coordinates locn) {
		// Use Settlement constructor.
		super(name, locn);
					
        // Set inventory total mass capacity.
		getEquipmentInventory().addCargoCapacity(Double.MAX_VALUE);

        // Initialize building manager
        buildingManager = new BuildingManager(this);

        // Initialize building connector manager.
        buildingConnectorManager = new BuildingConnectorManager(this,
                new ArrayList<>());

        // Initialize construction manager.
        constructionManager = new ConstructionManager(this);

        // Initialize power grid
        powerGrid = new PowerGrid(this);

		if (needGoods) {
			goodsManager = new GoodsManager(this);
			manuManager = new ManufacturingManager(this);
		}
	}	

	@Override
	public ManufacturingManager getManuManager() {
		return manuManager;
	}

	@Override
	public String getTemplate() {
		return SETTLEMENT_TEMPLATE;
	}
}