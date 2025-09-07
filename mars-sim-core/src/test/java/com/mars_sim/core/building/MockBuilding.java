package com.mars_sim.core.building;

import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.config.BuildingConfigTest;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.structure.Settlement;

@SuppressWarnings("serial")
public class MockBuilding extends Building {
    
    public MockBuilding(Settlement owner, String name, String id, BoundedObject bounds,
						String buildingType, BuildingCategory cat,
						boolean needsLifeSupport)  {
		super(owner, id, 1, name, bounds, buildingType, cat);
		// Set maintWorkTime to 100D
		// Set wearLifeTime to 334_000D
		malfunctionManager = new MalfunctionManager(this, 334_000D, 100D);
		if (needsLifeSupport) {
			addFunction(BuildingConfigTest.getLifeSupportSpec());
		}
		
		// Initialize the scope map.
		malfunctionManager.initScopes();
	}

	public MockBuilding(Settlement owner, String id, BoundedObject bounds) {
		super(owner, id, 1, "Mock Building " + id, bounds, "Mock", BuildingCategory.LIVING);
	}
	
	@Override
	public UnitType getUnitType() {
		return UnitType.BUILDING;
	}

}

