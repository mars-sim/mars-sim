/**
 * Mars Simulation Project
 * ManufactureConfig.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.manufacture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.configuration.ConfigHelper;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;

public class ManufactureConfig {

	private static final Logger logger = Logger.getLogger(ManufactureConfig.class.getName());

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
	private static final String VEHICLE = "vehicle";
	private static final String SALVAGE = "salvage";
	private static final String ITEM_NAME = "item-name";
	private static final String TYPE = "type";
	private static final String PART_SALVAGE = "part-salvage";

	private static List<ManufactureProcessInfo> manufactureProcessList;
	private static List<SalvageProcessInfo> salvageList;

	/**
	 * Constructor
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
		return manufactureProcessList;
	}
	
	/**
	 * Gets a list of manufacturing process information.
	 * 
	 * @return list of manufacturing process information.
	 * @throws Exception if error getting info.
	 */
	private synchronized void loadManufactureProcessList(Document manufactureDoc) {
		if (manufactureProcessList != null) {
			// just in case if another thread is being created
			return;
		}
		
		// Build the global list in a temp to avoid access before it is built
		List<ManufactureProcessInfo> newList = new ArrayList<>();

		Element root = manufactureDoc.getRootElement();
		List<Element> processNodes = root.getChildren(PROCESS);

		for (Element processElement : processNodes) {

			ManufactureProcessInfo process = new ManufactureProcessInfo();

			String name = "";
			String description = "";

			name = processElement.getAttributeValue(NAME);
			
			process.setName(name);
			process.setTechLevelRequired(Integer.parseInt(processElement.getAttributeValue(TECH)));
			process.setSkillLevelRequired(Integer.parseInt(processElement.getAttributeValue(SKILL)));
			process.setWorkTimeRequired(Double.parseDouble(processElement.getAttributeValue(WORK_TIME)));
			process.setProcessTimeRequired(Double.parseDouble(processElement.getAttributeValue(PROCESS_TIME)));
			process.setPowerRequired(Double.parseDouble(processElement.getAttributeValue(POWER_REQUIRED)));

			Element descriptElem = processElement.getChild(DESCRIPTION);
			if (descriptElem != null) {
				description = descriptElem.getText();
			}
			process.setDescription(description);

			Element inputs = processElement.getChild(INPUTS);
			
			List<ManufactureProcessItem> inputList = new ArrayList<>();
			
			process.setInputList(inputList);

			parseResources(inputList, inputs.getChildren(RESOURCE));
			parseParts(inputList, inputs.getChildren(PART));
			parseEquipment(inputList, inputs.getChildren(EQUIPMENT));
			parseVehicles(inputList, inputs.getChildren(VEHICLE));

			Element outputs = processElement.getChild(OUTPUTS);
			
			List<ManufactureProcessItem> outputList = new ArrayList<>();
			
			process.setOutputList(outputList);

			parseResources(outputList, outputs.getChildren(RESOURCE));
			parseParts(outputList, outputs.getChildren(PART));
			parseEquipment(outputList, outputs.getChildren(EQUIPMENT));
			parseVehicles(outputList, outputs.getChildren(VEHICLE));
			
			// Add process to newList.
			newList.add(process);
		}

		// Assign the newList now built
		manufactureProcessList = Collections.unmodifiableList(newList);
	}

	/**
	 * Parses the amount resource elements in a node list.
	 * 
	 * @param list          the list to store the resources in.
	 * @param resourceNodes the node list.
	 * @throws Exception if error parsing resources.
	 */
	private static void parseResources(List<ManufactureProcessItem> list, List<Element> resourceNodes) {
		for (Element resourceElement : resourceNodes) {
			ManufactureProcessItem resourceItem = new ManufactureProcessItem();
			resourceItem.setType(ItemType.AMOUNT_RESOURCE);
			String resourceName = resourceElement.getAttributeValue(NAME);
			AmountResource resource = ResourceUtil.findAmountResource(resourceName);
			if (resource == null)
				logger.severe(resourceName + " shows up in manufacturing.xml but doesn't exist in resources.xml.");
			else {
				resourceItem.setName(resourceName);
				resourceItem.setAmount(Double.parseDouble(resourceElement.getAttributeValue(AMOUNT)));
				list.add(resourceItem);
			}
		}
	}

	/**
	 * Parses the part elements in a node list.
	 * 
	 * @param list      the list to store the parts in.
	 * @param partNodes the node list.
	 * @throws Exception if error parsing parts.
	 */
	private static void parseParts(List<ManufactureProcessItem> list, List<Element> partNodes) {
		for (Element partElement : partNodes) {
			ManufactureProcessItem partItem = new ManufactureProcessItem();
			partItem.setType(ItemType.PART);

			String partName = partElement.getAttributeValue(NAME);
			Part part = (Part) ItemResourceUtil.findItemResource(partName);

			if (part == null)
				logger.severe(partName + " shows up in manufacturing.xml but doesn't exist in parts.xml.");
			else {
				partItem.setName(partName);
				partItem.setAmount(Integer.parseInt(partElement.getAttributeValue(NUMBER)));
				list.add(partItem);
			}
		}
	}

	/**
	 * Parses the equipment elements in a node list.
	 * 
	 * @param list           the list to store the equipment in.
	 * @param equipmentNodes the node list.
	 * @throws Exception if error parsing equipment.
	 */
	private static void parseEquipment(List<ManufactureProcessItem> list, List<Element> equipmentNodes) {
		for (Element equipmentElement : equipmentNodes) {
			ManufactureProcessItem equipmentItem = new ManufactureProcessItem();
			equipmentItem.setType(ItemType.EQUIPMENT);
			String equipmentName = equipmentElement.getAttributeValue(NAME);

			EquipmentType eType = EquipmentType.valueOf(ConfigHelper.convertToEnumName(equipmentName));
			if (eType != null) {
				equipmentItem.setName(equipmentName);
				equipmentItem.setAmount(Integer.parseInt(equipmentElement.getAttributeValue(NUMBER)));
				list.add(equipmentItem);
			}
		}
	}

	/**
	 * Parses the vehicle elements in a node list.
	 * 
	 * @param list         the list to store the vehicles in.
	 * @param vehicleNodes the node list.
	 * @throws Exception if error parsing vehicles.
	 */
	private static void parseVehicles(List<ManufactureProcessItem> list, List<Element> vehicleNodes) {
		for (Element vehicleElement : vehicleNodes) {
			ManufactureProcessItem vehicleItem = new ManufactureProcessItem();
			vehicleItem.setType(ItemType.VEHICLE);
			vehicleItem.setName(vehicleElement.getAttributeValue(NAME));
			vehicleItem.setAmount(Integer.parseInt(vehicleElement.getAttributeValue(NUMBER)));
			list.add(vehicleItem);
		}
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
			salvage.setTechLevelRequired(Integer.parseInt(salvageElement.getAttributeValue(TECH)));
			salvage.setSkillLevelRequired(Integer.parseInt(salvageElement.getAttributeValue(SKILL)));
			salvage.setWorkTimeRequired(Double.parseDouble(salvageElement.getAttributeValue(WORK_TIME)));

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
