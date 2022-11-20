/*
 * Mars Simulation Project
 * WasteProcessing.java
 * @date 2022-06-15
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.FunctionSpec;

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
		super(FunctionType.WASTE_PROCESSING, spec, building, buildingConfig.getWasteProcesses(building.getBuildingType()));
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
		return calculateFunctionValue(settlement, buildingConfig.getWasteProcesses(type));
	}
}
