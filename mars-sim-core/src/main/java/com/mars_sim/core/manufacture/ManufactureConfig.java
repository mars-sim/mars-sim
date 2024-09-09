/*
 * Mars Simulation Project
 * ManufactureConfig.java
 * @date 2023-07-30
 * @author Scott Davis
 */

package com.mars_sim.core.manufacture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.process.ProcessItemFactory;
import com.mars_sim.core.resource.ItemType;

public class ManufactureConfig {

	// Element names	
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
	private static final String BIN = "bin";
	private static final String VEHICLE = "vehicle";
	private static final String SALVAGE = "salvage";
	private static final String ITEM_NAME = "item-name";
	private static final String TYPE = "type";
	private static final String PART_SALVAGE = "part-salvage";

	public static final String ALT_PREFIX = " Alt #";
	/**
	 * A map of a list of processes at or below a tech level.
	 */
	private transient Map<Integer, List<ManufactureProcessInfo>> techLevelProcesses;
	
	private List<ManufactureProcessInfo> processInfoList;
	private List<SalvageProcessInfo> salvageInfoList;

	
	/**
	 * Constructor.
	 * 
	 * @param manufactureDoc DOM document containing manufacture process
	 *                       configuration.
	 */
	public ManufactureConfig(Document manufactureDoc) {
		
		techLevelProcesses = new HashMap<>();

		loadManufactureProcessList(manufactureDoc);
		loadSalvageList(manufactureDoc);
	}

	/**
	 * Gets a list of manufacturing process information.
	 * 
	 * @return list of manufacturing process information.
	 * @throws Exception if error getting info.
	 */
	public List<ManufactureProcessInfo> getManufactureProcessList() {
		return processInfoList;
	}
	
	/**
	 * Gets manufacturing processes within the capability of a tech level.
	 *
	 * @param techLevel the tech level.
	 * @return list of processes.
	 * @throws Exception if error getting processes.
	 */
	public List<ManufactureProcessInfo> getManufactureProcessesForTechLevel(int techLevel) {
		if (techLevelProcesses.containsKey(techLevel)) {
			return techLevelProcesses.get(techLevel);
		}
		
		List<ManufactureProcessInfo> list = getManufactureProcessList().stream()
				.filter(s -> s.getTechLevelRequired() <= techLevel)
    	        .collect(Collectors.toList());
		
		if (list != null && !list.isEmpty())
			techLevelProcesses.put(techLevel, list);
		
		return list;
	}
	
	/**
	 * Gets a list of manufacturing process information.
	 * 
	 * @param manufactureDoc
	 * @return list of manufacturing process information.
	 * @throws Exception if error getting info.
	 */
	private synchronized void loadManufactureProcessList(Document manufactureDoc) {
		if (processInfoList != null) {
			// just in case if another thread is being created
			return;
		}
		
		// Build the global list in a temp to avoid access before it is built
		List<ManufactureProcessInfo> newList = new ArrayList<>();
		
		Element root = manufactureDoc.getRootElement();
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
			int effort = 2;

			Element descriptElem = processElement.getChild(DESCRIPTION);
			if (descriptElem != null) {
				description = descriptElem.getText();
			}

			Element inputs = processElement.getChild(INPUTS);
			List<ProcessItem> inputList = new ArrayList<>();
			inputList.addAll(ConfigHelper.parseInputResources(inputs.getChildren(RESOURCE), alternateResourceMap));
			inputList.addAll(ConfigHelper.parseProcessItems(ItemType.PART, inputs.getChildren(PART)));
			inputList.addAll(ConfigHelper.parseProcessItems(ItemType.EQUIPMENT, inputs.getChildren(EQUIPMENT)));
			inputList.addAll(ConfigHelper.parseProcessItems(ItemType.BIN, inputs.getChildren(BIN)));
			inputList.addAll(ConfigHelper.parseProcessItems(ItemType.VEHICLE, inputs.getChildren(VEHICLE)));

			Element outputs = processElement.getChild(OUTPUTS);
			List<ProcessItem> outputList = new ArrayList<>();
			outputList.addAll(ConfigHelper.parseProcessItems(ItemType.AMOUNT_RESOURCE, outputs.getChildren(RESOURCE)));
			outputList.addAll(ConfigHelper.parseProcessItems(ItemType.PART, outputs.getChildren(PART)));
			outputList.addAll(ConfigHelper.parseProcessItems(ItemType.EQUIPMENT, outputs.getChildren(EQUIPMENT)));
			outputList.addAll(ConfigHelper.parseProcessItems(ItemType.BIN, outputs.getChildren(BIN)));
			outputList.addAll(ConfigHelper.parseProcessItems(ItemType.VEHICLE, outputs.getChildren(VEHICLE)));

			// Add process to newList.
			ManufactureProcessInfo process = new ManufactureProcessInfo(name, description,
						techLevel, skillLevel, workTime, processTime, power,
						inputList, outputList, effort);
			newList.add(process);
		
			if (!alternateResourceMap.isEmpty()) {
				// Create a list for the original resources from alternateResourceMap
				String processName = process.getName();
				int i = 1;
				for(var newInputItems : ConfigHelper.getAlternateInputsList(alternateResourceMap, inputList)) {
					
					// Write the modified input resource list onto the new list
					String altProcessName = processName + ALT_PREFIX + i++;

					// Add process to newList.
					newList.add(new ManufactureProcessInfo(altProcessName, process.getDescription(),
										process.getTechLevelRequired(), process.getSkillLevelRequired(),
										process.getWorkTimeRequired(), process.getProcessTimeRequired(),
										process.getPowerRequired(), newInputItems,
										process.getOutputList(), process.getEffortLevel()));
				}
			}
		}
		
		// Assign the newList now built
		processInfoList = Collections.unmodifiableList(newList);
	}


	/**
	 * Gets a full list of salvage process information.
	 * 
	 * @return list of salvage process information.
	 * @throws Exception if error getting info.
	 */
	public List<SalvageProcessInfo> getSalvageInfoList() {
		return salvageInfoList;
	}
		
	/**
	 * Gets a list of salvage process information.
	 * 
	 * @return list of salvage process information.
	 * @throws Exception if error getting info.
	 */
	private synchronized void loadSalvageList(Document manufactureDoc) {
		if (salvageInfoList != null) {
			// just in case if another thread is being created
			return;
		}
		
		Element root = manufactureDoc.getRootElement();
		List<Element> salvageNodes = root.getChildren(SALVAGE);
		List<SalvageProcessInfo> newList = new ArrayList<>();
		
		for(var salvageElement : salvageNodes) {
			String itemName = salvageElement.getAttributeValue(ITEM_NAME);
			ItemType itemT = ItemType.valueOf(ConfigHelper.convertToEnumName(
									salvageElement.getAttributeValue(TYPE)));
			ProcessItem salvaged = ProcessItemFactory.createByName(itemName, itemT, 1);

			int techLevel = ConfigHelper.getAttributeInt(salvageElement, TECH);
			int skill = ConfigHelper.getAttributeInt(salvageElement, SKILL);
			double workTime = ConfigHelper.getAttributeDouble(salvageElement, WORK_TIME);

			List<Element> partSalvageNodes = salvageElement.getChildren(PART_SALVAGE);
			var outputs = ConfigHelper.parseProcessItems(ItemType.PART, partSalvageNodes);

			newList.add(new SalvageProcessInfo(salvaged, null, techLevel, skill, workTime, outputs));
		}

		// Assign the newList now built
		salvageInfoList = Collections.unmodifiableList(newList);
	}
}
