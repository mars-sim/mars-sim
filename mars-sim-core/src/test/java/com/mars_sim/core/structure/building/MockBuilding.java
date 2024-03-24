package com.mars_sim.core.structure.building;

import java.util.Map;

import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.mapdata.location.BoundedObject;

@SuppressWarnings("serial")
public class MockBuilding extends Building {

	/* default logger. */
	private static FunctionSpec lifeSupportSpec = null;
	
	private static FunctionSpec getLifeSupportSpec() {
		if (lifeSupportSpec == null) {
			
			lifeSupportSpec = new FunctionSpec(FunctionType.LIFE_SUPPORT, Map.of(BuildingConfig.POWER_REQUIRED, 1D,
													  FunctionSpec.CAPACITY, 10),
														null);
		}
		return lifeSupportSpec;
	}

    
    public MockBuilding(Settlement owner, String name, int id, BoundedObject bounds, String buildingType, BuildingCategory cat)  {
		super(owner, Integer.toString(id), 1, "Mock Building " + id, bounds, buildingType, cat);

		malfunctionManager = new MalfunctionManager(this, 0D, 0D);
		addFunction(getLifeSupportSpec());
	}

	public MockBuilding(Settlement owner, int id, BoundedObject bounds) {
		super(owner, Integer.toString(id), 1, "Mock Building " + id, bounds, "Mock", BuildingCategory.LIVING);
	}
}

