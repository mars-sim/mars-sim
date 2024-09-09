/*
 * Mars Simulation Project
 * FoodProductionConfig.java
 * @date 2023-08-17
 * @author Manny Kung
 */

package com.mars_sim.core.food;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.ItemType;

public class FoodProductionConfig {

	
	private static final String PROCESS = "process";
	private static final String NAME = "name";
	private static final String TECH = "tech";
	private static final String SKILL = "skill";
	private static final String WORK_TIME = "work-time";
	private static final String PROCESS_TIME = "process-time";
	private static final String POWER_REQUIRED = "power-required";
	private static final String DESCRIPTION = "description";
	private static final String INPUTS = "inputs";
	private static final String OUTPUTS = "outputs";
	private static final String RESOURCE = "resource";
	private static final String PART = "part";
	private static final String EQUIPMENT = "equipment";

	public static final String RECIPE_PREFIX = " Recipe #";
	/**
	 * A map of a list of processes at or below a tech level.
	 */
	private transient Map<Integer, List<FoodProductionProcessInfo>> techLevelProcesses;
	
	private List<FoodProductionProcessInfo> processList;

    /**
     * Constructor.
     * 
     * @param foodProductionDoc DOM document containing foodProduction process configuration.
     */
    public FoodProductionConfig(Document foodProductionDoc) {
    	
		techLevelProcesses = new HashMap<>();

    	buildProcessList(foodProductionDoc);
    }

    /**
     * Gets a list of manufacturing process information.
     * 
     * @return list of manufacturing process information.
     * @throws Exception if error getting info.
     */
    public List<FoodProductionProcessInfo> getProcessList() {
        return processList;
    }

	/**
	 * Gets manufacturing processes within the capability of a tech level.
	 *
	 * @param techLevel the tech level.
	 * @return list of processes.
	 * @throws Exception if error getting processes.
	 */
	public List<FoodProductionProcessInfo> getProcessesForTechLevel(int techLevel) {
		if (techLevelProcesses.containsKey(techLevel)) {
			return techLevelProcesses.get(techLevel);
		}
		
		List<FoodProductionProcessInfo> list = getProcessList().stream()
				.filter(s -> s.getTechLevelRequired() <= techLevel)
    	        .collect(Collectors.toList());
		
		if (list != null && !list.isEmpty())
			techLevelProcesses.put(techLevel, list);
		
		return list;
	}
	
	
	/**
	 * Builds a list of food production process information.
	 * 
	 * @param foodProductionDoc
	 * @return list of food production process information.
	 * @throws Exception if error getting info.
	 */
    private synchronized void buildProcessList(Document foodProductionDoc) {
    	if (processList != null) {
    		// List has been built by a different thread !!!
    		return;
    	}
    	
		// Build the global list in a temp to avoid access before it is built
        List<FoodProductionProcessInfo> newList = new ArrayList<>();
	
        Element root = foodProductionDoc.getRootElement();
        for (Element processElement : root.getChildren(PROCESS)) {

			// Create a map that stores the resource to be swapped out with an alternate resource
			Map<ProcessItem, String> alternateResourceMap = new HashMap<>();
			
			String description = "";

			String name = processElement.getAttributeValue(NAME);
			int techLevel = ConfigHelper.getAttributeInt(processElement, TECH);
			int skillLevel = ConfigHelper.getAttributeInt(processElement, SKILL);
			double workTime = ConfigHelper.getAttributeDouble(processElement, WORK_TIME);
			double processTime = ConfigHelper.getAttributeDouble(processElement, PROCESS_TIME);
			double power = ConfigHelper.getAttributeDouble(processElement, POWER_REQUIRED);

			Element descriptElem = processElement.getChild(DESCRIPTION);
			if (descriptElem != null) {
				description = descriptElem.getText();
			}

			Element inputs = processElement.getChild(INPUTS);
			List<ProcessItem> inputList = new ArrayList<>();
			inputList.addAll(ConfigHelper.parseInputResources(inputs.getChildren(RESOURCE), alternateResourceMap));
			inputList.addAll(ConfigHelper.parseProcessItems(ItemType.PART, inputs.getChildren(PART)));
			inputList.addAll(ConfigHelper.parseProcessItems(ItemType.EQUIPMENT, inputs.getChildren(EQUIPMENT)));

			Element outputs = processElement.getChild(OUTPUTS);
			List<ProcessItem> outputList = new ArrayList<>();
			outputList.addAll(ConfigHelper.parseProcessItems(ItemType.AMOUNT_RESOURCE, outputs.getChildren(RESOURCE)));
			outputList.addAll(ConfigHelper.parseProcessItems(ItemType.PART, outputs.getChildren(PART)));
			outputList.addAll(ConfigHelper.parseProcessItems(ItemType.EQUIPMENT, outputs.getChildren(EQUIPMENT)));

			// Add primary process to newList.
			var process = new FoodProductionProcessInfo(name, description,
						techLevel, skillLevel, workTime, processTime, power,
						inputList, outputList);
			newList.add(process);
		
			if (!alternateResourceMap.isEmpty()) {
				// Create a list for the original resources from alternateResourceMap
				String processName = process.getName();
				int i = 1;
				for(var newInputItems : ConfigHelper.getAlternateInputsList(alternateResourceMap, inputList)) {
					
					// Write the modified input resource list onto the new list
					String altProcessName = processName + RECIPE_PREFIX + i++;

					// Add process to newList.
					newList.add(new FoodProductionProcessInfo(altProcessName, process.getDescription(),
										process.getTechLevelRequired(), process.getSkillLevelRequired(),
										process.getWorkTimeRequired(), process.getProcessTimeRequired(),
										process.getPowerRequired(), newInputItems,
										process.getOutputList()));
				}
			}
		}
        
		// Assign the newList now built
		processList = Collections.unmodifiableList(newList);
    }
    

    /**
     * Prepares object for garbage collection.
     */
    public void destroy() {
        if(processList != null){
            processList.clear();
            processList = null;
        }
    }
}
