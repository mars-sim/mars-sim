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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.equipment.BinType;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.resource.ResourceUtil;

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
	private static final String AMOUNT = "amount";
	private static final String PART = "part";
	private static final String NUMBER = "number";
	private static final String EQUIPMENT = "equipment";
	private static final String BIN = "bin";
	private static final String VEHICLE = "vehicle";
	private static final String SALVAGE = "salvage";
	private static final String ITEM_NAME = "item-name";
	private static final String TYPE = "type";
	private static final String PART_SALVAGE = "part-salvage";

	public static final String ALT_PREFIX = " Alt #";

	private static final String ALTERNATIVE = "alternative";

	private List<ManufactureProcessInfo> processList;
	private List<SalvageProcessInfo> salvageList;

	/**
	 * Constructor.
	 * 
	 * @param manufactureDoc DOM document containing manufacture process
	 *                       configuration.
	 */
	public ManufactureConfig(Document manufactureDoc) {
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
		return processList;
	}
	
	/**
	 * Gets a list of manufacturing process information.
	 * 
	 * @return list of manufacturing process information.
	 * @throws Exception if error getting info.
	 */
	private synchronized void loadManufactureProcessList(Document manufactureDoc) {
		if (processList != null) {
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
			inputList.addAll(parseInputResources(inputs.getChildren(RESOURCE), alternateResourceMap));
			inputList.addAll(parseProcessItems(ItemType.PART, inputs.getChildren(PART)));
			inputList.addAll(parseProcessItems(ItemType.EQUIPMENT, inputs.getChildren(EQUIPMENT)));
			inputList.addAll(parseProcessItems(ItemType.BIN, inputs.getChildren(BIN)));
			inputList.addAll(parseProcessItems(ItemType.VEHICLE, inputs.getChildren(VEHICLE)));

			Element outputs = processElement.getChild(OUTPUTS);
			List<ProcessItem> outputList = new ArrayList<>();
			outputList.addAll(parseProcessItems(ItemType.AMOUNT_RESOURCE, outputs.getChildren(RESOURCE)));
			outputList.addAll(parseProcessItems(ItemType.PART, outputs.getChildren(PART)));
			outputList.addAll(parseProcessItems(ItemType.EQUIPMENT, outputs.getChildren(EQUIPMENT)));
			outputList.addAll(parseProcessItems(ItemType.BIN, outputs.getChildren(BIN)));
			outputList.addAll(parseProcessItems(ItemType.VEHICLE, outputs.getChildren(VEHICLE)));

			// Add process to newList.
			ManufactureProcessInfo process = new ManufactureProcessInfo(name, description,
						techLevel, skillLevel, workTime, processTime, power,
						inputList, outputList, effort);
			newList.add(process);
		
			if (!alternateResourceMap.isEmpty()) {
				// Create a list for the original resources from alternateResourceMap
				String processName = process.getName();
				int i = 1;
				for(var entry : alternateResourceMap.entrySet()) {
					String originalResource = entry.getValue();

					// Create a brand new list
					List<ProcessItem> newInputItems = new ArrayList<>();
					for (var item: inputList) {
						String resName = item.getName();														
						if (resName.equalsIgnoreCase(originalResource)) {
							item = entry.getKey();
						}
						newInputItems.add(item);	
					}
					
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
		processList = Collections.unmodifiableList(newList);
	}

	/**
	 * Parses the input amount resource elements in a node list.
	 * 
	 * @param resourceNodes the node list.
	 * @param alternateResourceMap the map that stores the resource to be swapped out with an alternate resource
	 * @throws Exception if error parsing resources.
	 */
	private static List<ProcessItem> parseInputResources(List<Element> resourceNodes, 
			Map<ProcessItem, String> alternateResourceMap) {
		List<ProcessItem> list = new ArrayList<>();

		for (Element resourceElement : resourceNodes) {
			ProcessItem primaryItem = parseProcessItem(resourceElement, ItemType.AMOUNT_RESOURCE);
			list.add(primaryItem);

			var alternatives = resourceElement.getChildren(ALTERNATIVE);
			if (!alternatives.isEmpty()) {
				var altItems = parseProcessItems(ItemType.AMOUNT_RESOURCE, alternatives);
				altItems.forEach(i -> alternateResourceMap.put(i, primaryItem.getName()));
			}		
		}

		return list;
	}

	private static void checkItemName(String name, ItemType type) {
		switch(type) {
			case AMOUNT_RESOURCE:
				if (ResourceUtil.findAmountResource(name) == null) {
					throw new IllegalArgumentException(name + " shows up in manufacturing.xml is not a known Resource");
				}
				break;
			case PART:
				if (ItemResourceUtil.findItemResource(name) == null) {
					throw new IllegalArgumentException(name + " shows up in manufacturing.xml is not a known Part");	
				}
				break;
			case BIN:
				if (BinType.valueOf(ConfigHelper.convertToEnumName(name)) == null) {
					throw new IllegalArgumentException(name + " shows up in manufacturing.xml is not a known Bin");	
				}
				break;
			case EQUIPMENT:
				if (EquipmentType.valueOf(ConfigHelper.convertToEnumName(name)) == null) {
					throw new IllegalArgumentException(name + " shows up in manufacturing.xml is not a known Equipment");	
				}
				break;
			case VEHICLE:
			default:
		}
	}

	private static ProcessItem parseProcessItem(Element resourceElement, ItemType type) {
		String name = resourceElement.getAttributeValue(NAME);
		checkItemName(name, type);

		String sizeAttr = (type == ItemType.AMOUNT_RESOURCE ? AMOUNT : NUMBER);
		double amount = ConfigHelper.getAttributeDouble(resourceElement, sizeAttr);
		return new ProcessItem(name, type, amount);
	}

	/**
	 * Parses the output amount resource elements in a node list.
	 * 
	 * @param resourceNodes the node list.
	 * @return 
	 * @throws Exception if error parsing resources.
	 */
	private static List<ProcessItem> parseProcessItems(ItemType type, List<Element> resourceNodes) {
		return resourceNodes.stream()
					.map(i -> parseProcessItem(i, type))
					.toList();
	}

	/**
	 * Gets a list of salvage process information.
	 * 
	 * @return list of salvage process information.
	 * @throws Exception if error getting info.
	 */
	public List<SalvageProcessInfo> getSalvageList() {
		return salvageList;
	}
		
	/**
	 * Gets a list of salvage process information.
	 * 
	 * @return list of salvage process information.
	 * @throws Exception if error getting info.
	 */
	private synchronized void loadSalvageList(Document manufactureDoc) {
		if (salvageList != null) {
			// just in case if another thread is being created
			return;
		}
		
		Element root = manufactureDoc.getRootElement();
		List<Element> salvageNodes = root.getChildren(SALVAGE);
		List<SalvageProcessInfo> newList = new ArrayList<>();
		
		Iterator<Element> i = salvageNodes.iterator();
		while (i.hasNext()) {
			Element salvageElement = i.next();
			SalvageProcessInfo salvage = new SalvageProcessInfo();
			String itemName = "";
			itemName = salvageElement.getAttributeValue(ITEM_NAME);
			
			salvage.setItemName(itemName);
			salvage.setType(salvageElement.getAttributeValue(TYPE));
			salvage.setTechLevelRequired(ConfigHelper.getAttributeInt(salvageElement, TECH));
			salvage.setSkillLevelRequired(ConfigHelper.getAttributeInt(salvageElement, SKILL));
			salvage.setWorkTimeRequired(ConfigHelper.getAttributeDouble(salvageElement, WORK_TIME));

			List<Element> partSalvageNodes = salvageElement.getChildren(PART_SALVAGE);
			List<PartSalvage> partSalvageList = new ArrayList<>();
			salvage.setPartSalvageList(partSalvageList);

			Iterator<Element> j = partSalvageNodes.iterator();
			while (j.hasNext()) {
				Element partSalvageElement = j.next();
				PartSalvage part = new PartSalvage();
				
				partSalvageList.add(part);

				part.setName(partSalvageElement.getAttributeValue(NAME));
				part.setNumber(Integer.parseInt(partSalvageElement.getAttributeValue(NUMBER)));
			}

			// Add salvage to newList.
			newList.add(salvage);
		}

		// Assign the newList now built
		salvageList = Collections.unmodifiableList(newList);
	}
}
