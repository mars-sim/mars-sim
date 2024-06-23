/*
 * Mars Simulation Project
 * FoodProductionProcessInfo.java
 * @date 2023-08-17
 * @author Manny Kung
 */

package com.mars_sim.core.food;

import java.util.List;

import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.process.ProcessItem;

/**
 * Information about a type of food production process.
 */
public class FoodProductionProcessInfo extends ProcessInfo {

	private static final long serialVersionUID = 1L;

	public FoodProductionProcessInfo(String name, String description, int techLevelRequired, int skillLevelRequired,
			double workTimeRequired, double processTimeRequired, double powerRequired, List<ProcessItem> inputList,
			List<ProcessItem> outputList) {
		super(name, description, techLevelRequired, skillLevelRequired, workTimeRequired, processTimeRequired, powerRequired,
		        inputList, outputList);
	}
	
}
