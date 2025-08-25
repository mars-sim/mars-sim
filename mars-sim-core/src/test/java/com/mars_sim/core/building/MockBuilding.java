package com.mars_sim.core.building;

import java.util.Map;

import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.structure.Settlement;

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

    
    public MockBuilding(Settlement owner, String name, String id, BoundedObject bounds,
						String buildingType, BuildingCategory cat,
						boolean needsLifeSupport)  {
		super(owner, id, 1, name, bounds, buildingType, cat);
		// Set maintWorkTime to 100D
		// Set wearLifeTime to 334_000D
		malfunctionManager = new MalfunctionManager(this, 334_000D, 100D);
		if (needsLifeSupport) {
			addFunction(getLifeSupportSpec());
		}
	}

	public MockBuilding(Settlement owner, String id, BoundedObject bounds) {
		super(owner, id, 1, "Mock Building " + id, bounds, "Mock", BuildingCategory.LIVING);
	}
	
	@Override
	public UnitType getUnitType() {
		return UnitType.BUILDING;
	}

}

