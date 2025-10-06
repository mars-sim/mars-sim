/*
 * Mars Simulation Project
 * ManufactureConfig.java
 * @date 2024-09-10
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
import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.process.ProcessItemFactory;
import com.mars_sim.core.resource.ItemType;


public class ManufactureConfig {

	public static final String WITH_PREFIX = " with ";
	// Element names	
	private static final String PROCESS = "process";
	private static final String NAME = "name";	
	private static final String TECH = "tech";
	private static final String SKILL = "skill";
	private static final String WORK_TIME = "work-time";
	private static final String PROCESS_TIME = "process-time";
	private static final String POWER_REQUIRED = "power-required";
	private static final String TOOL = "tooling";
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
	
	public static final String PRINTER = "3D printer";
	public static final String LIFTING = "Lifting";
	

	// Process broken down by tech level; this are accumulative
	private List<List<ManufactureProcessInfo>> manuByTechLevel;
	private List<List<SalvageProcessInfo>> salvageByTechLevel;
	
	private List<ManufactureProcessInfo> manuProcessInfoList;
	private List<SalvageProcessInfo> salvageInfoList;

	private Map<String, Tooling> tools = new HashMap<>();
	
	/**
	 * Constructor.
	 * 
	 * @param manufactureDoc DOM document containing manufacture process
	 *                       configuration.
	 */
	public ManufactureConfig(Document manufactureDoc) {
		Element root = manufactureDoc.getRootElement();

		loadTooling(root);
		loadManufactureProcessList(root);
		loadSalvageList(root);
	}

	/**
	 * Gets a list of manufacturing process information.
	 * 
	 * @return list of manufacturing process information.
	 * @throws Exception if error getting info.
	 */
	public List<ManufactureProcessInfo> getManufactureProcessList() {
		return manuProcessInfoList;
	}
	
	private void loadTooling(Element root) {
		var tooling = root.getChild(TOOL);
		for(var elem : tooling.getChildren()) {
			String name = elem.getAttributeValue(NAME);
			String description = elem.getAttributeValue(DESCRIPTION);
			Tooling tool = new Tooling(name, description);
			tools.put(name.toLowerCase(), tool);
		}
	}

	/**
	 * Finds a specified tooling by name.
	 * 
	 * @param name
	 * @return
	 */
	public Tooling getTooling(String name) {
		var result = tools.get(name.toLowerCase());
		if (result == null) {
			throw new IllegalArgumentException("Tooling not found: " + name);
		}
		return result;
	}

	/**
	 * Gets manufacturing processes within (at or below) the capability of a tech level.
	 *
	 * @param techLevel the tech level.
	 * @return list of processes.
	 * @throws Exception if error getting processes.
	 */
	public List<ManufactureProcessInfo> getManufactureProcessesForTechLevel(int techLevel) {
		if (techLevel < 0) {
			return Collections.emptyList();
		}
		techLevel = Math.min(techLevel, manuByTechLevel.size()-1);
		return manuByTechLevel.get(techLevel);
	}
	
	/**
	 * Gets a list of manufacturing process information.
	 * 
	 * @param root
	 * @return list of manufacturing process information.
	 * @throws Exception if error getting info.
	 */
	private synchronized void loadManufactureProcessList(Element root) {
		if (manuProcessInfoList != null) {
			// just in case if another thread is being created
			return;
		}
		
		// Build the global list in a temp to avoid access before it is built
		List<ManufactureProcessInfo> newList = new ArrayList<>();
		
		for (Element processElement : root.getChildren(PROCESS)) {

			// Create a map that stores the resource to be swapped out with an alternate resource
			Map<ProcessItem, String> alternateResourceMap = new HashMap<>();
			
			String description = "";

			String name = processElement.getAttributeValue(NAME);
			int techLevel = ConfigHelper.getAttributeInt(processElement, TECH);
			int skillLevel = ConfigHelper.getAttributeInt(processElement, SKILL);
			double workTime = ConfigHelper.getAttributeDouble(processElement, WORK_TIME);
			double processTime = ConfigHelper.getOptionalAttributeDouble(processElement, PROCESS_TIME, 0D);
			double power = ConfigHelper.getAttributeDouble(processElement, POWER_REQUIRED);
			String toolName = processElement.getAttributeValue(TOOL);
			Tooling tool = null;

			// Backfill approach
			if ((processTime > 0) && (toolName == null)) {
				toolName = PRINTER;
			}
			if (toolName != null) {
				tool = getTooling(toolName);
			}

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
						techLevel, skillLevel, workTime, processTime, power, tool,
						inputList, outputList, effort);
			
			newList.add(process);
		
			if (!alternateResourceMap.isEmpty()) {
				// Create a list for the original resources from alternateResourceMap
				String processName = process.getName();
				for (var newInputItems : ConfigHelper.getAlternateInputsList(alternateResourceMap, inputList).entrySet()) {
					
					// Write the modified input resource list into a new process with the replacement name
					String altProcessName = processName + WITH_PREFIX + newInputItems.getKey();
					ManufactureProcessInfo process1 = new ManufactureProcessInfo(altProcessName, process, newInputItems.getValue());
					
					// Add process to newList.
					newList.add(process1);
				}
			}
		}
		
		// Assign the newList now built and create the map by tech level
		manuProcessInfoList = Collections.unmodifiableList(newList);
		manuByTechLevel = createListByTech(manuProcessInfoList);
	}
	
	/**
	 * Creates a list of supported Processes according to the tech level starting at zero.
	 * This is an accumulation, e.g. level 2 contains level 1 etc etc.
	 * 
	 * @param <T> The type of the process info.
	 * @param process
	 * @return
	 */
	public static <T extends ProcessInfo> List<List<T>> createListByTech(List<T> process) {
		var splitByLevel = process.stream()
				.collect(Collectors.groupingBy(m -> m.getTechLevelRequired()));
		var highestLevel = splitByLevel.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1);
		
		// Build the final set so it is an accumulation
		List<List<T>> result = new ArrayList<>();
		List<T> previous = Collections.emptyList();
		for(int i = 0; i <= highestLevel; i++) {
			var thisLevel = splitByLevel.getOrDefault(i, Collections.emptyList());

			List<T> newList = new ArrayList<>(thisLevel);
			newList.addAll(previous);  // Add in the previous list as well
			result.addLast(newList);

			previous = newList;
		}

		return result;
	}

	/**
	 * Gets salvage processes within (at or below) the capability of a tech level.
	 *
	 * @param techLevel the tech level.
	 * @return list of processes.
	 * @throws Exception if error getting processes.
	 */
	public List<SalvageProcessInfo> getSalvageProcessesForTechLevel(int techLevel) {
		if (techLevel < 0) {
			return Collections.emptyList();
		}
		techLevel = Math.min(techLevel, salvageByTechLevel.size()-1);

		return salvageByTechLevel.get(techLevel);
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
	private synchronized void loadSalvageList(Element root) {
		if (salvageInfoList != null) {
			// just in case if another thread is being created
			return;
		}
		
		List<Element> salvageNodes = root.getChildren(SALVAGE);
		List<SalvageProcessInfo> newList = new ArrayList<>();
		
		for (var salvageElement : salvageNodes) {
			String itemName = salvageElement.getAttributeValue(ITEM_NAME);
			ItemType itemT = ItemType.valueOf(ConfigHelper.convertToEnumName(
									salvageElement.getAttributeValue(TYPE)));
			ProcessItem salvaged = ProcessItemFactory.createByName(itemName, itemT, 1);

			int techLevel = ConfigHelper.getAttributeInt(salvageElement, TECH);
			int skill = ConfigHelper.getAttributeInt(salvageElement, SKILL);
			double workTime = ConfigHelper.getAttributeDouble(salvageElement, WORK_TIME);

			List<Element> partSalvageNodes = salvageElement.getChildren(PART_SALVAGE);
			var outputs = ConfigHelper.parseProcessItems(ItemType.PART, partSalvageNodes);

			Tooling tool = null;
			if (itemT == ItemType.VEHICLE) {
				tool = getTooling(LIFTING);
			}


			SalvageProcessInfo process = new SalvageProcessInfo(salvaged, null, techLevel, skill, workTime, tool, outputs);
			newList.add(process);
		}

		// Assign the newList now built
		salvageInfoList = Collections.unmodifiableList(newList);
		salvageByTechLevel = createListByTech(salvageInfoList);
	}
}
