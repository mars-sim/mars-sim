/*
 * Mars Simulation Project
 * WasteProcessing.java
 * @date 2022-06-15
 * @author Manny Kung
 */
package com.mars_sim.core.building.function;

import java.util.Collections;
import java.util.List;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.config.FunctionSpec;
import com.mars_sim.core.building.config.ResourceProcessingSpec;
import com.mars_sim.core.resourceprocess.ResourceProcessEngine;
import com.mars_sim.core.structure.Settlement;

/**
 * The WasteProcessing class is a building function for handling waste disposal and recycling.
 */
public class WasteProcessing extends ResourceProcessor {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 */
	public WasteProcessing(Building building, FunctionSpec spec) {
		// Use Function constructor
		super(FunctionType.WASTE_PROCESSING, spec, building, ((ResourceProcessingSpec) spec).getProcesses());
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param type the building name.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 */
	public static double getFunctionValue(String type, boolean newBuilding, Settlement settlement) {
		var spec = buildingConfig.getBuildingSpec(type);
		var processSpec = (ResourceProcessingSpec)spec.getFunctionSpec(FunctionType.WASTE_PROCESSING);
		List<ResourceProcessEngine> processes = (processSpec != null ? processSpec.getProcesses() : Collections.emptyList());
		return calculateFunctionValue(settlement, processes);
	}
}
